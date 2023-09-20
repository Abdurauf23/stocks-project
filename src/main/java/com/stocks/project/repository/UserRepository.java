package com.stocks.project.repository;

import com.stocks.project.model.User;
import com.stocks.project.utils.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UserRepository {
    private final DataSource dataSource;
    private final UserMapper userMapper;

    @Autowired
    public UserRepository(DataSource dataSource, UserMapper userMapper) {
        this.dataSource = dataSource;
        this.userMapper = userMapper;
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM \"user\";";

        try(
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query)
        ) {
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                users.add(userMapper.mapRow(resultSet));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }
}
