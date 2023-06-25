package workoutresourceserver.controller;

import workoutresourceserver.entity.Workout;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import workoutresourceserver.service.WorkoutService;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/workout")
public class WorkoutController {

    private WorkoutService workoutService;

    @GetMapping("/test")
    public String test(Authentication a){
        return a.getAuthorities().stream().toString();
    }

    @GetMapping("/")
    public List<Workout> findAll(){
        return workoutService.findWorkouts();
    }

    @PostMapping("/")
    public void add(@RequestBody Workout workout){
        workoutService.saveWorkout(workout);
    }

    /**
     * 컨트롤러 계층에서는 관리자만 DELETE 엔드포인트에 접근할 수 있다.
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id){
        workoutService.deleteWorkout(id);
    }
}
