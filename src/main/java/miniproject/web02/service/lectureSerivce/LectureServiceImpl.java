package miniproject.web02.service.lectureSerivce;

import lombok.RequiredArgsConstructor;
import miniproject.web02.apiPayLoad.code.status.ErrorStatus;
import miniproject.web02.apiPayLoad.exception.handler.TempHandler;
import miniproject.web02.domain.Lecture;
import miniproject.web02.domain.LectureImage;
import miniproject.web02.repository.LectureImageRepository;
import miniproject.web02.repository.LectureRepository;
import miniproject.web02.service.S3FileStorageService;
import miniproject.web02.web.dto.lectureDTO.LectureRequestDTO;
import miniproject.web02.web.dto.lectureDTO.LectureResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class LectureServiceImpl implements LectureService {
    private final LectureRepository lectureRepository;
    private final LectureImageRepository lectureImageRepository;
    private final S3FileStorageService fileStorageService;


    @Override
    public LectureResponseDTO.LectureDTO getLecture(Long lectureId) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new TempHandler(ErrorStatus.LECTURE_NOT_FOUND));

        String imageUrl = lectureImageRepository.findFirstByLecture(lecture)
                .map(LectureImage::getImageUrl)
                .orElse(null);

        return LectureResponseDTO.LectureDTO.builder()
                .lectureId(lecture.getLectureID())
                .lectureName(lecture.getName())
                .platform(lecture.getPlatform().toString())
                .teacher(lecture.getTeacher())
                .imageUrl(imageUrl)
                .build();
    }

    @Override
    public LectureResponseDTO.LectureDTO createLecture(LectureRequestDTO lectureRequestDTO, MultipartFile image) {
        String imageUrl = null;

        // 이미지 파일이 제공된 경우 S3에 업로드
        if (image != null && !image.isEmpty()) {
            // 파일 이름 생성 및 S3에 업로드
            String fileName = fileStorageService.generateFileName(image);
            imageUrl = fileStorageService.uploadFile(image, fileName);
        }

        // Lecture 엔티티 생성
        Lecture lecture = Lecture.builder()
                .name(lectureRequestDTO.getName())
                .teacher(lectureRequestDTO.getTeacher())
                .platform(lectureRequestDTO.getPlatform())
                .category(lectureRequestDTO.getCategory())
                .level(lectureRequestDTO.getLevel())
                .build();

        // 강의 저장
        Lecture savedLecture = lectureRepository.save(lecture);

        // 이미지 URL이 존재하면, LectureImage로 저장
        if (imageUrl != null) {
            LectureImage lectureImage = LectureImage.builder()
                    .imageUrl(imageUrl)
                    .lecture(savedLecture)  // Lecture와 연관 설정
                    .build();
            lectureImageRepository.save(lectureImage);  // 이미지 저장
        }

        // 강의 정보를 DTO로 반환
        return LectureResponseDTO.LectureDTO.builder()
                .lectureId(savedLecture.getLectureID())
                .lectureName(savedLecture.getName())
                .platform(savedLecture.getPlatform().toString())
                .teacher(savedLecture.getTeacher())
                .category(savedLecture.getCategory().toString())
                .totalRating(savedLecture.getTotalRating())
                .imageUrl(imageUrl)  // 이미지 URL 반환
                .build();
    }
}
