package com.stocks.project.service;

import com.stocks.project.exception.EmailOrUsernameIsAlreadyUsedException;
import com.stocks.project.exception.NoSuchUserException;
import com.stocks.project.exception.NotEnoughDataException;
import com.stocks.project.model.SecurityInfo;
import com.stocks.project.repository.SecurityInfoRepository;
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
public class SecurityInfoServiceTest {
    private final Integer ID = 5;

    @InjectMocks
    private SecurityInfoService securityInfoService;

    @Mock
    private SecurityInfoRepository securityInfoRepository;

    private final SecurityInfo securityInfo = SecurityInfo.builder()
            .email("email").password("123321").username("un").build();

    @BeforeAll
    public static void beforeAll() {
        Authentication authenticationMock = Mockito.mock(Authentication.class);
        SecurityContext securityContextMock = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContextMock.getAuthentication()).thenReturn(authenticationMock);
        SecurityContextHolder.setContext(securityContextMock);
    }

    @Test
    public void testGetAllUsersSecurityInfo() {
        securityInfoService.findAll();
        Mockito.verify(securityInfoRepository, Mockito.times(1)).findAll();
    }

    @Test
    public void testGetUserSecurityInfo() {
        securityInfoService.findById(ID);
        Mockito.verify(securityInfoRepository, Mockito.times(1)).findById(anyInt());
    }

    @Test
    public void testCreateUser() throws EmailOrUsernameIsAlreadyUsedException, NoSuchUserException, NotEnoughDataException {
        securityInfoService.create(securityInfo, ID);
        Mockito.verify(securityInfoRepository, Mockito.times(1)).createSecurityInfo(any(), anyInt());
    }

    @Test
    public void testUpdateUser() throws NoSuchUserException, EmailOrUsernameIsAlreadyUsedException {
        securityInfoService.update(securityInfo, ID);
        Mockito.verify(securityInfoRepository, Mockito.times(1)).update(any(), anyInt());
    }

    @Test
    public void testDeleteUserAsUser() throws NoSuchUserException {
        securityInfoService.delete(ID);
        Mockito.verify(securityInfoRepository, Mockito.times(1)).deleteForAdmin(anyInt());
    }
}
