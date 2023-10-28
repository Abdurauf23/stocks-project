package com.stocks.project.repository;

import com.stocks.project.exception.EmailOrUsernameIsAlreadyUsedException;
import com.stocks.project.exception.NoFirstNameException;
import com.stocks.project.exception.NoStockMetaDataForThisSymbol;
import com.stocks.project.exception.NoStockWithThisNameException;
import com.stocks.project.exception.NoSuchUserException;
import com.stocks.project.exception.StockWithThisNameAlreadyExistsException;
import com.stocks.project.model.Meta;
import com.stocks.project.model.Role;
import com.stocks.project.model.StockData;
import com.stocks.project.model.StockValue;
import com.stocks.project.model.User;
import com.stocks.project.model.UserSecurityDTO;
import com.stocks.project.security.model.SecurityCredentials;
import com.stocks.project.security.repository.SecurityCredentialsRepository;
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
public class UserRepositoryTest {
    private final UserRepository userRepository;
    private final SecurityRepository securityRepository;
    private final StockRepository stockRepository;
    private final SecurityCredentialsRepository credentialsRepository;

    @Autowired
    public UserRepositoryTest(UserRepository userRepository,
                              SecurityRepository securityRepository,
                              StockRepository stockRepository, SecurityCredentialsRepository credentialsRepository) {
        this.userRepository = userRepository;
        this.securityRepository = securityRepository;
        this.stockRepository = stockRepository;
        this.credentialsRepository = credentialsRepository;
    }

    @Test
    public void testGetAll() throws NoFirstNameException {
        // insert users
        for (int i = 0; i < 3; i++) {
            User user = User.builder().firstName("First Name " + i)
                    .secondName("Second name " + i).build();
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
        Optional<User> user = userRepository.createUser(
                User.builder().firstName("FN").secondName("SN").build()
        );

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
    public void testUpdatingUser() throws NoFirstNameException, NoSuchUserException {
        // create user in DB
        String firstName = "Kahn", secondName = "Shao";
        User user  = User.builder().firstName(firstName).secondName(secondName).build();
        Optional<User> userFromDB = userRepository.createUser(user);

        // get user from DB check fist name and second name
        assertTrue(userFromDB.isPresent());
        int id = userFromDB.get().getUserId();
        assertEquals(firstName, userFromDB.get().getFirstName());
        assertEquals(secondName, userFromDB.get().getSecondName());

        // update user's first name and second name
        String newFirstName = "Kung", newSecondName = "Lao";
        User updatedUser = User.builder().firstName(newFirstName).secondName(newSecondName).build();
        userRepository.updateUser(updatedUser, userFromDB.get().getUserId());

        // check if name has changed
        Optional<User> updatedUserFromDB = userRepository.findById(id);
        assertTrue(updatedUserFromDB.isPresent());
        assertNotEquals(firstName, updatedUserFromDB.get().getFirstName());
        assertNotEquals(secondName, updatedUserFromDB.get().getSecondName());
        assertEquals(newFirstName, updatedUserFromDB.get().getFirstName());
        assertEquals(newSecondName, updatedUserFromDB.get().getSecondName());

        // delete user from DD
        userRepository.deleteForAdmin(updatedUserFromDB.get().getUserId());
    }

    @Test
    public void testRegistration() throws EmailOrUsernameIsAlreadyUsedException, NoSuchUserException {
        // register User
        String email = "scorpion@mortal.kombat";
        UserSecurityDTO dto = UserSecurityDTO.builder()
                .firstName("Scorpion")
                .email(email)
                .username("ScorpioN")
                .password("1234")
                .build();
        userRepository.register(dto, Role.USER);
        Optional<SecurityCredentials> byUserLogin = credentialsRepository.findByUserLogin(email);

        // check if it has appeared in both stock_user and security_info tables
        assertTrue(byUserLogin.isPresent());
        int id = byUserLogin.get().getId();
        assertNotNull(userRepository.findById(id).get());
        assertNotNull(securityRepository.findById(id).get());

        // delete user
        userRepository.deleteForAdmin(id);
    }

    @Test
    public void testFavouriteStocksOperations() throws NoFirstNameException,
            StockWithThisNameAlreadyExistsException, NoStockWithThisNameException,
            NoSuchUserException, NoStockMetaDataForThisSymbol {
        // create user
        User user = User.builder().firstName("Kitana").build();
        Optional<User> userFromDB = userRepository.createUser(user);

        // add some stocks to the database
        Meta appleMeta = Meta.builder()
                .symbol("AAPL")
                .exchangeTimezone("Asia/Tashkent")
                .currency("USD")
                .build();
        Meta googleMeta = Meta.builder()
                .symbol("GOOGL")
                .exchangeTimezone("Asia/Tashkent")
                .currency("EUR")
                .build();
        stockRepository.addStockMeta(appleMeta);
        stockRepository.addStockMeta(googleMeta);
        assertEquals(2, stockRepository.findAllMeta().size());

        // add some stocks to favourite
        assertTrue(userFromDB.isPresent());
        int id = userFromDB.get().getUserId();

        userRepository.addStockToFavourite(id, "AAPL");
        userRepository.addStockToFavourite(id, "GOOGL");

        // add stock data
        StockData appleStockData = new StockData(
                appleMeta,
                List.of(new StockValue(
                        "2023-10-10 10:10:10",
                        100, 105, 103, 100, 15
                )),
                "ok"
        );
        StockData googleStockData = new StockData(
                googleMeta,
                List.of(new StockValue(
                        "2023-10-10 10:10:10",
                        100, 105, 103, 100, 15
                )),
                "ok"
        );
        stockRepository.addStockData(appleStockData);
        stockRepository.addStockData(googleStockData);

        // check if they are added
        int numOfFavStocks = userRepository.getAllFavouriteStocks(id).size();
        assertEquals(2, numOfFavStocks);

        // delete user and stocks from DB
        userRepository.deleteStockFromFavourite(id, "AAPL");
        numOfFavStocks = userRepository.getAllFavouriteStocks(id).size();
        assertEquals(1, numOfFavStocks);

        userRepository.deleteForAdmin(id);
        stockRepository.deleteMeta("AAPL");
        stockRepository.deleteMeta("GOOGL");

        assertTrue(userRepository.findById(id).isEmpty());
        assertTrue(stockRepository.findAllMeta().isEmpty());
    }
}