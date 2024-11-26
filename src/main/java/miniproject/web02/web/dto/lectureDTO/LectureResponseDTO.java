package miniproject.web02.web.dto.lectureDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class LectureResponseDTO {
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LectureDTO {
        Long lectureId;
        String lectureName;
        String platform;
        String teacher;
        String imageUrl;
    }
}
