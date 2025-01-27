package what.what2eat.domain.diary.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import what.what2eat.domain.auth.entity.User;
import what.what2eat.domain.auth.entity.UserStatus;
import what.what2eat.domain.auth.exception.AuthErrorCode;
import what.what2eat.domain.auth.exception.MemberException;
import what.what2eat.domain.auth.repository.AuthRepository;
import what.what2eat.domain.diary.Converter.DiaryConverter;
import what.what2eat.domain.diary.Exception.DiaryErrorCode;
import what.what2eat.domain.diary.Exception.DiaryException;
import what.what2eat.domain.diary.controller.dto.DiaryRequestDTO;
import what.what2eat.domain.diary.controller.dto.DiaryResponseDTO;
import what.what2eat.domain.diary.entity.Diary;
import what.what2eat.domain.diary.entity.DiaryImage;
import what.what2eat.domain.diary.repository.DiaryRepository;
import what.what2eat.global.s3.S3Service;
import what.what2eat.global.security.jwt.JwtProvider;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final JwtProvider jwtProvider;
    private final AuthRepository authRepository;
    private final DiaryConverter diaryConverter;
    private final S3Service s3Service;
    private final GeometryFactory geometryFactory;


    // 다이어리 작성
    public void writeDiary(DiaryRequestDTO.DiaryWriteDTO request){

        // 사용자 조회
        User user = authRepository.findByUserIdAndUserStatus(jwtProvider.extractUserId(), UserStatus.ACTIVE)
                .orElseThrow(() -> new MemberException(AuthErrorCode.USER_NOT_FOUND));

        // 위도,경도 -> Point 타입으로 변환
        Point point = getPoint(request.getLatitude(), request.getLongitude());

        // 다이어리 객체 생성
        Diary diary = diaryConverter.todiary(request, user, point);

        // 저장될 경로 설정
        String preFilePath = "diary_image/" + user.getUserId();

        // 다이어리 이미지를 첨부한 경우에만 연관관계 설정
        uploadDiaryImages(diary, request.getUploadImgList(), preFilePath);

        // Diary 저장(이미지 함께 저장됨)
        diaryRepository.save(diary);

        log.info("다이어리 작성 완료");
    }


    // 다이어리 수정
    public void updateDiary(Long diaryId, DiaryRequestDTO.DiaryUpdateDTO request) {
        Long userId = jwtProvider.extractUserId();

        log.info("diaryId = " + diaryId);

        Diary existingDiary = diaryRepository.findById(diaryId).orElseThrow(
                () -> new DiaryException(DiaryErrorCode.DIARY_NOT_FOUND));

        // 다이어리 업데이트
        existingDiary.update(request, getPoint(request.getLatitude(), request.getLongitude()));

        // 기존 이미지 URL 리스트
        List<String> existingUrlList = existingDiary.getDiaryImageList().stream()
                .map(diaryImage -> diaryImage.getImageUrl())
                .collect(Collectors.toList());

        // 삭제할 이미지 URL 리스트 추출
        List<String> deleteUrlList = existingUrlList.stream()
                .filter(imgUrl -> !request.getExistingImgList().contains(imgUrl))
                .collect(Collectors.toList());

        // 삭제할 이미지가 존재하는 경우
        if (!deleteUrlList.isEmpty()) {

            // S3 데이터 삭제
            s3Service.deleteFiles(deleteUrlList);

            // DB 데이터 삭제
            existingDiary.getDiaryImageList().removeIf(
                    diaryImage -> deleteUrlList.contains(diaryImage.getImageUrl())
            );
        }

        // 새로 업로드할 이미지 리스트
        List<MultipartFile> newImgList = request.getNewImgList();

        // 비어있지 않은 경우 s3에 데이터 저장
        if (!isMultipartFileListEmpty(newImgList)) {
            String preFilePath = "diary_image/" + userId;
            uploadDiaryImages(existingDiary, newImgList, preFilePath);
        }
    }

    // 다이어리 삭제
    public void deleteDiary(DiaryRequestDTO.DiaryDeleteDTO request) {

        // 다이어리 목록 조회
        List<Diary> existingDiaryList = diaryRepository.findAllById(request.getDiaryIdList());

        if (existingDiaryList.isEmpty()) {
            throw new DiaryException(DiaryErrorCode.DIARY_NOT_FOUND);
        }

        // s3에 저장된 imgUrl List 반환
        List<String> uploadedImgUrlList = existingDiaryList.stream()
                .flatMap(diary -> diary.getDiaryImageList().stream())
                .map(DiaryImage::getImageUrl)
                .collect(Collectors.toList());

        // s3에 저장된 파일 삭제
        s3Service.deleteFiles(uploadedImgUrlList);

        // DB에 저장된 다이어리, 다이어리 이미지 삭제
        diaryRepository.deleteAll(existingDiaryList);

    }

    // 다이어리 상세 정보 조회
    public DiaryResponseDTO.GetDiaryDTO getDiary(Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new DiaryException(DiaryErrorCode.DIARY_NOT_FOUND));

        DiaryResponseDTO.GetDiaryDTO getDiaryDTO = diaryConverter.toGetDiaryDTO(diary);

        log.info("다이어리 조회 완료");

        return getDiaryDTO;
    }

    // 다이어리 썸네일 데이터 목록 조회
    public List<DiaryResponseDTO.GetDiaryThumbnailDTO> getAllDiaryThumbnails() {

        // userId로 필터링 필요
        List<Diary> diaryList = diaryRepository.findByUserUserId(jwtProvider.extractUserId());

        // 다이어리 썸네일 목록 -> DTO 목록으로 변환
        List<DiaryResponseDTO.GetDiaryThumbnailDTO> thumbnailDTOList = diaryList.stream()
                .map(diaryConverter::diaryToThumbnailDTO)
                .collect(Collectors.toList());

        return thumbnailDTOList;
    }

    private void uploadDiaryImages(Diary diary, List<MultipartFile> files, String filePath) {
        if (isMultipartFileListEmpty(files)) {
            return;
        }

        // S3에 파일 업로드
        List<String> uploadedUrls = s3Service.uploadFiles(files, filePath);

        // 업로드된 파일 URL들을 바탕으로 DiaryImage 엔티티 생성
        List<DiaryImage> imageList = uploadedUrls.stream()
                .map(url -> DiaryImage.builder()
                        .imageUrl(url)
                        .diary(diary)
                        .build())
                .collect(Collectors.toList());

        // Diary 엔티티에 연관관계 설정
        diary.getDiaryImageList().addAll(imageList);
    }


    /**
     * 해당 MultipartFile 리스트에 실제 업로드할 파일이 있는지 검사
     * @return 리스트가 null이거나, 요소가 없거나 모든 파일이 비어있다면 true, 그렇지 않으면 false를 반환
     */
    public boolean isMultipartFileListEmpty(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return true;
        }
        // 모든 파일이 null이거나 비어있으면 "빈 리스트"로 판단
        return files.stream().allMatch(file -> file == null || file.isEmpty());
    }


    // 위도(latitude), 경도(longitude) -> point로 변환
    private Point getPoint(Double latitude, Double longitude) {
        return geometryFactory.createPoint(new Coordinate(latitude, longitude));
    }

}
