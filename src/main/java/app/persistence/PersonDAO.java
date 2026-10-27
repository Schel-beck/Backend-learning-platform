package app.persistence;
import app.config.HibernateConfig;
import app.entities.Course;
import app.entities.Person;
import jakarta.persistence.*;

import java.util.List;

public class PersonDAO
{

    private EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

    public Person createPerson(Person person)
    {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(person);
        em.getTransaction().commit();
        em.close();
        return person;
    }
    public Person update(Person person)
    {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Person updatedUnicorn = em.merge(person);
        em.getTransaction().commit();
        em.close();
        return updatedUnicorn;
    }
    public Person findById(int id)
    {
        EntityManager em = emf.createEntityManager();
        Person foundUnicorn = em.find(Person.class, id);
        em.close();
        return foundUnicorn;
    }
    public void delete(int id)
    {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Person person = findById(id);
        if (person != null)
        {
            em.remove(person);
        }
        em.getTransaction().commit();
        em.close();
    }
    public List<Person> getAllPersons() {
        EntityManager em = emf.createEntityManager();

        TypedQuery<Person> query = em.createQuery("SELECT p FROM Person p", Person.class);
        List<Person> results = query.getResultList();

        em.close();
        return  results;

    }


}