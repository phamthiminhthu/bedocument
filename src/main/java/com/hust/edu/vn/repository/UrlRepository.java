package com.hust.edu.vn.repository;

import com.hust.edu.vn.entity.Document;
import com.hust.edu.vn.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {
    boolean existsByDocumentAndUrl(Document document, String url);

    boolean existsByDocument(Document document);

    List<Url> findAllByDocument(Document document);
}