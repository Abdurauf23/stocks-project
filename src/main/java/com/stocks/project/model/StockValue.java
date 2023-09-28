package com.stocks.project.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class StockValue {
    private String datetime;
    private String open;
    private String high;
    private String low;
    private String close;
    private String volume;
}