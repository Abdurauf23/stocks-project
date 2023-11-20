package com.stocks.project.repository;

import com.stocks.project.exception.EmailOrUsernameIsAlreadyUsedException;
import com.stocks.project.exception.NoFirstNameException;
import com.stocks.project.exception.NoSuchUserException;
import com.stocks.project.exception.NotEnoughDataException;
import com.stocks.project.model.Role;
import com.stocks.project.model.SecurityInfo;
import com.stocks.project.model.StockUser;
import com.stocks.project.security.model.SecurityCredentials;
import com.stocks.project.security.repository.SecurityCredentialsRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureTestDatabase
public class SecurityInfoRepositoryTest {
    private final SecurityInfoRepository securityInfoRepository;
    private final UserRepository userRepository;
    private final SecurityCredentialsRepository credentialsRepository;

    static StockUser stockUser;
    static SecurityInfo securityInfo;
    static String username;

    @Autowired
    public SecurityInfoRepositoryTest(SecurityInfoRepository securityInfoRepository,
                                      SecurityCredentialsRepository credentialsRepository,
                                      UserRepository userRepository) {
        this.securityInfoRepository = securityInfoRepository;
        this.userRepository = userRepository;
        this.credentialsRepository = credentialsRepository;
    }

    @BeforeAll
    static void beforeAll() {
        username = "subZero";
        stockUser = StockUser.builder()
                .firstName("Sub-Zero")
                .secondName("Surname")
                .build();

        securityInfo = SecurityInfo.builder()
                .email("subzero@gmail.com")
                .password("SubZeroTheBest")
                .username(username)
                .role(Role.USER)
                .build();
    }

    @Test
    public void findAllTest() {
        List<SecurityInfo> securityInfoAll = securityInfoRepository.findAll();
        System.out.println(securityInfoAll);

        // false because of admin
        assertFalse(securityInfoAll.isEmpty());
    }

    @Test
    public void createAndFindByIdTest() throws NoFirstNameException,
            EmailOrUsernameIsAlreadyUsedException, NoSuchUserException, NotEnoughDataException {
        // crate user in DB
        Optional<StockUser> user = userRepository.createUser(stockUser);
        assertTrue(user.isPresent());

        // create Security info for new User in DB
        Optional<SecurityInfo> info = securityInfoRepository
                .createSecurityInfo(securityInfo, user.get().getUserId());
        assertTrue(info.isPresent());

        // find by ID
        Optional<SecurityInfo> byId = securityInfoRepository.findById(info.get().getUserId());
        assertTrue(byId.isPresent());
    }

    @Test
    public void deleteTest() throws NoSuchUserException, NoFirstNameException,
            EmailOrUsernameIsAlreadyUsedException, NotEnoughDataException {
        Optional<StockUser> user = userRepository.createUser(StockUser.builder().firstName("Abdurauf").build());
        assertTrue(user.isPresent());
        int id = user.get().getUserId();

        Optional<SecurityInfo> info = securityInfoRepository
                .createSecurityInfo(SecurityInfo.builder().username(" ").email(" ").password(" ").build(), id);
        assertTrue(info.isPresent());

        securityInfoRepository.deleteForAdmin(id);

        Optional<SecurityInfo> deletedUser = securityInfoRepository.findById(id);
        assertTrue(deletedUser.isEmpty());
    }

    @Test
    public void updateTest() throws EmailOrUsernameIsAlreadyUsedException, NoSuchUserException, NoFirstNameException, NotEnoughDataException {
        int id;
        Optional<SecurityCredentials> user = credentialsRepository.findByUserLogin(username);
        if (user.isPresent()) {
            id = user.get().getId();
        } else {
            Optional<StockUser> someUser = userRepository.createUser(StockUser.builder().firstName("Abdurauf").build());
            assertTrue(someUser.isPresent());
            id = someUser.get().getUserId();

            Optional<SecurityInfo> info = securityInfoRepository
                    .createSecurityInfo(SecurityInfo.builder().username(" ").email(" ").password(" ").build(), id);
            assertTrue(info.isPresent());
        }

        String newEmail = "new_email@gmail.com";
        SecurityInfo newSecurityInfo = SecurityInfo.builder()
                .email(newEmail)
                .build();

        // update
        securityInfoRepository.update(newSecurityInfo, id);

        // check if email has changed
        Optional<SecurityInfo> byId = securityInfoRepository.findById(id);
        assertTrue(byId.isPresent());
        assertEquals(newEmail, byId.get().getEmail());
    }
}
