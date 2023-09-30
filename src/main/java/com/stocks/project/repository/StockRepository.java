package com.stocks.project.repository;

import com.stocks.project.model.Meta;
import com.stocks.project.model.StockData;
import com.stocks.project.utils.StockDataMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class StockRepository {
    private final DataSource dataSource;
    private final StockDataMapper stockDataMapper;

    @Autowired
    public StockRepository(DataSource dataSource, StockDataMapper stockDataMapper) {
        this.dataSource = dataSource;
        this.stockDataMapper = stockDataMapper;
    }

    public Optional<StockData> findBySymbol(String symbol) {
        StockData stockData = null;
        String query = """
                SELECT m.id AS meta_id, symbol, data_interval, currency,
                exchange_timezone, exchange, mic_code, type_, stock_status, 
                v.id AS value_id, date_time, open, high, low, close, volume 
                FROM stock_meta AS m 
                INNER JOIN stock_value AS v ON m.id = v.meta_id 
                WHERE date_time >= date_trunc('DAY', now()) 
                AND symbol = ?;
                """;
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setString(1, symbol);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                stockData = stockDataMapper.mapRow(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(stockData);
    }

    public List<Meta> findAllMeta() {
        List<Meta> list = new ArrayList<>();
        String query = "SELECT * FROM stock_meta;";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query)
        ) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                list.add(stockDataMapper.mapMeta(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void addStockData(StockData stockData) {

    }
}
