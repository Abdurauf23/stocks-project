package com.stocks.project.controller;

import com.stocks.project.model.ErrorModel;
import com.stocks.project.model.StockMetaData;
import com.stocks.project.model.StockData;
import com.stocks.project.service.StockService;
import com.stocks.project.service.UpdateStocksService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/stocks")
@SecurityRequirement(name = "Bearer Authentication")
public class StockController {
    private final StockService stockService;
    private final UpdateStocksService updateStocksService;

    @Autowired
    public StockController(StockService stockService, UpdateStocksService updateStocksService) {
        this.stockService = stockService;
        this.updateStocksService = updateStocksService;
    }

    @Operation(description = "Get value for particular Stock symbol.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404",
                    description = "No stock with this name",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorModel.class))),
            @ApiResponse(responseCode = "200",
                    description = "Get requested stock.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StockData.class)))
    })
    @GetMapping("/{symbol}")
    public ResponseEntity<?> getStock(@PathVariable String symbol) {
        Optional<StockData> stockData = stockService.getStock(symbol);
        if (stockData.isPresent()) {
            return new ResponseEntity<>(stockData.get(), HttpStatus.OK);
        }
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorModel(404, "No stock with this name"));
    }

    @Operation(description = "List of stocks available.")
    @ApiResponses(value = {
           @ApiResponse(responseCode = "200",
                    description = "Get list of stocks (JSON Array).",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StockMetaData.class)))
    })
    @GetMapping
    public ResponseEntity<?> getAllStocks() {
        return new ResponseEntity<>(stockService.getAllMeta(), HttpStatus.OK);
    }

    @Operation(description = "Update stocks in database.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Update stocks in database. Fill with data from API.",
                    content = @Content)
    })
    @PostMapping("/update")
    public ResponseEntity<?> updateStocks() {
        updateStocksService.updateStocks();
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
