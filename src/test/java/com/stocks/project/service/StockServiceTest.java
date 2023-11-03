package com.stocks.project.service;

import com.stocks.project.repository.StockRepository;
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

@ExtendWith(MockitoExtension.class)
public class StockServiceTest {
    @InjectMocks
    private StockService stockService;

    @Mock
    private StockRepository stockRepository;

    @BeforeAll
    public static void beforeAll() {
        Authentication authenticationMock = Mockito.mock(Authentication.class);
        SecurityContext securityContextMock = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContextMock.getAuthentication()).thenReturn(authenticationMock);
        SecurityContextHolder.setContext(securityContextMock);
    }

    @Test
    public void testGetStockBySymbol() {
        String stockSymbol = "AAPL";
        stockService.getStock(stockSymbol);
        Mockito.verify(stockRepository, Mockito.times(1)).findBySymbol(any());
    }

    @Test
    public void testGetAllMeta() {
        stockService.getAllMeta();
        Mockito.verify(stockRepository, Mockito.times(1)).findAllMeta();
    }
}
