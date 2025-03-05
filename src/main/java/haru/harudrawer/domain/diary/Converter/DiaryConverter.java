package haru.harudrawer.domain.diary.Converter;

import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;
import haru.harudrawer.domain.auth.entity.User;
import haru.harudrawer.domain.diary.controller.dto.DiaryRequestDTO;
import haru.harudrawer.domain.diary.controller.dto.DiaryResponseDTO;
import haru.harudrawer.domain.diary.entity.Diary;
import haru.harudrawer.domain.diary.entity.DiaryImage;

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

    // 다이어리 정보 DTO로 변환
    public DiaryResponseDTO.GetDiaryDTO toGetDiaryDTO(Diary diary) {
        return DiaryResponseDTO.GetDiaryDTO.builder()
                .diaryId(diary.getDiaryId())
                .title(diary.getTitle())
                .content(diary.getContent())
                .placeName(diary.getPlaceName())
                .rate(diary.getRate())
                .latitude(diary.getLocation().getX())
                .longitude(diary.getLocation().getY())
                .markerNumber(diary.getMarkerNumber())
                .visitDate(diary.getVisitDate())
                .uploadImgList(convertImagesToUrls(diary.getDiaryImageList()))
                .build();
    }

    private List<String> convertImagesToUrls(List<DiaryImage> imageList) {
        return imageList.stream()
                .map(DiaryImage::getImageUrl)
                .collect(Collectors.toList());
    }
}
