package app.persistence;

import app.entities.Subject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import lombok.AllArgsConstructor;

import java.util.List;
@AllArgsConstructor
public class SubjectDAO implements IDAO<Subject, Integer>{
    private EntityManagerFactory emf;
    @Override
    public Subject create(Subject obj) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(obj);
        em.getTransaction().commit();
        em.close();
        return obj;
    }

    @Override
    public Subject read(Integer id) {
        EntityManager em = emf.createEntityManager();
        Subject obj = em.find(Subject.class, id);
        em.close();
        return obj;
    }

    @Override
    public List<Subject> readAll() {
        EntityManager em = emf.createEntityManager();

        TypedQuery<Subject> query = em.createQuery("SELECT p FROM Subject p", Subject.class);
        List<Subject> results = query.getResultList();

        em.close();
        return  results;
    }

    @Override
    public Subject update(Subject object) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Subject updatedObject = em.merge(object);
        em.getTransaction().commit();
        em.close();
        return updatedObject;
    }

    @Override
    public void delete(Integer id) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Subject obj = em.find(Subject.class, id);
        if (obj != null)
        {
            em.remove(obj);
        }
        em.getTransaction().commit();
        em.close();
    }
    public boolean existsByName(String name) {
        EntityManager em = emf.createEntityManager();

        try {
            Long count = em.createQuery(
                            "SELECT COUNT(s) FROM Subject s WHERE s.name = :name",
                            Long.class
                    )
                    .setParameter("name", name)
                    .getSingleResult();

            return count > 0;

        } finally {
            em.close();
        }
    }
    public Subject findByName(String name){
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT d FROM Subject d WHERE d.name = :name", Subject.class)
                    .setParameter("name", name)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        }
    }
}
