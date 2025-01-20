package what.what2eat.domain.diary.Converter;

import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;
import what.what2eat.domain.auth.entity.User;
import what.what2eat.domain.diary.controller.dto.DiaryRequestDTO;
import what.what2eat.domain.diary.controller.dto.DiaryResponseDTO;
import what.what2eat.domain.diary.entity.Diary;
import what.what2eat.domain.diary.entity.DiaryImage;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DiaryConverter {

    // 다이어리 엔티티로 변환
    public Diary todiary(DiaryRequestDTO.DiaryWriteDTO request, User user, Point point) {
        return Diary.builder().title(request.getTitle())
                .content(request.getContent())
                .user(user)
                .visitDate(request.getVisitDate())
                .placeName(request.getPlaceName())
                .location(point)
                .rate(request.getRate())
                .markerNumber(request.getMarkerNumber())
                .build();
    }

    // 다이어리 상세 정보 DTO로 변환
    public DiaryResponseDTO.GetDiaryDTO toGetDiaryDTO(Diary diary) {
        return DiaryResponseDTO.GetDiaryDTO.builder()
                .title(diary.getTitle())
                .content(diary.getContent())

                .placeName(diary.getPlaceName())
                .rate(diary.getRate())
                .markerNumber(diary.getMarkerNumber())
                .visitDate(diary.getVisitDate())
                .uploadImgList(convertImagesToUrls(diary.getUploadImgList()))
                .build();
    }

    // 다이어리 목록 조회에 필요한 썸네일 DTO로 변환
    public DiaryResponseDTO.GetDiaryThumbnailDTO diaryToThumbnailDTO(Diary diary) {
        return DiaryResponseDTO.GetDiaryThumbnailDTO.builder()
                .diaryId(diary.getDiaryId())
                .title(diary.getTitle())
                .latitude(diary.getLocation().getY())
                .longitude(diary.getLocation().getX())
                .markerNumber(diary.getMarkerNumber())
                .placeName(diary.getPlaceName())
                .uploadImgList(convertImagesToUrls(diary.getUploadImgList()))
                .visitDate(diary.getVisitDate()).build();
    }

    private List<String> convertImagesToUrls(List<DiaryImage> imageList) {
        return imageList.stream()
                .map(DiaryImage::getImageUrl)
                .collect(Collectors.toList());
    }
}
