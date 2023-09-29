package com.stocks.project.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class StockValue {
    private String datetime;
    private double open;
    private double high;
    private double low;
    private double close;
    private int volume;
}