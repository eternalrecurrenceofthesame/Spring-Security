package workoutresourceserver.service;

import workoutresourceserver.entity.Workout;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import workoutresourceserver.repository.WorkoutRepository;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class WorkoutService {

    private WorkoutRepository workoutRepository;

    /**
     * 서비스 계층에서는 인증된 사용자 본인의 운동 기록만 추가할수 있게 설계한다.
     */
    @PreAuthorize("#workout.user == authentication.name")
    public void saveWorkout(Workout workout){
        workoutRepository.save(workout);
    }

    public List<Workout> findWorkouts(){
        return workoutRepository.findAllByUser();
    }

    public void deleteWorkout(Integer id){
        workoutRepository.deleteById(id);
    }

}
