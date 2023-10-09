package com.stocks.project.repository;

import com.stocks.project.exception.NoFirstNameException;
import com.stocks.project.exception.NoSuchUserException;
import com.stocks.project.model.EmailStockDTO;
import com.stocks.project.model.User;
import com.stocks.project.model.UserSecurityDTO;
import com.stocks.project.utils.UserMapper;
import jakarta.transaction.Transactional;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {
    private final DataSource dataSource;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserRepository(DataSource dataSource, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.dataSource = dataSource;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM stocks_user;";
        try (
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

    public Optional<User> findById(int id) {
        User user = null;
        String query = "SELECT * FROM stocks_user WHERE id = ?;";
        try (
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
        return Optional.ofNullable(user);
    }

    public Optional<User> createUser(User newUser) throws NoFirstNameException {
        if (newUser.getFirstName() == null) {
            throw new NoFirstNameException("Column 'first_name' is required to create a user.");
        }
        Optional<User> user = Optional.empty();
        String query = "INSERT INTO stocks_user (first_name, second_name, birthday) VALUES (?, ?, ?);";
        try (
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

    public void delete(int userId) throws NoSuchUserException {
        if (findById(userId).isEmpty()) {
            throw new NoSuchUserException();
        }
        String query = "DELETE FROM stocks_user WHERE id = ?;";
        String queryToDeleteInfo = "DELETE FROM security_info WHERE id = ?;";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement deleteUserPrepStatement = connection.prepareStatement(query);
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

    public Optional<User> updateUser(User updatedUser, int userId) throws NoSuchUserException {
        if (findById(userId).isEmpty()) {
            throw new NoSuchUserException();
        }
        User oldUser = findById(userId).get();
        if (updatedUser.getFirstName() == null) {
            updatedUser.setFirstName(oldUser.getFirstName());
        }
        if (updatedUser.getSecondName() == null) {
            updatedUser.setSecondName(oldUser.getSecondName());
        }
        if (updatedUser.getBirthday() == null) {
            updatedUser.setBirthday(oldUser.getBirthday());
        }

        Optional<User> user = Optional.empty();
        String query = "UPDATE stocks_user SET first_name = ?, second_name = ?, birthday = ?, updated_at = NOW() WHERE id = ?;";
        try (
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

    @Transactional
    public void register(UserSecurityDTO dto) {
        String query = "INSERT INTO stocks_user (first_name, second_name, birthday) VALUES (?, ?, ?);";
        String queryToDeleteInfo = "INSERT INTO security_info (id, username, password, email) VALUES (?, ?, ?, ?);";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement insertUser =
                        connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                PreparedStatement insertSecurityInfo =
                        connection.prepareStatement(queryToDeleteInfo)
        ) {
            try {
                connection.setAutoCommit(false);
                insertUser.setString(1, dto.getFirstName());
                insertUser.setString(2, dto.getSecondName());
                insertUser.setDate(3, dto.getBirthday());
                insertUser.executeUpdate();

                // get primary key of created user
                ResultSet userPKResultSet = insertUser.getGeneratedKeys();
                userPKResultSet.next();
                int userPK = userPKResultSet.getInt(1);

                // insert new row into security_info table with userPK
                insertSecurityInfo.setInt(1, userPK);
                insertSecurityInfo.setString(2, dto.getUsername());
                insertSecurityInfo.setString(3, passwordEncoder.encode(dto.getPassword()));
                insertSecurityInfo.setString(4, dto.getEmail());
                insertSecurityInfo.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<EmailStockDTO> getAllFavouriteStocks(int userId) {
        List<EmailStockDTO> stocksList = new ArrayList<>();
        String query = """
                SELECT symbol, date_time, open, high, low, close, volume, currency
                FROM stock_users_fav_stocks
                         INNER JOIN stock_meta sm ON sm.id = stock_users_fav_stocks.meta_id
                         INNER JOIN stock_value sv ON sm.id = sv.meta_id
                WHERE user_id = ? AND date_time = (
                    SELECT MAX(date_time)
                    FROM stock_value
                );
                """;
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                stocksList.add(new EmailStockDTO(
                        resultSet.getString("symbol"),
                        resultSet.getString("date_time"),
                        resultSet.getDouble("open"),
                        resultSet.getDouble("high"),
                        resultSet.getDouble("low"),
                        resultSet.getDouble("close"),
                        resultSet.getInt("volume"),
                        resultSet.getString("currency")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stocksList;
    }

    public List<Pair<Integer, String>> getPeopleWithFavStocks() {
        List<Pair<Integer, String>> integerList = new ArrayList<>();
        String query = """
                SELECT DISTINCT fav.user_id, email
                FROM stock_users_fav_stocks fav
                INNER JOIN public.security_info si ON fav.user_id = si.id;
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)
        ) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                integerList.add(new Pair<>(
                        resultSet.getInt("user_id"),
                        resultSet.getString("email")
                        )
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return integerList;
    }

}
