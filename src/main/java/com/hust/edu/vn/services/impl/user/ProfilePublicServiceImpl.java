package com.hust.edu.vn.services.impl.user;

import com.hust.edu.vn.entity.Document;
import com.hust.edu.vn.entity.User;
import com.hust.edu.vn.model.ProfilePublicModel;
import com.hust.edu.vn.repository.DocumentRepository;
import com.hust.edu.vn.repository.FollowRepository;
import com.hust.edu.vn.repository.UserRepository;
import com.hust.edu.vn.services.user.ProfilePublicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@Slf4j
public class ProfilePublicServiceImpl implements ProfilePublicService {
    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    private final DocumentRepository documentRepository;

    @Autowired
    public ProfilePublicServiceImpl(UserRepository userRepository, FollowRepository followRepository, DocumentRepository documentRepository) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.documentRepository = documentRepository;
    }

    // todo: display profile -> wait upload document
    @Override
    public ProfilePublicModel getProfileModel(String username) {
        User user = userRepository.findByUsername(username);
        ArrayList<Document> listPublicDocuments = documentRepository.findByUserIdAndDocsPublic(user.getId(), 1);
        log.info(String.valueOf(listPublicDocuments));
        return null;
    }
}
