package haru.harudrawer.domain.diary.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import haru.harudrawer.domain.diary.controller.dto.DiaryRequestDTO;
import haru.harudrawer.domain.diary.controller.dto.DiaryResponseDTO;
import haru.harudrawer.domain.diary.service.DiaryService;
import haru.harudrawer.global.response.ApiResponse;
import haru.harudrawer.global.response.ResponseCode;

import java.util.List;

@Controller
@Slf4j
@RequestMapping("/api/v1/diary")
@RequiredArgsConstructor
@Tag(name = "다이어리 관련 컨트롤러", description = "다이어리 관련 API를 처리하는 컨트롤러 입니다.")
public class DiaryController {

    private final DiaryService diaryService;

    // 다이어리 작성
    @PostMapping("")
    @Operation(summary = "다이어리 작성", description = "다이어리 작성을 처리하는 API 입니다.")
    public ResponseEntity<ApiResponse<Void>> writeDiary(@ModelAttribute DiaryRequestDTO.DiaryWriteDTO request) {
        diaryService.writeDiary(request);

        return ResponseEntity.ok(ApiResponse.of(ResponseCode.CREATED));
    }

    // 다이어리 목록 조회
    @GetMapping("")
    @Operation(summary = "다이어리 목록 조회", description = "다이어리 목록 조회를 처리하는 API 입니다.")
    public ResponseEntity<ApiResponse<List<DiaryResponseDTO.GetDiaryDTO>>> getAllDiaryThumbnail() {
        List<DiaryResponseDTO.GetDiaryDTO> allDiaryThumbnails = diaryService.getDiary();

        return ResponseEntity.ok(ApiResponse.of(ResponseCode.SUCCESS, allDiaryThumbnails));
    }

    // 다이어리 수정
    @PutMapping("/{diaryId}")
    @Operation(summary = "다이어리 수정", description = "다이어리 수정을 처리하는 API 입니다.")
    public ResponseEntity<ApiResponse<Void>> updateDiary(@PathVariable Long diaryId, DiaryRequestDTO.DiaryUpdateDTO request) {
        diaryService.updateDiary(diaryId, request);

        return ResponseEntity.ok(ApiResponse.of(ResponseCode.SUCCESS));
    }


    @DeleteMapping("")
    @Operation(summary = "다이어리 삭제", description = "다이어리 삭제를 처리하는 API 입니다.")
    public ResponseEntity<ApiResponse<Void>> deleteDiary(@RequestBody DiaryRequestDTO.DiaryDeleteDTO request) {
        diaryService.deleteDiary(request);

        return ResponseEntity.ok(ApiResponse.of(ResponseCode.SUCCESS));
    }
}
