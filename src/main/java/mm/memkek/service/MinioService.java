package mm.memkek.service;

import io.minio.*;
import io.minio.errors.MinioException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
public class MinioService {

    private final MinioClient minioClient;
    private final String bucketName;

    public MinioService(
            @Value("${minio.endpoint}") String endpoint,
            @Value("${minio.access-key}") String accessKey,
            @Value("${minio.secret-key}") String secretKey,
            @Value("${minio.bucket}") String bucketName) {

        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        this.bucketName = bucketName;

        // Создаем bucket при старте, если его нет
        createBucketIfNotExists();
    }

    private void createBucketIfNotExists() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                System.out.println("Bucket '" + bucketName + "' created");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error creating bucket", e);
        }
    }

    /**
     * Загрузить файл в MinIO
     * @return URL загруженного файла
     */
    public String uploadFile(byte[] data, String fileName, String contentType) {
        try {
            String objectName = UUID.randomUUID() + "_" + fileName;

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(new java.io.ByteArrayInputStream(data), data.length, -1)
                    .contentType(contentType)
                    .build());

            // Возвращаем URL для доступа к файлу
            return String.format("http://localhost:9000/%s/%s", bucketName, objectName);

        } catch (Exception e) {
            throw new RuntimeException("Error uploading file to MinIO", e);
        }
    }

    /**
     * Загрузить файл из InputStream
     */
    public String uploadFile(InputStream inputStream, String fileName, String contentType, long size) {
        try {
            String objectName = UUID.randomUUID() + "_" + fileName;

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build());

            return String.format("http://localhost:9000/%s/%s", bucketName, objectName);

        } catch (Exception e) {
            throw new RuntimeException("Error uploading file to MinIO", e);
        }
    }
}