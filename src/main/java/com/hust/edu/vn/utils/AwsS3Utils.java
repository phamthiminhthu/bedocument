package com.hust.edu.vn.utils;


import com.amazonaws.services.kafka.model.S3;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;

@Component
@Slf4j
public class AwsS3Utils {
    private final AmazonS3 s3client;

    @Value("${amazonProperties.endpointUrl}")
    private String endpointUrl;
    @Value("${amazonProperties.bucketName}")
    private String bucketName;

    public AwsS3Utils(AmazonS3 s3client) {
        this.s3client = s3client;
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }
    private String generateFileName(MultipartFile multiPart) {
        return new Date().getTime() + "-" + multiPart.getOriginalFilename().replace(" ", "_");
    }
    private void uploadAvatarTos3bucket(String fileName, File file, String rootPath) {
        s3client.putObject(new PutObjectRequest(bucketName, rootPath + fileName, file)
                .withCannedAcl(CannedAccessControlList.PublicRead));
    }
    private void uploadDocumentTos3bucket(String fileName, File file, String rootPath){
        s3client.putObject(new PutObjectRequest(bucketName, rootPath + fileName, file)
                .withCannedAcl(CannedAccessControlList.BucketOwnerFullControl));
    }
    public String uploadAvatar(MultipartFile multipartFile, String rootPath) {
        String fileUrl = "";
        try {
            File file = convertMultiPartToFile(multipartFile);
            String fileName = generateFileName(multipartFile);
            fileUrl = endpointUrl + "/" + rootPath + fileName;
            uploadAvatarTos3bucket(fileName, file, rootPath);
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileUrl;
    }

    public String uploadFileDocument(MultipartFile multipartFile, String rootPath) {
        String fileUrl = "";
        try {
            File file = convertMultiPartToFile(multipartFile);
            String fileName = generateFileName(multipartFile);
            fileUrl = endpointUrl + "/" + rootPath + fileName;
            uploadDocumentTos3bucket(fileName, file, rootPath);
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileUrl;
    }

    public byte[] readFile(String rootPath, String fileName){
        String fileUrl = rootPath + "document/" + fileName;
        S3Object object = s3client.getObject(new GetObjectRequest(bucketName, fileUrl));
        InputStream objectData = object.getObjectContent();
        try {
            byte[] data = objectData.readAllBytes();
            return data;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteFileFromS3Bucket(String rootPath, String keyName, String type){
        String fileUrl = rootPath + type + "/" + keyName;
        s3client.deleteObject(new DeleteObjectRequest(bucketName, fileUrl));
    }


}
