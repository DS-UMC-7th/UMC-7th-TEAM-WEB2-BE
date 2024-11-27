package miniproject.web02.service.reviewService;

import miniproject.web02.web.dto.reviewDTO.ReviewRequestDto;
import miniproject.web02.web.dto.reviewDTO.ReviewResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface ReviewService {
    ReviewResponseDTO.ReviewListDTO getLectureReviews(long lectureId, Integer rating, String sortField, Integer page);

    ReviewResponseDTO.ReviewImageListDTO getReviewImages(long lectureId);

    ReviewResponseDTO.ReviewDTO createdReview(ReviewRequestDto requestDto,MultipartFile image);

    void deleteReview(Long reviewId);

}

