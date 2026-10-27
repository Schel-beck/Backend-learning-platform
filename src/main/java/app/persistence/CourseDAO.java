package app.persistence;

import app.config.HibernateConfig;
import app.entities.Course;
import app.entities.Person;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.awt.*;
import java.util.List;

public class CourseDAO {
    private EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    public Course createCourse(Course course)
    {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(course);
        em.getTransaction().commit();
        em.close();
        return course;
    }
    public Course update(Course course)
    {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Course updatedUnicorn = em.merge(course);
        em.getTransaction().commit();
        em.close();
        return updatedUnicorn;
    }
    public Course findById(int id)
    {
        EntityManager em = emf.createEntityManager();
        Course foundUnicorn = em.find(Course.class, id);
        em.close();
        return foundUnicorn;
    }
    public void delete(int id)
    {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Course course = findById(id);
        if (course != null)
        {
            em.remove(course);
        }
        em.getTransaction().commit();
        em.close();
    }
    public List<Course> getAllCourses() {
        EntityManager em = emf.createEntityManager();

        TypedQuery<Course> query = em.createQuery("SELECT c FROM Course c", Course.class);
        List<Course> results = query.getResultList();

        em.close();
        return  results;

    }


}
