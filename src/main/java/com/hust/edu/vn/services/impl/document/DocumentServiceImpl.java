package com.hust.edu.vn.services.impl.document;


import com.hust.edu.vn.dto.DocumentDto;
import com.hust.edu.vn.dto.TagDto;
import com.hust.edu.vn.dto.TypeDocumentDto;
import com.hust.edu.vn.dto.UrlDto;
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
    private final TypeDocumentService typeDocumentServic;
    private final UrlService urlService;
    private final LikeDocumentService likeDocumentService;

    @Value("${google.scholar.api.key}")
    private String apiKey;
    private final String BASE_URL = "https://serpapi.com/search.json?engine=google_scholar";

    @Autowired
    public DocumentServiceImpl(AwsS3Utils awsS3Utils, ModelMapperUtils modelMapperUtils, ExtractDataFileUtils extractDataFileUtils, RestTemplate restTemplate, DocumentRepository documentRepository, CollectionHasDocumentRepository collectionHasDocumentRepository,
                               BaseUtils baseUtils, CollectionRepository collectionRepository,
                               GroupCollectionHasDocumentRepository groupCollectionHasDocumentRepository,
                               UrlRepository urlRepository,
                               TagRepository tagRepository,
                               TypeDocumentRepository typeDocumentRepository, TagService tagService, TypeDocumentService typeDocumentServic, UrlService urlService, LikeDocumentService likeDocumentService) {
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
        this.typeDocumentServic = typeDocumentServic;
        this.urlService = urlService;
        this.likeDocumentService = likeDocumentService;
    }
    @Override
    public Document uploadDocument(MultipartFile multipartFile) {
        CompletableFuture<String> documentKeyCompletableFuture = getNameDocument(multipartFile);
        CompletableFuture<DocumentModel> documentModelCompletableFuture = getResultDocumentModelFromPDF(multipartFile);
        CompletableFuture.allOf(documentKeyCompletableFuture, documentModelCompletableFuture).join();
        try {
            String documentKey = documentKeyCompletableFuture.get();
            DocumentModel documentModel = documentModelCompletableFuture.get();
            Document document = modelMapperUtils.mapAllProperties(documentModel, Document.class);
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
                    DocumentDto documentModel = modelMapperUtils.mapAllProperties(document, DocumentDto.class);
                    List<TagDto> tagDtoList = tagService.showAllTag(document.getDocumentKey());
                    documentModel.setTagDtoList(tagDtoList);
                    List<TypeDocumentDto> typeDocumentDtoList = typeDocumentServic.showAllTypeDocument(document.getDocumentKey());
                    documentModel.setTypeDocumentsList(typeDocumentDtoList);
                    List<UrlDto> urlDtoList = urlService.showAllUrl(document.getDocumentKey());
                    documentModel.setUrls(urlDtoList);
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
        if(user != null){
            Document document = documentRepository.findByDocumentKeyAndUserAndStatusDelete(documentKey, user, (byte) 0);
            if(document != null){
                return modelMapperUtils.mapAllProperties(document, DocumentDto.class);
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
            return true;
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
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("api_key", apiKey);
        queryParams.add("q", query);
        String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .queryParams(queryParams)
                .build()
                .toUriString();
        String response = restTemplate.getForObject(url, String.class);
        log.info("response: " + response);
//        String response = "{\n  \"search_metadata\": {\n    \"id\": \"6468305136e6a97722a19c5c\",\n    \"status\": \"Success\",\n    \"json_endpoint\": \"https://serpapi.com/searches/af53d6a8c0c413ed/6468305136e6a97722a19c5c.json\",\n    \"created_at\": \"2023-05-20 02:28:33 UTC\",\n    \"processed_at\": \"2023-05-20 02:28:33 UTC\",\n    \"google_scholar_url\": \"https://scholar.google.com/scholar?q=Data+Structures+and+Algorithms+in+Java&hl=en\",\n    \"raw_html_file\": \"https://serpapi.com/searches/af53d6a8c0c413ed/6468305136e6a97722a19c5c.html\",\n    \"total_time_taken\": 0.47\n  },\n  \"search_parameters\": {\n    \"engine\": \"google_scholar\",\n    \"q\": \"Data Structures and Algorithms in Java\",\n    \"hl\": \"en\"\n  },\n  \"search_information\": {\n    \"organic_results_state\": \"Results for exact spelling\",\n    \"total_results\": 487000,\n    \"time_taken_displayed\": 0.03,\n    \"query_displayed\": \"Data Structures and Algorithms in Java\"\n  },\n  \"organic_results\": [\n    {\n      \"position\": 0,\n      \"title\": \"Data structures and algorithms in Java\",\n      \"result_id\": \"XlkwLuSy2rwJ\",\n      \"type\": \"Book\",\n      \"link\": \"https://books.google.com/books?hl=en&lr=&id=UqmYAgAAQBAJ&oi=fnd&pg=PA2&dq=Data+Structures+and+Algorithms+in+Java&ots=p7I1_E18u-&sig=zg-zN-RVph06P9Ovt_6KEfBwiG8\",\n      \"snippet\": \"… Java, we provide a primer on the Java language in Chapter 1. Still, this book is primarily a data structures book, not a Java … provide a comprehensive treatment of Java. Nevertheless, we …\",\n      \"publication_info\": {\n        \"summary\": \"MT Goodrich, R Tamassia, MH Goldwasser - 2014 - books.google.com\",\n        \"authors\": [\n          {\n            \"name\": \"MT Goodrich\",\n            \"link\": \"https://scholar.google.com/citations?user=sSS9gSoAAAAJ&hl=en&oi=sra\",\n            \"serpapi_scholar_link\": \"https://serpapi.com/search.json?author_id=sSS9gSoAAAAJ&engine=google_scholar_author&hl=en\",\n            \"author_id\": \"sSS9gSoAAAAJ\"\n          },\n          {\n            \"name\": \"R Tamassia\",\n            \"link\": \"https://scholar.google.com/citations?user=eEYPTKUAAAAJ&hl=en&oi=sra\",\n            \"serpapi_scholar_link\": \"https://serpapi.com/search.json?author_id=eEYPTKUAAAAJ&engine=google_scholar_author&hl=en\",\n            \"author_id\": \"eEYPTKUAAAAJ\"\n          },\n          {\n            \"name\": \"MH Goldwasser\",\n            \"link\": \"https://scholar.google.com/citations?user=9x4inqAAAAAJ&hl=en&oi=sra\",\n            \"serpapi_scholar_link\": \"https://serpapi.com/search.json?author_id=9x4inqAAAAAJ&engine=google_scholar_author&hl=en\",\n            \"author_id\": \"9x4inqAAAAAJ\"\n          }\n        ]\n      },\n      \"resources\": [\n        {\n          \"title\": \"academia.edu\",\n          \"file_format\": \"PDF\",\n          \"link\": \"https://www.academia.edu/download/36020958/Data_Structures_and_Algorithms_in_Java_Fourth_Edition.pdf\"\n        }\n      ],\n      \"inline_links\": {\n        \"serpapi_cite_link\": \"https://serpapi.com/search.json?engine=google_scholar_cite&q=XlkwLuSy2rwJ\",\n        \"cited_by\": {\n          \"total\": 701,\n          \"link\": \"https://scholar.google.com/scholar?cites=13608385917150583134&as_sdt=2005&sciodt=0,5&hl=en\",\n          \"cites_id\": \"13608385917150583134\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=2005&cites=13608385917150583134&engine=google_scholar&hl=en\"\n        },\n        \"related_pages_link\": \"https://scholar.google.com/scholar?q=related:XlkwLuSy2rwJ:scholar.google.com/&scioq=Data+Structures+and+Algorithms+in+Java&hl=en&as_sdt=0,5\",\n        \"serpapi_related_pages_link\": \"https://serpapi.com/search.json?as_sdt=0%2C5&engine=google_scholar&hl=en&q=related%3AXlkwLuSy2rwJ%3Ascholar.google.com%2F\",\n        \"versions\": {\n          \"total\": 9,\n          \"link\": \"https://scholar.google.com/scholar?cluster=13608385917150583134&hl=en&as_sdt=0,5\",\n          \"cluster_id\": \"13608385917150583134\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=0%2C5&cluster=13608385917150583134&engine=google_scholar&hl=en\"\n        }\n      }\n    },\n    {\n      \"position\": 1,\n      \"title\": \"Data structures and algorithms in Java\",\n      \"result_id\": \"1pXOfJo3CZkJ\",\n      \"type\": \"Book\",\n      \"link\": \"https://books.google.com/books?hl=en&lr=&id=iFc0DwAAQBAJ&oi=fnd&pg=PT20&dq=Data+Structures+and+Algorithms+in+Java&ots=8pxsWer2Ur&sig=geKHmuq4xE3BEDBYs7TUSP1mNhk\",\n      \"snippet\": \"… data structures and algorithms as used in computer programming. Data structures are ways in which data is … Algorithms are the procedures a software program uses to manipulate the …\",\n      \"publication_info\": {\n        \"summary\": \"R Lafore - 2017 - books.google.com\"\n      },\n      \"resources\": [\n        {\n          \"title\": \"msu.ac.th\",\n          \"file_format\": \"PDF\",\n          \"link\": \"http://www.wbi.msu.ac.th/file/1033/doc_40699.621226851941.pdf\"\n        }\n      ],\n      \"inline_links\": {\n        \"serpapi_cite_link\": \"https://serpapi.com/search.json?engine=google_scholar_cite&q=1pXOfJo3CZkJ\",\n        \"cited_by\": {\n          \"total\": 195,\n          \"link\": \"https://scholar.google.com/scholar?cites=11027406299251774934&as_sdt=2005&sciodt=0,5&hl=en\",\n          \"cites_id\": \"11027406299251774934\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=2005&cites=11027406299251774934&engine=google_scholar&hl=en\"\n        },\n        \"related_pages_link\": \"https://scholar.google.com/scholar?q=related:1pXOfJo3CZkJ:scholar.google.com/&scioq=Data+Structures+and+Algorithms+in+Java&hl=en&as_sdt=0,5\",\n        \"serpapi_related_pages_link\": \"https://serpapi.com/search.json?as_sdt=0%2C5&engine=google_scholar&hl=en&q=related%3A1pXOfJo3CZkJ%3Ascholar.google.com%2F\",\n        \"versions\": {\n          \"total\": 26,\n          \"link\": \"https://scholar.google.com/scholar?cluster=11027406299251774934&hl=en&as_sdt=0,5\",\n          \"cluster_id\": \"11027406299251774934\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=0%2C5&cluster=11027406299251774934&engine=google_scholar&hl=en\"\n        }\n      }\n    },\n    {\n      \"position\": 2,\n      \"title\": \"Data structures and algorithms in Java\",\n      \"result_id\": \"rnj7jhCP3Z4J\",\n      \"type\": \"Book\",\n      \"link\": \"https://citeseerx.ist.psu.edu/document?repid=rep1&type=pdf&doi=9cd9c15548322894e0378bf71e9214a04714377b\",\n      \"snippet\": \"… Java-related issues within the confines of one chapter. This chapter … of Java that are necessary for understanding the Java code offered in this book. The reader familiar with Java can …\",\n      \"publication_info\": {\n        \"summary\": \"A Drozdek - 2001 - Citeseer\"\n      },\n      \"resources\": [\n        {\n          \"title\": \"psu.edu\",\n          \"file_format\": \"PDF\",\n          \"link\": \"https://citeseerx.ist.psu.edu/document?repid=rep1&type=pdf&doi=9cd9c15548322894e0378bf71e9214a04714377b\"\n        }\n      ],\n      \"inline_links\": {\n        \"serpapi_cite_link\": \"https://serpapi.com/search.json?engine=google_scholar_cite&q=rnj7jhCP3Z4J\",\n        \"cited_by\": {\n          \"total\": 68,\n          \"link\": \"https://scholar.google.com/scholar?cites=11447463129126762670&as_sdt=2005&sciodt=0,5&hl=en\",\n          \"cites_id\": \"11447463129126762670\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=2005&cites=11447463129126762670&engine=google_scholar&hl=en\"\n        },\n        \"related_pages_link\": \"https://scholar.google.com/scholar?q=related:rnj7jhCP3Z4J:scholar.google.com/&scioq=Data+Structures+and+Algorithms+in+Java&hl=en&as_sdt=0,5\",\n        \"serpapi_related_pages_link\": \"https://serpapi.com/search.json?as_sdt=0%2C5&engine=google_scholar&hl=en&q=related%3Arnj7jhCP3Z4J%3Ascholar.google.com%2F\",\n        \"versions\": {\n          \"total\": 4,\n          \"link\": \"https://scholar.google.com/scholar?cluster=11447463129126762670&hl=en&as_sdt=0,5\",\n          \"cluster_id\": \"11447463129126762670\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=0%2C5&cluster=11447463129126762670&engine=google_scholar&hl=en\"\n        },\n        \"cached_page_link\": \"https://scholar.googleusercontent.com/scholar?q=cache:rnj7jhCP3Z4J:scholar.google.com/+Data+Structures+and+Algorithms+in+Java&hl=en&as_sdt=0,5\"\n      }\n    },\n    {\n      \"position\": 3,\n      \"title\": \"JGraphT—A Java library for graph data structures and algorithms\",\n      \"result_id\": \"RcUmDiYNDj0J\",\n      \"link\": \"https://dl.acm.org/doi/abs/10.1145/3381449\",\n      \"snippet\": \"… and data scientists alike. This article introduces JGraphT, a library that contains very efficient … generic graph data structures along with a sizeable collection of sophisticated algorithms. …\",\n      \"publication_info\": {\n        \"summary\": \"D Michail, J Kinable, B Naveh, JV Sichi - ACM Transactions on …, 2020 - dl.acm.org\",\n        \"authors\": [\n          {\n            \"name\": \"D Michail\",\n            \"link\": \"https://scholar.google.com/citations?user=DD0NjLEAAAAJ&hl=en&oi=sra\",\n            \"serpapi_scholar_link\": \"https://serpapi.com/search.json?author_id=DD0NjLEAAAAJ&engine=google_scholar_author&hl=en\",\n            \"author_id\": \"DD0NjLEAAAAJ\"\n          },\n          {\n            \"name\": \"J Kinable\",\n            \"link\": \"https://scholar.google.com/citations?user=u-qiJ0oAAAAJ&hl=en&oi=sra\",\n            \"serpapi_scholar_link\": \"https://serpapi.com/search.json?author_id=u-qiJ0oAAAAJ&engine=google_scholar_author&hl=en\",\n            \"author_id\": \"u-qiJ0oAAAAJ\"\n          },\n          {\n            \"name\": \"B Naveh\",\n            \"link\": \"https://scholar.google.com/citations?user=nWzfbRUAAAAJ&hl=en&oi=sra\",\n            \"serpapi_scholar_link\": \"https://serpapi.com/search.json?author_id=nWzfbRUAAAAJ&engine=google_scholar_author&hl=en\",\n            \"author_id\": \"nWzfbRUAAAAJ\"\n          }\n        ]\n      },\n      \"resources\": [\n        {\n          \"title\": \"arxiv.org\",\n          \"file_format\": \"PDF\",\n          \"link\": \"https://arxiv.org/pdf/1904.08355\"\n        }\n      ],\n      \"inline_links\": {\n        \"serpapi_cite_link\": \"https://serpapi.com/search.json?engine=google_scholar_cite&q=RcUmDiYNDj0J\",\n        \"cited_by\": {\n          \"total\": 106,\n          \"link\": \"https://scholar.google.com/scholar?cites=4399468343084893509&as_sdt=2005&sciodt=0,5&hl=en\",\n          \"cites_id\": \"4399468343084893509\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=2005&cites=4399468343084893509&engine=google_scholar&hl=en\"\n        },\n        \"related_pages_link\": \"https://scholar.google.com/scholar?q=related:RcUmDiYNDj0J:scholar.google.com/&scioq=Data+Structures+and+Algorithms+in+Java&hl=en&as_sdt=0,5\",\n        \"serpapi_related_pages_link\": \"https://serpapi.com/search.json?as_sdt=0%2C5&engine=google_scholar&hl=en&q=related%3ARcUmDiYNDj0J%3Ascholar.google.com%2F\",\n        \"versions\": {\n          \"total\": 6,\n          \"link\": \"https://scholar.google.com/scholar?cluster=4399468343084893509&hl=en&as_sdt=0,5\",\n          \"cluster_id\": \"4399468343084893509\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=0%2C5&cluster=4399468343084893509&engine=google_scholar&hl=en\"\n        }\n      }\n    },\n    {\n      \"position\": 4,\n      \"title\": \"Data structures and algorithms using Java\",\n      \"result_id\": \"xJXKBtO6Yp4J\",\n      \"type\": \"Book\",\n      \"link\": \"https://books.google.com/books?hl=en&lr=&id=JPkBDj1C53YC&oi=fnd&pg=PP1&dq=Data+Structures+and+Algorithms+in+Java&ots=qDWffdt9Ib&sig=xhcD_b7e1N4UlIh_tIUURIw0WK8\",\n      \"snippet\": \"With an accessible writing style and manageable amount of content, Data Structures and Algorithms Using Java is the ideal text for your course. This outstanding text correlates to the …\",\n      \"publication_info\": {\n        \"summary\": \"W McAllister - 2008 - books.google.com\"\n      },\n      \"inline_links\": {\n        \"serpapi_cite_link\": \"https://serpapi.com/search.json?engine=google_scholar_cite&q=xJXKBtO6Yp4J\",\n        \"cited_by\": {\n          \"total\": 28,\n          \"link\": \"https://scholar.google.com/scholar?cites=11412889821225063876&as_sdt=2005&sciodt=0,5&hl=en\",\n          \"cites_id\": \"11412889821225063876\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=2005&cites=11412889821225063876&engine=google_scholar&hl=en\"\n        },\n        \"related_pages_link\": \"https://scholar.google.com/scholar?q=related:xJXKBtO6Yp4J:scholar.google.com/&scioq=Data+Structures+and+Algorithms+in+Java&hl=en&as_sdt=0,5\",\n        \"serpapi_related_pages_link\": \"https://serpapi.com/search.json?as_sdt=0%2C5&engine=google_scholar&hl=en&q=related%3AxJXKBtO6Yp4J%3Ascholar.google.com%2F\",\n        \"versions\": {\n          \"total\": 3,\n          \"link\": \"https://scholar.google.com/scholar?cluster=11412889821225063876&hl=en&as_sdt=0,5\",\n          \"cluster_id\": \"11412889821225063876\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=0%2C5&cluster=11412889821225063876&engine=google_scholar&hl=en\"\n        }\n      }\n    },\n    {\n      \"position\": 5,\n      \"title\": \"A practical guide to data structures and algorithms using Java\",\n      \"result_id\": \"W1hdYykwW7wJ\",\n      \"type\": \"Book\",\n      \"link\": \"https://books.google.com/books?hl=en&lr=&id=xT8qBgAAQBAJ&oi=fnd&pg=PP1&dq=Data+Structures+and+Algorithms+in+Java&ots=tnLLBktUlK&sig=Z6mrQVwQfpwLrRayNQHizhp7e98\",\n      \"snippet\": \"Although traditional texts present isolated algorithms and data structures, they do not provide a unifying structure and offer little guidance on how to appropriately select among them. …\",\n      \"publication_info\": {\n        \"summary\": \"SA Goldman, KJ Goldman - 2007 - books.google.com\",\n        \"authors\": [\n          {\n            \"name\": \"SA Goldman\",\n            \"link\": \"https://scholar.google.com/citations?user=m9UbvIkAAAAJ&hl=en&oi=sra\",\n            \"serpapi_scholar_link\": \"https://serpapi.com/search.json?author_id=m9UbvIkAAAAJ&engine=google_scholar_author&hl=en\",\n            \"author_id\": \"m9UbvIkAAAAJ\"\n          }\n        ]\n      },\n      \"inline_links\": {\n        \"serpapi_cite_link\": \"https://serpapi.com/search.json?engine=google_scholar_cite&q=W1hdYykwW7wJ\",\n        \"cited_by\": {\n          \"total\": 23,\n          \"link\": \"https://scholar.google.com/scholar?cites=13572494856329975899&as_sdt=2005&sciodt=0,5&hl=en\",\n          \"cites_id\": \"13572494856329975899\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=2005&cites=13572494856329975899&engine=google_scholar&hl=en\"\n        },\n        \"related_pages_link\": \"https://scholar.google.com/scholar?q=related:W1hdYykwW7wJ:scholar.google.com/&scioq=Data+Structures+and+Algorithms+in+Java&hl=en&as_sdt=0,5\",\n        \"serpapi_related_pages_link\": \"https://serpapi.com/search.json?as_sdt=0%2C5&engine=google_scholar&hl=en&q=related%3AW1hdYykwW7wJ%3Ascholar.google.com%2F\",\n        \"versions\": {\n          \"total\": 4,\n          \"link\": \"https://scholar.google.com/scholar?cluster=13572494856329975899&hl=en&as_sdt=0,5\",\n          \"cluster_id\": \"13572494856329975899\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=0%2C5&cluster=13572494856329975899&engine=google_scholar&hl=en\"\n        }\n      }\n    },\n    {\n      \"position\": 6,\n      \"title\": \"Data structures and algorithm analysis in Java\",\n      \"result_id\": \"4Y-lZnKwHdEJ\",\n      \"type\": \"Book\",\n      \"link\": \"https://thuvienso.dau.edu.vn:88/handle/DHKTDN/6913\",\n      \"snippet\": \"… This book is suitable for either an advanced data structures (CS7) course or a first-year graduate course in algorithm analysis. Students should have some knowledge of intermediate …\",\n      \"publication_info\": {\n        \"summary\": \"MA Weiss - 2012 - thuvienso.dau.edu.vn\",\n        \"authors\": [\n          {\n            \"name\": \"MA Weiss\",\n            \"link\": \"https://scholar.google.com/citations?user=gfYCK9gAAAAJ&hl=en&oi=sra\",\n            \"serpapi_scholar_link\": \"https://serpapi.com/search.json?author_id=gfYCK9gAAAAJ&engine=google_scholar_author&hl=en\",\n            \"author_id\": \"gfYCK9gAAAAJ\"\n          }\n        ]\n      },\n      \"resources\": [\n        {\n          \"title\": \"dau.edu.vn\",\n          \"file_format\": \"PDF\",\n          \"link\": \"https://thuvienso.dau.edu.vn:88/bitstream/DHKTDN/6913/1/6238.Data%20structures%20and%20algorithm%20analysis%20in%20Java%20%283rd%20ed%29.pdf\"\n        }\n      ],\n      \"inline_links\": {\n        \"serpapi_cite_link\": \"https://serpapi.com/search.json?engine=google_scholar_cite&q=4Y-lZnKwHdEJ\",\n        \"cited_by\": {\n          \"total\": 350,\n          \"link\": \"https://scholar.google.com/scholar?cites=15068393933646434273&as_sdt=2005&sciodt=0,5&hl=en\",\n          \"cites_id\": \"15068393933646434273\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=2005&cites=15068393933646434273&engine=google_scholar&hl=en\"\n        },\n        \"related_pages_link\": \"https://scholar.google.com/scholar?q=related:4Y-lZnKwHdEJ:scholar.google.com/&scioq=Data+Structures+and+Algorithms+in+Java&hl=en&as_sdt=0,5\",\n        \"serpapi_related_pages_link\": \"https://serpapi.com/search.json?as_sdt=0%2C5&engine=google_scholar&hl=en&q=related%3A4Y-lZnKwHdEJ%3Ascholar.google.com%2F\",\n        \"versions\": {\n          \"total\": 8,\n          \"link\": \"https://scholar.google.com/scholar?cluster=15068393933646434273&hl=en&as_sdt=0,5\",\n          \"cluster_id\": \"15068393933646434273\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=0%2C5&cluster=15068393933646434273&engine=google_scholar&hl=en\"\n        },\n        \"cached_page_link\": \"https://scholar.googleusercontent.com/scholar?q=cache:4Y-lZnKwHdEJ:scholar.google.com/+Data+Structures+and+Algorithms+in+Java&hl=en&as_sdt=0,5\"\n      }\n    },\n    {\n      \"position\": 7,\n      \"title\": \"Data structures & algorithm analysis in Java\",\n      \"result_id\": \"UHyi9JTbQrsJ\",\n      \"type\": \"Book\",\n      \"link\": \"https://books.google.com/books?hl=en&lr=&id=4EFq0NjvrvwC&oi=fnd&pg=PP1&dq=Data+Structures+and+Algorithms+in+Java&ots=wFMsrrL5Ff&sig=D5npPnVEyEWL8izAyBAhjzziXVQ\",\n      \"snippet\": \"… If one data structure or algorithm is superior to another in all respects, the inferior one will usually have long been forgotten. For nearly every data structure and algorithm presented in …\",\n      \"publication_info\": {\n        \"summary\": \"CA Shaffer - 2011 - books.google.com\",\n        \"authors\": [\n          {\n            \"name\": \"CA Shaffer\",\n            \"link\": \"https://scholar.google.com/citations?user=v0tJmyYAAAAJ&hl=en&oi=sra\",\n            \"serpapi_scholar_link\": \"https://serpapi.com/search.json?author_id=v0tJmyYAAAAJ&engine=google_scholar_author&hl=en\",\n            \"author_id\": \"v0tJmyYAAAAJ\"\n          }\n        ]\n      },\n      \"resources\": [\n        {\n          \"title\": \"liberty.edu\",\n          \"file_format\": \"PDF\",\n          \"link\": \"https://digitalcommons.liberty.edu/cgi/viewcontent.cgi?article=1005&context=textbooks\"\n        }\n      ],\n      \"inline_links\": {\n        \"serpapi_cite_link\": \"https://serpapi.com/search.json?engine=google_scholar_cite&q=UHyi9JTbQrsJ\",\n        \"cited_by\": {\n          \"total\": 56,\n          \"link\": \"https://scholar.google.com/scholar?cites=13493588866361359440&as_sdt=2005&sciodt=0,5&hl=en\",\n          \"cites_id\": \"13493588866361359440\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=2005&cites=13493588866361359440&engine=google_scholar&hl=en\"\n        },\n        \"related_pages_link\": \"https://scholar.google.com/scholar?q=related:UHyi9JTbQrsJ:scholar.google.com/&scioq=Data+Structures+and+Algorithms+in+Java&hl=en&as_sdt=0,5\",\n        \"serpapi_related_pages_link\": \"https://serpapi.com/search.json?as_sdt=0%2C5&engine=google_scholar&hl=en&q=related%3AUHyi9JTbQrsJ%3Ascholar.google.com%2F\",\n        \"versions\": {\n          \"total\": 3,\n          \"link\": \"https://scholar.google.com/scholar?cluster=13493588866361359440&hl=en&as_sdt=0,5\",\n          \"cluster_id\": \"13493588866361359440\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=0%2C5&cluster=13493588866361359440&engine=google_scholar&hl=en\"\n        }\n      }\n    },\n    {\n      \"position\": 8,\n      \"title\": \"Data structures and problem solving using Java\",\n      \"result_id\": \"nR2BdIc0F6gJ\",\n      \"type\": \"Pdf\",\n      \"link\": \"https://dl.acm.org/doi/pdf/10.1145/288079.288084\",\n      \"snippet\": \"… is typically known as Data Structures (CS-… algorithms, and elementary data structures. An advanced course is offered at many universities that covers topics in data structures, algorithms…\",\n      \"publication_info\": {\n        \"summary\": \"MA Weiss - ACM SIGACT News, 1998 - dl.acm.org\",\n        \"authors\": [\n          {\n            \"name\": \"MA Weiss\",\n            \"link\": \"https://scholar.google.com/citations?user=gfYCK9gAAAAJ&hl=en&oi=sra\",\n            \"serpapi_scholar_link\": \"https://serpapi.com/search.json?author_id=gfYCK9gAAAAJ&engine=google_scholar_author&hl=en\",\n            \"author_id\": \"gfYCK9gAAAAJ\"\n          }\n        ]\n      },\n      \"resources\": [\n        {\n          \"title\": \"acm.org\",\n          \"file_format\": \"PDF\",\n          \"link\": \"https://dl.acm.org/doi/pdf/10.1145/288079.288084\"\n        }\n      ],\n      \"inline_links\": {\n        \"serpapi_cite_link\": \"https://serpapi.com/search.json?engine=google_scholar_cite&q=nR2BdIc0F6gJ\",\n        \"cited_by\": {\n          \"total\": 352,\n          \"link\": \"https://scholar.google.com/scholar?cites=12112207479216086429&as_sdt=2005&sciodt=0,5&hl=en\",\n          \"cites_id\": \"12112207479216086429\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=2005&cites=12112207479216086429&engine=google_scholar&hl=en\"\n        },\n        \"related_pages_link\": \"https://scholar.google.com/scholar?q=related:nR2BdIc0F6gJ:scholar.google.com/&scioq=Data+Structures+and+Algorithms+in+Java&hl=en&as_sdt=0,5\",\n        \"serpapi_related_pages_link\": \"https://serpapi.com/search.json?as_sdt=0%2C5&engine=google_scholar&hl=en&q=related%3AnR2BdIc0F6gJ%3Ascholar.google.com%2F\",\n        \"versions\": {\n          \"total\": 10,\n          \"link\": \"https://scholar.google.com/scholar?cluster=12112207479216086429&hl=en&as_sdt=0,5\",\n          \"cluster_id\": \"12112207479216086429\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=0%2C5&cluster=12112207479216086429&engine=google_scholar&hl=en\"\n        }\n      }\n    },\n    {\n      \"position\": 9,\n      \"title\": \"An introduction to data structures and algorithms with Java\",\n      \"result_id\": \"PjokkBDxnx4J\",\n      \"type\": \"Book\",\n      \"link\": \"https://val.serc.iisc.ernet.in/i6kduas9s2ny/03-lori-goyette-1/0138577498-introduction-to-data-structures-and-algorithms-w-in.pdf\",\n      \"snippet\": \"eBook \\\\ Introduction to Data Structures and Algorithms with Java, An « Read … Introduction to Data Structures and Algorithms with Java, An Kindle < MU4TA8WYX8 Introduction to …\",\n      \"publication_info\": {\n        \"summary\": \"GW Rowe, GW Rowe - 1998 - val.serc.iisc.ernet.in\"\n      },\n      \"inline_links\": {\n        \"serpapi_cite_link\": \"https://serpapi.com/search.json?engine=google_scholar_cite&q=PjokkBDxnx4J\",\n        \"cited_by\": {\n          \"total\": 24,\n          \"link\": \"https://scholar.google.com/scholar?cites=2206747395874896446&as_sdt=2005&sciodt=0,5&hl=en\",\n          \"cites_id\": \"2206747395874896446\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=2005&cites=2206747395874896446&engine=google_scholar&hl=en\"\n        },\n        \"related_pages_link\": \"https://scholar.google.com/scholar?q=related:PjokkBDxnx4J:scholar.google.com/&scioq=Data+Structures+and+Algorithms+in+Java&hl=en&as_sdt=0,5\",\n        \"serpapi_related_pages_link\": \"https://serpapi.com/search.json?as_sdt=0%2C5&engine=google_scholar&hl=en&q=related%3APjokkBDxnx4J%3Ascholar.google.com%2F\",\n        \"versions\": {\n          \"total\": 2,\n          \"link\": \"https://scholar.google.com/scholar?cluster=2206747395874896446&hl=en&as_sdt=0,5\",\n          \"cluster_id\": \"2206747395874896446\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=0%2C5&cluster=2206747395874896446&engine=google_scholar&hl=en\"\n        },\n        \"cached_page_link\": \"https://scholar.googleusercontent.com/scholar?q=cache:PjokkBDxnx4J:scholar.google.com/+Data+Structures+and+Algorithms+in+Java&hl=en&as_sdt=0,5\"\n      }\n    }\n  ],\n  \"related_searches\": [\n    {\n      \"query\": \"data structures algorithm analysis\",\n      \"link\": \"https://scholar.google.com/scholar?hl=en&as_sdt=0,5&qsp=1&q=data+structures+algorithm+analysis&qst=br\"\n    },\n    {\n      \"query\": \"data structures and algorithms in python\",\n      \"link\": \"https://scholar.google.com/scholar?hl=en&as_sdt=0,5&qsp=2&q=data+structures+and+algorithms+in+python&qst=br\"\n    },\n    {\n      \"query\": \"data structures and algorithms practical guide\",\n      \"link\": \"https://scholar.google.com/scholar?hl=en&as_sdt=0,5&qsp=3&q=data+structures+and+algorithms+%22practical+guide%22&qst=br\"\n    },\n    {\n      \"query\": \"java library graph data structures\",\n      \"link\": \"https://scholar.google.com/scholar?hl=en&as_sdt=0,5&qsp=4&q=%22java+library%22+graph+data+structures&qst=br\"\n    },\n    {\n      \"query\": \"data structures applications in java\",\n      \"link\": \"https://scholar.google.com/scholar?hl=en&as_sdt=0,5&qsp=5&q=%22data+structures%22+applications+in+java&qst=br\"\n    },\n    {\n      \"query\": \"classic data structures\",\n      \"link\": \"https://scholar.google.com/scholar?hl=en&as_sdt=0,5&qsp=6&q=classic+data+structures&qst=br\"\n    },\n    {\n      \"query\": \"fundamentals of data structures\",\n      \"link\": \"https://scholar.google.com/scholar?hl=en&as_sdt=0,5&qsp=7&q=fundamentals+of+data+structures&qst=br\"\n    },\n    {\n      \"query\": \"data structures and problem\",\n      \"link\": \"https://scholar.google.com/scholar?hl=en&as_sdt=0,5&qsp=8&q=data+structures+and+problem&qst=br\"\n    },\n    {\n      \"query\": \"basic data structures\",\n      \"link\": \"https://scholar.google.com/scholar?hl=en&as_sdt=0,5&qsp=9&q=basic+data+structures&qst=br\"\n    },\n    {\n      \"query\": \"data structures practical introduction\",\n      \"link\": \"https://scholar.google.com/scholar?hl=en&as_sdt=0,5&qsp=10&q=data+structures+practical+introduction&qst=br\"\n    }\n  ],\n  \"pagination\": {\n    \"current\": 1,\n    \"next\": \"https://scholar.google.com/scholar?start=10&q=Data+Structures+and+Algorithms+in+Java&hl=en&as_sdt=0,5\",\n    \"other_pages\": {\n      \"2\": \"https://scholar.google.com/scholar?start=10&q=Data+Structures+and+Algorithms+in+Java&hl=en&as_sdt=0,5\",\n      \"3\": \"https://scholar.google.com/scholar?start=20&q=Data+Structures+and+Algorithms+in+Java&hl=en&as_sdt=0,5\",\n      \"4\": \"https://scholar.google.com/scholar?start=30&q=Data+Structures+and+Algorithms+in+Java&hl=en&as_sdt=0,5\",\n      \"5\": \"https://scholar.google.com/scholar?start=40&q=Data+Structures+and+Algorithms+in+Java&hl=en&as_sdt=0,5\",\n      \"6\": \"https://scholar.google.com/scholar?start=50&q=Data+Structures+and+Algorithms+in+Java&hl=en&as_sdt=0,5\",\n      \"7\": \"https://scholar.google.com/scholar?start=60&q=Data+Structures+and+Algorithms+in+Java&hl=en&as_sdt=0,5\",\n      \"8\": \"https://scholar.google.com/scholar?start=70&q=Data+Structures+and+Algorithms+in+Java&hl=en&as_sdt=0,5\",\n      \"9\": \"https://scholar.google.com/scholar?start=80&q=Data+Structures+and+Algorithms+in+Java&hl=en&as_sdt=0,5\",\n      \"10\": \"https://scholar.google.com/scholar?start=90&q=Data+Structures+and+Algorithms+in+Java&hl=en&as_sdt=0,5\"\n    }\n  },\n  \"serpapi_pagination\": {\n    \"current\": 1,\n    \"next_link\": \"https://serpapi.com/search.json?as_sdt=0%2C5&engine=google_scholar&hl=en&q=Data+Structures+and+Algorithms+in+Java&start=10\",\n    \"next\": \"https://serpapi.com/search.json?as_sdt=0%2C5&engine=google_scholar&hl=en&q=Data+Structures+and+Algorithms+in+Java&start=10\",\n    \"other_pages\": {\n      \"2\": \"https://serpapi.com/search.json?as_sdt=0%2C5&engine=google_scholar&hl=en&q=Data+Structures+and+Algorithms+in+Java&start=10\",\n      \"3\": \"https://serpapi.com/search.json?as_sdt=0%2C5&engine=google_scholar&hl=en&q=Data+Structures+and+Algorithms+in+Java&start=20\",\n      \"4\": \"https://serpapi.com/search.json?as_sdt=0%2C5&engine=google_scholar&hl=en&q=Data+Structures+and+Algorithms+in+Java&start=30\",\n      \"5\": \"https://serpapi.com/search.json?as_sdt=0%2C5&engine=google_scholar&hl=en&q=Data+Structures+and+Algorithms+in+Java&start=40\",\n      \"6\": \"https://serpapi.com/search.json?as_sdt=0%2C5&engine=google_scholar&hl=en&q=Data+Structures+and+Algorithms+in+Java&start=50\",\n      \"7\": \"https://serpapi.com/search.json?as_sdt=0%2C5&engine=google_scholar&hl=en&q=Data+Structures+and+Algorithms+in+Java&start=60\",\n      \"8\": \"https://serpapi.com/search.json?as_sdt=0%2C5&engine=google_scholar&hl=en&q=Data+Structures+and+Algorithms+in+Java&start=70\",\n      \"9\": \"https://serpapi.com/search.json?as_sdt=0%2C5&engine=google_scholar&hl=en&q=Data+Structures+and+Algorithms+in+Java&start=80\",\n      \"10\": \"https://serpapi.com/search.json?as_sdt=0%2C5&engine=google_scholar&hl=en&q=Data+Structures+and+Algorithms+in+Java&start=90\"\n    }\n  }\n}";
//        String response = "{\n  \"search_metadata\": {\n    \"id\": \"64684ff0af6304f900470b60\",\n    \"status\": \"Success\",\n    \"json_endpoint\": \"https://serpapi.com/searches/d89cd98c81408727/64684ff0af6304f900470b60.json\",\n    \"created_at\": \"2023-05-20 04:43:28 UTC\",\n    \"processed_at\": \"2023-05-20 04:43:28 UTC\",\n    \"google_scholar_url\": \"https://scholar.google.com/scholar?q=Science+of+science+Article14+authorsSome+of+the+authors+of+this+publication+are+also+working+on+these+related+projects%3A&hl=en\",\n    \"raw_html_file\": \"https://serpapi.com/searches/d89cd98c81408727/64684ff0af6304f900470b60.html\",\n    \"total_time_taken\": 1.93\n  },\n  \"search_parameters\": {\n    \"engine\": \"google_scholar\",\n    \"q\": \"Science of science Article14 authorsSome of the authors of this publication are also working on these related projects:\",\n    \"hl\": \"en\"\n  },\n  \"search_information\": {\n    \"organic_results_state\": \"Showing results for exact spelling despite spelling suggestion\",\n    \"total_results\": 104,\n    \"time_taken_displayed\": 1.34,\n    \"query_displayed\": \"Science of science Article14 authorsSome of the authors of this publication are also working on these related projects:\",\n    \"spelling_fix\": \"Article 14\"\n  },\n  \"profiles\": {\n    \"link\": \"https://scholar.google.com/scholar?hl=en&as_sdt=0,21&q=Science+of+science+Article+14+authors+Some+of+the+authors+of+this+publication+are+also+working+on+these+related+projects:\",\n    \"serpapi_link\": \"https://serpapi.com/search.json?engine=google_scholar_profiles&hl=en&mauthors=Science+of+science+Article14+authorsSome+of+the+authors+of+this+publication+are+also+working+on+these+related+projects%3A\"\n  },\n  \"organic_results\": [\n    {\n      \"position\": 0,\n      \"title\": \"Academic essay writing in Turkish higher education system: Critical thinking or ready-made structure\",\n      \"result_id\": \"4UIVBXTuC0UJ\",\n      \"type\": \"Pdf\",\n      \"link\": \"https://www.academia.edu/download/56691060/ONUR_3.pdf\",\n      \"snippet\": \"… to imitate the writing of the authors before them – with all its … texts rather than scientific journals and other publications in their … students are not willing to contribute in scientific projects. …\",\n      \"publication_info\": {\n        \"summary\": \"O Şaraplı - International Journal on New Trends in Education and …, 2013 - academia.edu\"\n      },\n      \"resources\": [\n        {\n          \"title\": \"academia.edu\",\n          \"file_format\": \"PDF\",\n          \"link\": \"https://www.academia.edu/download/56691060/ONUR_3.pdf\"\n        }\n      ],\n      \"inline_links\": {\n        \"serpapi_cite_link\": \"https://serpapi.com/search.json?engine=google_scholar_cite&q=4UIVBXTuC0UJ\",\n        \"cited_by\": {\n          \"total\": 5,\n          \"link\": \"https://scholar.google.com/scholar?cites=4975332395429741281&as_sdt=20000005&sciodt=0,21&hl=en\",\n          \"cites_id\": \"4975332395429741281\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=20000005&cites=4975332395429741281&engine=google_scholar&hl=en\"\n        },\n        \"related_pages_link\": \"https://scholar.googleusercontent.com/scholar?q=cache:4UIVBXTuC0UJ:scholar.google.com/+Science+of+science+Article14+authorsSome+of+the+authors+of+this+publication+are+also+working+on+these+related+projects:&hl=en&as_sdt=0,21\",\n        \"serpapi_related_pages_link\": \"https://serpapi.com/search.json?as_sdt=0%2C21&engine=google_scholar&hl=en&q=cache%3A4UIVBXTuC0UJ%3Ascholar.google.com%2F+Science+of+science+Article14+authorsSome+of+the+authors+of+this+publication+are+also+working+on+these+related+projects%3A\",\n        \"versions\": {\n          \"total\": 7,\n          \"link\": \"https://scholar.google.com/scholar?cluster=4975332395429741281&hl=en&as_sdt=0,21\",\n          \"cluster_id\": \"4975332395429741281\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=0%2C21&cluster=4975332395429741281&engine=google_scholar&hl=en\"\n        }\n      }\n    },\n    {\n      \"position\": 1,\n      \"title\": \"A third of the tropical African flora is potentially threatened with extinction\",\n      \"result_id\": \"_ZdE3pf34rwJ\",\n      \"link\": \"https://www.science.org/doi/abs/10.1126/sciadv.aax9444\",\n      \"snippet\": \"… be conducted before implementing these projects. To reduce … We compared our results with published full IUCN Red List … data related to this paper may be requested from the authors. …\",\n      \"publication_info\": {\n        \"summary\": \"T Stévart, G Dauby, PP Lowry, A Blach-Overgaard… - Science …, 2019 - science.org\",\n        \"authors\": [\n          {\n            \"name\": \"T Stévart\",\n            \"link\": \"https://scholar.google.com/citations?user=9IDDEHgAAAAJ&hl=en&oi=sra\",\n            \"serpapi_scholar_link\": \"https://serpapi.com/search.json?author_id=9IDDEHgAAAAJ&engine=google_scholar_author&hl=en\",\n            \"author_id\": \"9IDDEHgAAAAJ\"\n          },\n          {\n            \"name\": \"A Blach-Overgaard\",\n            \"link\": \"https://scholar.google.com/citations?user=RchOdVUAAAAJ&hl=en&oi=sra\",\n            \"serpapi_scholar_link\": \"https://serpapi.com/search.json?author_id=RchOdVUAAAAJ&engine=google_scholar_author&hl=en\",\n            \"author_id\": \"RchOdVUAAAAJ\"\n          }\n        ]\n      },\n      \"resources\": [\n        {\n          \"title\": \"science.org\",\n          \"file_format\": \"HTML\",\n          \"link\": \"https://www.science.org/doi/full/10.1126/sciadv.aax9444\"\n        },\n        {\n          \"title\": \"Full View\",\n          \"link\": \"https://scholar.google.com/scholar?output=instlink&q=info:_ZdE3pf34rwJ:scholar.google.com/&hl=en&as_sdt=0,21&scillfp=12666675757300944136&oi=lle\"\n        }\n      ],\n      \"inline_links\": {\n        \"serpapi_cite_link\": \"https://serpapi.com/search.json?engine=google_scholar_cite&q=_ZdE3pf34rwJ\",\n        \"html_version\": \"https://www.science.org/doi/full/10.1126/sciadv.aax9444\",\n        \"cited_by\": {\n          \"total\": 87,\n          \"link\": \"https://scholar.google.com/scholar?cites=13610713255508219901&as_sdt=20000005&sciodt=0,21&hl=en\",\n          \"cites_id\": \"13610713255508219901\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=20000005&cites=13610713255508219901&engine=google_scholar&hl=en\"\n        },\n        \"related_pages_link\": \"https://scholar.google.com/scholar?q=related:_ZdE3pf34rwJ:scholar.google.com/&scioq=Science+of+science+Article14+authorsSome+of+the+authors+of+this+publication+are+also+working+on+these+related+projects:&hl=en&as_sdt=0,21\",\n        \"serpapi_related_pages_link\": \"https://serpapi.com/search.json?as_sdt=0%2C21&engine=google_scholar&hl=en&q=related%3A_ZdE3pf34rwJ%3Ascholar.google.com%2F\",\n        \"versions\": {\n          \"total\": 18,\n          \"link\": \"https://scholar.google.com/scholar?cluster=13610713255508219901&hl=en&as_sdt=0,21\",\n          \"cluster_id\": \"13610713255508219901\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=0%2C21&cluster=13610713255508219901&engine=google_scholar&hl=en\"\n        }\n      }\n    },\n    {\n      \"position\": 2,\n      \"title\": \"Tools for analyzing qualitative data: The history and relevance of qualitative data analysis software\",\n      \"result_id\": \"mGq3CclyipQJ\",\n      \"link\": \"https://link.springer.com/chapter/10.1007/978-1-4614-3185-5_18\",\n      \"snippet\": \"… projects, before we turn to the software meant to support … these sorts of tasks have been addressed by multiple authors, … new tools would be out of date before this chapter is published. …\",\n      \"publication_info\": {\n        \"summary\": \"LS Gilbert, K Jackson, S Di Gregorio - Handbook of research on …, 2014 - Springer\",\n        \"authors\": [\n          {\n            \"name\": \"LS Gilbert\",\n            \"link\": \"https://scholar.google.com/citations?user=WvblTjUAAAAJ&hl=en&oi=sra\",\n            \"serpapi_scholar_link\": \"https://serpapi.com/search.json?author_id=WvblTjUAAAAJ&engine=google_scholar_author&hl=en\",\n            \"author_id\": \"WvblTjUAAAAJ\"\n          }\n        ]\n      },\n      \"resources\": [\n        {\n          \"title\": \"researchgate.net\",\n          \"file_format\": \"PDF\",\n          \"link\": \"https://www.researchgate.net/profile/Amira-Khattak/post/Best-easiest-software-to-use-for-qualitative-research-Grounded-Theory/attachment/59d62db279197b807798bf4d/AS%3A351214161154048%401460747389741/download/NVivo+and+qualitatitve+data+analysis.pdf\"\n        }\n      ],\n      \"inline_links\": {\n        \"serpapi_cite_link\": \"https://serpapi.com/search.json?engine=google_scholar_cite&q=mGq3CclyipQJ\",\n        \"cited_by\": {\n          \"total\": 131,\n          \"link\": \"https://scholar.google.com/scholar?cites=10703493672176413336&as_sdt=20000005&sciodt=0,21&hl=en\",\n          \"cites_id\": \"10703493672176413336\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=20000005&cites=10703493672176413336&engine=google_scholar&hl=en\"\n        },\n        \"related_pages_link\": \"https://scholar.google.com/scholar?q=related:mGq3CclyipQJ:scholar.google.com/&scioq=Science+of+science+Article14+authorsSome+of+the+authors+of+this+publication+are+also+working+on+these+related+projects:&hl=en&as_sdt=0,21\",\n        \"serpapi_related_pages_link\": \"https://serpapi.com/search.json?as_sdt=0%2C21&engine=google_scholar&hl=en&q=related%3AmGq3CclyipQJ%3Ascholar.google.com%2F\",\n        \"versions\": {\n          \"total\": 4,\n          \"link\": \"https://scholar.google.com/scholar?cluster=10703493672176413336&hl=en&as_sdt=0,21\",\n          \"cluster_id\": \"10703493672176413336\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=0%2C21&cluster=10703493672176413336&engine=google_scholar&hl=en\"\n        }\n      }\n    },\n    {\n      \"position\": 3,\n      \"title\": \"Acknowledgments-based networks for mapping the social structure of research fields. A case study on recent analytic philosophy\",\n      \"result_id\": \"UY7pyKK41o0J\",\n      \"type\": \"Html\",\n      \"link\": \"https://link.springer.com/article/10.1007/s11229-022-03515-2\",\n      \"snippet\": \"… well on those fields where multi-authored publications are … for contributions to the work of others and likewise expect to … to the publication, but also of symbolic alliance, when the authors …\",\n      \"publication_info\": {\n        \"summary\": \"E Petrovich - Synthese, 2022 - Springer\",\n        \"authors\": [\n          {\n            \"name\": \"E Petrovich\",\n            \"link\": \"https://scholar.google.com/citations?user=U6nN8roAAAAJ&hl=en&oi=sra\",\n            \"serpapi_scholar_link\": \"https://serpapi.com/search.json?author_id=U6nN8roAAAAJ&engine=google_scholar_author&hl=en\",\n            \"author_id\": \"U6nN8roAAAAJ\"\n          }\n        ]\n      },\n      \"resources\": [\n        {\n          \"title\": \"springer.com\",\n          \"file_format\": \"HTML\",\n          \"link\": \"https://link.springer.com/article/10.1007/s11229-022-03515-2\"\n        }\n      ],\n      \"inline_links\": {\n        \"serpapi_cite_link\": \"https://serpapi.com/search.json?engine=google_scholar_cite&q=UY7pyKK41o0J\",\n        \"html_version\": \"https://link.springer.com/article/10.1007/s11229-022-03515-2\",\n        \"cited_by\": {\n          \"total\": 3,\n          \"link\": \"https://scholar.google.com/scholar?cites=10220559413658881617&as_sdt=20000005&sciodt=0,21&hl=en\",\n          \"cites_id\": \"10220559413658881617\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=20000005&cites=10220559413658881617&engine=google_scholar&hl=en\"\n        },\n        \"related_pages_link\": \"https://scholar.google.com/scholar?q=related:UY7pyKK41o0J:scholar.google.com/&scioq=Science+of+science+Article14+authorsSome+of+the+authors+of+this+publication+are+also+working+on+these+related+projects:&hl=en&as_sdt=0,21\",\n        \"serpapi_related_pages_link\": \"https://serpapi.com/search.json?as_sdt=0%2C21&engine=google_scholar&hl=en&q=related%3AUY7pyKK41o0J%3Ascholar.google.com%2F\",\n        \"versions\": {\n          \"total\": 5,\n          \"link\": \"https://scholar.google.com/scholar?cluster=10220559413658881617&hl=en&as_sdt=0,21\",\n          \"cluster_id\": \"10220559413658881617\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=0%2C21&cluster=10220559413658881617&engine=google_scholar&hl=en\"\n        }\n      }\n    },\n    {\n      \"position\": 4,\n      \"title\": \"The Quest for a Sound Conception of Copyright's Derivative Work Right\",\n      \"result_id\": \"p9NAivkbJ_8J\",\n      \"link\": \"https://heinonline.org/hol-cgi-bin/get_pdf.cgi?handle=hein.journals/glj101&section=48\",\n      \"snippet\": \"… within ten years of the work's first publication). See Daniel … and cinematographic adaptations (article 14). Berne Convention … A second rationale for granting authors some derivative work …\",\n      \"publication_info\": {\n        \"summary\": \"P Samuelson - Geo. LJ, 2012 - HeinOnline\",\n        \"authors\": [\n          {\n            \"name\": \"P Samuelson\",\n            \"link\": \"https://scholar.google.com/citations?user=m1x1I0EAAAAJ&hl=en&oi=sra\",\n            \"serpapi_scholar_link\": \"https://serpapi.com/search.json?author_id=m1x1I0EAAAAJ&engine=google_scholar_author&hl=en\",\n            \"author_id\": \"m1x1I0EAAAAJ\"\n          }\n        ]\n      },\n      \"resources\": [\n        {\n          \"title\": \"escholarship.org\",\n          \"file_format\": \"PDF\",\n          \"link\": \"https://escholarship.org/content/qt6j86d60d/qt6j86d60d.pdf\"\n        }\n      ],\n      \"inline_links\": {\n        \"serpapi_cite_link\": \"https://serpapi.com/search.json?engine=google_scholar_cite&q=p9NAivkbJ_8J\",\n        \"cited_by\": {\n          \"total\": 93,\n          \"link\": \"https://scholar.google.com/scholar?cites=18385694762343650215&as_sdt=20000005&sciodt=0,21&hl=en\",\n          \"cites_id\": \"18385694762343650215\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=20000005&cites=18385694762343650215&engine=google_scholar&hl=en\"\n        },\n        \"related_pages_link\": \"https://scholar.google.com/scholar?q=related:p9NAivkbJ_8J:scholar.google.com/&scioq=Science+of+science+Article14+authorsSome+of+the+authors+of+this+publication+are+also+working+on+these+related+projects:&hl=en&as_sdt=0,21\",\n        \"serpapi_related_pages_link\": \"https://serpapi.com/search.json?as_sdt=0%2C21&engine=google_scholar&hl=en&q=related%3Ap9NAivkbJ_8J%3Ascholar.google.com%2F\",\n        \"versions\": {\n          \"total\": 10,\n          \"link\": \"https://scholar.google.com/scholar?cluster=18385694762343650215&hl=en&as_sdt=0,21\",\n          \"cluster_id\": \"18385694762343650215\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=0%2C21&cluster=18385694762343650215&engine=google_scholar&hl=en\"\n        }\n      }\n    },\n    {\n      \"position\": 5,\n      \"title\": \"Radiation-related genomic profile of papillary thyroid carcinoma after the Chernobyl accident\",\n      \"result_id\": \"xBfctRKhA-oJ\",\n      \"link\": \"https://www.science.org/doi/abs/10.1126/science.abg2538\",\n      \"snippet\": \"… Similar to our observations for small deletions, these results … This work utilized the computational resources of the NIH … The opinions expressed by the authors are their own, and this …\",\n      \"publication_info\": {\n        \"summary\": \"LM Morton, DM Karyadi, C Stewart, TI Bogdanova… - Science, 2021 - science.org\",\n        \"authors\": [\n          {\n            \"name\": \"C Stewart\",\n            \"link\": \"https://scholar.google.com/citations?user=gGode7YAAAAJ&hl=en&oi=sra\",\n            \"serpapi_scholar_link\": \"https://serpapi.com/search.json?author_id=gGode7YAAAAJ&engine=google_scholar_author&hl=en\",\n            \"author_id\": \"gGode7YAAAAJ\"\n          },\n          {\n            \"name\": \"TI Bogdanova\",\n            \"link\": \"https://scholar.google.com/citations?user=uyiN2g4AAAAJ&hl=en&oi=sra\",\n            \"serpapi_scholar_link\": \"https://serpapi.com/search.json?author_id=uyiN2g4AAAAJ&engine=google_scholar_author&hl=en\",\n            \"author_id\": \"uyiN2g4AAAAJ\"\n          }\n        ]\n      },\n      \"resources\": [\n        {\n          \"title\": \"nih.gov\",\n          \"file_format\": \"HTML\",\n          \"link\": \"https://www.ncbi.nlm.nih.gov/pmc/articles/PMC9022889/\"\n        }\n      ],\n      \"inline_links\": {\n        \"serpapi_cite_link\": \"https://serpapi.com/search.json?engine=google_scholar_cite&q=xBfctRKhA-oJ\",\n        \"html_version\": \"https://www.ncbi.nlm.nih.gov/pmc/articles/PMC9022889/\",\n        \"cited_by\": {\n          \"total\": 74,\n          \"link\": \"https://scholar.google.com/scholar?cites=16862498531537852356&as_sdt=20000005&sciodt=0,21&hl=en\",\n          \"cites_id\": \"16862498531537852356\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=20000005&cites=16862498531537852356&engine=google_scholar&hl=en\"\n        },\n        \"related_pages_link\": \"https://scholar.google.com/scholar?q=related:xBfctRKhA-oJ:scholar.google.com/&scioq=Science+of+science+Article14+authorsSome+of+the+authors+of+this+publication+are+also+working+on+these+related+projects:&hl=en&as_sdt=0,21\",\n        \"serpapi_related_pages_link\": \"https://serpapi.com/search.json?as_sdt=0%2C21&engine=google_scholar&hl=en&q=related%3AxBfctRKhA-oJ%3Ascholar.google.com%2F\",\n        \"versions\": {\n          \"total\": 11,\n          \"link\": \"https://scholar.google.com/scholar?cluster=16862498531537852356&hl=en&as_sdt=0,21\",\n          \"cluster_id\": \"16862498531537852356\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=0%2C21&cluster=16862498531537852356&engine=google_scholar&hl=en\"\n        }\n      }\n    },\n    {\n      \"position\": 6,\n      \"title\": \"Open access bibliography: liberating scholarly literature with e-prints and open access journals\",\n      \"result_id\": \"F8vtfQ-oKusJ\",\n      \"type\": \"Book\",\n      \"link\": \"https://dspace-libros.metabiblioteca.com.co/handle/001/279\",\n      \"snippet\": \"… preprints for publication, granting authors the right to archive their works, and allowing them to … These projects supplement existing efforts by search engines to index e-prints, allowing …\",\n      \"publication_info\": {\n        \"summary\": \"CW Bailey Jr - 2005 - dspace-libros.metabiblioteca.com.co\",\n        \"authors\": [\n          {\n            \"name\": \"CW Bailey Jr\",\n            \"link\": \"https://scholar.google.com/citations?user=A09ous8AAAAJ&hl=en&oi=sra\",\n            \"serpapi_scholar_link\": \"https://serpapi.com/search.json?author_id=A09ous8AAAAJ&engine=google_scholar_author&hl=en\",\n            \"author_id\": \"A09ous8AAAAJ\"\n          }\n        ]\n      },\n      \"resources\": [\n        {\n          \"title\": \"metabiblioteca.com.co\",\n          \"file_format\": \"PDF\",\n          \"link\": \"https://dspace-libros.metabiblioteca.com.co/jspui/bitstream/001/279/8/oab.pdf\"\n        }\n      ],\n      \"inline_links\": {\n        \"serpapi_cite_link\": \"https://serpapi.com/search.json?engine=google_scholar_cite&q=F8vtfQ-oKusJ\",\n        \"cited_by\": {\n          \"total\": 184,\n          \"link\": \"https://scholar.google.com/scholar?cites=16945541332425624343&as_sdt=20000005&sciodt=0,21&hl=en\",\n          \"cites_id\": \"16945541332425624343\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=20000005&cites=16945541332425624343&engine=google_scholar&hl=en\"\n        },\n        \"related_pages_link\": \"https://scholar.googleusercontent.com/scholar?q=cache:F8vtfQ-oKusJ:scholar.google.com/+Science+of+science+Article14+authorsSome+of+the+authors+of+this+publication+are+also+working+on+these+related+projects:&hl=en&as_sdt=0,21\",\n        \"serpapi_related_pages_link\": \"https://serpapi.com/search.json?as_sdt=0%2C21&engine=google_scholar&hl=en&q=cache%3AF8vtfQ-oKusJ%3Ascholar.google.com%2F+Science+of+science+Article14+authorsSome+of+the+authors+of+this+publication+are+also+working+on+these+related+projects%3A\",\n        \"versions\": {\n          \"total\": 10,\n          \"link\": \"https://scholar.google.com/scholar?cluster=16945541332425624343&hl=en&as_sdt=0,21\",\n          \"cluster_id\": \"16945541332425624343\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=0%2C21&cluster=16945541332425624343&engine=google_scholar&hl=en\"\n        }\n      }\n    },\n    {\n      \"position\": 7,\n      \"title\": \"Graphene quantum dots as anti-inflammatory therapy for colitis\",\n      \"result_id\": \"NwSeIxVQX78J\",\n      \"link\": \"https://www.science.org/doi/abs/10.1126/sciadv.aaz2630\",\n      \"snippet\": \"… -induced animals are very similar to those observed in human patients… For these reasons, we used the DSS colitis model to identify … on a second patent related to this work filed by Seoul …\",\n      \"publication_info\": {\n        \"summary\": \"BC Lee, JY Lee, J Kim, JM Yoo, I Kang, JJ Kim… - Science …, 2020 - science.org\",\n        \"authors\": [\n          {\n            \"name\": \"BC Lee\",\n            \"link\": \"https://scholar.google.com/citations?user=xLHlAhMAAAAJ&hl=en&oi=sra\",\n            \"serpapi_scholar_link\": \"https://serpapi.com/search.json?author_id=xLHlAhMAAAAJ&engine=google_scholar_author&hl=en\",\n            \"author_id\": \"xLHlAhMAAAAJ\"\n          },\n          {\n            \"name\": \"JM Yoo\",\n            \"link\": \"https://scholar.google.com/citations?user=1Gw4psgAAAAJ&hl=en&oi=sra\",\n            \"serpapi_scholar_link\": \"https://serpapi.com/search.json?author_id=1Gw4psgAAAAJ&engine=google_scholar_author&hl=en\",\n            \"author_id\": \"1Gw4psgAAAAJ\"\n          },\n          {\n            \"name\": \"I Kang\",\n            \"link\": \"https://scholar.google.com/citations?user=o4pTkoQAAAAJ&hl=en&oi=sra\",\n            \"serpapi_scholar_link\": \"https://serpapi.com/search.json?author_id=o4pTkoQAAAAJ&engine=google_scholar_author&hl=en\",\n            \"author_id\": \"o4pTkoQAAAAJ\"\n          }\n        ]\n      },\n      \"resources\": [\n        {\n          \"title\": \"science.org\",\n          \"file_format\": \"HTML\",\n          \"link\": \"https://www.science.org/doi/full/10.1126/sciadv.aaz2630\"\n        },\n        {\n          \"title\": \"Full View\",\n          \"link\": \"https://scholar.google.com/scholar?output=instlink&q=info:NwSeIxVQX78J:scholar.google.com/&hl=en&as_sdt=0,21&scillfp=5096179332904646366&oi=lle\"\n        }\n      ],\n      \"inline_links\": {\n        \"serpapi_cite_link\": \"https://serpapi.com/search.json?engine=google_scholar_cite&q=NwSeIxVQX78J\",\n        \"html_version\": \"https://www.science.org/doi/full/10.1126/sciadv.aaz2630\",\n        \"cited_by\": {\n          \"total\": 77,\n          \"link\": \"https://scholar.google.com/scholar?cites=13789828635753841719&as_sdt=20000005&sciodt=0,21&hl=en\",\n          \"cites_id\": \"13789828635753841719\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=20000005&cites=13789828635753841719&engine=google_scholar&hl=en\"\n        },\n        \"related_pages_link\": \"https://scholar.google.com/scholar?q=related:NwSeIxVQX78J:scholar.google.com/&scioq=Science+of+science+Article14+authorsSome+of+the+authors+of+this+publication+are+also+working+on+these+related+projects:&hl=en&as_sdt=0,21\",\n        \"serpapi_related_pages_link\": \"https://serpapi.com/search.json?as_sdt=0%2C21&engine=google_scholar&hl=en&q=related%3ANwSeIxVQX78J%3Ascholar.google.com%2F\",\n        \"versions\": {\n          \"total\": 7,\n          \"link\": \"https://scholar.google.com/scholar?cluster=13789828635753841719&hl=en&as_sdt=0,21\",\n          \"cluster_id\": \"13789828635753841719\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=0%2C21&cluster=13789828635753841719&engine=google_scholar&hl=en\"\n        }\n      }\n    },\n    {\n      \"position\": 8,\n      \"title\": \"Sewage sludge pyrolysis for liquid production: a review\",\n      \"result_id\": \"YcrSAmk6Q9kJ\",\n      \"link\": \"https://www.sciencedirect.com/science/article/pii/S1364032112001657\",\n      \"snippet\": \"… This paper reviews the published research on sewage sludge … This paper seeks to review the work carried out into sewage … also indicated for those references whose authors express …\",\n      \"publication_info\": {\n        \"summary\": \"I Fonts, G Gea, M Azuara, J Ábrego, J Arauzo - Renewable and sustainable …, 2012 - Elsevier\",\n        \"authors\": [\n          {\n            \"name\": \"J Ábrego\",\n            \"link\": \"https://scholar.google.com/citations?user=s5-lkEgAAAAJ&hl=en&oi=sra\",\n            \"serpapi_scholar_link\": \"https://serpapi.com/search.json?author_id=s5-lkEgAAAAJ&engine=google_scholar_author&hl=en\",\n            \"author_id\": \"s5-lkEgAAAAJ\"\n          },\n          {\n            \"name\": \"J Arauzo\",\n            \"link\": \"https://scholar.google.com/citations?user=ybDg9XEAAAAJ&hl=en&oi=sra\",\n            \"serpapi_scholar_link\": \"https://serpapi.com/search.json?author_id=ybDg9XEAAAAJ&engine=google_scholar_author&hl=en\",\n            \"author_id\": \"ybDg9XEAAAAJ\"\n          }\n        ]\n      },\n      \"inline_links\": {\n        \"serpapi_cite_link\": \"https://serpapi.com/search.json?engine=google_scholar_cite&q=YcrSAmk6Q9kJ\",\n        \"cited_by\": {\n          \"total\": 556,\n          \"link\": \"https://scholar.google.com/scholar?cites=15655420952363321953&as_sdt=20000005&sciodt=0,21&hl=en\",\n          \"cites_id\": \"15655420952363321953\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=20000005&cites=15655420952363321953&engine=google_scholar&hl=en\"\n        },\n        \"related_pages_link\": \"https://scholar.google.com/scholar?q=related:YcrSAmk6Q9kJ:scholar.google.com/&scioq=Science+of+science+Article14+authorsSome+of+the+authors+of+this+publication+are+also+working+on+these+related+projects:&hl=en&as_sdt=0,21\",\n        \"serpapi_related_pages_link\": \"https://serpapi.com/search.json?as_sdt=0%2C21&engine=google_scholar&hl=en&q=related%3AYcrSAmk6Q9kJ%3Ascholar.google.com%2F\",\n        \"versions\": {\n          \"total\": 8,\n          \"link\": \"https://scholar.google.com/scholar?cluster=15655420952363321953&hl=en&as_sdt=0,21\",\n          \"cluster_id\": \"15655420952363321953\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=0%2C21&cluster=15655420952363321953&engine=google_scholar&hl=en\"\n        }\n      }\n    },\n    {\n      \"position\": 9,\n      \"title\": \"Contrasting high scientific production with low international collaboration and scientific impact: the Brazilian case\",\n      \"result_id\": \"L4Tj9k6JpqAJ\",\n      \"link\": \"https://books.google.com/books?hl=en&lr=&id=n0P8DwAAQBAJ&oi=fnd&pg=PA93&dq=Science+of+science+Article14+authorsSome+of+the+authors+of+this+publication+are+also+working+on+these+related+projects:&ots=dsDEC_mFIg&sig=6sw54k5o18OeFbHkG3BaSy98xq8\",\n      \"snippet\": \"… other studies and new projects, which strengthen scientific communities. In addition, the … of citations when compared to works published individually. The authors also point out that good …\",\n      \"publication_info\": {\n        \"summary\": \"C Haeffner, SR Zanotto, HB Nader… - Scientometrics Recent …, 2019 - books.google.com\",\n        \"authors\": [\n          {\n            \"name\": \"C Haeffner\",\n            \"link\": \"https://scholar.google.com/citations?user=1R13fYEAAAAJ&hl=en&oi=sra\",\n            \"serpapi_scholar_link\": \"https://serpapi.com/search.json?author_id=1R13fYEAAAAJ&engine=google_scholar_author&hl=en\",\n            \"author_id\": \"1R13fYEAAAAJ\"\n          },\n          {\n            \"name\": \"SR Zanotto\",\n            \"link\": \"https://scholar.google.com/citations?user=yDRontcAAAAJ&hl=en&oi=sra\",\n            \"serpapi_scholar_link\": \"https://serpapi.com/search.json?author_id=yDRontcAAAAJ&engine=google_scholar_author&hl=en\",\n            \"author_id\": \"yDRontcAAAAJ\"\n          }\n        ]\n      },\n      \"resources\": [\n        {\n          \"title\": \"intechopen.com\",\n          \"file_format\": \"HTML\",\n          \"link\": \"https://www.intechopen.com/chapters/66700\"\n        }\n      ],\n      \"inline_links\": {\n        \"serpapi_cite_link\": \"https://serpapi.com/search.json?engine=google_scholar_cite&q=L4Tj9k6JpqAJ\",\n        \"html_version\": \"https://www.intechopen.com/chapters/66700\",\n        \"cited_by\": {\n          \"total\": 2,\n          \"link\": \"https://scholar.google.com/scholar?cites=11576090864444998703&as_sdt=20000005&sciodt=0,21&hl=en\",\n          \"cites_id\": \"11576090864444998703\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=20000005&cites=11576090864444998703&engine=google_scholar&hl=en\"\n        },\n        \"related_pages_link\": \"https://scholar.google.com/scholar?q=related:L4Tj9k6JpqAJ:scholar.google.com/&scioq=Science+of+science+Article14+authorsSome+of+the+authors+of+this+publication+are+also+working+on+these+related+projects:&hl=en&as_sdt=0,21\",\n        \"serpapi_related_pages_link\": \"https://serpapi.com/search.json?as_sdt=0%2C21&engine=google_scholar&hl=en&q=related%3AL4Tj9k6JpqAJ%3Ascholar.google.com%2F\",\n        \"versions\": {\n          \"total\": 2,\n          \"link\": \"https://scholar.google.com/scholar?cluster=11576090864444998703&hl=en&as_sdt=0,21\",\n          \"cluster_id\": \"11576090864444998703\",\n          \"serpapi_scholar_link\": \"https://serpapi.com/search.json?as_sdt=0%2C21&cluster=11576090864444998703&engine=google_scholar&hl=en\"\n        }\n      }\n    }\n  ],\n  \"pagination\": {\n    \"current\": 1,\n    \"next\": \"https://scholar.google.com/scholar?start=10&q=Science+of+science+Article14+authorsSome+of+the+authors+of+this+publication+are+also+working+on+these+related+projects:&hl=en&as_sdt=0,21\",\n    \"other_pages\": {\n      \"2\": \"https://scholar.google.com/scholar?start=10&q=Science+of+science+Article14+authorsSome+of+the+authors+of+this+publication+are+also+working+on+these+related+projects:&hl=en&as_sdt=0,21\",\n      \"3\": \"https://scholar.google.com/scholar?start=20&q=Science+of+science+Article14+authorsSome+of+the+authors+of+this+publication+are+also+working+on+these+related+projects:&hl=en&as_sdt=0,21\",\n      \"4\": \"https://scholar.google.com/scholar?start=30&q=Science+of+science+Article14+authorsSome+of+the+authors+of+this+publication+are+also+working+on+these+related+projects:&hl=en&as_sdt=0,21\",\n      \"5\": \"https://scholar.google.com/scholar?start=40&q=Science+of+science+Article14+authorsSome+of+the+authors+of+this+publication+are+also+working+on+these+related+projects:&hl=en&as_sdt=0,21\",\n      \"6\": \"https://scholar.google.com/scholar?start=50&q=Science+of+science+Article14+authorsSome+of+the+authors+of+this+publication+are+also+working+on+these+related+projects:&hl=en&as_sdt=0,21\",\n      \"7\": \"https://scholar.google.com/scholar?start=60&q=Science+of+science+Article14+authorsSome+of+the+authors+of+this+publication+are+also+working+on+these+related+projects:&hl=en&as_sdt=0,21\",\n      \"8\": \"https://scholar.google.com/scholar?start=70&q=Science+of+science+Article14+authorsSome+of+the+authors+of+this+publication+are+also+working+on+these+related+projects:&hl=en&as_sdt=0,21\",\n      \"9\": \"https://scholar.google.com/scholar?start=80&q=Science+of+science+Article14+authorsSome+of+the+authors+of+this+publication+are+also+working+on+these+related+projects:&hl=en&as_sdt=0,21\",\n      \"10\": \"https://scholar.google.com/scholar?start=90&q=Science+of+science+Article14+authorsSome+of+the+authors+of+this+publication+are+also+working+on+these+related+projects:&hl=en&as_sdt=0,21\"\n    }\n  },\n  \"serpapi_pagination\": {\n    \"current\": 1,\n    \"next_link\": \"https://serpapi.com/search.json?as_sdt=0%2C21&engine=google_scholar&hl=en&q=Science+of+science+Article14+authorsSome+of+the+authors+of+this+publication+are+also+working+on+these+related+projects%3A&start=10\",\n    \"next\": \"https://serpapi.com/search.json?as_sdt=0%2C21&engine=google_scholar&hl=en&q=Science+of+science+Article14+authorsSome+of+the+authors+of+this+publication+are+also+working+on+these+related+projects%3A&start=10\",\n    \"other_pages\": {\n      \"2\": \"https://serpapi.com/search.json?as_sdt=0%2C21&engine=google_scholar&hl=en&q=Science+of+science+Article14+authorsSome+of+the+authors+of+this+publication+are+also+working+on+these+related+projects%3A&start=10\",\n      \"3\": \"https://serpapi.com/search.json?as_sdt=0%2C21&engine=google_scholar&hl=en&q=Science+of+science+Article14+authorsSome+of+the+authors+of+this+publication+are+also+working+on+these+related+projects%3A&start=20\",\n      \"4\": \"https://serpapi.com/search.json?as_sdt=0%2C21&engine=google_scholar&hl=en&q=Science+of+science+Article14+authorsSome+of+the+authors+of+this+publication+are+also+working+on+these+related+projects%3A&start=30\",\n      \"5\": \"https://serpapi.com/search.json?as_sdt=0%2C21&engine=google_scholar&hl=en&q=Science+of+science+Article14+authorsSome+of+the+authors+of+this+publication+are+also+working+on+these+related+projects%3A&start=40\",\n      \"6\": \"https://serpapi.com/search.json?as_sdt=0%2C21&engine=google_scholar&hl=en&q=Science+of+science+Article14+authorsSome+of+the+authors+of+this+publication+are+also+working+on+these+related+projects%3A&start=50\",\n      \"7\": \"https://serpapi.com/search.json?as_sdt=0%2C21&engine=google_scholar&hl=en&q=Science+of+science+Article14+authorsSome+of+the+authors+of+this+publication+are+also+working+on+these+related+projects%3A&start=60\",\n      \"8\": \"https://serpapi.com/search.json?as_sdt=0%2C21&engine=google_scholar&hl=en&q=Science+of+science+Article14+authorsSome+of+the+authors+of+this+publication+are+also+working+on+these+related+projects%3A&start=70\",\n      \"9\": \"https://serpapi.com/search.json?as_sdt=0%2C21&engine=google_scholar&hl=en&q=Science+of+science+Article14+authorsSome+of+the+authors+of+this+publication+are+also+working+on+these+related+projects%3A&start=80\",\n      \"10\": \"https://serpapi.com/search.json?as_sdt=0%2C21&engine=google_scholar&hl=en&q=Science+of+science+Article14+authorsSome+of+the+authors+of+this+publication+are+also+working+on+these+related+projects%3A&start=90\"\n    }\n  }\n}";
        JSONObject obj = new JSONObject(response);
        if(obj.has("profiles")){
            JSONObject objQuery = obj.getJSONObject("profiles");
            if(objQuery.has("link")){
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

    private DocumentModel getDataDocumentModel(JSONObject response, String query){
        JSONArray arrResults = response.getJSONArray("organic_results");
        DocumentModel docModel = new DocumentModel();
        if(arrResults.length() > 0){
            for(int i = 0; i < arrResults.length(); i++){
                JSONObject data = arrResults.getJSONObject(i);
                String title = data.getString("title");
                String newQuery = query.toLowerCase().replaceAll("[^a-zA-Z0-9]", "").trim();
                String newTitle = title.toLowerCase().replaceAll("[^a-zA-Z0-9]", "").trim();
                if( newQuery.contains(newTitle) || newTitle.contains(newQuery) || arrResults.length() < 2){
                    docModel.setTitle(title);
                    JSONObject publicationInfo = data.getJSONObject("publication_info");
                    String summary = null;

                    if(publicationInfo.has("summary")){
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
                    if(publicationInfo.has("authors")){
                        JSONArray listAuthors = publicationInfo.getJSONArray("authors");
                        if(listAuthors.length() > 0){
                            StringBuilder strAuthors = new StringBuilder();
                            for(int j = 0; j < listAuthors.length(); j++){
                                strAuthors.append(listAuthors.getJSONObject(j).getString("name"));
                                if (j < listAuthors.length() - 1) {
                                    strAuthors.append(", ");
                                }
                            }
                            docModel.setAuthors(strAuthors.toString());
                        }else{
                            docModel.setAuthors(null);
                        }
                    }else{
                        if(summary != null){
                            String[] parts = summary.split("-");
                            String authors = parts[0].trim();
                            docModel.setAuthors(authors);
                        }else{
                            docModel.setAuthors(null);
                        }
                    }
                    return docModel;
                }

            }
        }

        return docModel;
    }


}
