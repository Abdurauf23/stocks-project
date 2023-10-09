package com.stocks.project.security.repository;

import com.stocks.project.security.model.SecurityCredentials;
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

    @Autowired
    public SecurityCredentialsRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Optional<SecurityCredentials> findByUserLogin(String username) {
        SecurityCredentials securityCredentials = null;
        String query = "SELECT * FROM security_info WHERE username = ? OR email = ?;";
        try(
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                securityCredentials = new SecurityCredentials(
                        resultSet.getInt("id"),
                        resultSet.getString("username"),
                        resultSet.getString("password"),
                        resultSet.getString("email")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.ofNullable(securityCredentials);
    }
}
