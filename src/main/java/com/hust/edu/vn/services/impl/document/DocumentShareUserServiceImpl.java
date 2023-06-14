package com.hust.edu.vn.services.impl.document;

import com.hust.edu.vn.entity.Document;
import com.hust.edu.vn.entity.DocumentShareUser;
import com.hust.edu.vn.entity.User;
import com.hust.edu.vn.repository.DocumentRepository;
import com.hust.edu.vn.repository.DocumentShareUserRepository;
import com.hust.edu.vn.repository.UserRepository;
import com.hust.edu.vn.services.document.DocumentShareUserService;
import com.hust.edu.vn.services.user.EmailService;
import com.hust.edu.vn.utils.AwsS3Utils;
import com.hust.edu.vn.utils.BaseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DocumentShareUserServiceImpl implements DocumentShareUserService {
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final BaseUtils baseUtils;
    private final DocumentShareUserRepository documentShareUserRepository;
    private final AwsS3Utils awsS3Utils;
    private final EmailService emailService;

    public DocumentShareUserServiceImpl(DocumentRepository documentRepository, BaseUtils baseUtils, DocumentShareUserRepository documentShareUserRepository,
                                        UserRepository userRepository, AwsS3Utils awsS3Utils, EmailService emailService) {
        this.documentRepository = documentRepository;
        this.baseUtils = baseUtils;
        this.documentShareUserRepository = documentShareUserRepository;
        this.userRepository = userRepository;
        this.awsS3Utils = awsS3Utils;
        this.emailService = emailService;
    }


    // neu dc share roi thi khong can luu vao db nua
    @Override
    public boolean shareDocument(String documentKey, List<String> emailUsers, String link) {
        User user = baseUtils.getUser();
        Document document = documentRepository.findByDocumentKeyAndUserAndStatusDelete(documentKey, user, (byte) 0);
        if(document != null){
            if(emailUsers != null && !emailUsers.isEmpty()){
                for(String emailUser : emailUsers){
                    User guest = userRepository.findByEmail(emailUser);
                    if(guest != null){
                        if(!documentShareUserRepository.existsByUserAndDocument(guest, document)){
                            DocumentShareUser documentShareUser = new DocumentShareUser();
                            documentShareUser.setDocument(document);
                            documentShareUser.setUser(guest);
                            documentShareUserRepository.save(documentShareUser);
                        }
                        sendEmailShareDocument(guest.getEmail(), link, documentKey);
                    }
                }
                return true;
            }
            return false;
        }
        return false;
    }


    @Override
    public byte[] loadFileFromS3(String documentKey) {
        User user = baseUtils.getUser();
        Document document = documentRepository.findByDocumentKeyAndStatusDelete(documentKey, (byte) 0);
        if(document != null){
            DocumentShareUser documentShareUser = documentShareUserRepository.findByDocumentAndUser(document, user);
            if(documentShareUser != null){
                return awsS3Utils.readFile(document.getUser().getRootPath(), documentKey);
            }
            return null;
        }
        return null;
    }

    @Override
    public boolean deleteShareDocument(String documentKey, List<String> emailUsers) {
        User user = baseUtils.getUser();
        if(user != null){
            Document document = documentRepository.findByDocumentKeyAndUserAndStatusDelete(documentKey, user, (byte) 0);
            if(document != null){
                if(emailUsers != null && !emailUsers.isEmpty()){
                    for(String emailUser : emailUsers){
                        User guest = userRepository.findByEmail(emailUser);
                        DocumentShareUser documentShareUser = documentShareUserRepository.findByDocumentAndUser(document, guest);
                        if(documentShareUser != null){
                            documentShareUserRepository.delete(documentShareUser);
                        }
                    }
                    return true;
                }
                return false;
            }
            return false;
        }
        return false;
    }

    private void sendEmailShareDocument(String email, String applicationUrl, String documentKey){
        String url = applicationUrl + "/api/v1/management/document/share/read/" + documentKey;
        emailService.sendSimpleMessage(email, "Share document to you: ", url);
    }

}
