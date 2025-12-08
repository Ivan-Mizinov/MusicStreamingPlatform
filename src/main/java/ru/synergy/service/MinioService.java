package ru.synergy.service;

import io.minio.*;
import io.minio.errors.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
public class MinioService {
    private final MinioClient minioClient;

    @PostConstruct
    public void initBucket() {
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket("music-bucket").build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket("music-bucket").build());
            }
            setBucketPublic("music-bucket");
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при инициализации bucket'а ", e);
        }
    }

    public void uploadFile(String bucketName, String objectName, MultipartFile file)
            throws ServerException, InsufficientDataException, ErrorResponseException,
            IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException,
            XmlParserException, InternalException {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );
    }

    public String getFileUrl(String bucketName, String objectName) {
        return String.format("http://localhost:9000/%s/%s", bucketName, objectName);
    }

    public void setBucketPublic(String bucketName) {
        try {
            String policyJson = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":\"*\"},\"Action\":[\"s3:GetObject\"],\"Resource\":[\"arn:aws:s3:::" + bucketName + "/*\"]}]}";

            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketName)
                            .config(policyJson)
                            .build()
            );
        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException e) {
            System.err.println("Ошибка при изменении доступа: " + ExceptionUtils.getStackTrace(e));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
