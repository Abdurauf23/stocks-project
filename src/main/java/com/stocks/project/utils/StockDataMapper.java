package com.stocks.project.utils;

import com.stocks.project.model.Meta;
import com.stocks.project.model.StockData;
import com.stocks.project.model.StockValue;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class StockDataMapper {
    public StockData mapRow(ResultSet rs) throws SQLException {
        StockData stockData = new StockData();
        Meta meta = mapMeta(rs);
        stockData.setMeta(meta);
        stockData.setStatus(rs.getString("stock_status"));
        stockData.setValues(new ArrayList<>());
        do {
            addStockValues(rs, stockData.getValues());
        } while (rs.next());
        return stockData;
    }

    public Meta mapMeta(ResultSet rs) throws SQLException {
        return new Meta(
                rs.getString("symbol"),
                rs.getString("data_interval"),
                rs.getString("currency"),
                rs.getString("exchange_timezone"),
                rs.getString("exchange"),
                rs.getString("mic_code"),
                rs.getString("type_")
        );
    }

    private void addStockValues(ResultSet rs, List<StockValue> stockValues) throws SQLException {
        stockValues.add(new StockValue(
                rs.getString("date_time"),
                rs.getDouble("open"),
                rs.getDouble("high"),
                rs.getDouble("low"),
                rs.getDouble("close"),
                rs.getInt("volume")
        ));
    }
}
