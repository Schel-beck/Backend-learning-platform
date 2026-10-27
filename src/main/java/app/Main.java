package app;

import app.entities.Course;
import app.entities.Person;
import app.persistence.CourseDAO;
import app.persistence.PersonDAO;

import java.util.ArrayList;
import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        PersonDAO personDAO = new PersonDAO();
        Person person = new Person("Harry",12);
        personDAO.createPerson(person);
        //personDAO.update(person);

        List<Person> persons = new ArrayList<>();
        persons = personDAO.getAllPersons();
        for(Person p: persons){
            System.out.println(p);
        }


        CourseDAO courseDAO = new CourseDAO();
        Course course = new Course("math");
        courseDAO.createCourse(course);
        List<Course> courses = new ArrayList<>();
        courses = courseDAO.getAllCourses();
        for(Course c: courses){
            System.out.println(c);
        }
        //courseDAO.update(course);


    }
}