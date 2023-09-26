package com.stocks.project.controller;

import com.stocks.project.exception.NoStockWithThisName;
import com.stocks.project.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stocks")
public class StockController {
    private final StockService stockService;

    @Autowired
    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<?> getStock(@PathVariable String symbol) {
        try {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(stockService.getStock(symbol));
        } catch (NoStockWithThisName e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("""
                            {
                                "error" : "No stock with such symbol";
                            }
                            """);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllStocks() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(stockService.getAllStocks());
    }
}
