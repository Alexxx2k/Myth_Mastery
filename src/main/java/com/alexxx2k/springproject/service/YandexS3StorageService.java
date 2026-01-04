package com.alexxx2k.springproject.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

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

            String fileKey = productImagesFolder + "/" + imageFile.getOriginalFilename();

            if (fileExistsInS3(fileKey)) {
                throw new IOException("Файл с именем '" + imageFile.getOriginalFilename() +
                        "' уже существует в хранилище. Пожалуйста, выберите другое имя или переименуйте файл.");
            }

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

            return publicUrl;

        } catch (S3Exception e) {
            throw new IOException("Ошибка Yandex Cloud S3: " +
                    e.awsErrorDetails().errorMessage(), e);
        } catch (Exception e) {
            throw new IOException("Ошибка загрузки: " + e.getMessage(), e);
        }
    }

    public List<Map<String, String>> getExistingImages() {
        try {
            String prefix = productImagesFolder + "/";

            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .build();

            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);

            List<Map<String, String>> images = new ArrayList<>();

            for (S3Object s3Object : listResponse.contents()) {
                if (!s3Object.key().endsWith("/") && isImageFile(s3Object.key())) {
                    Map<String, String> imageInfo = new HashMap<>();
                    imageInfo.put("key", s3Object.key());
                    imageInfo.put("url", getPublicUrl(s3Object.key()));
                    imageInfo.put("name", extractFileName(s3Object.key()));
                    imageInfo.put("size", formatFileSize(s3Object.size()));
                    imageInfo.put("lastModified", s3Object.lastModified().toString());

                    images.add(imageInfo);
                }
            }

            return images;

        } catch (Exception e) {
            System.err.println("Ошибка получения списка изображений: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private boolean isImageFile(String fileName) {
        String[] imageExtensions = {".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp", ".svg"};
        String lowerFileName = fileName.toLowerCase();

        for (String ext : imageExtensions) {
            if (lowerFileName.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private String extractFileName(String key) {
        if (key.contains("/")) {
            return key.substring(key.lastIndexOf("/") + 1);
        }
        return key;
    }

    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        }
    }

    public Map<String, String> getImageInfo(String imageUrl) {
        try {
            String fileKey = extractFileKeyFromUrl(imageUrl);
            if (fileKey == null || fileKey.isEmpty()) {
                return null;
            }

            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            HeadObjectResponse headResponse = s3Client.headObject(headRequest);

            Map<String, String> imageInfo = new HashMap<>();
            imageInfo.put("key", fileKey);
            imageInfo.put("url", imageUrl);
            imageInfo.put("name", extractFileName(fileKey));
            imageInfo.put("contentType", headResponse.contentType());
            imageInfo.put("size", formatFileSize(headResponse.contentLength()));
            imageInfo.put("lastModified", headResponse.lastModified().toString());

            return imageInfo;

        } catch (Exception e) {
            System.err.println("Ошибка получения информации об изображении: " + e.getMessage());
            return null;
        }
    }

    public List<Map<String, String>> searchImages(String searchTerm) {
        List<Map<String, String>> allImages = getExistingImages();

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return allImages;
        }

        String searchLower = searchTerm.toLowerCase().trim();

        return allImages.stream()
                .filter(image -> image.get("name").toLowerCase().contains(searchLower))
                .collect(Collectors.toList());
    }

    private void validateImageFile(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("Файл пустой");
        }

        if (imageFile.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Размер файла превышает 5MB");
        }

        String contentType = imageFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Поддерживаются только изображения (JPEG, PNG, GIF)");
        }
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

    private void ensureBucketExists() throws IOException {
        try {
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

            s3Client.headBucket(headBucketRequest);

        } catch (S3Exception e) {
            throw new IOException("Ошибка проверки bucket: " + e.getMessage(), e);
        }
    }

    private boolean fileExistsInS3(String fileKey) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
            throw e;
        }
    }
}
