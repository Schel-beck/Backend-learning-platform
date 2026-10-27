package app.persistence;

import app.security.entities.User_role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import lombok.AllArgsConstructor;

import java.util.List;
@AllArgsConstructor
public class User_roleDAO implements IDAO<User_role, Integer>{
    private EntityManagerFactory emf;
    @Override
    public User_role create(User_role obj) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(obj);
        em.getTransaction().commit();
        em.close();
        return obj;
    }

    @Override
    public User_role read(Integer id) {
        EntityManager em = emf.createEntityManager();
        User_role obj = em.find(User_role.class, id);
        em.close();
        return obj;
    }

    @Override
    public List<User_role> readAll() {
        EntityManager em = emf.createEntityManager();

        TypedQuery<User_role> query = em.createQuery("SELECT p FROM User_role p", User_role.class);
        List<User_role> results = query.getResultList();

        em.close();
        return results;
    }

    @Override
    public User_role update(User_role object) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        User_role updatedObject = em.merge(object);
        em.getTransaction().commit();
        em.close();
        return updatedObject;
    }

    @Override
    public void delete(Integer id) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        User_role obj = em.find(User_role.class, id);
        if (obj != null) {
            em.remove(obj);
        }
        em.getTransaction().commit();
        em.close();
    }

}
