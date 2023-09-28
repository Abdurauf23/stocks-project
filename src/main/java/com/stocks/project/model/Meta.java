package com.stocks.project.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class Meta {
    private String symbol;
    private String interval;
    private String currency;
    private String exchangeTimezone;
    private String exchange;
    private String micCode;
    private String type;
}