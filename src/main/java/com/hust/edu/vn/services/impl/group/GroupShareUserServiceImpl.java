package com.hust.edu.vn.services.impl.group;

import com.hust.edu.vn.dto.MemberGroupDto;
import com.hust.edu.vn.dto.TokenInviteGroupDto;
import com.hust.edu.vn.entity.GroupDoc;
import com.hust.edu.vn.entity.GroupShareUser;
import com.hust.edu.vn.entity.TokenInviteGroup;
import com.hust.edu.vn.entity.User;
import com.hust.edu.vn.repository.GroupDocRepository;
import com.hust.edu.vn.repository.GroupShareUserRepository;
import com.hust.edu.vn.repository.TokenInviteGroupRepository;
import com.hust.edu.vn.repository.UserRepository;
import com.hust.edu.vn.services.group.GroupShareUserService;
import com.hust.edu.vn.services.user.EmailService;
import com.hust.edu.vn.utils.BaseUtils;
import com.hust.edu.vn.utils.ModelMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class GroupShareUserServiceImpl implements GroupShareUserService {
    private final TokenInviteGroupRepository tokenInviteGroupRepository;
    private final GroupDocRepository groupDocRepository;
    private final UserRepository userRepository;
    private final GroupShareUserRepository groupShareUserRepository;

    private final ModelMapperUtils modelMapperUtils;
    private final BaseUtils baseUtils;
    private final EmailService emailService;

    @Value("${URL_FE}")
    private String hostname;

    public GroupShareUserServiceImpl(GroupShareUserRepository groupShareUserRepository, BaseUtils baseUtils,
                                     UserRepository userRepository, EmailService emailService,
                                     GroupDocRepository groupDocRepository, ModelMapperUtils modelMapperUtils,
                                     TokenInviteGroupRepository tokenInviteGroupRepository) {
        this.groupShareUserRepository = groupShareUserRepository;
        this.baseUtils = baseUtils;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.groupDocRepository = groupDocRepository;
        this.modelMapperUtils = modelMapperUtils;
        this.tokenInviteGroupRepository = tokenInviteGroupRepository;
    }
    @Override
    public boolean inviteMemberGroup(Long groupId, List<String> emailUsers) {
        User user = baseUtils.getUser();
        if(user != null){
            GroupDoc groupDoc = baseUtils.getGroupDoc(user, groupId);
            if(groupDoc != null){
                if(emailUsers != null && !emailUsers.isEmpty()){
                    for(String emailUser : emailUsers){
                        User user1 = userRepository.findByEmail(emailUser);
                        if(user1 != null && (groupShareUserRepository.existsByGroupIdAndUser(groupId, user1) || groupDoc.getUser() == user1 )){
                            continue;
                        }
                        if(!tokenInviteGroupRepository.existsByEmailAndGroupId(emailUser, groupId)){
                            TokenInviteGroup tokenInviteGroup = new TokenInviteGroup();
                            tokenInviteGroup.setGroup(groupDoc);
                            tokenInviteGroup.setEmail(emailUser);
                            tokenInviteGroupRepository.save(tokenInviteGroup);
                        }
                        String link = hostname + "/groups/" + groupDoc.getId();
                        sendEmailShareGroup(emailUser, link);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean acceptInviteMember(Long groupId) {
        User user = baseUtils.getUser();
        if(user != null){
            TokenInviteGroup tokenInviteGroup = tokenInviteGroupRepository.findByEmailAndGroupId(user.getEmail(), groupId);
            if(tokenInviteGroup != null){
                tokenInviteGroupRepository.delete(tokenInviteGroup);
                GroupDoc groupDoc = groupDocRepository.findById(groupId).orElse(null);
                if(groupDoc != null){
                    GroupShareUser groupShareUser = new GroupShareUser();
                    groupShareUser.setGroup(groupDoc);
                    groupShareUser.setUser(user);
                    groupShareUserRepository.save(groupShareUser);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public int getPermissionGroup(Long groupId) {
        User user = baseUtils.getUser();
        if(user != null){
            GroupDoc groupDoc = baseUtils.getGroupDoc(user, groupId);
            if(groupDoc != null){
                return 1;
            }
            TokenInviteGroup tokenInviteGroup = tokenInviteGroupRepository.findByEmailAndGroupId(user.getEmail(), groupId);
            if(tokenInviteGroup != null){
                return 2;
            }
        }
        return 0;
    }

    @Override
    public List<TokenInviteGroupDto> getPendingInvites(Long groupId) {
        User user = baseUtils.getUser();
        if(user != null){
            GroupDoc groupDoc = baseUtils.getGroupDoc(user, groupId);
            if(groupDoc != null){
                List<TokenInviteGroup> tokenInviteGroups = tokenInviteGroupRepository.findByGroup(groupDoc);
                List<TokenInviteGroupDto> pendingInvites = new ArrayList<>();
                if(tokenInviteGroups != null && !tokenInviteGroups.isEmpty()){
                    for(TokenInviteGroup tokenInviteGroup : tokenInviteGroups){
                        pendingInvites.add(modelMapperUtils.mapAllProperties(tokenInviteGroup, TokenInviteGroupDto.class));
                    }
                }
                return pendingInvites;
            }
        }
        return null;
    }

    @Override
    public boolean inviteResendMemberGroup(Long groupId, String emailUser) {
        User user = baseUtils.getUser();
        if(user != null){
            GroupDoc groupDoc = baseUtils.getGroupDoc(user, groupId);
            if(groupDoc != null){
                TokenInviteGroup tokenInviteGroup = tokenInviteGroupRepository.findByEmailAndGroupId(emailUser, groupDoc.getId());
                if(tokenInviteGroup != null){
                    tokenInviteGroup.setUpdatedAt(new Date());
                    String link = hostname + "/groups/" + groupDoc.getId();
                    sendEmailShareGroup(emailUser, link);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean cancelInviteMember(Long groupId, String emailUser) {
        User user = baseUtils.getUser();
        if(user != null){
            GroupDoc groupDoc = baseUtils.getGroupDoc(user, groupId);
            if(groupDoc != null){
                TokenInviteGroup tokenInviteGroup = tokenInviteGroupRepository.findByEmailAndGroupId(emailUser, groupId);
                if(tokenInviteGroup != null){
                    tokenInviteGroupRepository.delete(tokenInviteGroup);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean leaveGroup(Long groupId, String emailUser) {
        User user = baseUtils.getUser();
        if(user != null){
            GroupDoc groupDoc = baseUtils.getGroupDoc(user, groupId);
            if(groupDoc != null){
                GroupShareUser groupShareUser = groupShareUserRepository.findByUserAndGroupId(user, groupId);
                if(groupShareUser != null){
                    groupShareUserRepository.delete(groupShareUser);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<MemberGroupDto> getMembersGroup(Long groupId) {
        User user = baseUtils.getUser();
        if(user != null){
            GroupDoc groupDoc = baseUtils.getGroupDoc(user, groupId);
            if(groupDoc != null){
                List<GroupShareUser> groupShareUserList = groupShareUserRepository.findAllByGroupId(groupId);
                List<MemberGroupDto> userDtoList = new ArrayList<>();
                MemberGroupDto owner = modelMapperUtils.mapAllProperties(groupDoc.getUser(), MemberGroupDto.class);
                owner.setCreatedAt(groupDoc.getCreatedAt());
                owner.setStatusOwner((byte) 1);
                userDtoList.add(owner);
                if(groupShareUserList != null && !groupShareUserList.isEmpty()){
                    for (GroupShareUser groupShareUser : groupShareUserList) {
                        MemberGroupDto memberGroupDto = modelMapperUtils.mapAllProperties(groupShareUser.getUser(), MemberGroupDto.class);
                        memberGroupDto.setCreatedAt(groupShareUser.getUpdatedAt());
                        userDtoList.add(memberGroupDto);
                    }
                }
                return userDtoList;
            }
            return null;
        }
        return null;
    }

    @Override
    public boolean deleteMembersGroup(Long groupId, String username) {
        User user = baseUtils.getUser();
        if(user != null){
            GroupDoc groupDoc = groupDocRepository.findByIdAndUser(groupId, user);
            if(groupDoc != null){
                User member = userRepository.findByUsername(username);
                if(member != null){
                    GroupShareUser memberGroup = groupShareUserRepository.findByUserAndGroupId(member, groupId);
                    if(memberGroup != null){
                        groupShareUserRepository.delete(memberGroup);
                        return true;
                    }
                    return false;
                }
                return false;
            }
            return false;
        }
        return false;
    }

    @Override
    public boolean changePositionMemberGroup(Long groupId, String username) {
        User user = baseUtils.getUser();
        if(user != null){
            GroupDoc groupDoc = groupDocRepository.findByIdAndUser(groupId, user);
            if(groupDoc != null){
                User member = userRepository.findByUsername(username);
                if(member != null){
                    GroupShareUser groupShareUser = groupShareUserRepository.findByUserAndGroupId(member, groupId);
                    if(groupShareUser != null){
                        groupShareUser.setUser(user);
                        groupDoc.setUser(member);
                        groupShareUserRepository.save(groupShareUser);
                        groupDocRepository.save(groupDoc);
                        return true;
                    }
                    return false;
                }
                return false;
            }
            return false;
        }
        return false;
    }

    private void sendEmailShareGroup(String email, String applicationUrl){
        String url= "<b>Link accept</b><a href='" + applicationUrl + "'>Accept</a>";
        emailService.sendSimpleMessage(email, "Invite group: ", url);
    }
}
