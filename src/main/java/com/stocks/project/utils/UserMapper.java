package com.stocks.project.utils;

import com.stocks.project.model.StockUser;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class UserMapper {
    public StockUser mapRow(ResultSet rs) throws SQLException {
        return new StockUser(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("second_name"),
                rs.getDate("birthday"),
                rs.getTimestamp("created_at"),
                rs.getTimestamp("updated_at"),
                rs.getBoolean("is_deleted")
        );
    }
}
