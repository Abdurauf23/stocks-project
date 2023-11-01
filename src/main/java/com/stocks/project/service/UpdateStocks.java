package com.stocks.project.service;

import com.stocks.project.exception.NoStockMetaDataForThisSymbol;
import com.stocks.project.exception.StockWithThisNameAlreadyExistsException;
import com.stocks.project.model.StockMetaData;
import com.stocks.project.model.StockData;
import com.stocks.project.repository.StockRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class UpdateStocks {
    @Value("${base_url_external_api}")
    private String BASE_URL;
    @Value("${external_api_key}")
    private String API_KEY;

    private final StockRepository stockRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public UpdateStocks(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
        this.restTemplate = new RestTemplate();
    }

    @Scheduled(cron = "1 0 0 ? * *") // updates at first second of the day
    public void updateStocksDate() {
        boolean empty = false;
        long start = System.currentTimeMillis();
        log.info("Updating db started!");

        // "AAPL", "GOOGL", "TSLA", "MSFT", "AMZN", "NVDA", "META"
        List<String> symbols = stockRepository.findAllMeta().stream().map(StockMetaData::getSymbol).toList();
        if (symbols.isEmpty()) {
            symbols = List.of("AAPL", "GOOGL", "TSLA", "MSFT", "AMZN", "NVDA", "META");
            empty = true;
        }

        for (String symbol : symbols) {
            String uri = BASE_URL + "/time_series?" +
                    "symbol=" + symbol +
                    "&interval=1min" +
                    "&timezone=Asia/Tashkent" +
                    "&outputsize=1" +
                    "&apikey=" + API_KEY;
            StockData stockData = restTemplate.getForObject(uri, StockData.class);
            try {
                if (empty && stockData != null) {
                    StockMetaData stockMetaData = stockData.getMeta();
                    stockMetaData.setMicCode("XNGS");
                    stockMetaData.setExchangeTimezone("Asia/Tashkent");
                    stockRepository.addStockMeta(stockData.getMeta());
                }
                stockRepository.addStockData(Objects.requireNonNull(stockData));
            } catch (NoStockMetaDataForThisSymbol | StockWithThisNameAlreadyExistsException e) {
                log.error(e.getMessage());
            }
        }

        log.info("Updating ended. Time took: " + (System.currentTimeMillis() - start));
    }
}
