package app.security.persistence;

import app.dto.CreateUserDTO;
import app.persistence.RoleDAO;
import app.persistence.User_roleDAO;
import app.security.entities.Role;
import app.security.entities.User;
import app.security.entities.User_role;
import app.service.APIReader;
import dk.bugelhartmann.UserDTO;
import io.javalin.validation.ValidationException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AllArgsConstructor
public class SecurityDAO {
    private EntityManagerFactory emf;
    private static final Logger log = LoggerFactory.getLogger(SecurityDAO.class);

    public User getVerifiedUser(String username, String password) throws ValidationException {
        EntityManager em = emf.createEntityManager();

        User user = em.createQuery("SELECT p FROM User p WHERE p.username = :username",
                        User.class)
                .setParameter("username", username)
                .getSingleResult();


        if (!user.verifyPassword(password)) {
            log.warn("password dosent match");
            return null;
        }
        em.close();
        return user;
    }

    public User createUser(CreateUserDTO createUserDTO) {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            User user = new User(createUserDTO.getUsername(), createUserDTO.getPassword(), createUserDTO.getEmail());

            User existingUser = em.createQuery(
                            "SELECT u FROM User u WHERE u.username = :username",
                            User.class
                    )
                    .setParameter("username", createUserDTO.getUsername())
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (existingUser != null) {
                throw new EntityExistsException(
                        "User already exists"
                );
            }

            Role role = em.createQuery("SELECT r FROM Role r WHERE r.role = :role", Role.class
                    )
                    .setParameter("role", "anyone")
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (role == null) {
                role = new Role("anyone");
                em.persist(role);
            }
            em.persist(user);

            User_role userRole = new User_role(user, role);
            em.persist(userRole);

            user.addUser_role(userRole);

            em.getTransaction().commit();

            return user;

        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;

        } finally {
            em.close();
        }
    }

    public Role createRole(String roleName) {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            Role existingRole = em.createQuery(
                            "SELECT r FROM Role r WHERE r.role = :role",
                            Role.class
                    )
                    .setParameter("role", roleName)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (existingRole != null) {
                throw new EntityExistsException(
                        "Role already exists: " + roleName
                );
            }

            Role newRole = new Role(roleName);
            em.persist(newRole);

            em.getTransaction().commit();

            return newRole;

        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;

        } finally {
            em.close();
        }
    }

    public User addUserRole(String username, String roleName) {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            User user = em.find(User.class, username);

            if (user == null) {
                throw new EntityNotFoundException(
                        "User not found: " + username
                );
            }

            Role role = em.createQuery(
                            "SELECT r FROM Role r WHERE r.role = :role",
                            Role.class
                    )
                    .setParameter("role", roleName)
                    .getResultStream()
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Role not found: " + roleName
                    ));

            // Avoid adding the same role twice
            boolean alreadyHasRole = user.getUser_roles().stream()
                    .anyMatch(ur ->
                            ur.getRole().getRole().equals(roleName)
                    );

            if (alreadyHasRole) {
                em.getTransaction().commit();
                return user;
            }

            User_role userRole = new User_role(user, role);

            em.persist(userRole);
            user.addUser_role(userRole);

            em.getTransaction().commit();

            return user;

        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;

        } finally {
            em.close();
        }
    }

    public User addRole(UserDTO userDTO, String newRole) {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            User existingUser = em.createQuery(
                            "SELECT u FROM User u WHERE u.username = :username",
                            User.class
                    )
                    .setParameter("username", userDTO.getUsername())
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (existingUser == null) {
                throw new EntityExistsException(
                        "User dosent exist"
                );
            }

            Role role = em.createQuery("SELECT r FROM Role r WHERE r.role = :role", Role.class
                    )
                    .setParameter("role", newRole)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (role == null) {
                role = new Role(newRole);
                em.persist(role);
            }
            existingUser.addUser_role(new User_role(existingUser, role));
            //em.merge(user);
            em.getTransaction().commit();
            return existingUser;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

