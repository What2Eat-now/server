package what.what2eat.domain.diary.Converter;

import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;
import what.what2eat.domain.auth.entity.User;
import what.what2eat.domain.diary.controller.dto.DiaryRequestDTO;
import what.what2eat.domain.diary.controller.dto.DiaryResponseDTO;
import what.what2eat.domain.diary.entity.Diary;

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
                .markerNumber(request.getMarker_number())
                .uploadImg(request.getUploadImg())
                .build();
    }

    // 다이어리 상세 정보 DTO로 변환
    public DiaryResponseDTO.GetDiaryDTO toGetDiaryDTO(Diary diary) {
        return DiaryResponseDTO.GetDiaryDTO.builder()
                .title(diary.getTitle())
                .content(diary.getContent())
                .latitude(diary.getLocation().getY())
                .longitude(diary.getLocation().getX())
                .placeName(diary.getPlaceName())
                .rate(diary.getRate())
                .marker_number(diary.getMarkerNumber())
                .visitDate(diary.getVisitDate())
                .upload_img(diary.getUploadImg())
                .build();
    }

    // 다이어리 목록 조회에 필요한 썸네일 DTO로 변환
    public DiaryResponseDTO.GetDiaryThumbnailDTO diaryToThumbnailDTO(Diary diary) {
        return DiaryResponseDTO.GetDiaryThumbnailDTO.builder()
                .diaryId(diary.getDiaryId())
                .title(diary.getTitle())
                .markerNumber(diary.getMarkerNumber())
                .placeName(diary.getPlaceName())
                .uploadImg(diary.getUploadImg())
                .visitDate(diary.getVisitDate()).build();
    }
}
