package app.persistence;

import app.entities.Question;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import lombok.AllArgsConstructor;

import java.util.List;
@AllArgsConstructor
public class QuestionDAO implements IDAO<Question,Integer>{
    private EntityManagerFactory emf;
    @Override
    public Question create(Question obj) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(obj);
        em.getTransaction().commit();
        em.close();
        return obj;
    }

    @Override
    public Question read(Integer id) {
        EntityManager em = emf.createEntityManager();
        Question obj = em.find(Question.class, id);
        em.close();
        return obj;
    }

    @Override
    public List<Question> readAll() {
        EntityManager em = emf.createEntityManager();

        TypedQuery<Question> query = em.createQuery("SELECT p FROM Question p", Question.class);
        List<Question> results = query.getResultList();

        em.close();
        return  results;
    }

    @Override
    public Question update(Question object) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Question updatedObject = em.merge(object);
        em.getTransaction().commit();
        em.close();
        return updatedObject;
    }


    @Override
    public void delete(Integer id) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Question obj = em.find(Question.class, id);
        if (obj != null)
        {
            em.remove(obj);
        }
        em.getTransaction().commit();
        em.close();
    }
    public List<Question> readAllFromOneSubject(int subjectId) {
        EntityManager em = emf.createEntityManager();

        TypedQuery<Question> query = em.createQuery(
                "SELECT p FROM Question p WHERE p.subject.id = :subjectId",
                Question.class
        );

        query.setParameter("subjectId", subjectId);

        List<Question> results = query.getResultList();

        em.close();
        return results;
    }
    public boolean existsByName(String name) {
        EntityManager em = emf.createEntityManager();

        try {
            Long count = em.createQuery(
                            "SELECT COUNT(q) FROM Question q WHERE q.name = :name",
                            Long.class
                    )
                    .setParameter("name", name)
                    .getSingleResult();

            return count > 0;

        } finally {
            em.close();
        }
    }
}
