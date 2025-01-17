package what.what2eat.domain.diary.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import what.what2eat.domain.auth.entity.User;
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

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final JwtProvider jwtProvider;
    private final GeometryFactory geometryFactory;
    private final AuthRepository authRepository;
    private final DiaryConverter diaryConverter;
    private final S3Service s3Service;

    // 다이어리 작성
    public void writeDiary(DiaryRequestDTO.DiaryWriteDTO request){

        // 사용자 조회
        User user = authRepository.findById(jwtProvider.extractUserId())
                .orElseThrow(() -> new MemberException(AuthErrorCode.USER_NOT_FOUND));

        // 위도,경도 -> Point 타입으로 변환
        Point point = getPoint(request.getLatitude(), request.getLongitude());

        String preFilePath = "diary_image/" + user.getUserId();

        // 다이어리 객체 생성
        Diary diary = diaryConverter.todiary(request, user, point);

        // 다이어리 이미지를 첨부한 경우에만 연관관계 설정
        if (request.getUploadImgList().isEmpty()) {

            // 다이어리 이미지 저장
            List<String> uploadedFileUrl = s3Service.uploadFiles(request.getUploadImgList(), preFilePath);

            //S3 업로드 후 DiaryImageList 생성
            List<DiaryImage> imageList = uploadedFileUrl.stream()
                    .map(url -> DiaryImage.builder()
                            .imageUrl(url) // 이미지 URL 설정
                            .diary(diary) // Diary와 연관 설정
                            .build())
                    .collect(Collectors.toList());

            // Diary와 이미지 리스트 연관 설정
            diary.getUploadImgList().addAll(imageList);
        }

        // Diary 저장(이미지 함께 저장됨)
        diaryRepository.save(diary);

        log.info("다이어리 작성 완료");
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

    // 위도, 경도 -> point로 변환
    private Point getPoint(Double latitude, Double longitude) {
        return geometryFactory.createPoint(new Coordinate(latitude, longitude));
    }

}
