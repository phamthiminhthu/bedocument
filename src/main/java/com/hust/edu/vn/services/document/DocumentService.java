package com.hust.edu.vn.services.document;


import com.hust.edu.vn.dto.DocumentDto;
import com.hust.edu.vn.entity.Document;
import com.hust.edu.vn.model.DocumentModel;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {
  Document uploadDocument(MultipartFile file);
  byte[] loadFileFromS3(String filename);

  boolean moveToTrash(List<String> listDocumentKey);

  boolean  deleteDocument(List<String> listDocumentKey);

  boolean undoDocument(List<String> listDocumentKey);

  boolean updateInformationDocument(String documentKey, DocumentModel documentModel);

  Document updateContentDocument(String documentKey);

  List<DocumentDto> getListDocument();

  DocumentDto getDocumentModel(String documentKey);

  boolean updateInformationListDocument(List<DocumentModel> listDataDocumentRequest);

  List<DocumentDto> getTrashListDocument();

  boolean moveDocument(List<String> listDocumentKey, List<Long> listCollectionId, String type);

  List<DocumentDto> getListDocumentLoved();

  boolean updateLovedDocument(String documentKey);

  boolean updatePublicDocument(String documentKey);
}
