package com.hust.edu.vn.services.impl.group;

import com.hust.edu.vn.dto.UserDto;
import com.hust.edu.vn.entity.GroupDoc;
import com.hust.edu.vn.entity.GroupShareUser;
import com.hust.edu.vn.entity.User;
import com.hust.edu.vn.repository.GroupDocRepository;
import com.hust.edu.vn.repository.GroupShareUserRepository;
import com.hust.edu.vn.repository.UserRepository;
import com.hust.edu.vn.services.group.GroupShareUserService;
import com.hust.edu.vn.services.user.EmailService;
import com.hust.edu.vn.utils.BaseUtils;
import com.hust.edu.vn.utils.ModelMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class GroupShareUserServiceImpl implements GroupShareUserService {
    private final GroupDocRepository groupDocRepository;
    private final UserRepository userRepository;
    private final GroupShareUserRepository groupShareUserRepository;

    private final ModelMapperUtils modelMapperUtils;
    private final BaseUtils baseUtils;
    private final EmailService emailService;

    public GroupShareUserServiceImpl(GroupShareUserRepository groupShareUserRepository, BaseUtils baseUtils,
                                     UserRepository userRepository, EmailService emailService,
                                     GroupDocRepository groupDocRepository, ModelMapperUtils modelMapperUtils) {
        this.groupShareUserRepository = groupShareUserRepository;
        this.baseUtils = baseUtils;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.groupDocRepository = groupDocRepository;
        this.modelMapperUtils = modelMapperUtils;
    }
    @Override
    public boolean inviteMemberGroup(Long groupId, List<String> emailUsers, String link) {
        User user = baseUtils.getUser();
        if(user != null){
            if(emailUsers != null && !emailUsers.isEmpty()){
                for(String email : emailUsers){
                    User guest = userRepository.findByEmail(email);
                    if(guest != null){


                    }else{

                    }
                }
            }
        }
        return false;
    }

    @Override
    public List<UserDto> getMembersGroup(Long groupId) {
        User user = baseUtils.getUser();
        if(user != null){
            boolean checkOwner = groupDocRepository.existsByIdAndUser(groupId, user);
            boolean checkMember = groupShareUserRepository.existsByGroupIdAndUser(groupId, user);
            if(checkOwner || checkMember){
                List<GroupShareUser> groupShareUserList = groupShareUserRepository.findAllByGroupId(groupId);
                List<UserDto> userDtoList = new ArrayList<>();
                if(groupShareUserList != null && !groupShareUserList.isEmpty()){
                    for (GroupShareUser groupShareUser : groupShareUserList) {
                        userDtoList.add(modelMapperUtils.mapAllProperties(groupShareUser.getUser(), UserDto.class));
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

    private boolean sendEmailShareGroup(String email, String applicationUrl){
        String url= applicationUrl;
        return emailService.sendSimpleMessage(email, "Share document to you: ", url);
    }
}
