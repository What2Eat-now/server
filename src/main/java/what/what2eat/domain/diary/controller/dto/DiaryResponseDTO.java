package what.what2eat.domain.diary.controller.dto;

import lombok.Builder;
import lombok.Getter;


import java.time.LocalDate;
import java.util.List;

public class DiaryResponseDTO {

    @Builder
    @Getter
    public static class GetDiaryDTO {

        private String title;

        private String content;

        private String placeName;

        private LocalDate visitDate;

        private String rate;

        private Integer markerNumber;

        private List<String> uploadImgList;
    }


    @Builder
    @Getter
    public static class GetDiaryThumbnailDTO {

        private Long diaryId;

        private String title;

        private String placeName;

        private LocalDate visitDate;

        private Double latitude;

        private Double longitude;

        private String rate;

        private Integer markerNumber;

        private List<String> uploadImgList;
    }


}
