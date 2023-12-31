package com.stocks.project.repository;

import com.stocks.project.exception.NoStockMetaDataForThisSymbol;
import com.stocks.project.exception.StockWithThisNameAlreadyExistsException;
import com.stocks.project.model.StockMetaData;
import com.stocks.project.model.StockData;
import com.stocks.project.model.StockValue;
import com.stocks.project.utils.StockDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class StockRepository {
    private final DataSource dataSource;
    private final StockDataMapper stockDataMapper;

    @Value("${stocks.findAllMetaQuery}")
    private String findAllMetaQuery;

    @Value("${stocks.findBySymbolQuery}")
    private String findBySymbolQuery;

    @Value("${stocks.addStockDataQuery1}")
    private String addStockDataQuery1;

    @Value("${stocks.addStockDataQuery2}")
    private String addStockDataQuery2;

    @Value("${stocks.addStockMetaQuery}")
    private String addStockMetaQuery;

    @Value("${stocks.deleteMetaQuery}")
    private String deleteMetaQuery;

    @Autowired
    public StockRepository(DataSource dataSource, StockDataMapper stockDataMapper) {
        this.dataSource = dataSource;
        this.stockDataMapper = stockDataMapper;
    }

    public Optional<StockData> findBySymbol(String symbol) {
        StockData stockData = null;
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(findBySymbolQuery)
        ) {
            preparedStatement.setString(1, symbol);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                stockData = stockDataMapper.mapRow(resultSet);
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return Optional.ofNullable(stockData);
    }

    public List<StockMetaData> findAllMeta() {
        List<StockMetaData> list = new ArrayList<>();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(findAllMetaQuery)
        ) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                list.add(stockDataMapper.mapMeta(resultSet));
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return list;
    }

    public void addStockData(StockData stockData) throws NoStockMetaDataForThisSymbol {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement getStatement = connection.prepareStatement(addStockDataQuery1);
                PreparedStatement insertStatement = connection.prepareStatement(addStockDataQuery2)
        ) {
            getStatement.setString(1, stockData.getMeta().getSymbol());
            ResultSet resultSet = getStatement.executeQuery();

            if (resultSet.next()) {
                int metaId = resultSet.getInt(1);
                List<StockValue> values = stockData.getValues();

                for (StockValue value : values) {
                    insertStatement.setInt(1, metaId);
                    insertStatement.setTimestamp(2, Timestamp.valueOf(value.getDatetime()));
                    insertStatement.setDouble(3, value.getOpen());
                    insertStatement.setDouble(4, value.getHigh());
                    insertStatement.setDouble(5, value.getLow());
                    insertStatement.setDouble(6, value.getClose());
                    insertStatement.setInt(7, value.getVolume());
                    insertStatement.addBatch();
                }
                insertStatement.executeBatch();
            } else {
                throw new NoStockMetaDataForThisSymbol();
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    public void addStockMeta(StockMetaData stockMetaData) throws StockWithThisNameAlreadyExistsException {
        // stocks with this name already exists
        if (findAllMeta().stream().anyMatch(m -> m.getSymbol().equals(stockMetaData.getSymbol()))) {
            throw new StockWithThisNameAlreadyExistsException();
        }
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(addStockMetaQuery)
        ) {
            statement.setString(1, stockMetaData.getSymbol());
            statement.setString(2, stockMetaData.getInterval());
            statement.setString(3, stockMetaData.getCurrency());
            statement.setString(4, stockMetaData.getExchangeTimezone());
            statement.setString(5, stockMetaData.getExchange());
            statement.setString(6, stockMetaData.getMicCode());
            statement.setString(7, stockMetaData.getType());
            statement.setString(8, "ok");

            statement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    public void deleteMeta(String symbol) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(deleteMetaQuery)
        ) {
            statement.setString(1, symbol);
            statement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }
}
