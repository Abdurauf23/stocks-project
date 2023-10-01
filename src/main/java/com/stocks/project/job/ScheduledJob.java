package com.stocks.project.job;

import com.stocks.project.exception.NoStockMetaDataForThisSymbol;
import com.stocks.project.model.Meta;
import com.stocks.project.model.StockData;
import com.stocks.project.repository.StockRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
public class ScheduledJob {
    @Value("${base_url_external_api}")
    private String BASE_URL;
    @Value("${external_api_key}")
    private String API_KEY;

    private final StockRepository stockRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public ScheduledJob(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
        this.restTemplate = new RestTemplate();
    }

    @Scheduled(cron = "1 0 0 ? * *") // updates at first second of the day
    public void updateStocksDate() {
        long start = System.currentTimeMillis();
        log.info("Updating db started!");

        // "AAPL", "GOOGL", "TSLA", "MSFT", "AMZN", "NVDA", "META", "TSM"
        List<String> symbols = stockRepository.findAllMeta().stream().map(Meta::getSymbol).toList();

        for (String symbol : symbols) {
            String uri = BASE_URL + "/time_series?" +
                    "symbol=" + symbol +
                    "&interval=1min" +
                    "&timezone=Asia/Tashkent" +
                    "&outputsize=1" +
                    "&apikey=" + API_KEY;
            StockData stockData = restTemplate.getForObject(uri, StockData.class);
            try {
                stockRepository.addStockData(stockData);
            } catch (NoStockMetaDataForThisSymbol e) {
                e.printStackTrace();
            }
            System.out.println(stockData);
        }

        log.info("Updating ended. Time took: " + (System.currentTimeMillis() - start));
    }
}
