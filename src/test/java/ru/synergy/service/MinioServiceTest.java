package ru.synergy.service;

import io.minio.*;
import io.minio.errors.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MinioServiceTest {
    @Mock
    private MinioClient minioClient;

    @InjectMocks
    private MinioService minioService;

    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() throws IOException {
        mockFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                new ByteArrayInputStream("Hello, MinIO!".getBytes())
        );
    }

    @Test
    void initBucket_shouldCreateBucketIfNotExists() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class)))
                .thenReturn(false);
        doNothing().when(minioClient).makeBucket(any(MakeBucketArgs.class));

        minioService.initBucket();

        verify(minioClient).bucketExists(BucketExistsArgs.builder().bucket("music-bucket").build());
        verify(minioClient).makeBucket(MakeBucketArgs.builder().bucket("music-bucket").build());
    }

    @Test
    void initBucket_shouldNotCreateBucketIfAlreadyExists() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class)))
                .thenReturn(true);

        minioService.initBucket();

        verify(minioClient).bucketExists(BucketExistsArgs.builder().bucket("music-bucket").build());
        verify(minioClient, never()).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    void initBucket_shouldThrowRuntimeExceptionOnError() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class)))
                .thenThrow(new RuntimeException("Test error"));

        assertThatThrownBy(() -> minioService.initBucket())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ошибка при инициализации bucket'а");
    }

    @Test
    void uploadFile_shouldUploadFileSuccessfully() throws Exception {
        doAnswer(invocation -> null)
                .when(minioClient).putObject(any(PutObjectArgs.class));

        minioService.uploadFile("music-bucket", "test.txt", mockFile);

        ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient).putObject(captor.capture());

        PutObjectArgs args = captor.getValue();
        assertThat(args.bucket()).isEqualTo("music-bucket");
        assertThat(args.object()).isEqualTo("test.txt");
        assertThat(args.contentType()).isEqualTo("text/plain");
        try (InputStream inputStream = args.stream()) {
            byte[] buffer = new byte[1024];
            int totalRead = 0;
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                totalRead += bytesRead;
            }
            assertThat(totalRead).isEqualTo(13);
        }
    }

    @Test
    void uploadFile_shouldPropagateMinioExceptions() throws Exception {
        doThrow(new ServerException("Server error", 500, "Status: 500"))
                .when(minioClient).putObject(any(PutObjectArgs.class));

        assertThatThrownBy(() -> minioService.uploadFile("music-bucket", "test.txt", mockFile))
                .isInstanceOf(ServerException.class)
                .hasMessageContaining("Server error");
    }

    @Test
    void getFileUrl_shouldReturnCorrectUrl() {
        String url = minioService.getFileUrl("music-bucket", "test.txt");

        assertThat(url).isEqualTo("http://localhost:9000/music-bucket/test.txt");
    }

    @Test
    void setBucketPublic_shouldSetPolicySuccessfully() throws Exception {
        doNothing().when(minioClient).setBucketPolicy(any(SetBucketPolicyArgs.class));

        minioService.setBucketPublic("music-bucket");

        ArgumentCaptor<SetBucketPolicyArgs> captor = ArgumentCaptor.forClass(SetBucketPolicyArgs.class);
        verify(minioClient).setBucketPolicy(captor.capture());

        SetBucketPolicyArgs args = captor.getValue();
        assertThat(args.bucket()).isEqualTo("music-bucket");
        assertThat(args.config()).contains("\"Effect\":\"Allow\"");
        assertThat(args.config()).contains("arn:aws:s3:::music-bucket/*");
    }

    @Test
    void setBucketPublic_shouldRethrowIOException() throws Exception {
        doThrow(new IOException("IO error"))
                .when(minioClient).setBucketPolicy(any(SetBucketPolicyArgs.class));

        assertThatThrownBy(() -> minioService.setBucketPublic("music-bucket"))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(IOException.class);
    }
}
