package workoutresourceserver.repository;

import workoutresourceserver.entity.Workout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WorkoutRepository extends JpaRepository<Workout, Integer> {

    /**
     * 보안 컨텍스트에 저장된 인증된 사용자의 운동 기록만 가져오는 JPQL
     */
    @Query("select w from Workout w where w.user = ?#{authentication.name}")
    List<Workout> findAllByUser();

}
