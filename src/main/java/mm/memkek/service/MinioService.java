package mm.memkek.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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

    public Mono<String> uploadFile(byte[] data, String fileName, String contentType) {
        return Mono.fromCallable(() -> {
                    String objectName = UUID.randomUUID() + "_" + fileName;

                    minioClient.putObject(PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(new java.io.ByteArrayInputStream(data), data.length, -1)
                            .contentType(contentType)
                            .build());

                    return objectName;
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<String> getPresignedUrl(String objectName, int expirySeconds) {
        return Mono.fromCallable(() -> presignBlocking(objectName, expirySeconds))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<String> getPresignedUrlIfPresent(String objectName, int expirySeconds) {
        if (objectName == null || objectName.isBlank()) {
            return Mono.empty();
        }
        return getPresignedUrl(objectName, expirySeconds);
    }

    private String presignBlocking(String objectName, int expirySeconds) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(objectName)
                        .expiry(expirySeconds)
                        .build()
        );
    }
}