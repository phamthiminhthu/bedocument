package com.hust.edu.vn.services.document;


import com.hust.edu.vn.dto.DocumentDto;
import com.hust.edu.vn.dto.UserDto;
import com.hust.edu.vn.entity.Document;
import com.hust.edu.vn.model.DocumentEditModel;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {
  DocumentDto uploadDocument(MultipartFile file);
  byte[] loadFileFromS3(String filename);
  boolean moveToTrash(List<String> listDocumentKey);
  boolean  deleteDocument(List<String> listDocumentKey);
  boolean undoDocument(List<String> listDocumentKey);
//  boolean updateInformationDocument(String documentKey, DocumentModel documentModel);
  Document updateContentDocument(String documentKey);
  List<DocumentDto> getListDocument();
  DocumentDto getDocumentModel(String documentKey);
  List<DocumentDto> getTrashListDocument();
  List<DocumentDto> getListDocumentLoved();
  boolean updateLovedDocument(String documentKey);
//  boolean updatePublicDocument(String documentKey);
  boolean editDocumentByKey(String documentKey, DocumentEditModel documentEditModel);
  List<DocumentDto> getListDocumentPublic();

  List<DocumentDto> getListDocumentShared();

  List<DocumentDto> getListDocumentCompleted();

  List<DocumentDto> getListDocumentPublicFollowing();

  List<UserDto> getListSuggestUsers();

  List<DocumentDto> getListDocumentPublicSuggest();

  List<DocumentDto> getListDocumentPublicByUsername(String username);

  boolean updateLovedListDocuments(List<String> listDocumentModel);

  boolean updatePublicListDocuments(List<String> listDocumentKeys);

  boolean updateCompletedListDocument(List<String> listDocumentKeys);

}
