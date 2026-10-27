package app.entities;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;


@Entity
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id", nullable = false)
    private Integer id;

    @Column(name = "name", length = 100)
    private String name;

    public Course(String name) {
        this.name = name;
    }

    public Course() {

    }

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
// Getters and setters
}