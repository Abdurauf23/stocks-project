package com.stocks.project.service;
import com.stocks.project.exception.EmailOrUsernameIsAlreadyUsedException;
import com.stocks.project.exception.NoFirstNameException;
import com.stocks.project.exception.NoStockWithThisNameException;
import com.stocks.project.exception.NoSuchUserException;
import com.stocks.project.exception.NotEnoughDataException;
import com.stocks.project.model.Role;
import com.stocks.project.model.StockUser;
import com.stocks.project.model.UserRegistrationDTO;
import com.stocks.project.repository.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    private final Integer ID = 5;
    private final String STOCK_NAME = "AAPL";
    private final String LOGIN = "username";

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    static StockUser user = StockUser.builder().firstName("Abdurauf").build();

    @BeforeAll
    public static void beforeAll() {
        Authentication authenticationMock = Mockito.mock(Authentication.class);
        SecurityContext securityContextMock = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContextMock.getAuthentication()).thenReturn(authenticationMock);
        SecurityContextHolder.setContext(securityContextMock);
    }

    @Test
    public void testGetAllUsers() {
        userService.findAll();
        Mockito.verify(userRepository, Mockito.times(1)).findAll();
    }

    @Test
    public void testGetUser() {
        userService.findById(ID);
        Mockito.verify(userRepository, Mockito.times(1)).findById(anyInt());
    }

    @Test
    public void testCreateUser() throws NoFirstNameException {
        userService.createUser(user);
        Mockito.verify(userRepository, Mockito.times(1)).createUser(any());
    }

    @Test
    public void testUpdateUser() throws NoSuchUserException {
        userService.updateUser(user, user.getUserId());
        Mockito.verify(userRepository, Mockito.times(1)).updateUser(any(), anyInt());
    }

    @Test
    public void testDeleteUserAsUser() throws NoSuchUserException {
        userService.delete(ID);
        Mockito.verify(userRepository, Mockito.times(1)).deleteForUser(anyInt());
    }

    @Test
    public void testDeleteUserAsAdmin() throws NoSuchUserException {
        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn(LOGIN);
        Mockito.when(userService.isAdmin(LOGIN)).thenReturn(true);

        userService.delete(ID);
        Mockito.verify(userRepository, Mockito.times(1)).deleteForAdmin(anyInt());
    }

    @Test
    public void testRegister() throws EmailOrUsernameIsAlreadyUsedException, NotEnoughDataException {
        UserRegistrationDTO dto = UserRegistrationDTO.builder().firstName("Abdu")
                        .email("some_email").password("123321123").username("un").build();
        userService.register(dto, Role.USER);
        Mockito.verify(userRepository, Mockito.times(1)).register(any(), any());
    }

    @Test
    public void testGetFavouriteStocks() {
        userService.getAllFavouriteStocks(ID);
        Mockito.verify(userRepository, Mockito.times(1)).getAllFavouriteStocks(anyInt());
    }

    @Test
    public void testAddStockToFav() throws NoStockWithThisNameException, NoSuchUserException {
        userService.addStockToFavourite(ID, STOCK_NAME);
        Mockito.verify(userRepository, Mockito.times(1)).addStockToFavourite(anyInt(), any());
    }

    @Test
    public void testDeleteStockFromFav() throws NoStockWithThisNameException, NoSuchUserException {
        userService.deleteStockFromFavourite(ID, STOCK_NAME);
        Mockito.verify(userRepository, Mockito.times(1)).deleteStockFromFavourite(anyInt(), any());
    }

    @Test
    public void testIsSame() {
        userService.isSame(LOGIN, ID);
        Mockito.verify(userRepository, Mockito.times(1)).isSamePerson(any(), anyInt());
    }
}
