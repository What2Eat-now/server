package what.what2eat.global.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import what.what2eat.global.exception.CommonErrorCode;
import what.what2eat.global.exception.CustomException;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.region.static}")
    private String region;

    public List<String> uploadFiles(List<MultipartFile> files, String preFilePath) {
        return files.stream()
                .map(file -> {
                    String key = preFilePath + "/" + generateFileName(file); // S3에 저장될 파일 경로 생성
                    try {
                        // S3에 파일 업로드
                        s3Client.putObject(
                                PutObjectRequest.builder()
                                        .bucket(bucket) // S3 버킷 이름
                                        .key(key)       // S3 파일 경로(Key)
                                        .build(),
                                RequestBody.fromBytes(file.getBytes()) // 파일 데이터를 바이트 배열로 변환
                        );
                        // 업로드된 파일의 URL 반환
                        return getUploadedFileUrl(key);
                    } catch (IOException e) {
                        throw new CustomException(CommonErrorCode.FAIL_S3_UPLOAD);
                    }
                })
                .collect(Collectors.toList()); // 결과를 리스트로 변환하여 반환
    }

    /**
     * 고유한 파일명을 생성
     *
     * @param file 업로드된 파일
     * @return 고유한 파일명
     */
    private String generateFileName(MultipartFile file) {
        return UUID.randomUUID() + "-" + file.getOriginalFilename();
    }

    private String getUploadedFileUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
    }
}
