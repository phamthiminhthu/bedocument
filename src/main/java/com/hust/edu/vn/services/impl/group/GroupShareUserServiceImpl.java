package com.hust.edu.vn.services.impl.group;

import com.hust.edu.vn.dto.MemberGroupDto;
import com.hust.edu.vn.dto.InvitationMemberDto;
import com.hust.edu.vn.entity.GroupDoc;
import com.hust.edu.vn.entity.GroupShareUser;
import com.hust.edu.vn.entity.InvitationMember;
import com.hust.edu.vn.entity.User;
import com.hust.edu.vn.repository.GroupDocRepository;
import com.hust.edu.vn.repository.GroupShareUserRepository;
import com.hust.edu.vn.repository.InvitationMemberRepository;
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
    private final InvitationMemberRepository invitationMemberRepository;
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
                                     InvitationMemberRepository invitationMemberRepository) {
        this.groupShareUserRepository = groupShareUserRepository;
        this.baseUtils = baseUtils;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.groupDocRepository = groupDocRepository;
        this.modelMapperUtils = modelMapperUtils;
        this.invitationMemberRepository = invitationMemberRepository;
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
                        if(!invitationMemberRepository.existsByEmailAndGroupId(emailUser, groupId)){
                            InvitationMember invitationMember = new InvitationMember();
                            invitationMember.setGroup(groupDoc);
                            invitationMember.setEmail(emailUser);
                            invitationMemberRepository.save(invitationMember);
                        }
                        String link = hostname + "/groups/" + groupDoc.getId();
                        sendEmailShareGroup(emailUser, link, user, groupDoc);
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
            InvitationMember invitationMember = invitationMemberRepository.findByEmailAndGroupId(user.getEmail(), groupId);
            if(invitationMember != null){
                invitationMemberRepository.delete(invitationMember);
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
            InvitationMember invitationMember = invitationMemberRepository.findByEmailAndGroupId(user.getEmail(), groupId);
            if(invitationMember != null){
                return 2;
            }
        }
        return 0;
    }

    @Override
    public List<InvitationMemberDto> getPendingInvites(Long groupId) {
        User user = baseUtils.getUser();
        if(user != null){
            GroupDoc groupDoc = baseUtils.getGroupDoc(user, groupId);
            if(groupDoc != null){
                List<InvitationMember> invitationMembers = invitationMemberRepository.findByGroup(groupDoc);
                List<InvitationMemberDto> pendingInvites = new ArrayList<>();
                if(invitationMembers != null && !invitationMembers.isEmpty()){
                    for(InvitationMember invitationMember : invitationMembers){
                        pendingInvites.add(modelMapperUtils.mapAllProperties(invitationMember, InvitationMemberDto.class));
                    }
                }
                return pendingInvites;
            }
        }
        return null;
    }


    @Override
    public List<InvitationMemberDto> getAllPendingInvitations() {
        User user = baseUtils.getUser();
        if(user != null){
            List<InvitationMember> pendingInvites = invitationMemberRepository.findAllByEmail(user.getEmail());
            List<InvitationMemberDto> tokenInviteGroupsDto = new ArrayList<>();
            if(pendingInvites != null && pendingInvites.size() > 0) {
                for (InvitationMember invitationMember : pendingInvites){
                    InvitationMemberDto invitationMemberDto = modelMapperUtils.mapAllProperties(invitationMember, InvitationMemberDto.class);
                    invitationMemberDto.setGroupName(invitationMember.getGroup().getGroupName());
                    invitationMemberDto.setGroupId(invitationMember.getGroup().getId());
                    tokenInviteGroupsDto.add(invitationMemberDto);
                }
            }
            return tokenInviteGroupsDto;
        }
        return null;
    }


    @Override
    public boolean inviteResendMemberGroup(Long groupId, String emailUser) {
        User user = baseUtils.getUser();
        if(user != null){
            GroupDoc groupDoc = baseUtils.getGroupDoc(user, groupId);
            if(groupDoc != null){
                InvitationMember invitationMember = invitationMemberRepository.findByEmailAndGroupId(emailUser, groupDoc.getId());
                if(invitationMember != null){
                    invitationMember.setUpdatedAt(new Date());
                    String link = hostname + "/groups/" + groupDoc.getId();
                    sendEmailShareGroup(emailUser, link, user, groupDoc);
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
                InvitationMember invitationMember = invitationMemberRepository.findByEmailAndGroupId(emailUser, groupId);
                if(invitationMember != null){
                    invitationMemberRepository.delete(invitationMember);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean declineInviteMember(Long groupId) {
        User user = baseUtils.getUser();
        if(user != null){
            InvitationMember invitationMember = invitationMemberRepository.findByEmailAndGroupId(user.getEmail(), groupId);
            if(invitationMember != null){
                invitationMemberRepository.delete(invitationMember);
                return true;
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

    private void sendEmailShareGroup(String email, String applicationUrl, User user, GroupDoc groupDoc){
        String message= "<h1>Hi,</h1><h2>" + user.getUsername() +  " has invited you to join group <strong>" + groupDoc.getGroupName() +
                "</strong></h2><h3><a href='" + applicationUrl + "'><button>Accept Invite</a></h3>";
        emailService.sendSimpleMessage(email, "Docskanry: Group invitation request", message);
    }
}
