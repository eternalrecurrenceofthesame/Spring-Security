package workoutresourceserver.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Table(name = "spring_workout")
@Entity
public class Workout {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String user;

    private LocalDateTime start;

    private LocalDateTime end;

    private int difficulty;
}
