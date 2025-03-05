package haru.harudrawer.domain.diary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import haru.harudrawer.domain.diary.entity.Diary;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {

    Optional<List<Diary>> findAllByUserUserId(Long userId);
}
