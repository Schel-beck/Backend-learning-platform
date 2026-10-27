package app.persistence;

import app.entities.User_question;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import lombok.AllArgsConstructor;

import java.util.List;
@AllArgsConstructor
public class User_questionDAO implements IDAO<User_question, Integer>{
    private EntityManagerFactory emf;
    @Override
    public User_question create(User_question obj) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(obj);
        em.getTransaction().commit();
        em.close();
        return obj;
    }

    @Override
    public User_question read(Integer id) {
        EntityManager em = emf.createEntityManager();
        User_question obj = em.find(User_question.class, id);
        em.close();
        return obj;
    }

    @Override
    public List<User_question> readAll() {
        EntityManager em = emf.createEntityManager();

        TypedQuery<User_question> query = em.createQuery("SELECT p FROM User_question p", User_question.class);
        List<User_question> results = query.getResultList();

        em.close();
        return  results;
    }

    @Override
    public User_question update(User_question object) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        User_question updatedObject = em.merge(object);
        em.getTransaction().commit();
        em.close();
        return updatedObject;
    }

    @Override
    public void delete(Integer id) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        User_question obj = em.find(User_question.class, id);
        if (obj != null)
        {
            em.remove(obj);
        }
        em.getTransaction().commit();
        em.close();
    }
}
