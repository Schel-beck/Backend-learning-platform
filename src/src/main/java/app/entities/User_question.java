package app.entities;

import app.security.entities.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@ToString
public class User_question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_question_id", nullable = false)
    private int id;
    @Setter
    @Column(name = "is_answered", length = 100)
    private boolean isAnswered;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    @Setter
    @ManyToOne
    @ToString.Exclude
    private User user;

    @Setter
    @ManyToOne
    @ToString.Exclude
    private Question question;
    public User_question(User user, Question question, boolean isAnswered){
        this.user = user;
        this.question = question;
        this.isAnswered = isAnswered;
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
