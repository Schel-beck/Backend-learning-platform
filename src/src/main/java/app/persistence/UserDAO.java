package app.persistence;

import app.security.entities.User;
import app.security.entities.User_role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import lombok.AllArgsConstructor;

import java.util.List;
@AllArgsConstructor
public class UserDAO implements IDAO<User, Integer> {
    private EntityManagerFactory emf;

    @Override
    public User create(User obj) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(obj);
        em.getTransaction().commit();
        em.close();
        return obj;
    }

    @Override
    public User read(Integer id) {
        EntityManager em = emf.createEntityManager();
        User obj = em.find(User.class, id);
        em.close();
        return obj;
    }

    @Override
    public List<User> readAll() {
        EntityManager em = emf.createEntityManager();

        TypedQuery<User> query = em.createQuery("SELECT p FROM User p", User.class);
        List<User> results = query.getResultList();

        em.close();
        return results;
    }

    @Override
    public User update(User object) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        User updatedObject = em.merge(object);
        em.getTransaction().commit();
        em.close();
        return updatedObject;
    }

    @Override
    public void delete(Integer id) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        User obj = em.find(User.class, id);
        if (obj != null) {
            em.remove(obj);
        }
        em.getTransaction().commit();
        em.close();
    }


}

