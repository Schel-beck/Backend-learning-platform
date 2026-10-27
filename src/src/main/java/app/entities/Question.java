package app.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor
@ToString
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id", nullable = false)
    private int id;

    @Setter
    @Column(name = "name", length = 1000)
    private String name;

    @Setter
    @ManyToOne
    @ToString.Exclude
    @JsonIgnore
    private Subject subject;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;




    public Question(String name, Subject subject) {
        this.name = name;
        this.subject = subject;
    }

    @OneToMany(mappedBy = "question", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    //kig fetch type og cascade type
    private Set<User_question> user_question = new HashSet<>();



    public void addUser_question(User_question user_question) {
        this.user_question.add(user_question);
        if (user_question != null) {
            user_question.setQuestion(this);
        }
    }

    public Question(String name) {
        this.name = name;
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
