package com.stocks.project.repository;

import com.stocks.project.exception.EmailOrUsernameIsAlreadyUsedException;
import com.stocks.project.exception.NoFirstNameException;
import com.stocks.project.exception.NoStockMetaDataForThisSymbol;
import com.stocks.project.exception.NoStockWithThisNameException;
import com.stocks.project.exception.NoSuchUserException;
import com.stocks.project.exception.NotEnoughDataException;
import com.stocks.project.exception.StockWithThisNameAlreadyExistsException;
import com.stocks.project.model.Role;
import com.stocks.project.model.SecurityInfo;
import com.stocks.project.model.StockData;
import com.stocks.project.model.StockMetaData;
import com.stocks.project.model.StockUser;
import com.stocks.project.model.StockValue;
import com.stocks.project.model.UserRegistrationDTO;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureTestDatabase
public class UserRepositoryTest {
    private final UserRepository userRepository;
    private final SecurityInfoRepository securityInfoRepository;
    private final StockRepository stockRepository;
    private final SecurityCredentialsRepository credentialsRepository;

    @Autowired
    public UserRepositoryTest(UserRepository userRepository,
                              SecurityInfoRepository securityInfoRepository,
                              StockRepository stockRepository, SecurityCredentialsRepository credentialsRepository) {
        this.userRepository = userRepository;
        this.securityInfoRepository = securityInfoRepository;
        this.stockRepository = stockRepository;
        this.credentialsRepository = credentialsRepository;
    }

    @Test
    public void testGetAll() throws NoFirstNameException {
        // insert users
        for (int i = 0; i < 3; i++) {
            StockUser stockUser = StockUser.builder().firstName("First Name " + i)
                    .secondName("Second name " + i).build();
            userRepository.createUser(stockUser);
        }

        // find all and test
        List<StockUser> usersList = userRepository.findAll();
        int size = usersList.size();
        assertNotEquals(null, usersList, "List is null");
        // 4 because + admin
        assertEquals(4, size, "Size is not 4");

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
    public void testUsedEmailOrUsername() throws EmailOrUsernameIsAlreadyUsedException,
            NoSuchUserException, NotEnoughDataException {
        // register new user with email "used.email@gmail.com" and "used.username"
        String email = "used.email@gmail.com";
        String username = "used.username";
        userRepository.register(new UserRegistrationDTO(
                "FN", "SN", email, username, "pass", null),
                Role.USER
        );

        // Check if email and username are used
        assertTrue(userRepository.emailOrUsernameIsUsed(email, ""));
        assertTrue(userRepository.emailOrUsernameIsUsed("", username));
        assertTrue(userRepository.emailOrUsernameIsUsed(email, username));
        assertFalse(userRepository.emailOrUsernameIsUsed(" ", " "));

        // delete created user
        Optional<SecurityCredentials> byUserLogin = credentialsRepository.findByUserLogin(email);
        assertTrue(byUserLogin.isPresent());
        int id = byUserLogin.get().getId();
        userRepository.deleteForAdmin(id);
    }

    @Test
    public void checkIfAdmin() throws EmailOrUsernameIsAlreadyUsedException, NoSuchUserException, NotEnoughDataException {
        // register ordinary user
        String userUsername = "username";
        userRepository.register(new UserRegistrationDTO(
                        "FN", "SN", "some@gmail.com",
                        userUsername, "pass", null),
                Role.USER
        );

        // register admin
        String adminUsername = "adminUsername";
        userRepository.register(new UserRegistrationDTO(
                        "FN", "SN", "another@gmail.com",
                        adminUsername, "pass", null),
                Role.ADMIN
        );

        // get id for both of them
        Optional<SecurityCredentials> user = credentialsRepository.findByUserLogin(userUsername);
        assertTrue(user.isPresent());
        assertFalse(userRepository.isAdminByLogin(user.get().getLogin()));

        Optional<SecurityCredentials> admin = credentialsRepository.findByUserLogin(adminUsername);
        assertTrue(admin.isPresent());
        assertTrue(userRepository.isAdminByLogin(admin.get().getLogin()));

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
        Optional<StockUser> user = userRepository.createUser(
                StockUser.builder().firstName("FN").secondName("SN").build()
        );

        // test for creation
        assertTrue(user.isPresent());

        // get this user by id
        Optional<StockUser> opStockUserFromDB = userRepository.findById(user.get().getUserId());
        assertTrue(opStockUserFromDB.isPresent());
        StockUser stockUserFromDB = opStockUserFromDB.get();

        // test for null and test for first name
        assertNotEquals(null, stockUserFromDB);
        assertEquals(user.get().getFirstName(), stockUserFromDB.getFirstName());

        // delete created user
        userRepository.deleteForAdmin(stockUserFromDB.getUserId());
    }

    @Test
    public void testCreatingUser() throws NoFirstNameException, NoSuchUserException {
        // create user with blank firstName
        StockUser stockUser = StockUser.builder().firstName("").build();
        boolean fistNameIsOk = true;
        try {
            userRepository.createUser(stockUser);
        } catch (NoFirstNameException e) {
            fistNameIsOk = false;
        }
        assertFalse(fistNameIsOk);

        // create normal user
        StockUser normalStockUser = StockUser.builder().firstName("Jake").build();
        Optional<StockUser> normalUserFromDb = userRepository.createUser(normalStockUser);

        // check if created
        assertTrue(normalUserFromDb.isPresent());
        assertEquals("Jake" ,normalUserFromDb.get().getFirstName());

        // delete from db
        userRepository.deleteForAdmin(normalUserFromDb.get().getUserId());
    }

    @Test
    public void testDeletionForUser() throws NoFirstNameException, NoSuchUserException {
        // create user
        StockUser normalStockUser = StockUser.builder().firstName("Jake").build();
        Optional<StockUser> normalUserFromDb = userRepository.createUser(normalStockUser);

        // check if created
        assertTrue(normalUserFromDb.isPresent());

        // try delete for user (set is_deleted = true)
        int id = normalUserFromDb.get().getUserId();
        userRepository.deleteForUser(id);

        // get the user from db and see is_deleted value
        Optional<StockUser> userById = userRepository.findById(id);
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
        StockUser stockUser = StockUser.builder().firstName(firstName).secondName(secondName).build();
        Optional<StockUser> userFromDB = userRepository.createUser(stockUser);

        // get user from DB check fist name and second name
        assertTrue(userFromDB.isPresent());
        int id = userFromDB.get().getUserId();
        assertEquals(firstName, userFromDB.get().getFirstName());
        assertEquals(secondName, userFromDB.get().getSecondName());

        // update user's first name and second name
        String newFirstName = "Kung", newSecondName = "Lao";
        StockUser updatedStockUser = StockUser.builder().firstName(newFirstName).secondName(newSecondName).build();
        userRepository.updateUser(updatedStockUser, userFromDB.get().getUserId());

        // check if name has changed
        Optional<StockUser> updatedUserFromDB = userRepository.findById(id);
        assertTrue(updatedUserFromDB.isPresent());
        assertNotEquals(firstName, updatedUserFromDB.get().getFirstName());
        assertNotEquals(secondName, updatedUserFromDB.get().getSecondName());
        assertEquals(newFirstName, updatedUserFromDB.get().getFirstName());
        assertEquals(newSecondName, updatedUserFromDB.get().getSecondName());

        // delete user from DD
        userRepository.deleteForAdmin(updatedUserFromDB.get().getUserId());
    }

    @Test
    public void testRegistration() throws EmailOrUsernameIsAlreadyUsedException, NoSuchUserException, NotEnoughDataException {
        // register User
        String email = "scorpion@mortal.kombat";
        UserRegistrationDTO dto = UserRegistrationDTO.builder()
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
        assertTrue(userRepository.findById(id).isPresent());
        assertTrue(securityInfoRepository.findById(id).isPresent());

        // delete user
        userRepository.deleteForAdmin(id);
    }

    @Test
    public void testFavouriteStocksOperations() throws NoFirstNameException,
            StockWithThisNameAlreadyExistsException, NoStockWithThisNameException,
            NoSuchUserException, NoStockMetaDataForThisSymbol, EmailOrUsernameIsAlreadyUsedException, NotEnoughDataException {
        // create user
        StockUser stockUser = StockUser.builder().firstName("Kitana").build();
        Optional<StockUser> userFromDB = userRepository.createUser(stockUser);

        // add some stocks to the database
        StockMetaData appleStockMetaData = StockMetaData.builder()
                .symbol("AAPL")
                .exchangeTimezone("Asia/Tashkent")
                .currency("USD")
                .build();
        StockMetaData googleStockMetaData = StockMetaData.builder()
                .symbol("GOOGL")
                .exchangeTimezone("Asia/Tashkent")
                .currency("EUR")
                .build();
        stockRepository.addStockMeta(appleStockMetaData);
        stockRepository.addStockMeta(googleStockMetaData);
        assertEquals(2, stockRepository.findAllMeta().size());

        // add some stocks to favourite
        assertTrue(userFromDB.isPresent());
        int id = userFromDB.get().getUserId();

        SecurityInfo securityInfo = SecurityInfo.builder().email("some@email").username("abc").password("123").build();
        securityInfoRepository.createSecurityInfo(securityInfo, id);

        userRepository.addStockToFavourite(id, "AAPL");
        userRepository.addStockToFavourite(id, "GOOGL");

        // add stock data
        StockData appleStockData = new StockData(
                appleStockMetaData,
                List.of(new StockValue(
                        "2023-10-10 10:10:10",
                        100, 105, 103, 100, 15
                )),
                "ok"
        );
        StockData googleStockData = new StockData(
                googleStockMetaData,
                List.of(new StockValue(
                        "2023-10-10 10:10:10",
                        100, 105, 103, 100, 15
                )),
                "ok"
        );
        stockRepository.addStockData(appleStockData);
        stockRepository.addStockData(googleStockData);

        // check if they are added and check people wth fav stocks
        int numOfFavStocks = userRepository.getAllFavouriteStocks(id).size();
        assertEquals(2, numOfFavStocks);
        assertEquals(1, userRepository.getPeopleWithFavStocks().size());

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
