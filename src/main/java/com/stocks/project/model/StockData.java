package com.stocks.project.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class StockData {
    private Meta meta;
    private List<StockValue> values;
    private String status;
}
