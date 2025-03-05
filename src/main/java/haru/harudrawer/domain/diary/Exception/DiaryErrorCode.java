package haru.harudrawer.domain.diary.Exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import haru.harudrawer.global.exception.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum DiaryErrorCode implements BaseErrorCode {

    DIARY_NOT_FOUND("404", HttpStatus.NOT_FOUND, "다이어리를 찾을 수 없습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
