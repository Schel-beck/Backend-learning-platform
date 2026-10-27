package app.routes;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.entities.Question;
import app.persistence.QuestionDAO;
import app.testUtils.Populator;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QuestionRouteTest {

    private static Javalin app;

    private static final EntityManagerFactory emf =
        HibernateTestConfig.getEntityManagerFactory();

    private static QuestionDAO questionDAO;

    private Map<String, Question> questionMap;


    @BeforeAll
    static void init() {
        app = ApplicationConfig.startServer(7070,emf);

        RestAssured.baseURI =
            "http://localhost:7070";
    }


    @BeforeEach
    void setUp() {
        questionDAO = new QuestionDAO(emf);

        questionMap = Populator.questionPopulate(emf);
    }


    @AfterAll
    static void shutDown() {
        ApplicationConfig.stopServer(app);
    }


    @Test
    void getAllQuestions() {

        List<Question> expected =
            questionDAO.readAll();

        List<Question> actual =
            given()
                .when()
                .get("/questions")
                .then()
                .statusCode(200)
                .body("size()", is(3))
                .body("name",
                    containsInAnyOrder(
                        "Who was the first king of Denmark?",
                        "What is the capital of Denmark?",
                        "When did Denmark become a kingdom?"
                    ))
                .extract()
                .jsonPath()
                .getList("", Question.class);

        assertEquals(expected.size(), actual.size());
    }


    @Test
    void getQuestionById() {

        Question question =
            questionMap.get("question1");

        given()
            .when()
            .get("/questions/" + question.getId())
            .then()
            .statusCode(200)
            .body("name",
                is("Who was the first king of Denmark?"));
    }


    @Test
    void getQuestionByIdNotFound() {

        given()
            .when()
            .get("/questions/9999")
            .then()
            .statusCode(404);
    }


    @Test
    void createQuestion() {

        // questionPopulate creates subject with id 1
        given()
            .contentType("application/json")
            .body("""
                    {
                        "question": "What is an integer in Java?"                  
                    }
                    """)
            .when()
            .post("/subjects/1/questions")
            .then()
            .log().all()
            .statusCode(201)
            .body("question",
                is("What is an integer in Java?"));
    }


    @Test
    void updateQuestion() {

        Question question =
            questionMap.get("question1");

        given()
            .contentType("application/json")
            .body("""
                    {
                        "question": "Who was the first king of Denmark?"
                    }
                    """)
            .when()
            .put("/questions/" + question.getId())
            .then()
            .statusCode(200)
            .body("name",
                is("Who was the first king of Denmark?"));
    }


    @Test
    void updateQuestionNotFound() {

        given()
            .contentType("application/json")
            .body("""
                    {
                        "question": "New question"
                    }
                    """)
            .when()
            .put("/questions/9999")
            .then()
            .statusCode(404);
    }


    @Test
    void deleteQuestion() {

        Question question =
            questionMap.get("question1");

        given()
            .when()
            .delete("/questions/" + question.getId())
            .then()
            .statusCode(204);
    }


    @Test
    void deleteQuestionNotFound() {

        given()
            .when()
            .delete("/questions/9999")
            .then()
            .statusCode(404);
    }
}