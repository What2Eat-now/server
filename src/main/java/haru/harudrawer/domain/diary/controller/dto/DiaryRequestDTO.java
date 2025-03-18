package haru.harudrawer.domain.diary.controller.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DiaryRequestDTO {

    @Builder
    @Getter
    public static class DiaryWriteDTO {

        @NotBlank(message = "제목은 필수 입력 항목 입니다.")
        @Size(max = 50, message = "제목은 최대 50자까지 입력 가능합니다.")
        private String title;

        @NotBlank(message = "내용은 필수 입력 항목 입니다.")
        @Size(max = 200, message = "내용은 최대 200자까지 입력 가능합니다.")
        private String content;

        @NotBlank(message = "장소 명은 필수 입력 항목 입니다.")
        private String placeName;

        @NotNull(message = "날짜는 필수 입력 항목입니다.")
        private LocalDate visitDate;

        @NotNull(message = "위도는 필수 입력 항목입니다.")
        private Double latitude;

        @NotNull(message = "경도는 필수 입력 항목입니다.")
        private Double longitude;

        private Integer rate;

        private Integer markerNumber;

        private List<MultipartFile> uploadImgList;
    }

    @JsonDeserialize(builder = DiaryRequestDTO.DiaryUpdateDTO.DiaryUpdateDTOBuilder.class)
    @Builder
    @Getter
    public static class DiaryUpdateDTO {

        @NotBlank(message = "제목은 필수 입력 항목 입니다.")
        @Size(max = 50, message = "제목은 최대 50자까지 입력 가능합니다.")
        private String title;

        @NotBlank(message = "내용은 필수 입력 항목 입니다.")
        @Size(max = 200, message = "내용은 최대 200자까지 입력 가능합니다.")
        private String content;

        @NotBlank(message = "장소 명은 필수 입력 항목 입니다.")
        private String placeName;

        @NotNull(message = "날짜는 필수 입력 항목입니다.")
        private LocalDate visitDate;

        @NotNull(message = "위도는 필수 입력 항목입니다.")
        private Double latitude;

        @NotNull(message = "경도는 필수 입력 항목입니다.")
        private Double longitude;

        private Integer rate;

        private Integer markerNumber;

        @Builder.Default
        private List<String> existingImgList = new ArrayList<>();

        @Builder.Default
        private List<MultipartFile> newImgList = new ArrayList<>();
    }

    @Getter
    @Builder
    public static class DiaryDeleteDTO {
        private List<Long> diaryIdList;
    }
}

