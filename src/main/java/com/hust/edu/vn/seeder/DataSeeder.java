package com.hust.edu.vn.seeder;

import com.hust.edu.vn.entity.*;
import com.hust.edu.vn.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private GroupDocRepository groupDocRepository;

    @Autowired
    private DocumentShareUserRepository documentShareUserRepository;

    @Autowired
    private GroupHasDocumentRepository groupHasDocumentRepository;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findAll().size() != 0) return;

        // insert data user
        User user = new User(1L,"minhthutb111@gmail.com","$2a$11$DJDtBTPL9kA6GzFY71Yb1O5b6STafSUhgUFfugEbgbGtVMEqMm8jG","phamthiminhthu",null,null,"0362989028",null,"4fb6a3a4-5aba-4e34-9b28-325a0b81cf5c/",null,null,null,new Date(),new Date());
        userRepository.save(user);
        User user1 = new User(2L, "hoanghailong2642k@gmail.com", "$2a$11$AzBeWQ.8vwOJaOLNemhMdeApeWTeiDTBqgk0Z/HHPGmQ7n/k.S0Fm", "hoanghailongvn",null,null,null,null,"d7dbc2f4-df07-4785-a69c-346f9c9d4f8e/",null,null,null,new Date(),new Date());
        userRepository.save(user1);
        User user2 = new User(3L, "seadragnol@gmail.com", "$2a$11$SIO8ncuHE7WyBfLzrFikvepP.LG6NEA6646vgVIjn.gh62CbZ3S4q", "seadragnol" ,null,null,null,null,"86c40359-564d-4ff3-a7ba-8b159ec17101/",null,null,null,new Date(),new Date());
        userRepository.save(user2);

        // insert data document
        Document document = new Document(1L, "1685820826326-ChemListy1162020719EN.pdf" ,"ChemListy1162020719EN.pdf", user, "Eponyms in Laboratory Equipment", "K Nesměráka, R Chalupab", (byte) 0, (byte) 0, (byte) 0,2022,"K Nesměráka, R Chalupab - Chem. Listy, 2022 - ww-w.chemicke-listy.cz", null,0L, (byte) 0, new Date(),new Date());
        documentRepository.save(document);
        Document document1 = new Document(2L, "1685820973622-Anintroductiontodockerandanalysisofitsperformance.pdf" ,"Anintroductiontodockerandanalysisofitsperformance.pdf",user,"An introduction to docker and analysis of its performance","BB Rad, HJ Bhatti", (byte) 0, (byte) 0, (byte) 0, 2017,"BB Rad, HJ Bhatti, M Ahmadi - International Journal of Computer …, 2017 - researchgate.net",null,0L, (byte) 0, new Date(), new Date());
        documentRepository.save(document1);
        Document document2 = new Document(3L, "1685821008192-Data_Structures_and_Algorithms_in_Java_Fourth_Edition.pdf", "Data Structures and Algorithms in Java Fourth Edition.pdf", user, "Data structures and algorithms in Java" ,"MT Goodrich, R Tamassia, MH Goldwasser", (byte) 0, (byte) 0, (byte) 0, 2014,"MT Goodrich, R Tamassia, MH Goldwasser - 2014 - books.google.com",null,0L, (byte) 0, new Date(), new Date());
        documentRepository.save(document2);
        Document document3 = new Document(4L,"1685821096684-Scienceofscience.pdf", "Scienceofscience.pdf",user1,"Science of science","S Fortunato, CT Bergstrom, K Börner, JA Evans", (byte) 0, (byte) 0, (byte) 0,2018,"S Fortunato, CT Bergstrom, K Börner, JA Evans… - Science, 2018 - science.org",null,0L, (byte) 0,new Date(), new Date());
        documentRepository.save(document3);
        Document document4 = new Document(5L, "1685821132055-Fundamentals_of_Condensed_Matter_and_Crystalline_Physics__An_Introduction_for_Students_of_Physics_and_Materials_Science_(_PDFDrive_).pdf", "Fundamentals of Condensed Matter and Crystalline Physics_ An Introduction for Students of Physics and Materials Science ( PDFDrive ).pdf", user1, "Fundamentals of Condensed Matter and Crystalline Physics: An Introduction for Students of Physics and Materials Science", "DL Sidebottom", (byte) 0, (byte) 0, (byte) 0,2012,"DL Sidebottom - 2012 - books.google.com",null,0L, (byte) 0, new Date(), new Date());
        documentRepository.save(document4);
        Document document5 = new Document(6L, "1685821247585-Wes-McKinney-Python-for-Data-Analysis_-Data-Wrangling-with-Pandas-NumPy-and-IPython.pdf", "Wes-McKinney-Python-for-Data-Analysis_-Data-Wrangling-with-Pandas-NumPy-and-IPython.pdf", user2, "Python for data analysis: Data wrangling with Pandas, NumPy, and IPython", "W McKinney", (byte) 0, (byte) 0, (byte) 0,2012, "W McKinney - 2012 - books.google.com", null,0L, (byte) 0, new Date(), new Date());
        documentRepository.save(document5);
        Document document6 = new Document(7L, "1685821997656-Data_Structures_and_Algorithms_in_Java_Fourth_Edition.pdf", "Data Structures and Algorithms in Java Fourth Edition.pdf", user, "Data structures and algorithms in Java","MT Goodrich, R Tamassia, MH Goldwasser", (byte) 0, (byte) 0, (byte) 0,2014,"MT Goodrich, R Tamassia, MH Goldwasser - 2014 - books.google.com", null,0L, (byte) 0, new Date(), new Date());
        documentRepository.save(document6);
        Document document7 = new Document(8L,"1685876072330-A_Survey_of_MachineLearning_DataMining_in_Multimedia_System.pdf", "A_Survey_of_MachineLearning_DataMining_in_Multimedia_System.pdf", user, "A survey of machine learning and data mining techniques used in multimedia system", "H Tran", (byte) 0, (byte) 0, (byte) 0,2019,"H Tran - no, 2019 - researchgate.net",null,0L, (byte) 1,new Date(), new Date());
        documentRepository.save(document7);

        // insert document share user
        DocumentShareUser documentShareUser = new DocumentShareUser(1L, user1, document1, new Date(), new Date());
        documentShareUserRepository.save(documentShareUser);
        DocumentShareUser documentShareUser1 = new DocumentShareUser(2L, user2, document1, new Date(), new Date());
        documentShareUserRepository.save(documentShareUser1);

        // insert groupDocuments
        GroupDoc groupDoc = new GroupDoc(1L, user, "Chemistry", new Date(), new Date());
        groupDocRepository.save(groupDoc);

        // insert data collections
        Collection collection = new Collection(1L,null, user,null,"Scientific Research", new Date(), new Date());
        collectionRepository.save(collection);
        Collection collection1 = new Collection(2L,null, user,null, "Chemistry", new Date(), new Date());
        collectionRepository.save(collection1);
        Collection collection2 = new Collection(3L,1L, user,null,"Books",new Date(), new Date());
        collectionRepository.save(collection2);
        Collection collection3 = new Collection(4L,2L, user,null, "Books", new Date(), new Date());
        collectionRepository.save(collection3);
        Collection collection4 = new Collection(5L,null, user, groupDoc,"Books", new Date(), new Date());
        collectionRepository.save(collection4);
        Collection collection5 = new Collection(6L,5L, user, groupDoc, "References", new Date(), new Date());
        collectionRepository.save(collection5);


        // group collection has documents
        GroupHasDocument groupHasDocument = new GroupHasDocument(2L, groupDoc, document1, new Date(), new Date());
        groupHasDocumentRepository.save(groupHasDocument);

    }
}
