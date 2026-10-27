package app.service;

import app.config.HibernateConfig;
import app.dto.QuestionDTO;
import app.dto.SubjectsWithQuestionsDTO;
import app.entities.Question;
import app.entities.Subject;
import app.persistence.QuestionDAO;
import app.persistence.SubjectDAO;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class Importer {

    public void importIntoDatabase() throws Exception {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        Fetcher fetcher = new Fetcher();
        SubjectDAO subjectDAO = new SubjectDAO(emf);
        QuestionDAO questionDAO = new QuestionDAO(emf);
        if (subjectDAO.readAll().size() > 12) {
            System.out.println(
                    "Database already contains 14 or more subjects. Skipping import."
            );
            return;
        }
        List<SubjectsWithQuestionsDTO> subjectsWithQuestionsDTOS = fetcher.fetchSubjectsAndQuestions();


        for(int i = 0;i <subjectsWithQuestionsDTOS.size(); i++){
            if (subjectDAO.readAll().size() > 14) {
                System.out.println(
                        "Database already contains 14 or more subjects. Skipping import."
                );
                return;
            }
            Subject subject =  new Subject(
                    subjectsWithQuestionsDTOS.get(i).getSubject().getName(),
                    subjectsWithQuestionsDTOS.get(i).getSubject().getNumberOfQuestions()
            );
            if(!subjectDAO.existsByName(subject.getName())){
                subjectDAO.create(subject);
            }
            else{
                subject = subjectDAO.findByName(subject.getName());
            }

            for(int j =0; j<subjectsWithQuestionsDTOS.get(i).getQuestions().size(); j++){
                Question question = new Question(
                        subjectsWithQuestionsDTOS.get(i).getQuestions().get(j).getName()
                );
                question.setSubject(subject);
                if(!questionDAO.existsByName(question.getName())){
                    questionDAO.create(question);
                }

            }

        }
    }


}
