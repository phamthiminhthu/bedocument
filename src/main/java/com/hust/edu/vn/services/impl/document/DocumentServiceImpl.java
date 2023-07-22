package com.hust.edu.vn.services.impl.document;


import com.hust.edu.vn.dto.*;
import com.hust.edu.vn.entity.*;

import com.hust.edu.vn.model.DocumentEditModel;
import com.hust.edu.vn.model.DocumentModel;
import com.hust.edu.vn.repository.*;
import com.hust.edu.vn.services.document.*;
import com.hust.edu.vn.utils.AwsS3Utils;
import com.hust.edu.vn.utils.BaseUtils;
import com.hust.edu.vn.utils.ExtractDataFileUtils;
import com.hust.edu.vn.utils.ModelMapperUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
@Slf4j
public class DocumentServiceImpl implements DocumentService {
    private final LikeDocumentRepository likeDocumentRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final DocumentShareUserRepository documentShareUserRepository;
    private final TypeDocumentRepository typeDocumentRepository;
    private final TagRepository tagRepository;
    private final UrlRepository urlRepository;
    private final GroupHasDocumentRepository groupHasDocumentRepository;
    private final AwsS3Utils awsS3Utils;
    private final ModelMapperUtils modelMapperUtils;
    private final ExtractDataFileUtils extractDataFileUtils;
    private final RestTemplate restTemplate;
    private final DocumentRepository documentRepository;
    private final BaseUtils baseUtils;
    private final CollectionHasDocumentRepository collectionHasDocumentRepository;

    private final TagService tagService;
    private final TypeDocumentService typeDocumentService;

//    private final LikeDocumentService likeDocumentService;

    @Value("${google.scholar.api.key}")
    private String apiKey;
    private final String BASE_URL = "https://serpapi.com/search.json?engine=google_scholar";

    @Autowired
    public DocumentServiceImpl(AwsS3Utils awsS3Utils, ModelMapperUtils modelMapperUtils, ExtractDataFileUtils extractDataFileUtils, RestTemplate restTemplate, DocumentRepository documentRepository, CollectionHasDocumentRepository collectionHasDocumentRepository,
                               BaseUtils baseUtils,
                               GroupHasDocumentRepository groupHasDocumentRepository,
                               UrlRepository urlRepository,
                               TagRepository tagRepository,
                               TypeDocumentRepository typeDocumentRepository, TagService tagService, TypeDocumentService typeDocumentService,
                               DocumentShareUserRepository documentShareUserRepository,
                               FollowRepository followRepository,
                               UserRepository userRepository,
                               LikeDocumentRepository likeDocumentRepository) {
        this.baseUtils = baseUtils;
        this.awsS3Utils = awsS3Utils;
        this.modelMapperUtils = modelMapperUtils;
        this.extractDataFileUtils = extractDataFileUtils;
        this.restTemplate = restTemplate;
        this.documentRepository = documentRepository;
        this.collectionHasDocumentRepository = collectionHasDocumentRepository;
        this.groupHasDocumentRepository = groupHasDocumentRepository;
        this.urlRepository = urlRepository;
        this.tagRepository = tagRepository;
        this.typeDocumentRepository = typeDocumentRepository;
        this.tagService = tagService;
        this.typeDocumentService = typeDocumentService;
        this.documentShareUserRepository = documentShareUserRepository;
        this.followRepository = followRepository;
        this.userRepository = userRepository;
        this.likeDocumentRepository = likeDocumentRepository;
    }
    @Override
    public Document uploadDocument(MultipartFile multipartFile) {
        CompletableFuture<String> documentKeyCompletableFuture = getNameDocument(multipartFile);
        CompletableFuture<DocumentModel> documentModelCompletableFuture = getResultDocumentModelFromPDF(multipartFile);
        CompletableFuture.allOf(documentKeyCompletableFuture, documentModelCompletableFuture).join();
        try {
            String documentKey = documentKeyCompletableFuture.get();
            DocumentModel documentModel = documentModelCompletableFuture.get();
            if(documentModel != null){
                Document document = modelMapperUtils.mapAllProperties(documentModel, Document.class);
                document.setDocumentKey(documentKey);
                document.setDocsName(multipartFile.getOriginalFilename());
                User user = baseUtils.getUser();
                document.setUser(user);
                documentRepository.save(document);
                return document;
            }
            Document document = new Document();
            document.setDocumentKey(documentKey);
            document.setDocsName(multipartFile.getOriginalFilename());
            User user = baseUtils.getUser();
            document.setUser(user);
            documentRepository.save(document);
            return document;

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] loadFileFromS3(String documentKey) {
        User user = baseUtils.getUser();
        Document document = documentRepository.findByDocumentKeyAndStatusDelete(documentKey, (byte) 0);
        if(document != null){
            if(document.getUser() == user || document.getDocsPublic() == 1 || documentShareUserRepository.existsByUserAndDocument(user, document)){
                return awsS3Utils.readFile(document.getUser().getRootPath(), documentKey);
            }
            return null;
        }
        return null;
    }

    @Override
    public List<DocumentDto> getListDocument() {
        User user = baseUtils.getUser();
        if(user != null){
            List<Document> documents = documentRepository.findByUserAndStatusDeleteOrderByCreatedAtDesc(user, (byte) 0);
            return baseUtils.getListDocumentsDto(documents);
        }
        return null;
    }

    @Override
    public DocumentDto getDocumentModel(String documentKey) {
        User user = baseUtils.getUser();
        if(user != null){
            Document document = documentRepository.findByDocumentKeyAndStatusDelete(documentKey, (byte) 0);
            if(document != null){
                if(document.getDocsPublic() == 1 || document.getUser() == user || documentShareUserRepository.existsByUserAndDocument(user, document) || groupHasDocumentRepository.existsUserInGroupWithDocument(user, document)) {
                    return baseUtils.getDocumentDto(document);
                }
                return null;
            }
        }
        return null;
    }
    @Override
    public List<DocumentDto> getTrashListDocument() {
        User user = baseUtils.getUser();
        if(user != null){
            List<Document> listDocumentTrash = documentRepository.findByUserAndStatusDeleteOrderByCreatedAtDesc(user, (byte) 1);
            return baseUtils.getListDocumentsDto(listDocumentTrash);
        }
        return null;
    }

    @Override
    public List<DocumentDto> getListDocumentLoved() {
        User user = baseUtils.getUser();
        if(user != null){
            List<Document> documentList = documentRepository.findAllByUserAndLovedOrderByCreatedAtDesc(user, (byte) 1);
            return baseUtils.getListDocumentsDto(documentList);
        }
        return null;
    }

    @Override
    public List<DocumentDto> getListDocumentPublic() {
        User user = baseUtils.getUser();
        if(user != null){
            List<Document> documentList = documentRepository.findAllByUserAndDocsPublicOrderByCreatedAtDesc(user, (byte) 1);
            return baseUtils.getListDocumentsDto(documentList);
        }
        return null;
    }

    @Override
    public List<DocumentDto> getListDocumentPublicByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if(user != null){
            List<Document> documentList = documentRepository.findAllByUserAndDocsPublicOrderByCreatedAtDesc(user, (byte) 1);
            User currentUser = baseUtils.getUser();
            if(currentUser != null && documentList != null && documentList.size() > 0){
                List<DocumentDto> documentDtoList = new ArrayList<>();
                for (Document document : documentList){
                    if(likeDocumentRepository.existsByUserAndDocument(currentUser, document)){
                        DocumentDto documentDto = baseUtils.getDocumentDto(document);
                        documentDto.setLiked((byte) 1);
                        documentDtoList.add(documentDto);
                    }else{
                        DocumentDto documentDto = baseUtils.getDocumentDto(document);
                        documentDtoList.add(documentDto);
                    }
                }
                return documentDtoList;
            }
            return baseUtils.getListDocumentsDto(documentList);
        }
        return null;
    }

    @Override
    public List<DocumentDto> getListDocumentShared() {
        User user = baseUtils.getUser();
        if(user != null){
            List<DocumentShareUser> documentShareUserList = documentShareUserRepository.findAllByUser(user);
            List<Document> documents = new ArrayList<>();
            if(documentShareUserList != null  && !documentShareUserList.isEmpty()){
                for(DocumentShareUser documentShareUser : documentShareUserList){
                    documents.add(documentShareUser.getDocument());
                }
            }
            return baseUtils.getListDocumentsDto(documents);
        }
        return null;
    }

    @Override
    public List<DocumentDto> getListDocumentCompleted() {
        User user = baseUtils.getUser();
        if(user != null){
            List<Document> documentList = documentRepository.findAllByUserAndDocsStatusOrderByCreatedAtDesc(user, (byte) 1);
            return baseUtils.getListDocumentsDto(documentList);
        }
        return null;
    }

    @Override
    public List<DocumentDto>  getListDocumentPublicFollowing() {
        User user = baseUtils.getUser();
        if(user != null){
            List<Follow> followingList = followRepository.findAllByFollower(user);
            List<DocumentDto> documentDtoList = new ArrayList<>();
            if(followingList != null && !followingList.isEmpty()){
                List<User> followingUsers = new ArrayList<>();
                for(Follow following : followingList){
                        followingUsers.add(userRepository.findById(following.getFollowingId()).orElse(null));
                }
                for(User following : followingUsers){
                    List<Document> documentList = documentRepository.findAllByUserAndDocsPublicOrderByUpdatedAtDesc(following, (byte) 1);
                    documentDtoList.addAll(baseUtils.getListDocumentsDto(documentList));
                }
                documentDtoList.sort((obj1, obj2) -> obj2.getUpdatedAt().compareTo(obj1.getUpdatedAt()));
                for(DocumentDto document : documentDtoList){
                    if(likeDocumentRepository.existsByUserAndDocumentId(user, document.getId())){
                        document.setLiked(((byte) 1));
                    }
                }
            }
            return documentDtoList;
        }
        return null;
    }
    @Override
    public List<UserDto> getListSuggestUsers() {
       User user = baseUtils.getUser();
       if (user != null){
           Set<UserDto> userDtoList = new HashSet<>();
            List<Document> documentsSuggest = getDocumentSuggested(user);
            if(documentsSuggest != null && !documentsSuggest.isEmpty()){
                for(Document document : documentsSuggest){
                    userDtoList.add(modelMapperUtils.mapAllProperties(document.getUser(), UserDto.class));
                }
            }
            return new ArrayList<>(userDtoList);
       }
        return null;
    }

    @Override
    public List<DocumentDto> getListDocumentPublicSuggest() {
        User user = baseUtils.getUser();
        if(user != null){
            List<Document> documentsSuggest = getDocumentSuggested(user);
            if(documentsSuggest != null && documentsSuggest.size() == 0){
                documentsSuggest = getListDocumentHighStars(user);
            }
            List<DocumentDto> documentSuggestDto =  baseUtils.getListDocumentsDto(documentsSuggest);
            for(DocumentDto documentDto : documentSuggestDto){
                if(likeDocumentRepository.existsByUserAndDocumentId(user, documentDto.getId())){
                    documentDto.setLiked((byte) 1);
                }
            }
            return documentSuggestDto;
        }
        return null;
    }

    private List<Document> getDocumentSuggested(User user){
        List<Long> usersId = getListIdUsers(user);
        List<String> listTagNameFollow = tagRepository.findAllByUsersId(usersId);
        List<String> listTypeDocumentFollow = typeDocumentRepository.findAllByUsers(usersId);
        return documentRepository.findAllByTagsAndTypedocumentNotInUsers(listTagNameFollow, listTypeDocumentFollow, usersId);
    }

    private List<Document> getListDocumentHighStars(User user){
        List<Long> usersId = getListIdUsers(user);
        return documentRepository.findTop10ByOrderByQuantityLikeDescUpdatedAtDescAndNotUsers(usersId);
    }

    private List<Long> getListIdUsers(User user){
        List<Follow> usersFollowing = followRepository.findAllByFollower(user);
        List<Long> users = new ArrayList<>();
        users.add(user.getId());
        for(Follow follow : usersFollowing){
            users.add(follow.getFollowingId());
        }
        return users;
    }

    @Override
    public boolean updateLovedDocument(String documentKey) {
        User user = baseUtils.getUser();
        if(user != null){
            Document document = documentRepository.findByDocumentKeyAndUserAndStatusDelete(documentKey, user, (byte) 0);
            if(document != null){
                document.setLoved(document.getLoved() == 0 ? (byte) 1 : (byte) 0);
                documentRepository.save(document);
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public boolean updatePublicDocument(String documentKey) {
        User user = baseUtils.getUser();
        if(user != null){
            Document document = documentRepository.findByDocumentKeyAndUserAndStatusDelete(documentKey, user, (byte) 0);
            if(document != null){
                document.setDocsPublic(document.getDocsPublic() == 0 ? (byte) 1 : (byte) 0);
                documentRepository.save(document);
                return true;
            }
            return true;
        }
        return false;
    }
    @Override
    public boolean moveToTrash(List<String> listDocumentKey) {
        User user =  baseUtils.getUser();
        if(user != null){
            boolean check = baseUtils.checkDocument(listDocumentKey, (byte) 0);
            if(check){
                for(String keyName : listDocumentKey){
                    Document document = documentRepository.findByDocumentKeyAndUserAndStatusDelete(keyName, user, (byte) 0);
                    if(document != null){
                        document.setStatusDelete((byte) 1);
                        document.setUpdatedAt(new Date());
                        documentRepository.save(document);
                    }
                }
            }
            return check;
        }
        return false;
    }

    @Override
    public boolean deleteDocument(List<String> listDocumentKey) {
        boolean check = baseUtils.checkDocument(listDocumentKey, (byte) 1);
        if(check){
            for(String keyName : listDocumentKey){
                User user = baseUtils.getUser();
                if(user != null){
                    Document document = documentRepository.findByDocumentKeyAndUserAndStatusDelete( keyName, user, (byte) 1);
                    if(document != null){
                        if(collectionHasDocumentRepository.existsByDocumentId(document.getId())){
                            List<CollectionHasDocument> collectionHasDocuments = collectionHasDocumentRepository.findByDocumentId(document.getId());
                            if(collectionHasDocuments != null && !collectionHasDocuments.isEmpty()){
                               collectionHasDocumentRepository.deleteAll(collectionHasDocuments);
                            }
                        }
                        if(urlRepository.existsByDocument(document)){
                            List<Url> urls = urlRepository.findAllByDocument(document);
                            if(urls != null && !urls.isEmpty()){
                                urlRepository.deleteAll(urls);
                            }
                        }
                        if(tagRepository.existsByDocument(document)){
                            List<Tag> tags = tagRepository.findAllByDocument(document);
                            if(tags != null && !tags.isEmpty()){
                                tagRepository.deleteAll(tags);
                            }
                        }
                        if(typeDocumentRepository.existsByDocument(document)){
                            List<TypeDocument> typeDocuments = typeDocumentRepository.findAllByDocument(document);
                            if(typeDocuments != null && !typeDocuments.isEmpty()){
                                typeDocumentRepository.deleteAll(typeDocuments);
                            }
                        }
                        if(documentShareUserRepository.existsByDocument(document)){
                            List<DocumentShareUser> documentShareUsers = documentShareUserRepository.findAllByDocument(document);
                            if(documentShareUsers!= null &&!documentShareUsers.isEmpty()){
                                documentShareUserRepository.deleteAll(documentShareUsers);
                            }
                        }
                        if(groupHasDocumentRepository.existsByDocument(document)){
                            List<GroupHasDocument> groupHasDocuments = groupHasDocumentRepository.findAllByDocument(document);
                            if(groupHasDocuments != null && !groupHasDocuments.isEmpty()){
                                groupHasDocumentRepository.deleteAll(groupHasDocuments);
                            }
                        }
                        documentRepository.delete(document);
                        awsS3Utils.deleteFileFromS3Bucket(user.getRootPath(), document.getDocumentKey(), "document");
                    }else{
                        return false;
                    }
                }else{
                    return false;
                }
            }
        }
        return check;
    }

    @Override
    public boolean undoDocument(List<String> listDocumentKey) {
        boolean check = baseUtils.checkDocument(listDocumentKey, (byte) 1);
        if(check){
            for(String keyName : listDocumentKey){
                User user = baseUtils.getUser();
                Document document = documentRepository.findByDocumentKeyAndUserAndStatusDelete(keyName, user, (byte) 1);
                document.setStatusDelete((byte) 0);
                document.setUpdatedAt(new Date());
                documentRepository.save(document);
            }
        }
        return check;
    }

    @Override
    public boolean editDocumentByKey(String documentKey, DocumentEditModel documentEditModel) {
        User user = baseUtils.getUser();
        if(user!= null) {
            Document document = documentRepository.findByDocumentKeyAndUserAndStatusDelete(documentKey, user, (byte) 0);
            if (document != null) {
                Document newDocument = modelMapperUtils.mapAllProperties(documentEditModel.getDocumentModel(), Document.class);
                newDocument.setId(document.getId());
                newDocument.setDocumentKey(documentKey);
                newDocument.setUser(user);
                newDocument.setQuantityLike(document.getQuantityLike());
                newDocument.setCreatedAt(document.getCreatedAt());
                newDocument.setUpdatedAt(new Date());

                // tags
                List<Tag> tags = tagRepository.findAllByDocument(document);
                if(documentEditModel.getTags() != null && !documentEditModel.getTags().isEmpty()){
                    if(tags != null && !tags.isEmpty()){
                        List<String> oldListTags = new ArrayList<>();
                        for(Tag tag : tags){
                            oldListTags.add(tag.getTagName());
                        }
                        List<String> oldListTagsDeleted = removeDuplicatesMultiList(documentEditModel.getTags(), oldListTags);
                        if(oldListTagsDeleted != null && !oldListTagsDeleted.isEmpty()) {
                            for(String oldTag : oldListTagsDeleted){
                                tagService.deleteTag(documentKey, oldTag);
                            }
                        }
                        List<String> newListTags  = removeDuplicatesMultiList(oldListTags, documentEditModel.getTags());
                        if(newListTags != null && !newListTags.isEmpty()){
                            for(String newTag : newListTags){
                                tagService.createTag(documentKey, newTag);
                            }
                        }

                    }else{
                        for(String newTag : documentEditModel.getTags()){
                            tagService.createTag(documentKey, newTag);
                        }
                    }
                }else{
                    if(tags != null && !tags.isEmpty()){
                        tagRepository.deleteAll(tags);
                    }
                }
                // type docs
                List<TypeDocument> typeDocuments = typeDocumentRepository.findAllByDocument(document);
                if(documentEditModel.getTypesDoc() != null && !documentEditModel.getTypesDoc().isEmpty()){
                    if(typeDocuments != null && !typeDocuments.isEmpty()){
                        List<String> oldTypesDocs = new ArrayList<>();
                        for (TypeDocument typeDocument : typeDocuments){
                            oldTypesDocs.add(typeDocument.getTypeName());
                        }
                        List<String> newTypesDocs = removeDuplicatesMultiList(oldTypesDocs, documentEditModel.getTypesDoc());
                        List<String> oldTypesDeleted = removeDuplicatesMultiList(documentEditModel.getTypesDoc(), oldTypesDocs);
                        if(oldTypesDeleted!= null &&!oldTypesDeleted.isEmpty()){
                            for(String oldTypeDelete : oldTypesDeleted){
                                typeDocumentService.deleteTypeDocument(documentKey, oldTypeDelete);
                            }
                        }
                        if(newTypesDocs != null && !newTypesDocs.isEmpty()){
                            for(String typeName : newTypesDocs){
                                TypeDocument typeDocument = new TypeDocument();
                                typeDocument.setTypeName(typeName);
                                typeDocument.setDocument(document);
                                typeDocumentRepository.save(typeDocument);
                            }
                        }

                    }else{
                        for(String typeName : documentEditModel.getTypesDoc()){
                            TypeDocument typeDocument = new TypeDocument();
                            typeDocument.setTypeName(typeName);
                            typeDocument.setDocument(document);
                            typeDocumentRepository.save(typeDocument);
                        }
                    }
                }else{
                    if(typeDocuments != null && !typeDocuments.isEmpty()){
                        typeDocumentRepository.deleteAll(typeDocuments);
                    }
                }
                documentRepository.save(newDocument);
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public boolean updateInformationDocument(String documentKey, DocumentModel documentModel) {
        User user = baseUtils.getUser();
        if(user != null){
            Document document = documentRepository.findByDocumentKeyAndUserAndStatusDelete(documentKey, user, (byte) 0);
            if(document != null){
                Document newDocument = modelMapperUtils.mapAllProperties(documentModel, Document.class);
                newDocument.setId(document.getId());
                newDocument.setQuantityLike(document.getQuantityLike());
                newDocument.setUpdatedAt(new Date());
                newDocument.setCreatedAt(document.getCreatedAt());
                documentRepository.save(newDocument);
                return true;
            }
            return false;
        }
      return false;
    }
    @Override
    public boolean updateInformationListDocument(List<DocumentModel> listDataDocumentRequest) {
        User user = baseUtils.getUser();
        if(user != null){
            List<String> listDocumentKey = listDataDocumentRequest.stream()
                    .map(DocumentModel::getDocumentKey)
                    .collect(Collectors.toList());
           if(baseUtils.checkDocument(listDocumentKey, (byte) 0)){
               for(String key : listDocumentKey){
                   Document document = documentRepository.findByDocumentKeyAndUserAndStatusDelete(key, user, (byte) 0);
                   if(document != null){
                       document.setDocsPublic(document.getDocsPublic());
                       document.setLoved(document.getLoved());
                       document.setUpdatedAt(new Date());
                       documentRepository.save(document);
                       return true;
                   }
               }
           }
           return false;
        }
        return false;
    }

    @Override
    public Document updateContentDocument(String documentKey) {
        return null;
    }

    @Async
    protected CompletableFuture<String> getNameDocument(MultipartFile multipartFile){
        User user = baseUtils.getUser();
        if(user != null){
            String url = awsS3Utils.uploadFileDocument(multipartFile, user.getRootPath() + "document/");
            String documentKey = url.substring(url.lastIndexOf("/") + 1);
            return CompletableFuture.completedFuture(documentKey);
        }
        return null;
    }
    @Async
    public CompletableFuture<DocumentModel> getResultDocumentModelFromPDF(MultipartFile multipartFile){
        return CompletableFuture.completedFuture(searchDataDocsBySerApi(multipartFile));
    }
    private DocumentModel searchDataDocsBySerApi(MultipartFile multipartFile){
        HashMap<String, String> resultMap = extractDataFileUtils.extractData(multipartFile);
        if(resultMap != null && resultMap.containsKey("title")) {
            if(resultMap.get("title") != null){
                String query = resultMap.get("title").replaceAll("\\s+", " ").trim();
                return getDataDocumentFromTitle(query, resultMap, 0);
            }
        }
        return null;
    }

    private DocumentModel getDataDocumentFromTitle(String query, HashMap<String, String> resultMap, int check ){
        if (query != null && query.length() > 5) {
            MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
            queryParams.add("api_key", apiKey);
            queryParams.add("q", query);
            String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                    .queryParams(queryParams)
                    .build()
                    .toUriString();
            String response = restTemplate.getForObject(url, String.class);
            if(response != null){
                JSONObject obj = new JSONObject(response);
                if (obj.has("profiles")) {
                    JSONObject objQuery = obj.getJSONObject("profiles");
                    if (objQuery.has("link")) {
                        String suggestUrl = objQuery.getString("link");
                        try {
                            URL sUrl = new URL(suggestUrl);
                            String sQuery = sUrl.getQuery();
                            String[] params = sQuery.split("&");
                            String qValue = null;
                            for (String param : params) {
                                if (param.startsWith("q=")) {
                                    qValue = param.substring(2);
                                    break;
                                }
                            }
                            if (qValue != null) {
                                qValue = qValue.replace("+", " ");
                            }
                            queryParams.set("q", qValue);
                            String secondUrl = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                                    .queryParams(queryParams)
                                    .build()
                                    .toUriString();
                            String secondResponse = restTemplate.getForObject(secondUrl, String.class);
                            JSONObject secondObj = new JSONObject();
                            if(secondResponse != null){
                                secondObj = new JSONObject(secondResponse);
                            }
                            resultMap.put("title", qValue);
                            return getDataDocumentModel(secondObj, resultMap, check);
                        } catch (MalformedURLException e) {
                            log.info(e.getMessage());
                            if(check == 0 && resultMap.containsKey("title2")){
                                if(resultMap.get("title2") != null){
                                    String query2 = resultMap.get("title2").replaceAll("\\s+", " ").trim();
                                    return getDataDocumentFromTitle(query2, resultMap, 1);
                                }
                            }else{
                               return getDataDocumentModel(null, resultMap, 1);
                            }
                        }
                    }
                }
                if(check == 0){
                    DocumentModel documentModel =  getDataDocumentModel(obj, resultMap, 0);
                    if(documentModel.getTitle() == null){
                        return getDataDocumentModel(obj, resultMap, 1);
                    }
                    return documentModel;
                }else if(check == 1){
                    return getDataDocumentModel(obj, resultMap, 1);
                }
            }
        }
        if(check == 0 && resultMap.get("title2") != null){
            String query2 = resultMap.get("title2").replaceAll("\\s+", " ").trim();
            return getDataDocumentFromTitle(query2, resultMap, 1);
        }
        return getDataDocumentModel(null, resultMap, check);
    }

    private DocumentModel getDataDocumentModel(JSONObject response, HashMap<String, String> query, int checkResult){
        DocumentModel docModel = new DocumentModel();
        if(response != null && response.has("organic_results")) {
            JSONArray arrResults = response.getJSONArray("organic_results");
            if (arrResults.length() > 0) {
                for (int i = 0; i < arrResults.length(); i++) {
                    JSONObject data = arrResults.getJSONObject(i);
                    if(data.has("title")) {
                        String title = data.getString("title");
                        String newQuery = query.get("title").toLowerCase().replaceAll("[^a-zA-Z0-9]", "").trim();
                        String newTitle = title.toLowerCase().replaceAll("[^a-zA-Z0-9]", "").trim();
                        if (newQuery.contains(newTitle) || newTitle.contains(newQuery) || arrResults.length() < 2) {
                            docModel.setTitle(title);
                            JSONObject publicationInfo = data.getJSONObject("publication_info");
                            String summary = null;
                            if (publicationInfo.has("summary")) {
                                summary = publicationInfo.getString("summary");
                                docModel.setSummary(summary);
                                Pattern pattern = Pattern.compile("\\b\\d{4}\\b");
                                Matcher matcher = pattern.matcher(summary);
                                if (matcher.find()) {
                                    String publishingYear = matcher.group();
                                    docModel.setPublishingYear(Integer.parseInt(publishingYear));
                                } else {
                                    docModel.setPublishingYear(null);
                                }
                            }
                            if (publicationInfo.has("authors")) {
                                JSONArray listAuthors = publicationInfo.getJSONArray("authors");
                                if (listAuthors.length() > 0) {
                                    StringBuilder strAuthors = new StringBuilder();
                                    for (int j = 0; j < listAuthors.length(); j++) {
                                        strAuthors.append(listAuthors.getJSONObject(j).getString("name"));
                                        if (j < listAuthors.length() - 1) {
                                            strAuthors.append(", ");
                                        }
                                    }
                                    docModel.setAuthors(strAuthors.toString());
                                } else {
                                    docModel.setAuthors(null);
                                }
                            } else {
                                if (summary != null) {
                                    String[] parts = summary.split("-");
                                    String authors = parts[0].trim();
                                    docModel.setAuthors(authors);
                                } else {
                                    docModel.setAuthors(null);
                                }
                            }
                            return docModel;
                        }
                    }
                }
            }
        }
        if(checkResult == 0){
            if(query.containsKey("title2")){
                if(query.get("title2") != null){
                    String query2 = query.get("title2").replaceAll("\\s+", " ").trim();
                    return getDataDocumentFromTitle(query2, query, 1);
                }
            }
        }
        try {
            if(query.get("title") != null){
                String decodedQuery = URLDecoder.decode(query.get("title"), StandardCharsets.UTF_8);
                docModel.setTitle(decodedQuery);
            }
            if(query.get("author") != null) {
                String decodedAuthor = URLDecoder.decode(query.get("author"), StandardCharsets.UTF_8);
                docModel.setAuthors(decodedAuthor);
            }
            return docModel;
        } catch (Exception e) {
            log.info(e.getMessage());
            return docModel;
        }

    }

    private List<String> removeDuplicatesMultiList(List<String> fList, List<String> sList) {
       if(fList != null && !fList.isEmpty()){
           Set<String> fListSet = new HashSet<>(fList);
           if( sList != null && !sList.isEmpty()){
               List<String> result = new ArrayList<>();
               for (String str :  sList){
                   if(!fListSet.contains(str)){
                       result.add(str);
                   }
               }
               return result;
           }
           return fList;
       }
       return null;
    }


}
