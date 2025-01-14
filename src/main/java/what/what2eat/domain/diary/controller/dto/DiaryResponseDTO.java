package what.what2eat.domain.diary.controller.dto;

import lombok.Builder;
import lombok.Getter;


import java.time.LocalDate;

public class DiaryResponseDTO {

    @Builder
    @Getter
    public static class GetDiaryDTO {

        private String title;

        private String content;

        private String placeName;

        private LocalDate visitDate;

        private Double latitude;

        private Double longitude;

        private String rate;

        private Integer marker_number;

        private String upload_img;
    }


    @Builder
    @Getter
    public static class GetDiaryThumbnailDTO {

        private Long diaryId;

        private String title;

        private String placeName;

        private LocalDate visitDate;

        private Integer markerNumber;

        private String uploadImg;
    }
}
