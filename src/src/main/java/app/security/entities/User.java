package app.security.entities;

import app.entities.User_question;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor
@ToString
@Table(name = "a_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private int id;
    @Setter
    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "password")
    private String passWord;

    @Column(name = "questions_answered")
    private int questionsAnswered;

    @Column(name = "username")
    private String username;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user",fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    //kig fetch type og cascade type
    private Set<User_role> user_roles = new HashSet<>();
    public void addUser_role(User_role user_role)
    {
        this.user_roles.add(user_role);
        if (user_role != null)
        {
            user_role.setUser(this);
        }
    }
    public void removeUser_role(User_role user_role)
    {
         if (user_roles.contains(user_role))
        {
            this.user_roles.remove(user_role);
        }
    }



    @OneToMany(mappedBy = "user",fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    //kig fetch type og cascade type
    private Set<User_question> user_question = new HashSet<>();
    public void addUser_question(User_question user_question)
    {
        this.user_question.add(user_question);
        if (user_question != null)
        {
            user_question.setUser(this);
        }
    }

    public User(String email, String passWord, int questionsAnswered) {
        this.email = email;
        this.passWord = passWord;
        this.questionsAnswered = questionsAnswered;
    }

    public User(String username, String passWord, String email) {
        this.username = username;
        this.passWord = BCrypt.hashpw(passWord, BCrypt.gensalt());
        this.email = email;
    }

    public User(int id, String email, String passWord, int questionsAnswered, String username, Set<User_question> user_question) {
        this.id = id;
        this.email = email;
        this.passWord = BCrypt.hashpw(passWord, BCrypt.gensalt());
        this.questionsAnswered = questionsAnswered;
        this.username = username;
        this.user_question = user_question;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id && questionsAnswered == user.questionsAnswered && Objects.equals(email, user.email) && Objects.equals(passWord, user.passWord) && Objects.equals(user_question, user.user_question);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, passWord, questionsAnswered, user_question);
    }

    public boolean verifyPassword(String pw){
        return BCrypt.checkpw(pw, this.passWord);
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
