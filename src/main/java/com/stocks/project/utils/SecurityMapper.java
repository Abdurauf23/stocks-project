package com.stocks.project.utils;

import com.stocks.project.model.Role;
import com.stocks.project.model.SecurityInfo;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class SecurityMapper {
    public SecurityInfo mapRow(ResultSet rs) throws SQLException {
        return new SecurityInfo(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("email"),
                rs.getInt("role_id") == 1? Role.ADMIN: Role.USER
        );
    }
}
