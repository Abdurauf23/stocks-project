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

    @AllArgsConstructor
    @Data
    @NoArgsConstructor
    public static class Meta {
        private String symbol;
        private String interval;
        private String currency;
        private String exchange_timezone;
        private String exchange;
        private String mic_code;
        private String type;
    }

    @AllArgsConstructor
    @Data
    @NoArgsConstructor
    public static class StockValue {
        private String datetime;
        private String open;
        private String high;
        private String low;
        private String close;
        private String volume;
    }
}

