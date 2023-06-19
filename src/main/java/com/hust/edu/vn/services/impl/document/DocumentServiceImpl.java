package com.hust.edu.vn.services.impl.document;


import com.hust.edu.vn.dto.*;
import com.hust.edu.vn.entity.*;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
@Slf4j
public class DocumentServiceImpl implements DocumentService {
    private final TypeDocumentRepository typeDocumentRepository;
    private final TagRepository tagRepository;
    private final UrlRepository urlRepository;
    private final GroupCollectionHasDocumentRepository groupCollectionHasDocumentRepository;
    private final AwsS3Utils awsS3Utils;
    private final ModelMapperUtils modelMapperUtils;
    private final ExtractDataFileUtils extractDataFileUtils;
    private final RestTemplate restTemplate;
    private final DocumentRepository documentRepository;
    private final BaseUtils baseUtils;
    private final CollectionHasDocumentRepository collectionHasDocumentRepository;
    private final CollectionRepository collectionRepository;
    private final TagService tagService;
    private final TypeDocumentService typeDocumentService;
    private final UrlService urlService;
//    private final LikeDocumentService likeDocumentService;

    @Value("${google.scholar.api.key}")
    private String apiKey;
    private final String BASE_URL = "https://serpapi.com/search.json?engine=google_scholar";

    @Autowired
    public DocumentServiceImpl(AwsS3Utils awsS3Utils, ModelMapperUtils modelMapperUtils, ExtractDataFileUtils extractDataFileUtils, RestTemplate restTemplate, DocumentRepository documentRepository, CollectionHasDocumentRepository collectionHasDocumentRepository,
                               BaseUtils baseUtils, CollectionRepository collectionRepository,
                               GroupCollectionHasDocumentRepository groupCollectionHasDocumentRepository,
                               UrlRepository urlRepository,
                               TagRepository tagRepository,
                               TypeDocumentRepository typeDocumentRepository, TagService tagService, TypeDocumentService typeDocumentService, UrlService urlService) {
        this.baseUtils = baseUtils;
        this.awsS3Utils = awsS3Utils;
        this.modelMapperUtils = modelMapperUtils;
        this.extractDataFileUtils = extractDataFileUtils;
        this.restTemplate = restTemplate;
        this.documentRepository = documentRepository;
        this.collectionHasDocumentRepository = collectionHasDocumentRepository;
        this.collectionRepository = collectionRepository;
        this.groupCollectionHasDocumentRepository = groupCollectionHasDocumentRepository;
        this.urlRepository = urlRepository;
        this.tagRepository = tagRepository;
        this.typeDocumentRepository = typeDocumentRepository;
        this.tagService = tagService;
        this.typeDocumentService = typeDocumentService;
        this.urlService = urlService;
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
        if(user != null){
            Document document = documentRepository.findByDocumentKeyAndUserAndStatusDelete(documentKey, user, (byte) 0);
            if(document != null){
                return awsS3Utils.readFile(user.getRootPath(), documentKey);
            }
        }
        return null;
    }

    @Override
    public List<DocumentDto> getListDocument() {
        User user = baseUtils.getUser();
        if(user != null){
            List<Document> documents = documentRepository.findByUserAndStatusDelete(user, (byte) 0);
            List<DocumentDto> listDocumentModels = new ArrayList<>();
            if(documents != null && !documents.isEmpty()){
                for(Document document : documents){
                    DocumentDto documentModel = getDocumentDto(document);
                    listDocumentModels.add(documentModel);
                }
            }
            return listDocumentModels;
        }
        return null;
    }

    @Override
    public DocumentDto getDocumentModel(String documentKey) {
        User user = baseUtils.getUser();
        Document document = documentRepository.findByDocumentKeyAndStatusDelete(documentKey, (byte) 0);
        if(document != null){
            if(document.getDocsPublic() == 1 || (user != null && document.getUser() == user )) {
                DocumentDto documentModel = getDocumentDto(document);
                return documentModel;
            }
            return null;
        }
        return null;
    }
    @Override
    public List<DocumentDto> getTrashListDocument() {
        User user = baseUtils.getUser();
        if(user != null){
            List<Document> listDocumentTrash = documentRepository.findAllByUserIdAndStatusDelete(user.getId(), (byte) 1);
            if(listDocumentTrash != null && !listDocumentTrash.isEmpty()){
                List<DocumentDto> documentsModelList = new ArrayList<>();
                for(Document document : listDocumentTrash){
                    DocumentDto documentModel = modelMapperUtils.mapAllProperties(document, DocumentDto.class);
                    documentsModelList.add(documentModel);
                }
                return documentsModelList;
            }
            return null;

        }
        return null;
    }

    @Override
    public List<DocumentDto> getListDocumentLoved() {
        User user = baseUtils.getUser();
        if(user != null){
            List<Document> documentList = documentRepository.findAllByUserAndLoved(user, (byte) 1);
            List<DocumentDto> documentDtoList = new ArrayList<>();
            if(documentList != null &&  !documentList.isEmpty()){
                for (Document document : documentList){
                    DocumentDto documentDto = modelMapperUtils.mapAllProperties(document, DocumentDto.class);
                    documentDtoList.add(documentDto);
                }
                return documentDtoList;
            }
            return null;
        }
        return null;
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
    public boolean moveDocument(List<String> listDocumentKey, List<Long> listTypeId, String type) {
        User user = baseUtils.getUser();
        if(user != null){
            boolean check = baseUtils.checkDocument(listDocumentKey, (byte) 0);
            if(check){
                if(type.equals("collection")){
                    if(listDocumentKey != null && !listDocumentKey.isEmpty() && listTypeId != null && !listTypeId.isEmpty()){
                        for(String key : listDocumentKey){
                            for(Long id :  listTypeId){
                                if(!collectionHasDocumentRepository.existsByDocumentDocumentKeyAndCollectionIdAndCollectionUser(key, id, user)){
                                    Collection collection = collectionRepository.findByIdAndUserId(id, user.getId());
                                    if(collection != null){
                                        CollectionHasDocument collectionHasDocument = new CollectionHasDocument();
                                        Document document = documentRepository.findByDocumentKeyAndUserAndStatusDelete(key, user, (byte) 0);
                                        collectionHasDocument.setCollection(collection);
                                        collectionHasDocument.setDocument(document);
                                        collectionHasDocumentRepository.save(collectionHasDocument);
                                    }
                                }
                            }
                        }
                        return true;
                    }
                }
                if (type.equals("group")) {
                    if(listDocumentKey != null && !listDocumentKey.isEmpty() && listTypeId != null && !listTypeId.isEmpty()){
                        for(String key : listDocumentKey) {
                            for (Long id : listTypeId) {
                                Document document = documentRepository.findByDocumentKeyAndUserAndStatusDelete(key, user, (byte) 0);
                                GroupDoc groupDoc = baseUtils.getGroupDoc(user, id);
                                if(document != null && groupDoc != null){
                                    if(!groupCollectionHasDocumentRepository.existsByDocumentAndGroupIdAndCollection(document, id, null)){
                                        GroupCollectionHasDocument groupCollectionHasDocument = new GroupCollectionHasDocument();
                                        groupCollectionHasDocument.setGroup(groupDoc);
                                        groupCollectionHasDocument.setDocument(document);
                                        groupCollectionHasDocumentRepository.save(groupCollectionHasDocument);
                                    }
                                }
                            }
                        }
                        return true;
                    }
                    return false;
                }
                if(type.equals("group-collection")){
                    if(listDocumentKey != null && !listDocumentKey.isEmpty() && listTypeId != null && !listTypeId.isEmpty()){
                        for (String key : listDocumentKey){
                            Document document = documentRepository.findByDocumentKeyAndStatusDelete(key, (byte) 0);
                            if(document != null){
                                for (Long id : listTypeId){
                                    Collection collection = collectionRepository.findById(id).orElse(null);
                                    if(collection != null){
                                        if(collection.getGroupDoc() != null){
                                            if(!groupCollectionHasDocumentRepository.existsByDocumentAndCollectionId(document, id)){
                                                GroupCollectionHasDocument groupCollectionHasDocument = new GroupCollectionHasDocument();
                                                groupCollectionHasDocument.setCollection(collection);
                                                groupCollectionHasDocument.setDocument(document);
                                                groupCollectionHasDocument.setGroup(collection.getGroupDoc());
                                                groupCollectionHasDocumentRepository.save(groupCollectionHasDocument);
                                            }
                                        }

                                    }

                                }
                            }
                        }
                        return true;
                    }
                    return false;
                }
            }
            return false;
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
                documentRepository.save(document);
            }
        }
        return check;
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
        }else{
            return null;
        }

    }
    @Async
    public CompletableFuture<DocumentModel> getResultDocumentModelFromPDF(MultipartFile multipartFile){
        return CompletableFuture.completedFuture(searchDataDocsBySerApi(multipartFile));
    }
    private DocumentModel searchDataDocsBySerApi(MultipartFile multipartFile){
        String query = extractDataFileUtils.extractData(multipartFile).replaceAll("\\s+", " ").trim();
        if(query != null && query.length() > 5){
            MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
            queryParams.add("api_key", apiKey);
            queryParams.add("q", query);
            String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                    .queryParams(queryParams)
                    .build()
                    .toUriString();
            String response = restTemplate.getForObject(url, String.class);
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
                        JSONObject secondObj = new JSONObject(secondResponse);
                        return getDataDocumentModel(secondObj, qValue);
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return getDataDocumentModel(obj, query);
        }
        return null;
    }

    private DocumentModel getDataDocumentModel(JSONObject response, String query){
        DocumentModel docModel = new DocumentModel();
        if(response.has("organic_results")) {
            JSONArray arrResults = response.getJSONArray("organic_results");
            if (arrResults.length() > 0) {
                for (int i = 0; i < arrResults.length(); i++) {
                    JSONObject data = arrResults.getJSONObject(i);
                    if(data.has("title")) {
                        String title = data.getString("title");
                        String newQuery = query.toLowerCase().replaceAll("[^a-zA-Z0-9]", "").trim();
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
        try {
            String decodedQuery = URLDecoder.decode(query, "UTF-8");
            docModel.setTitle(decodedQuery);
            return docModel;
        } catch (Exception e) {
            return docModel;
        }
    }

    private DocumentDto getDocumentDto(Document document){
        DocumentDto documentModel = modelMapperUtils.mapAllProperties(document, DocumentDto.class);
        List<TagDto> tagDtoList = tagService.showAllTag(document.getDocumentKey());
        documentModel.setTagDtoList(tagDtoList);
        List<TypeDocumentDto> typeDocumentDtoList = typeDocumentService.showAllTypeDocument(document.getDocumentKey());
        documentModel.setTypeDocumentsList(typeDocumentDtoList);
        List<UrlDto> urlDtoList = urlService.showAllUrl(document.getDocumentKey());
        documentModel.setUrls(urlDtoList);
//        List<UserDto> likeDocumentList = likeDocumentService.showAllUserLikeDocument(document.getDocumentKey());
//        documentModel.setLikeDocumentList(likeDocumentList);
        return documentModel;
    }



}
