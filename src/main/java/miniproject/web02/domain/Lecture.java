package miniproject.web02.domain;

import jakarta.persistence.*;
import lombok.*;
import miniproject.web02.domain.common.BaseEntity;
import miniproject.web02.domain.enums.Category;
import miniproject.web02.domain.enums.Level;
import miniproject.web02.domain.enums.Platform;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Lecture extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lectureID;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 10)
    private String teacher;

    @Enumerated(EnumType.STRING)
    private Platform platform;

    @Column(precision = 3, scale = 2)
    private BigDecimal totalRating;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Enumerated(EnumType.STRING)
    private Level level;

    @OneToMany(mappedBy = "lecture", cascade = CascadeType.ALL)
    private List<LectureImage> lectureImageList = new ArrayList<>();

    @OneToMany(mappedBy = "lecture", cascade = CascadeType.ALL)
    private List<Review> reviewList = new ArrayList<>();

}
