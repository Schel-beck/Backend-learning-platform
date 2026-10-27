package app.persistence;

import app.security.entities.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import lombok.AllArgsConstructor;

import java.util.List;
@AllArgsConstructor
public class RoleDAO implements IDAO<Role, Integer>{
    private EntityManagerFactory emf;
    @Override
    public Role create(Role obj) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(obj);
        em.getTransaction().commit();
        em.close();
        return obj;
    }

    @Override
    public Role read(Integer id) {
        EntityManager em = emf.createEntityManager();
        Role obj = em.find(Role.class, id);
        em.close();
        return obj;
    }

    @Override
    public List<Role> readAll() {
        EntityManager em = emf.createEntityManager();

        TypedQuery<Role> query = em.createQuery("SELECT p FROM Role p", Role.class);
        List<Role> results = query.getResultList();

        em.close();
        return  results;
    }

    @Override
    public Role update(Role object) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Role updatedObject = em.merge(object);
        em.getTransaction().commit();
        em.close();
        return updatedObject;
    }

    @Override
    public void delete(Integer id) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Role obj = em.find(Role.class, id);
        if (obj != null) {
            em.remove(obj);
        }
        em.getTransaction().commit();
        em.close();
    }
    public Role findByRole(String role) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT d FROM Role d WHERE d.role = :role", Role.class)
                    .setParameter("role", role)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        }
    }
}
