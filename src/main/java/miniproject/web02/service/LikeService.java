package miniproject.web02.service;

import lombok.RequiredArgsConstructor;
import miniproject.web02.domain.Review;
import miniproject.web02.repository.ReviewRepository;
import miniproject.web02.web.dto.reviewDTO.ReviewResponseDTO;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final ReviewRepository reviewRepository;

    public ReviewResponseDTO.ReviewDTO likeReview(Long reviewId) {
        // 리뷰 확인
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다: " + reviewId));

        // 좋아요 수 증가
        review.setLikes(review.getLikes() + 1);

        Review updatedReview = reviewRepository.save(review);

        return ReviewResponseDTO.ReviewDTO.builder()
                .reviewId(updatedReview.getReviewId())
                .rating(updatedReview.getRating())
                .content(updatedReview.getContent())
                .studyTime(updatedReview.getStudyTime())
                .likes(updatedReview.getLikes())
                .imageUrl(updatedReview.getReviewImageList().isEmpty() ? null : updatedReview.getReviewImageList().get(0).getImageUrl())
                .lectureId(updatedReview.getLecture().getLectureID())
                .createdAt(updatedReview.getCreatedAt())
                .build();
    }
}
