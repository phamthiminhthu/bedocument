package com.hust.edu.vn.repository;

import com.hust.edu.vn.entity.Document;
import com.hust.edu.vn.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    boolean existsByIdAndDocument(Long id, Document document);

    Tag findByIdAndDocument(Long id, Document document);

    boolean existsByTagNameAndDocument(String tagName, Document document);

    boolean existsByDocument(Document document);

    List<Tag> findAllByDocument(Document document);

    Tag findByTagNameAndDocument(String tagName, Document document);

    List<Tag> findByTagNameContainingIgnoreCase(String tagName);

    @Query(""" 
            SELECT DISTINCT LOWER(t.tagName) FROM Tag t
            JOIN Document d
            ON t.document = d
            WHERE d.user.id IN :usersId
        """)
    List<String> findAllByUsersId(List<Long> usersId);
}