package com.stocks.project.service;

import com.stocks.project.exception.NoStockWithThisName;
import com.stocks.project.model.StockData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class StockService {
    @Value("${external_api_key}")
    private String API_KEY;

    @Value("${base_url_external_api}")
    private String BASE_URL;

    private final RestTemplate restTemplate = new RestTemplate();

    public StockData getStock(String symbol) throws NoStockWithThisName {
        String uri = BASE_URL + "/time_series?" +
                "symbol=" + symbol +
                "&interval=1min" +
                "&outputsize=1" +
                "&apikey=" + API_KEY;
        StockData stockData = restTemplate.getForObject(uri, StockData.class);
        if (stockData.getStatus().equals("error")) {
            throw new NoStockWithThisName();
        }
        return stockData;
    }

    public Object getAllStocks() {
        String uri = BASE_URL + "/stocks?" +
                "exchange=NASDAQ";
        return restTemplate.getForObject(uri, Object.class);
    }
}
