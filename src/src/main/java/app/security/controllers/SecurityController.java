package app.security.controllers;

import app.config.HibernateConfig;
import app.dto.CreateUserDTO;
import app.dto.UserDTO;
import app.security.entities.User;
import app.exceptions.ApiException;
import app.security.exceptions.NotAuthorizedException;
import app.security.persistence.SecurityDAO;
import app.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.bugelhartmann.ITokenSecurity;
import dk.bugelhartmann.TokenSecurity;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.security.RouteRole;
import io.javalin.validation.ValidationException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Set;
import java.util.stream.Collectors;


public class SecurityController implements ISecurityController {
    EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    ObjectMapper objectMapper = new ObjectMapper();
    ITokenSecurity tokenSecurity = new TokenSecurity();
    SecurityDAO securityDAO = new SecurityDAO(emf);
    private static final Logger log = LoggerFactory.getLogger(SecurityController.class);

    /**
     * Checks the database for a user with the given username and password, and will log in if the user is found
     *
     * @return is a Handler which is processing incoming HTTP requests and generates responses
     */
    @Override
    public Handler login() {
        return (ctx) -> {
            ObjectNode returnObject = objectMapper.createObjectNode(); // for sending json messages back to the client
            try {
                UserDTO userDTO = ctx.bodyAsClass(UserDTO.class);
                User verifiedUser = securityDAO.getVerifiedUser(userDTO.getUsername(), userDTO.getPassword());
                if (verifiedUser == null) {
                    ctx.status(401);
                    ctx.json(returnObject.put("msg", "password does not match"));
                }
                UserDTO verifiedUserDto = new UserDTO(verifiedUser.getUsername(), verifiedUser.getPassWord());
                String token = createToken(verifiedUserDto);
                ctx.attribute("user", verifiedUserDto);
                ctx.status(200).json(returnObject
                        .put("token", token)
                        .put("username", verifiedUser.getUsername()));

            } catch (EntityNotFoundException | ValidationException e) {
                ctx.status(401);

                log.info(e.getMessage());
                ctx.json(returnObject.put("msg", e.getMessage()));
            }
        };
    }

    /**
     * Creates and registers a user in the database if the user is unique.
     *
     * @return is a Handler which is processing incoming HTTP requests and generates responses
     */

    @Override
    public Handler register() {
        return (ctx) -> {
            ObjectNode returnObject = objectMapper.createObjectNode();

            try {
                CreateUserDTO userInput = ctx.bodyAsClass(CreateUserDTO.class);
                String error = validateUser(userInput.getUsername(), userInput.getEmail(), userInput.getPassword());
                if (!error.isEmpty()) {
                    ctx.attribute("msg", error);
                    throw new RuntimeException(error);
                } else {
                    User created = securityDAO.createUser(userInput);
                    UserDTO userDTO = new UserDTO(created.getUsername(), created.getPassWord());

                    String token = createToken(userDTO);
                    ctx.status(HttpStatus.CREATED).json(returnObject
                            .put("token", token)
                            .put("username", created.getUsername()));
                }
            } catch (EntityExistsException e) {

                ctx.status(HttpStatus.UNPROCESSABLE_CONTENT);

                ctx.json(returnObject.put("msg", "User already exists"));
            } catch (Exception e) {
                // Temporary debugging: print the actual error in IntelliJ
                e.printStackTrace();

                ctx.status(500).json(
                        returnObject.put("msg", "Registration failed")
                );
            }
        };
    }
    /**
     * Authenticates the user
     *
     * @return is a Handler which is processing incoming HTTP requests and generates responses
     */

    @Override
    public Handler authenticate() {

        ObjectNode returnObject = objectMapper.createObjectNode();
        return (ctx) -> {
            // This is a preflight request => OK
            if (ctx.method().toString().equals("OPTIONS")) {
                ctx.status(200);
                return;
            }
            String header = ctx.header("Authorization");
            if (header == null) {
                throw new UnauthorizedResponse("Authorization header missing");
            }

            String[] headerParts = header.split(" ");
            if (headerParts.length != 2) {
                throw new UnauthorizedResponse("Authorization header malformed");
            }

            String token = headerParts[1];
            UserDTO verifiedTokenUser = verifyToken(token);

            if (verifiedTokenUser == null) {
                throw new UnauthorizedResponse("Invalid User or Token");
            }
            log.info("User verified: " + verifiedTokenUser);
            ctx.attribute("user", verifiedTokenUser);
        };
    }
    /**
     * Creates and registers a user in the database if the user is unique.
     *
     * @param user is the user thats being checked.
     * @param allowedRoles is the roles that are allowed.
     * @return is a boolean where the method returns true if the user is allowed otherwise it returns false
     */
    @Override
    public boolean authorize(UserDTO user, Set<RouteRole> allowedRoles) {
        if (user == null) {
            throw new UnauthorizedResponse("You need to log in, dude!");
        }
        Set<String> roleNames = allowedRoles.stream()
                .map(RouteRole::toString)  // Convert RouteRoles to  Set of Strings
                .collect(Collectors.toSet());
        return user.getRoles().stream()
                .map(String::toUpperCase)
                .anyMatch(roleNames::contains);
    }
    /**
     * Creates and registers a user in the database if the user is unique.
     *
     * @param user is the user the token is created for
     * @return is a String where the method returns the created token
     */
    @Override
    public String createToken(UserDTO user) throws Exception {
        try {
            String ISSUER;
            String TOKEN_EXPIRE_TIME;
            String SECRET_KEY;

            if (System.getenv("DEPLOYED") != null) {
                ISSUER = System.getenv("ISSUER");
                TOKEN_EXPIRE_TIME = System.getenv("TOKEN_EXPIRE_TIME");
                SECRET_KEY = System.getenv("SECRET_KEY");
            } else {
                ISSUER = Utils.getPropertyValue("ISSUER", "config.properties");
                TOKEN_EXPIRE_TIME = Utils.getPropertyValue("TOKEN_EXPIRE_TIME", "config.properties");
                SECRET_KEY = Utils.getPropertyValue("SECRET_KEY", "config.properties");

            }
            System.out.println("ISSUER = " + ISSUER);
            System.out.println("TOKEN_EXPIRE_TIME = " + TOKEN_EXPIRE_TIME);
            System.out.println("SECRET_KEY loaded = "
                    + (SECRET_KEY != null && !SECRET_KEY.isBlank()));
            System.out.println("SECRET_KEY length = "
                    + (SECRET_KEY == null ? 0 : SECRET_KEY.length()));

            return tokenSecurity.createToken(user, ISSUER, TOKEN_EXPIRE_TIME, SECRET_KEY);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException(500, "Could not create token", e);

        }
    }
    /**
     * Verifies the created token
     *
     * @param token is the token that is being checked.
     * @return is app.dto.UserDTO to make sure it returns the correct UserDTO class.
     */
    @Override
    public app.dto.UserDTO verifyToken(String token) {
        boolean IS_DEPLOYED = (System.getenv("DEPLOYED") != null);
        String SECRET = IS_DEPLOYED ? System.getenv("SECRET_KEY") : Utils.getPropertyValue("SECRET_KEY", "config.properties");
        try {
            if (!tokenSecurity.tokenIsValid(token, SECRET)
                    || !tokenSecurity.tokenNotExpired(token)) {
                return null;
            }

            dk.bugelhartmann.UserDTO tokenUser =
                    tokenSecurity.getUserWithRolesFromToken(token);

            if (tokenUser == null) {
                return null;
            }

            return new app.dto.UserDTO(
                    tokenUser.getUsername(),
                    tokenUser.getRoles()
            );

        } catch (Exception e) {
            log.warn("Could not verify token", e);
            return null;
        }
    }
    /**
     * Validates the user.
     *
     * @param username is the users username
     * @param email is the users email
     * @param password is the users password
     * @return is a String where the method returns an empty String if the user is validated.
     * Otherwise, the method returns an error message
     */

    public static String validateUser(String username, String email, String password) {
        if (username.isEmpty()) {
            return "username skal udfyldes";
        } else if (email.isEmpty()) {
            return "email skal udfyldes";
        } else if (!email.contains("@")) {
            return "email mangler @";
        } else if (password.isEmpty()) {
            return "Password skal udfyldes";
        } else if (password.length() < 8) {
            return "password skal være mindst 8 tegn";
        } else if (!password.matches(".*\\d.*")) {
            return "password skal have mindst 1 tal";
        } else if (!password.matches(".*[^a-zA-Z0-9].*")) {
            return "password skal have mindst 1 special tegn";
        } else return "";
    }
    /**
     * Healthcheck
     *
     * @param ctx is the context from javalin
     */
    public void healthCheck(@NotNull Context ctx) {
        ctx.status(200).json("{\"msg\": \"API is up and running\"}");
    }


    public @NotNull Handler addRole() {
        return (ctx) -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                // get the role from the body. the json is {"role": "manager"}.
                // We need to get the role from the body and the username from the token
                String newRole = ctx.bodyAsClass(ObjectNode.class).get("role").asText();
                UserDTO user = ctx.attribute("user");

                if (user == null) {
                    ctx.status(401).json(
                            returnObject.put("msg",
                                    "No authenticated user found in request")
                    );
                    return;
                }
                User updatedUser = securityDAO.addRole(user, newRole);
                ctx.status(200).json(returnObject.put("msg", "Role " + newRole + " added to user"));
            } catch (EntityNotFoundException e) {
                ctx.status(404).json("{\"msg\": \"User not found\"}");
            }
        };
    }
}

