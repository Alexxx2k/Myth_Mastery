package com.alexxx2k.springproject.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class YandexS3StorageService {

    private final S3Client s3Client;

    @Value("${yandex.s3.bucket-name}")
    private String bucketName;

    @Value("${app.storage.product-images-folder}")
    private String productImagesFolder;

    @Value("${app.storage.public-url-prefix}")
    private String publicUrlPrefix;

    public YandexS3StorageService(S3Client yandexS3Client) {
        this.s3Client = yandexS3Client;
    }

    public String uploadProductImage(MultipartFile imageFile) throws IOException {
        try {
            validateImageFile(imageFile);

            String fileName = generateFileName(imageFile.getOriginalFilename());
            String fileKey = productImagesFolder + "/" + fileName;

            System.out.println("=== Загрузка в Yandex Cloud S3 ===");
            System.out.println("Bucket: " + bucketName);
            System.out.println("Key: " + fileKey);
            System.out.println("Content-Type: " + imageFile.getContentType());
            System.out.println("Size: " + imageFile.getSize() + " bytes");

            ensureBucketExists();

            Map<String, String> metadata = new HashMap<>();
            metadata.put("Content-Type", imageFile.getContentType());
            metadata.put("Cache-Control", "max-age=31536000");

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .contentType(imageFile.getContentType())
                    .metadata(metadata)
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            try (InputStream inputStream = imageFile.getInputStream()) {
                s3Client.putObject(putObjectRequest,
                        RequestBody.fromInputStream(inputStream, imageFile.getSize()));
            }

            String publicUrl = getPublicUrl(fileKey);
            System.out.println("Успешно загружено: " + publicUrl);
            System.out.println("=== Загрузка завершена ===");

            return publicUrl;

        } catch (S3Exception e) {
            System.err.println("=== YANDEX S3 ERROR ===");
            System.err.println("Status: " + e.statusCode());
            System.err.println("Error Code: " + e.awsErrorDetails().errorCode());
            System.err.println("Error Message: " + e.awsErrorDetails().errorMessage());
            System.err.println("=== END ERROR ===");

            throw new IOException("Ошибка Yandex Cloud S3: " +
                    e.awsErrorDetails().errorCode() + " - " +
                    e.awsErrorDetails().errorMessage(), e);
        } catch (Exception e) {
            throw new IOException("Ошибка загрузки: " + e.getMessage(), e);
        }
    }

    public void deleteProductImage(String imageUrl) {
        try {
            if (imageUrl == null || imageUrl.isEmpty()) {
                return;
            }

            String fileKey = extractFileKeyFromUrl(imageUrl);
            if (fileKey != null && !fileKey.isEmpty()) {
                System.out.println("Удаление из Yandex S3: " + fileKey);

                DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(fileKey)
                        .build();

                s3Client.deleteObject(deleteObjectRequest);
                System.out.println("Успешно удалено");
            }
        } catch (Exception e) {
            System.err.println("Ошибка удаления из Yandex S3: " + e.getMessage());
        }
    }

    private void ensureBucketExists() throws IOException {
        try {
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

            s3Client.headBucket(headBucketRequest);
            System.out.println("Bucket существует: " + bucketName);

        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                System.out.println("Bucket не существует, создаем: " + bucketName);
                createBucket();
            } else {
                throw new IOException("Ошибка проверки bucket: " + e.getMessage(), e);
            }
        }
    }

    private void createBucket() throws IOException {
        try {
            CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

            s3Client.createBucket(createBucketRequest);
            System.out.println("Bucket создан: " + bucketName);

            setBucketPublicAccess();

        } catch (Exception e) {
            throw new IOException("Ошибка создания bucket: " + e.getMessage(), e);
        }
    }

    private void setBucketPublicAccess() {
        try {
            String bucketPolicy = String.format(
                    "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":\"*\",\"Action\":[\"s3:GetObject\"],\"Resource\":[\"arn:aws:s3:::%s/*\"]}]}",
                    bucketName
            );

            PutBucketPolicyRequest putBucketPolicyRequest = PutBucketPolicyRequest.builder()
                    .bucket(bucketName)
                    .policy(bucketPolicy)
                    .build();

            s3Client.putBucketPolicy(putBucketPolicyRequest);
            System.out.println("Публичный доступ настроен для bucket: " + bucketName);

        } catch (Exception e) {
            System.err.println("Ошибка настройки публичного доступа: " + e.getMessage());
        }
    }

    private void validateImageFile(MultipartFile imageFile) throws IOException {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("Файл пустой");
        }

        if (imageFile.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Размер файла превышает 5MB");
        }

        String contentType = imageFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Поддерживаются только изображения (JPEG, PNG, GIF, WebP)");
        }
    }

    private String generateFileName(String originalFileName) {
        String extension = ".jpg";
        if (originalFileName != null && originalFileName.contains(".")) {
            String ext = originalFileName.substring(originalFileName.lastIndexOf(".")).toLowerCase();
            if (ext.matches("(?i)\\.(jpg|jpeg|png|gif|webp|bmp|svg)$")) {
                extension = ext;
            }
        }
        return UUID.randomUUID().toString() + extension;
    }

    private String getPublicUrl(String fileKey) {
        return publicUrlPrefix + "/" + fileKey;
    }

    private String extractFileKeyFromUrl(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains(publicUrlPrefix)) {
            return null;
        }
        return imageUrl.replace(publicUrlPrefix + "/", "");
    }

    public void testConnection() throws IOException {
        try {
            System.out.println("=== Тестирование подключения к Yandex Cloud S3 ===");
            System.out.println("Bucket: " + bucketName);
            System.out.println("Region: " + s3Client.serviceClientConfiguration().region());

            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

            s3Client.headBucket(headBucketRequest);
            System.out.println("✓ Подключение к bucket установлено");

            String testKey = productImagesFolder + "/test-connection.txt";
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(testKey)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromString("Test connection"));
            System.out.println("✓ Тестовый файл загружен: " + testKey);

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(testKey)
                    .build();

            s3Client.deleteObject(deleteRequest);
            System.out.println("✓ Тестовый файл удален");

            System.out.println("=== Тест пройден успешно! ===");

        } catch (S3Exception e) {
            throw new IOException("Yandex Cloud S3 Error: " +
                    e.awsErrorDetails().errorCode() + " - " +
                    e.awsErrorDetails().errorMessage(), e);
        }
    }
}