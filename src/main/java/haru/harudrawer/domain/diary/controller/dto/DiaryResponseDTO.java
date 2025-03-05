package haru.harudrawer.domain.diary.controller.dto;

import lombok.Builder;
import lombok.Getter;


import java.time.LocalDate;
import java.util.List;

public class DiaryResponseDTO {

    @Builder
    @Getter
    public static class GetDiaryDTO {
        private Long diaryId;

        private String title;

        private String content;

        private String placeName;

        private LocalDate visitDate;

        private Double latitude;

        private Double longitude;

        private Integer rate;

        private Integer markerNumber;

        private List<String> uploadImgList;
    }

}
