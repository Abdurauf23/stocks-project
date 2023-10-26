package com.stocks.project.repository;

import com.stocks.project.exception.EmailOrUsernameIsAlreadyUsedException;
import com.stocks.project.exception.NoFirstNameException;
import com.stocks.project.exception.NoSuchUserException;
import com.stocks.project.model.Role;
import com.stocks.project.model.User;
import com.stocks.project.model.UserSecurityDTO;
import com.stocks.project.security.model.SecurityCredentials;
import com.stocks.project.security.repository.SecurityCredentialsRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureTestDatabase
@Slf4j
public class UserRepositoryTest {
    private final UserRepository userRepository;
    private final SecurityRepository securityRepository;
    private final SecurityCredentialsRepository credentialsRepository;

    @Autowired
    public UserRepositoryTest(UserRepository userRepository,
                              SecurityRepository securityRepository,
                              SecurityCredentialsRepository credentialsRepository) {
        this.userRepository = userRepository;
        this.securityRepository = securityRepository;
        this.credentialsRepository = credentialsRepository;
    }

    @Test
    public void testGetAll() throws NoFirstNameException {
        // insert users
        for (int i = 0; i < 3; i++) {
            User user = new User(i + 1, "First Name" + i, "Second Name" + i,
                    null, null, null, false);
            userRepository.createUser(user);
        }

        // find all and test
        List<User> usersList = userRepository.findAll();
        int size = usersList.size();
        assertNotEquals(null, usersList, "List is null");
        assertEquals(3, size, "Size is not 3");

        // delete created users
        usersList.forEach(user -> {
            try {
                userRepository.deleteForAdmin(user.getUserId());
            } catch (NoSuchUserException e) {
                throw new RuntimeException(e);
            }
        });

        // check after deletion
        size = userRepository.findAll().size();
        assertEquals(0, size, "DB is not clear from users");
    }

    @Test
    public void testUsedEmailOrUsername() throws EmailOrUsernameIsAlreadyUsedException, NoSuchUserException {
        // register new user with email "used.email@gmail.com" and "used.username"
        String email = "used.email@gmail.com";
        String username = "used.username";
        userRepository.register(new UserSecurityDTO(
                "FN", "SN", email, username, "pass", null),
                Role.USER
        );

        // Check if email and username are used
        assertTrue(userRepository.emailOrUsernameIsUsed(email, ""));
        assertTrue(userRepository.emailOrUsernameIsUsed("", username));
        assertTrue(userRepository.emailOrUsernameIsUsed(email, username));
        assertFalse(userRepository.emailOrUsernameIsUsed(" ", " "));

        // delete created user
        int id = credentialsRepository.findByUserLogin(email).get().getId();
        userRepository.deleteForAdmin(id);
    }

    @Test
    public void testSamePerson() throws EmailOrUsernameIsAlreadyUsedException, NoSuchUserException {
        // register new user
        String email = "used.email@gmail.com";
        String username = "used.username";
        userRepository.register(new UserSecurityDTO(
                        "FN", "SN", email,
                        username, "pass", null),
                Role.USER
        );

        // get user id from login
        int id = credentialsRepository.findByUserLogin(email).get().getId();

        // check email and username belong to user with user_id = id (got earlier)
        assertFalse(userRepository.isSamePerson("some@gmail.com", id));
        assertTrue(userRepository.isSamePerson(email, id));
        assertTrue(userRepository.isSamePerson(username, id));

        // delete created user
        userRepository.deleteForAdmin(id);
    }

    @Test
    public void checkIfAdmin() throws EmailOrUsernameIsAlreadyUsedException, NoSuchUserException {
        // register ordinary user
        String userUsername = "username";
        userRepository.register(new UserSecurityDTO(
                        "FN", "SN", "some@gmail.com",
                        userUsername, "pass", null),
                Role.USER
        );

        // register admin
        String adminUsername = "adminUsername";
        userRepository.register(new UserSecurityDTO(
                        "FN", "SN", "another@gmail.com",
                        adminUsername, "pass", null),
                Role.ADMIN
        );

        // get id for both of them
        Optional<SecurityCredentials> user = credentialsRepository.findByUserLogin(userUsername);
        assertNotNull(user, "User is not found in DB");
        assertFalse(userRepository.isAdminByLogin(user.get().getLogin()), "This is not user");

        Optional<SecurityCredentials> admin = credentialsRepository.findByUserLogin(adminUsername);
        assertNotNull(admin, "Admin is not found in DB");
        assertTrue(userRepository.isAdminByLogin(admin.get().getLogin()), "This is not admin");

        // test if admin
        assertTrue(userRepository.isAdminByLogin(admin.get().getLogin()));
        assertFalse(userRepository.isAdminByLogin(user.get().getLogin()));

        // delete both from db
        userRepository.deleteForAdmin(admin.get().getId());
        userRepository.deleteForAdmin(user.get().getId());
    }

    @Test
    public void testGetById() throws NoFirstNameException, NoSuchUserException {
        // create user
        Optional<User> user = userRepository.createUser(new User(
                1, "FN", "SN", null, null, null, false
        ));

        // test for creation
        assertTrue(user.isPresent(), "Not created user in DB");

        // get this user by id
        User userFromDB = userRepository.findById(user.get().getUserId()).get();

        // test for null and test for first name
        assertNotEquals(null, userFromDB);
        assertEquals(user.get().getFirstName(), userFromDB.getFirstName());

        // delete created user
        userRepository.deleteForAdmin(userFromDB.getUserId());
    }

    @Test
    public void testCreatingUser() throws NoFirstNameException, NoSuchUserException {
        // create user with blank firstName
        User user = User.builder().firstName("").build();
        boolean fistNameIsOk = true;
        try {
            userRepository.createUser(user);
        } catch (NoFirstNameException e) {
            fistNameIsOk = false;
        }
        assertFalse(fistNameIsOk);

        // create normal user
        User normalUser = User.builder().firstName("Jake").build();
        Optional<User> normalUserFromDb = userRepository.createUser(normalUser);

        // check if created
        assertTrue(normalUserFromDb.isPresent());
        assertEquals("Jake" ,normalUserFromDb.get().getFirstName());

        // delete from db
        userRepository.deleteForAdmin(normalUserFromDb.get().getUserId());
    }

    @Test
    public void testDeletionForUser() throws NoFirstNameException, NoSuchUserException {
        // create user
        User normalUser = User.builder().firstName("Jake").build();
        Optional<User> normalUserFromDb = userRepository.createUser(normalUser);

        // check if created
        assertTrue(normalUserFromDb.isPresent());

        // try delete for user (set is_deleted = true)
        int id = normalUserFromDb.get().getUserId();
        userRepository.deleteForUser(id);

        // get the user from db and see is_deleted value
        Optional<User> userById = userRepository.findById(id);
        assertTrue(userById.isPresent());
        assertEquals("Jake", userById.get().getFirstName());
        assertTrue(userById.get().isDeleted());

        // delete from DB
        userRepository.deleteForAdmin(id);
    }

    @Test
    public void testUpdatingUser() {

    }
}
