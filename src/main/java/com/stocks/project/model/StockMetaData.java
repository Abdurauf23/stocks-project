package com.stocks.project.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class StockMetaData {
    private String symbol;
    private String interval;
    private String currency;
    private String exchangeTimezone;
    private String exchange;
    private String micCode;
    private String type;
}