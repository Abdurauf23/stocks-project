package com.stocks.project.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class EmailStockDTO {
    private String symbol;
    private String dateTime;
    private double open;
    private double high;
    private double low;
    private double close;
    private int volume;
    private String currency;
}
