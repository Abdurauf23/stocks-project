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
import java.sql.Statement;
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
        String query = "SELECT * FROM stocks_user;";
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

    public User findById(int id) {
        User user = null;
        String query = "SELECT * FROM stocks_user WHERE user_id = ?;";
        try(
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                user = userMapper.mapRow(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    public User createUser(User newUser) {
        User user = null;
        String query = "INSERT INTO stocks_user (first_name, second_name, birthday) VALUES (?, ?, ?);";
        try(
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement =
                        connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
        ) {
            preparedStatement.setString(1, newUser.getFirstName());
            preparedStatement.setString(2, newUser.getSecondName());
            preparedStatement.setDate(3, newUser.getBirthday());

            preparedStatement.executeUpdate();
            ResultSet res = preparedStatement.getGeneratedKeys();
            if (res.next()) {
                user = findById(res.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    public void delete(int userId) {
        String query = "DELETE FROM stocks_user WHERE user_id = ?;";
        String queryToDeleteInfo = "DELETE FROM security_info WHERE user_id = ?;";
        try(
                Connection connection = dataSource.getConnection();
                PreparedStatement deleteUserPrepStatement =
                        connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                PreparedStatement deleteSecurityInfoPrepStatement =
                        connection.prepareStatement(queryToDeleteInfo)
        ) {
            try {
                connection.setAutoCommit(false);
                deleteUserPrepStatement.setInt(1, userId);
                deleteSecurityInfoPrepStatement.setInt(1, userId);

                deleteSecurityInfoPrepStatement.executeUpdate();
                deleteUserPrepStatement.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public User updateUser(User updatedUser, int userId) {
        User user = null;
        String query = "UPDATE stocks_user SET first_name = ?, second_name = ?, birthday = ? WHERE user_id = ?;";
        try(
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement =
                        connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
        ) {
            preparedStatement.setString(1, updatedUser.getFirstName());
            preparedStatement.setString(2, updatedUser.getSecondName());
            preparedStatement.setDate(3, updatedUser.getBirthday());
            preparedStatement.setInt(4, userId);

            preparedStatement.executeUpdate();
            ResultSet res = preparedStatement.getGeneratedKeys();
            if (res.next()) {
                user = findById(res.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }
}
