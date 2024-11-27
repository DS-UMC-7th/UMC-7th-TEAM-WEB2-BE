package miniproject.web02.web.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import miniproject.web02.apiPayLoad.code.status.ErrorStatus;
import miniproject.web02.apiPayLoad.code.status.SuccessStatus;
import miniproject.web02.domain.enums.Category;
import miniproject.web02.domain.enums.Level;
import miniproject.web02.domain.enums.StudyTime;
import miniproject.web02.service.LikeService;
import miniproject.web02.service.ReviewFilterService;
import miniproject.web02.service.reviewService.ReviewService;
import miniproject.web02.web.dto.ApiResponse;
import miniproject.web02.web.dto.review.FilterRequestDTO;
import miniproject.web02.web.dto.review.ReviewFilterResponseDTO;
import miniproject.web02.web.dto.reviewDTO.ReviewRequestDto;
import miniproject.web02.web.dto.reviewDTO.ReviewResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewFilterService reviewFilterService;
    private final ReviewService reviewService;
    private final LikeService likeService;

    private static final Logger logger= LoggerFactory.getLogger(ReviewController.class);

    @GetMapping
    public ResponseEntity<List<ReviewFilterResponseDTO>> getFilteredReviews(
            @RequestParam(value = "category", required = false) Category category,
            @RequestParam(value = "level", required = false) Level level,
            @RequestParam(value = "studyTime", required = false) StudyTime studyTime,
            @RequestParam(value = "sort", required = false) String sort
    ) {
        FilterRequestDTO filterRequestDTO = new FilterRequestDTO(category, level, studyTime, sort);
        List<ReviewFilterResponseDTO> response = reviewFilterService.filterAndSortReviews(filterRequestDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<ReviewResponseDTO.ReviewDTO>> createReview(
            @RequestPart("review") ReviewRequestDto requestDto,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        logger.info("리뷰 생성 요청을 받았습니다: {}", requestDto);

        try {
            // 이미지 파일 정보 확인
            if (image != null && !image.isEmpty()) {
                logger.info("업로드된 파일 이름: {} (크기: {} 바이트)", image.getOriginalFilename(), image.getSize());
            } else {
                logger.warn("업로드된 파일이 없거나 파일이 비어 있습니다.");
            }

            // 리뷰 생성 로직 호출
            ReviewResponseDTO.ReviewDTO responseDto = reviewService.createdReview(requestDto, image);

            // 성공 응답
            return ResponseEntity
                    .status(HttpStatus.CREATED) // 201 Created
                    .body(ApiResponse.<ReviewResponseDTO.ReviewDTO>builder()
                            .isSuccess(true)
                            .code(SuccessStatus.SUCCESS_REVIEW_CREATED.getCode())
                            .message(SuccessStatus.SUCCESS_REVIEW_CREATED.getMessage())
                            .result(responseDto)
                            .build());
        } catch (IllegalArgumentException ex) {
            logger.error("유효성 검증 실패: {}", ex.getMessage());

            // 유효성 검증 실패 응답
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST) // 400 Bad Request
                    .body(ApiResponse.<ReviewResponseDTO.ReviewDTO>builder()
                            .isSuccess(false)
                            .code(ErrorStatus._BAD_REQUEST.getCode())
                            .message(ex.getMessage())
                            .result(null)
                            .build());
        } catch (Exception ex) {
            logger.error("서버 오류가 발생했습니다: {}", ex.getMessage());

            // 서버 내부 오류 응답
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR) // 500 Internal Server Error
                    .body(ApiResponse.<ReviewResponseDTO.ReviewDTO>builder()
                            .isSuccess(false)
                            .code(ErrorStatus._INTERNAL_SERVER_ERROR.getCode())
                            .message(ErrorStatus._INTERNAL_SERVER_ERROR.getMessage())
                            .result(null)
                            .build());
        }
    }

    @PatchMapping("/{reviewId}/like")
    @Operation(summary = "리뷰 좋아요 API", description = "리뷰에 좋아요를 추가합니다.")
    public ResponseEntity<ApiResponse<ReviewResponseDTO.ReviewDTO>> likeReview(@PathVariable Long reviewId) {
        // 좋아요 수 증가 처리
        ReviewResponseDTO.ReviewDTO updatedReview = likeService.likeReview(reviewId);

        // 성공 응답 반환
        return ResponseEntity.ok(ApiResponse.<ReviewResponseDTO.ReviewDTO>builder()
                .isSuccess(true)
                .code(SuccessStatus.SUCCESS_LIKE_REVIEW.getCode())
                .message(SuccessStatus.SUCCESS_LIKE_REVIEW.getMessage())
                .result(updatedReview)
                .build());
    }
}
