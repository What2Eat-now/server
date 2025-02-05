package what.what2eat.domain.auth.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import what.what2eat.global.exception.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    USER_NOT_FOUND("404", HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    DUPLICATE_USER_EMAIL("409", HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    DUPLICATE_USER_NICKNAME("409", HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다."),

    INVALID_USER_ROLE("403", HttpStatus.FORBIDDEN, "유효하지 않은 사용자 권한입니다."),
    ALREADY_LOGOUT_USER("409", HttpStatus.CONFLICT, "이미 로그아웃된 사용자입니다."),
    INVALID_PASSWORD("422", HttpStatus.UNPROCESSABLE_ENTITY, "비밀번호 형식이 잘못되었습니다."),
    DUPLICATE_PASSWORD("422", HttpStatus.UNPROCESSABLE_ENTITY, "변경 전 비밀번호와 동일합니다."),
    NEED_SIGNUP("307", HttpStatus.TEMPORARY_REDIRECT, "회원 가입이 필요합니다."),

    INVALID_TOKEN("401",HttpStatus.UNAUTHORIZED, "유효하지 않는 토큰입니다."),
    ALREADY_BLACK_LIST("401",HttpStatus.UNAUTHORIZED, "이미 블랙리스트에 포함된 토큰입니다."),
    EXPIRED_TOKEN("401",HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),

    NEED_VERIFICATION("401", HttpStatus.UNAUTHORIZED, "이메일 인증이 필요합니다.");



    private final String code;
    private final HttpStatus httpStatus;
    private final String message;


}
