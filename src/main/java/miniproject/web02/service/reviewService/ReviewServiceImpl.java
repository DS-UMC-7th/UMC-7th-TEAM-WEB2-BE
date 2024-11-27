package miniproject.web02.service.reviewService;

import lombok.RequiredArgsConstructor;
import miniproject.web02.apiPayLoad.code.status.ErrorStatus;
import miniproject.web02.apiPayLoad.exception.handler.TempHandler;
import miniproject.web02.domain.Lecture;
import miniproject.web02.domain.Review;
import miniproject.web02.domain.ReviewImage;
import miniproject.web02.repository.LectureRepository;
import miniproject.web02.repository.ReviewImageRepository;
import miniproject.web02.repository.ReviewRepository;
import miniproject.web02.service.S3FileStorageService;
import miniproject.web02.web.dto.reviewDTO.ReviewRequestDto;
import miniproject.web02.web.dto.reviewDTO.ReviewResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final LectureRepository lectureRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final S3FileStorageService fileStorageService;


    @Override
    public ReviewResponseDTO.ReviewListDTO getLectureReviews(long lectureId, Integer rating, String sortField, Integer page) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new TempHandler(ErrorStatus.LECTURE_NOT_FOUND));

        Pageable pageable;
        if ("recommend".equals(sortField)) {
            pageable = PageRequest.of(page, 5,
                    Sort.by(Sort.Order.desc("recommend"), Sort.Order.desc("createdAt")));
        } else {
            pageable = PageRequest.of(page, 5, Sort.by(Sort.Direction.DESC, sortField));
        }

        Page<Review> reviews = reviewRepository.findByLecture(lecture, pageable);
        List<Review> reviewList = new ArrayList<>(reviews.getContent());

        if (rating != null) {
            reviewList = reviewList.stream()
                    .filter(review -> review.getRating().equals(rating))
                    .collect(Collectors.toList());
        }

        List<ReviewResponseDTO.ReviewDTO> reviewDTOList = reviewList.stream()
                .map(review -> {
                    return ReviewResponseDTO.ReviewDTO.builder()
                            .reviewId(review.getReviewId())
                            .rating(review.getRating())
                            .studyTime(review.getStudyTime())
                            .likes(review.getLikes())
                            .content(review.getContent())
                            .createdAt(review.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        return ReviewResponseDTO.ReviewListDTO.builder()
                .reviewList(reviewDTOList)
                .listSize(reviewDTOList.size())
                .totalPage(reviews.getTotalPages())
                .totalElements(reviews.getTotalElements())
                .isFirst(reviews.isFirst())
                .isLast(reviews.isLast())
                .build();
    }

    @Override
    public ReviewResponseDTO.ReviewImageListDTO getReviewImages(long lectureId) {
        List<ReviewImage> reviewImageList = reviewImageRepository.findTop3ByReview_Lecture_LectureIDOrderByCreatedAtDesc(lectureId);

        List<ReviewResponseDTO.ReviewImageDTO> reviewImageDTOList = reviewImageList.stream()
                .map(image -> {
                    return ReviewResponseDTO.ReviewImageDTO.builder()
                            .reviewImageId(image.getReviewImageId())
                            .imageUrl(image.getImageUrl())
                            .createdAt(image.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        return ReviewResponseDTO.ReviewImageListDTO.builder()
                .reviewImageList(reviewImageDTOList)
                .build();
    }

    @Override
    public ReviewResponseDTO.ReviewDTO createdReview(ReviewRequestDto requestDto, MultipartFile image) {
        // 강의 확인
        Lecture lecture = lectureRepository.findById(requestDto.getLectureId())
                .orElseThrow(() -> new RuntimeException("강의를 찾을 수 없습니다: " + requestDto.getLectureId()));

        // S3 이미지 업로드
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            // 파일 이름 생성 및 업로드
            String fileName = fileStorageService.generateFileName(image);
            imageUrl = fileStorageService.uploadFile(image, fileName);

        }

        // 리뷰 생성
        Review review = Review.builder()
                .rating(requestDto.getRating())
                .content(requestDto.getContent())
                .studyTime(requestDto.getStudyTime())
                .lecture(lecture)
                .likes(0)
                .build();
        // 리뷰 이미지 리스트 초기화 확인
        if (review.getReviewImageList()==null) {
            review.setReviewImageList(new ArrayList<>());
        }

        Review savedReview = reviewRepository.save(review);

        // 리뷰 이미지 저장
        if (imageUrl != null) {
            ReviewImage reviewImage = ReviewImage.builder()
                    .imageUrl(imageUrl)
                    .review(savedReview)
                    .build();
            savedReview.getReviewImageList().add(reviewImage); //리뷰에 이미지 추가하기
            reviewRepository.save(savedReview); // Review와 연관된 리뷰 이미지 저장
        }

        // 총 별점 업데이트
        updateLectureRating(lecture);

        return ReviewResponseDTO.ReviewDTO.builder()
                .reviewId(savedReview.getReviewId())
                .rating(savedReview.getRating())
                .content(savedReview.getContent())
                .studyTime(savedReview.getStudyTime())
                .imageUrl(imageUrl)
                .lectureId(savedReview.getLecture().getLectureID())
                .createdAt(savedReview.getCreatedAt())
                .build();
    }

    @Override
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new TempHandler(ErrorStatus.REVIEW_NOT_FOUND));

        Lecture lecture = review.getLecture();
        reviewRepository.delete(review);

        updateLectureRating(lecture);
    }

    private void updateLectureRating(Lecture lecture) {
        // 리뷰 목록 가져오기
        List<Review> reviews = reviewRepository.findAllByLecture(lecture);

        // 리뷰가 없을 경우, 평균 별점 0으로 설정
        if (reviews.isEmpty()) {
            lecture.setTotalRating(BigDecimal.ZERO);
            lectureRepository.save(lecture);
            return;
        }

        // 모든 리뷰의 별점을 합산하여 평균을 계산
        BigDecimal averageRating = reviews.stream()
                .map(Review::getRating)  // Review에서 rating을 가져옴
                .reduce(BigDecimal.ZERO, BigDecimal::add)  // 별점 합산
                .divide(BigDecimal.valueOf(reviews.size()), 2, RoundingMode.HALF_UP);  // 평균 계산

        // Lecture의 totalRating 업데이트
        lecture.setTotalRating(averageRating);
        lectureRepository.save(lecture);
    }



}
