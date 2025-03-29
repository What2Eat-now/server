package haru.harudrawer.global.s3;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import haru.harudrawer.domain.auth.entity.User;
import haru.harudrawer.global.exception.CommonErrorCode;
import haru.harudrawer.global.exception.CustomException;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.cloud-front}")
    private String cloudFrontUrl;


    public List<String> uploadFiles(List<MultipartFile> files, String preFilePath) {
        return files.stream()
                .map(file -> {
                    String key = preFilePath + "/" + generateFileName(file); // S3에 저장될 파일 경로 생성
                    try {
                        // S3에 파일 업로드
                        s3Client.putObject(
                                PutObjectRequest.builder()
                                        .bucket(bucket) // S3 버킷 이름
                                        .key(key)
                                        .contentType(file.getContentType())
                                        .contentDisposition("inline")// S3 파일 경로(Key)
                                        .build(),
                                RequestBody.fromBytes(file.getBytes()) // 파일 데이터를 바이트 배열로 변환
                        );
                        // 업로드된 파일의 URL 반환
                        return getUploadFileUrl(key);
                    } catch (IOException e) {
                        throw new CustomException(CommonErrorCode.FAIL_S3_UPLOAD);
                    }
                })
                .collect(Collectors.toList()); // 결과를 리스트로 변환하여 반환
    }

    public void deleteFiles(List<String> deleteImgUrlList) {

        if (deleteImgUrlList.isEmpty()) {
            return;
        }

        // ObjectIdentifier 리스트 생성
        List<ObjectIdentifier> toDeleteList = deleteImgUrlList.stream()
                .map(url -> ObjectIdentifier.builder().key(extractKey(url)).build())
                .collect(Collectors.toList());

        // DeleteRequest 객체 생성
        DeleteObjectsRequest objectsRequest = DeleteObjectsRequest.builder()
                .bucket(bucket)
                .delete(Delete.builder().objects(toDeleteList).build()).build();

        try{
            // 요청 후 응답 객체 생성
            DeleteObjectsResponse objectsResponse = s3Client.deleteObjects(objectsRequest);

            // 성공/실패한 개체 확인
            List<DeletedObject> deletedObjects = objectsResponse.deleted();
            List<S3Error> errors = objectsResponse.errors();

            if (!errors.isEmpty()) {
                log.info("Successfully deleted objects: {}, failed: {}",
                        deletedObjects.size(), errors.size());
            }

        } catch (S3Exception e) {
            log.error("Failed to delete objects: {}", e.awsErrorDetails().errorMessage());
            throw new CustomException(CommonErrorCode.FAIL_S3_DELETE);

            // 필요한 경우 재시도 또는 예외 처리
        } catch (Exception e) {
            log.error("Unexpected error while deleting objects: {}", e.getMessage());
        }

    }

    /**
     * 파라미터로 받은 사용자의 다이어리 이미지 찾아서 삭제
     * @param user
     */
    public void deleteUserImgList(User user) {
        List<String> imgList = user.getDiaries().stream()
                .flatMap(diary -> diary.getDiaryImageList().stream())
                .map(diaryImage -> diaryImage.getImageUrl())
                .collect(Collectors.toList());

        if (!imgList.isEmpty()) {
            deleteFiles(imgList);
            log.info("탈퇴한 유저의 이미지 삭제 완료");
        }
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

    private String getUploadFileUrl(String key) {
        return cloudFrontUrl + key;
    }

    public String extractKey(String imgUrl) {
        return imgUrl.replace(cloudFrontUrl, "");
    }
}
