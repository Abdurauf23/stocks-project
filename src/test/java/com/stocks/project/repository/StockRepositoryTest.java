package com.stocks.project.repository;

import com.stocks.project.exception.NoStockMetaDataForThisSymbol;
import com.stocks.project.exception.StockWithThisNameAlreadyExistsException;
import com.stocks.project.model.StockData;
import com.stocks.project.model.StockMetaData;
import com.stocks.project.model.StockValue;
import com.stocks.project.service.UpdateStocksService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureTestDatabase
public class StockRepositoryTest {
    private final StockRepository stockRepository;
    private final UpdateStocksService updateStocksService;

    private static StockMetaData stockMetaData;
    private static StockValue stockValue;

    @BeforeAll
    static void beforeAll() {
        stockMetaData = StockMetaData.builder()
                .symbol("AAPL")
                .currency("USD")
                .exchange("NASDAQ")
                .micCode("XNGS")
                .type("Common Stock")
                .build();
        stockValue = StockValue.builder()
                .datetime("2023-11-01 22:13:00.000000")
                .close(181)
                .open(180)
                .low(178)
                .high(182)
                .volume(300_000)
                .build();
    }

    @Autowired
    public StockRepositoryTest(StockRepository stockRepository,
                               UpdateStocksService updateStocksService) {
        this.stockRepository = stockRepository;
        this.updateStocksService = updateStocksService;
    }

    @Test
    public void testFindBySymbolEmpty(){
        // test
        String symbol = stockMetaData.getSymbol();
        Optional<StockData> appleStock = stockRepository.findBySymbol(symbol);

        // db is clear
        assertTrue(appleStock.isEmpty());
    }

    @Test
    public void testFindBySymbolWithDBUpdate() {
        // fill db with data
        updateStocksService.updateStocks();

        // clear DB
        assertFalse(stockRepository.findAllMeta().isEmpty());
        stockRepository.findAllMeta().stream().map(StockMetaData::getSymbol).forEach(stockRepository::deleteMeta);
        assertTrue(stockRepository.findAllMeta().isEmpty());
    }

    @Test
    public void testFindAllMeta() {
        // test
        assertTrue(stockRepository.findAllMeta().isEmpty());
    }

    @Test
    public void testAddStockMetaDataAndData() throws NoStockMetaDataForThisSymbol, StockWithThisNameAlreadyExistsException {
        stockRepository.addStockMeta(stockMetaData);
        stockRepository.addStockData(new StockData(stockMetaData, Collections.singletonList(stockValue), "ok"));

        // check if meta has been added
        List<StockMetaData> metaDataList = stockRepository.findAllMeta();
        assertEquals(1, metaDataList.size());
        assertEquals(stockMetaData.getSymbol(), metaDataList.get(0).getSymbol());

        // check if value is added
        assertTrue(stockRepository.findBySymbol(stockMetaData.getSymbol()).isPresent());
    }
}
