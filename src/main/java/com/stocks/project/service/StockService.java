package com.stocks.project.service;

import com.stocks.project.model.StockData;
import com.stocks.project.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StockService {
    private final StockRepository stockRepository;

    @Autowired
    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public Optional<StockData> getStock(String symbol)  {
        return stockRepository.findBySymbol(symbol);
    }

    public Object getAllMeta() {
        return stockRepository.findAllMeta();
    }
}
