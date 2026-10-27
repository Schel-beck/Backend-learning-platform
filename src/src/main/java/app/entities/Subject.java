package app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subject_id", nullable = false)
    private int id;
    @Setter
    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "number_of_questions")
    private int numberOfQuestions;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Subject(String name, int numberOfQuestions) {
        this.name = name;
        this.numberOfQuestions = numberOfQuestions;
    }
    @OneToMany(mappedBy = "subject",fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    //kig fetch type og cascade type
    private Set<Question> questions = new HashSet<>();
    public void addQuestion(Question question)
    {
        this.questions.add(question);
        if (question != null)
        {
            question.setSubject(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Subject subject = (Subject) o;
        return id == subject.id && numberOfQuestions == subject.numberOfQuestions && Objects.equals(name, subject.name) && Objects.equals(questions, subject.questions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, numberOfQuestions, questions);
    }

    public Subject(String name) {
        this.name = name;
    }

    public Subject(int id, String name, int numberOfQuestions) {
        this.id = id;
        this.name = name;
        this.numberOfQuestions = numberOfQuestions;
    }
    @PrePersist
    protected  void onCreate(){
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    @PreUpdate
    protected  void onUpdate(){
        this.updatedAt = LocalDateTime.now();
    }
}
