package miniproject.web02.apiPayLoad.code.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import miniproject.web02.apiPayLoad.code.BaseCode;
import miniproject.web02.apiPayLoad.code.ReasonDTO;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessStatus implements BaseCode {

    // 일반적인 응답
    _OK(HttpStatus.OK, "COMMON200", "성공입니다."),

    // 강의 관련 응답
    SUCCESS_FETCH_LECTURE(HttpStatus.OK, "LECTURE2001", "강의 정보를 성공적으로 가져왔습니다."),
    SUCCESS_CREATE_LECTURE(HttpStatus.OK, "LECTURE2002", "강의 정보를 성공적으로 등록했습니다."),

    // 리뷰 관련 응답
    SUCCESS_FETCH_REVIEW_LIST(HttpStatus.OK, "REVIEW2001", "리뷰 목록을 성공적으로 가져왔습니다."),
    SUCCESS_FETCH_REVIEW_IMAGE(HttpStatus.OK, "REVIEW2002", "리뷰 이미지를 성공적으로 가져왔습니다."),
    SUCCESS_REVIEW_CREATED(HttpStatus.OK,"REVIEW2003", "리뷰가 성공적으로 생성되었습니다."),
    SUCCESS_LIKE_REVIEW(HttpStatus.OK, "REVIEW2004","좋아요를 성공적으로 추가했습니다.");




    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ReasonDTO getReason() {
        return ReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(true)
                .build();
    }

    @Override
    public ReasonDTO getReasonHttpStatus() {
        return ReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(true)
                .httpStatus(httpStatus)
                .build()
                ;
    }
}