package com.hust.edu.vn.services.impl.document;

import com.hust.edu.vn.dto.UserDto;
import com.hust.edu.vn.entity.Document;
import com.hust.edu.vn.entity.DocumentShareUser;
import com.hust.edu.vn.entity.User;
import com.hust.edu.vn.repository.DocumentRepository;
import com.hust.edu.vn.repository.DocumentShareUserRepository;
import com.hust.edu.vn.repository.GroupHasDocumentRepository;
import com.hust.edu.vn.repository.UserRepository;
import com.hust.edu.vn.services.document.DocumentShareUserService;
import com.hust.edu.vn.services.user.EmailService;
import com.hust.edu.vn.utils.BaseUtils;
import com.hust.edu.vn.utils.ModelMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DocumentShareUserServiceImpl implements DocumentShareUserService {
    private final GroupHasDocumentRepository groupHasDocumentRepository;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final BaseUtils baseUtils;
    private final DocumentShareUserRepository documentShareUserRepository;
    private final EmailService emailService;
    private final ModelMapperUtils modelMapperUtils;

    @Value("${URL_FE}")
    private String hostname;

    public DocumentShareUserServiceImpl(DocumentRepository documentRepository, BaseUtils baseUtils, DocumentShareUserRepository documentShareUserRepository,
                                        UserRepository userRepository, EmailService emailService, ModelMapperUtils modelMapperUtils,
                                        GroupHasDocumentRepository groupHasDocumentRepository) {
        this.documentRepository = documentRepository;
        this.baseUtils = baseUtils;
        this.documentShareUserRepository = documentShareUserRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.modelMapperUtils = modelMapperUtils;
        this.groupHasDocumentRepository = groupHasDocumentRepository;
    }


    // neu dc share roi thi khong can luu vao db nua
    @Override
    public boolean shareDocument(String documentKey, List<String> emailUsers) {
        User user = baseUtils.getUser();
        if(user != null){
            Document document = documentRepository.findByDocumentKeyAndStatusDelete(documentKey, (byte) 0);
            if(document != null && (document.getUser() == user || (documentShareUserRepository.existsByUserAndDocument(user, document)))){
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
                            String link = hostname + "/document/" + documentKey;
                            sendEmailShareDocument(guest.getEmail(), link, user);
                        }
                    }
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean deleteShareDocument(String documentKey, Long id) {
        User user = baseUtils.getUser();
        User member = userRepository.findById(id).orElse(null);
        if(user != null && member != null){
            Document document = documentRepository.findByDocumentKeyAndUserAndStatusDelete(documentKey, user, (byte) 0);
            if(document != null){
                DocumentShareUser documentShareUser = documentShareUserRepository.findByDocumentAndUser(document, member);
                if(documentShareUser!= null){
                    documentShareUserRepository.delete(documentShareUser);
                    return true;
                }
                return false;
            }
            return false;
        }
        return false;
    }

    @Override
    public List<UserDto> getUsersSharedDocuments(String documentKey) {
        Document document = documentRepository.findByDocumentKeyAndStatusDelete(documentKey, (byte) 0);
        if(document != null){
            User user = baseUtils.getUser();
            if (document.getDocsPublic() == 1 || document.getUser() == user || documentShareUserRepository.existsByUserAndDocument(user, document) || groupHasDocumentRepository.existsUserInGroupWithDocument(user, document)) {
                List<UserDto> userDtoList = new ArrayList<>();
                userDtoList.add(modelMapperUtils.mapAllProperties(document.getUser(), UserDto.class));
                List<DocumentShareUser> documentShareUserList = documentShareUserRepository.findAllByDocument(document);
                if(documentShareUserList!= null && !documentShareUserList.isEmpty()) {
                    for (DocumentShareUser documentShareUser : documentShareUserList) {
                        userDtoList.add(modelMapperUtils.mapAllProperties(documentShareUser.getUser(), UserDto.class));
                    }
                }
                return userDtoList;
            }
        }
        return null;
    }

    private void sendEmailShareDocument(String email, String applicationUrl, User user){
        String message= "<h1>Hi,</h1><h2>" + user.getUsername() +  " has shared the document for you! Please access the link to view document. </h2> <h3><a href='"
                + applicationUrl + "'>" + applicationUrl + "</a></h3>";
        emailService.sendSimpleMessage(email, "Docskanry: Document Sharing", message);
    }

}
