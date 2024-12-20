package miniproject.web02.service.rating;

import lombok.RequiredArgsConstructor;
import miniproject.web02.domain.Lecture;
import miniproject.web02.domain.Review;
import miniproject.web02.repository.ReviewSearchRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RatingCommandServiceImpl implements RatingCommandService {

    private final ReviewSearchRepository reviewSearchRepository;

    @Override
    public List<BigDecimal> getRatingList (Lecture lecture){
        List<Review> lectureReviewList = reviewSearchRepository.findAllByLecture(lecture);

        List<BigDecimal> lectureReviewRatingList = lectureReviewList.stream()
                .map(Review::getRating)
                .collect(Collectors.toList());

        return lectureReviewRatingList;
    }
    @Override
    public int[] getRatingCounts(List<BigDecimal>  lectureReviewRatingList){

        int[] ratingCount = new int[5];

        lectureReviewRatingList.stream()
                .forEach(i -> {
                    if (i.compareTo(new BigDecimal("2")) < 0) {
                        ratingCount[0]++;
                    } else if (i.compareTo(new BigDecimal("3")) < 0) {
                        ratingCount[1]++;
                    } else if (i.compareTo(new BigDecimal("4")) < 0) {
                        ratingCount[2]++;
                    } else if (i.compareTo(new BigDecimal("5")) < 0) {
                        ratingCount[3]++;
                    } else {
                        ratingCount[4]++;
                    }
                });
        return ratingCount;
    }
}
