package BE_Elixir.Elixir.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {


    // 전범위
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN.value(), "접근 권한이 없습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST.value(), "유효하지 않은 입력 값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류가 발생했습니다."),
    EMAIL_SENDING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "이메일 전송 중 오류가 발생했습니다."),
    UNSUPPORTED_ENCODING(HttpStatus.INTERNAL_SERVER_ERROR.value(), "지원하지 않는 문자 인코딩 방식입니다."),
    JSON_PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "데이터 처리 중 오류가 발생했습니다."),
    INVALID_ENCRYPTION_ALGORITHM(HttpStatus.INTERNAL_SERVER_ERROR.value(), "알 수 없는 암호화 알고리즘입니다."),

    // Auth
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED.value(), "회원 정보가 일치하지 않습니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED.value(), "Access Token이 유효하지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED.value(), "Refresh Token이 유효하지 않습니다."),
    TOKEN_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Token 발급 중 오류가 발생했습니다."),

    SOCIAL_USER_INFO_FETCH_FAILED(HttpStatus.BAD_GATEWAY.value(), "소셜 사용자 정보 조회에 실패했습니다."),

    // 회원
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "해당 회원을 찾을 수 없습니다."),
    EXISTS_MEMBER(HttpStatus.CONFLICT.value(), "이미 존재하는 회원입니다."),

    EMAIL_VERIFICATION_CODE_MISMATCH(HttpStatus.BAD_REQUEST.value(), "이메일 인증번호가 일치하지 않습니다."),
    EMAIL_VERIFICATION_CODE_EXPIRED(HttpStatus.BAD_REQUEST.value(), "이메일 인증번호의 유효 시간이 초과되었습니다."),

    EMAIL_REGISTERED_WITH_LOCAL(HttpStatus.CONFLICT.value(), "이미 가입된 이메일입니다. 일반 로그인을 사용해주세요."),
    EMAIL_REGISTERED_WITH_SOCIAL(HttpStatus.CONFLICT.value(), "이미 소셜 로그인으로 가입된 이메일입니다. 소셜 로그인을 사용해주세요."),
    EMAIL_REGISTERED_WITH_ANOTHER_SOCIAL(HttpStatus.CONFLICT.value(), "이미 다른 소셜로 가입된 이메일입니다."),
    LOGIN_TYPE_MISMATCH(HttpStatus.BAD_REQUEST.value(), "지원하지 않는 로그인 타입입니다."),

    // 회원 팔로우
    ALREADY_FOLLOWING(HttpStatus.CONFLICT.value(), "이미 팔로우 되어있는 회원입니다."),
    FOLLOW_RELATION_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "팔로우 관계가 없는 회원입니다."),
    SELF_FOLLOW_NOT_ALLOWED(HttpStatus.BAD_REQUEST.value(), "자기 자신을 팔로우할 수 없습니다."),

    // 식재료
    INGREDIENT_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "해당 식재료가 존재하지 않습니다."),

    // 식단
    DIETLOG_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "식단 기록이 존재하지 않습니다."),
    INVALID_DIETLOG_TYPE(HttpStatus.BAD_REQUEST.value(), "잘못된 식단 타입(아침, 점심 등)입니다."),
    DIET_LOG_TYPE_DUPLICATE(HttpStatus.BAD_REQUEST.value(), "오늘은 이미 해당 식사 유형을 기록하셨습니다."),

    // 레시피
    RECIPE_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "레시피가 존재하지 않습니다."),

    INVALID_FILTER(HttpStatus.BAD_REQUEST.value(), "필터링 값이 잘못되었습니다."),

    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "댓글이 존재하지 않습니다."),
    ALREADY_LIKED(HttpStatus.CONFLICT.value(), "이미 좋아요한 레시피입니다."),
    LIKE_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "좋아요한 레시피가 없습니다."),
    ALREADY_SCRAPPED(HttpStatus.CONFLICT.value(), "이미 스크랩한 레시피입니다."),
    SCRAP_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "스크랩한 레시피가 없습니다."),
    INVALID_EVENT_OPERATION(HttpStatus.BAD_REQUEST.value(), "잘못된 이벤트 요청입니다."),

    // 챌린지
    CHALLENGE_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "챌린지를 찾을 수 없습니다."),
    CHALLENGE_ACHIEVEMENT_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "챌린지 기록이 없습니다."),

    // 설문조사
    INVALID_ALLERGY_VALUE(HttpStatus.BAD_REQUEST.value(), "잘못된 알러지 값입니다."),
    INVALID_MEAL_STYLE(HttpStatus.BAD_REQUEST.value(), "잘못된 식사 스타일(고기위주 등)입니다."),
    INVALID_RECIPE_STYLE(HttpStatus.BAD_REQUEST.value(), "잘못된 레시피 스타일(한식, 중식 등) 입니다."),
    INVALID_REASON(HttpStatus.BAD_REQUEST.value(), "잘못된 식단 이유(항산화 강화 등)입니다."),

    // 챗봇
    CHATBOT_INITIAL_PROMPT_MISSING(HttpStatus.INTERNAL_SERVER_ERROR.value(), "프롬프트 메시지 생성을 실패했습니다."),
    CHATBOT_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "챗봇 세션 ID를 찾을 수 없습니다."),
    EXTERNAL_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "API를 정상적으로 호출하지 못했습니다."),

    // S3
    S3_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "MultipartFile을 File로 전환하지 못했습니다."),
    S3_DELETE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "S3에서 파일을 삭제하지 못했습니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "업로드하는 이미지의 용량이 초과되었습니다. (10MB미만)"),
    S3_INVALID_URL(HttpStatus.INTERNAL_SERVER_ERROR.value(), "잘못된 S3 이미지 URL입니다.")
    ;


    private final int status;
    private final String message;
}

