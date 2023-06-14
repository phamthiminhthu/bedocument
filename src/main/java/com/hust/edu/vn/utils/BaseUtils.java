package com.hust.edu.vn.utils;

import com.hust.edu.vn.entity.Document;
import com.hust.edu.vn.entity.GroupDoc;
import com.hust.edu.vn.entity.GroupShareUser;
import com.hust.edu.vn.entity.User;
import com.hust.edu.vn.repository.DocumentRepository;
import com.hust.edu.vn.repository.GroupDocRepository;
import com.hust.edu.vn.repository.GroupShareUserRepository;
import com.hust.edu.vn.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.print.Doc;
import java.util.List;

@Component
@Slf4j
public class BaseUtils {
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final GroupDocRepository groupDocRepository;
    private final GroupShareUserRepository groupShareUserRepository;

    public BaseUtils(UserRepository userRepository, DocumentRepository documentRepository, GroupDocRepository groupDocRepository,
                     GroupShareUserRepository groupShareUserRepository) {
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
        this.groupDocRepository = groupDocRepository;
        this.groupShareUserRepository = groupShareUserRepository;
    }

    public User getUser(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email);
    }

    public boolean checkDocument(List<String> listDocumentKey, byte statusDelete){
        User user = getUser();
        if(user != null){
            for(String keyName : listDocumentKey){
                Document document = documentRepository.findByDocumentKeyAndUserAndStatusDelete(keyName, user, statusDelete);
                if(document == null){
                    return false;
                }
            }
            return true;
        }
        return false;

    }

    public GroupDoc getGroupDoc(User user, Long groupId){
        GroupDoc groupDoc = groupDocRepository.findByIdAndUser(groupId, user);
        if(groupDoc == null){
            GroupShareUser groupShareUser = groupShareUserRepository.findByUserAndGroupId(user, groupId);
            if(groupShareUser != null){
                return groupShareUser.getGroup();
            }
            return null;
        }
        return groupDoc;
    }
}
