package com.stocks.project.security.repository;

import com.stocks.project.model.SecurityInfo;
import com.stocks.project.utils.SecurityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class SecurityCredentialsRepository {
    private final DataSource dataSource;
    private final SecurityMapper securityMapper;

    @Autowired
    public SecurityCredentialsRepository(DataSource dataSource, SecurityMapper securityMapper) {
        this.dataSource = dataSource;
        this.securityMapper = securityMapper;
    }

    public Optional<SecurityInfo> findByUserLogin(String username) {
        SecurityInfo securityInfo = null;
        String query = "SELECT * FROM security_info WHERE username = ?;";
        try(
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                securityInfo = securityMapper.mapRow(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.ofNullable(securityInfo);
    }
}
