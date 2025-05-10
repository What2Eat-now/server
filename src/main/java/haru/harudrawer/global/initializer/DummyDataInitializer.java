package haru.harudrawer.global.initializer;

import haru.harudrawer.domain.auth.entity.Provider;
import haru.harudrawer.domain.auth.entity.Role;
import haru.harudrawer.domain.auth.entity.User;
import haru.harudrawer.domain.auth.repository.AuthRepository;
import haru.harudrawer.domain.diary.entity.Diary;
import haru.harudrawer.domain.diary.repository.DiaryRepository;
import haru.harudrawer.global.security.service.EncryptService;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class DummyDataInitializer {

    private final AuthRepository authRepository;
    private final DiaryRepository diaryRepository;
    private final GeometryFactory geometryFactory;
    private final PasswordEncoder passwordEncoder;
    private final EncryptService encryptService;

//    @PostConstruct
    public void userDataInitializer() {

        List<User> userBuffer = new ArrayList<>();
        List<Diary> diaryBuffer = new ArrayList<>();

        for (long userIds = 5L; userIds < 110L; userIds++) {
            final long finalUserId = userIds;

            User user = User.builder()
                    .userEmail("user" + finalUserId + "@dummy.com")
                    .password(passwordEncoder.encode("Testtest@"))
                    .role(Role.USER)
                    .nickName("dummyUser" + finalUserId)
                    .provider(Provider.LOCAL)
                    .build();

            userBuffer.add(user);

            LocalDate baseDate = LocalDate.now().minusDays(60);

            for (int i = 0; i < 1000; i++) {
                LocalDate visitDate = baseDate.plusDays(i);
                Point location = generateRandomPoint();

                diaryBuffer.add(Diary.builder()
                        .user(user)
                        .rate(1 + new Random().nextInt(5))
                        .title("dummyTitle" + i)
                        .content(encryptService.encrypt("dummyContent" + i))
                        .location(location)
                        .visitDate(visitDate)
                        .markerNumber(0 + new Random().nextInt(4))
                        .placeName("dummyPlace" + i)
                        .build());
            }
        }
        authRepository.saveAll(userBuffer);
        diaryRepository.saveAll(diaryBuffer);

        userBuffer.clear();
        diaryBuffer.clear();

    }


    private Point generateRandomPoint() {
        // 서울 위도: 37.4 ~ 37.7, 경도: 126.8 ~ 127.1 범위 내에서 랜덤 생성
        double latitude = 37.4 + Math.random() * 0.3;   // 위도 (Y)
        double longitude = 126.8 + Math.random() * 0.3; // 경도 (X)
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }
}
