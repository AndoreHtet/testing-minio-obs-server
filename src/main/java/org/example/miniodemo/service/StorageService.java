package org.example.miniodemo.service;

import io.minio.*;
import io.minio.messages.Item;
import org.example.miniodemo.config.MinioConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class StorageService {

    @Autowired
    private MinioConfig minioConfig;

    @Value("${minio.bucket.name}")
    private String bucketName;

    @Autowired
    private MinioClient minioClient;


    public List<String> listObjectNames(String bucketName) {
        List<String> objectNames = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );
            for (Result<Item> result : results) {
                Item item = result.get();
                objectNames.add(item.objectName());
            }
        }catch (Exception e) {
            System.out.println("Error Message : " + e.getMessage());
        }
        return objectNames;
    }

    public void uploadFile(String bucketName, String objectName, MultipartFile file, String contentType, InputStream inputStream) {
        try {
            // Check if the bucket exists, if not, create it
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            // Upload the file to MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(
                                    inputStream, inputStream.available(), -1
                            )
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error uploading file: " + e.getMessage());
        }
    }

    public InputStream getObject(String bucketName, String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            // Handle error
            return null;
        }
    }



    public void editImage(String bucketName, String objectName, MultipartFile file, String contentType, InputStream inputStream, String fileName) {
        try {
            // Remove the existing object
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)  // Use the correct object name here
                            .build()
            );
            // Upload the new image using putFile method
            uploadFile(bucketName, objectName, file, contentType, inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Error editing image: " + e.getMessage());
        }
    }

    public void deleteObject(String bucketName,String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        }catch (Exception e) {
            throw new RuntimeException("Error deleting file" + e.getMessage());
        }
    }

    public void putFile(String bucketName, MultipartFile multipartFile, String objectName, InputStream inputStream, String contentType){
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket("sale-bucket").build());
            if(!found){
                 minioClient.makeBucket(MakeBucketArgs.builder().bucket("sale-bucket").build());
            }
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket("sale-bucket")
                            .object(objectName)
                            .stream(
                                    inputStream,inputStream.available(),-1
                            )
                            .contentType(contentType)
                            .build()
            );
        }catch (Exception e){
            throw new RuntimeException("Error occured while uploading file" + e.getMessage());
        }
    }
}
