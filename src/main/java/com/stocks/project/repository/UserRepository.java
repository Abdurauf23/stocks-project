package com.stocks.project.repository;

import com.stocks.project.exception.EmailOrUsernameIsAlreadyUsedException;
import com.stocks.project.exception.NoFirstNameException;
import com.stocks.project.exception.NoStockWithThisNameException;
import com.stocks.project.exception.NoSuchUserException;
import com.stocks.project.exception.NotEnoughDataException;
import com.stocks.project.model.EmailStockDTO;
import com.stocks.project.model.Role;
import com.stocks.project.model.StockUser;
import com.stocks.project.model.UserRegistrationDTO;
import com.stocks.project.utils.UserMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
@Slf4j
public class UserRepository {
    private final DataSource dataSource;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${stocksUser.findAllQuery}")
    private String selectAllFromStocksUser;
    @Value("${stocksUser.findByIdQuery}")
    private String selectByIdFromStocksUser;
    @Value("${stocksUser.checkUniqueColumnsQuery}")
    private String checkUniqueColumns;
    @Value("${stocksUser.isAdminByLoginQuery}")
    private String isAdminByLoginQuery;
    @Value("${stocksUser.createUserQuery}")
    private String createUserQuery;
    @Value("${stocksUser.deleteForAdminQuery1}")
    private String deleteForAdminQuery1;
    @Value("${stocksUser.deleteForAdminQuery2}")
    private String deleteForAdminQuery2;
    @Value("${stocksUser.deleteForUserQuery}")
    private String deleteForUserQuery;
    @Value("${stocksUser.updateUserQuery}")
    private String updateUserQuery;
    @Value("${stocksUser.registerQuery1}")
    private String registerQuery1;
    @Value("${stocksUser.registerQuery2}")
    private String registerQuery2;
    @Value("${stocksUser.getAllFavouriteStocksQuery}")
    private String getAllFavouriteStocksQuery;
    @Value("${stocksUser.getPeopleWithFavStocksQuery}")
    private String getPeopleWithFavStocksQuery;
    @Value("${stocksUser.addStockToFavouriteQuery1}")
    private String addStockToFavouriteQuery1;
    @Value("${stocksUser.addStockToFavouriteQuery2}")
    private String addStockToFavouriteQuery2;
    @Value("${stocksUser.deleteStockFromFavouriteQuery1}")
    private String deleteStockFromFavouriteQuery1;
    @Value("${stocksUser.deleteStockFromFavouriteQuery2}")
    private String deleteStockFromFavouriteQuery2;

    @Autowired
    public UserRepository(DataSource dataSource, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.dataSource = dataSource;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public List<StockUser> findAll() {
        List<StockUser> stockUsers = new ArrayList<>();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(selectAllFromStocksUser)
        ) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                stockUsers.add(userMapper.mapRow(resultSet));
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return stockUsers;
    }

    public boolean emailOrUsernameIsUsed(String email, String username) {
        boolean duplicate = false;
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement checkDuplicate =
                        connection.prepareStatement(checkUniqueColumns)
        ) {
            checkDuplicate.setString(1, email);
            checkDuplicate.setString(2, username);
            if (checkDuplicate.executeQuery().next()) {
                return true;
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return duplicate;
    }

    public boolean isAdminByLogin(String login) {
        boolean isAdmin = false;
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(isAdminByLoginQuery)
        ) {
            preparedStatement.setString(1, login);
            preparedStatement.setString(2, login);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                isAdmin = resultSet.getBoolean(1);
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return isAdmin;
    }

    public Optional<StockUser> findById(int id) {
        StockUser stockUser = null;
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(selectByIdFromStocksUser)
        ) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                stockUser = userMapper.mapRow(resultSet);
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return Optional.ofNullable(stockUser);
    }

    public Optional<StockUser> createUser(StockUser newStockUser) throws NoFirstNameException {
        if (newStockUser.getFirstName() == null || newStockUser.getFirstName().isEmpty()) {
            throw new NoFirstNameException();
        }
        Optional<StockUser> user = Optional.empty();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement =
                        connection.prepareStatement(createUserQuery, Statement.RETURN_GENERATED_KEYS)
        ) {
            preparedStatement.setString(1, newStockUser.getFirstName());
            preparedStatement.setString(2, newStockUser.getSecondName());
            preparedStatement.setDate(3, newStockUser.getBirthday());

            preparedStatement.executeUpdate();
            ResultSet res = preparedStatement.getGeneratedKeys();
            if (res.next()) {
                user = findById(res.getInt(1));
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return user;
    }

    @Transactional
    public void deleteForAdmin(int userId) throws NoSuchUserException {
        if (findById(userId).isEmpty()) {
            throw new NoSuchUserException();
        }
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement deleteUserPrepStatement = connection.prepareStatement(deleteForAdminQuery1);
                PreparedStatement deleteSecurityInfoPrepStatement =
                        connection.prepareStatement(deleteForAdminQuery2)
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
                log.error(e.getMessage());
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    public void deleteForUser(int userId) throws NoSuchUserException{
        if (findById(userId).isEmpty()) {
            throw new NoSuchUserException();
        }
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement =
                        connection.prepareStatement(deleteForUserQuery)
        ) {
            preparedStatement.setInt(1, userId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    public Optional<StockUser> updateUser(StockUser updatedStockUser, int userId) throws NoSuchUserException {
        if (findById(userId).isEmpty()) {
            throw new NoSuchUserException();
        }
        StockUser oldStockUser = findById(userId).get();
        if (updatedStockUser.getFirstName() == null) {
            updatedStockUser.setFirstName(oldStockUser.getFirstName());
        }
        if (updatedStockUser.getSecondName() == null) {
            updatedStockUser.setSecondName(oldStockUser.getSecondName());
        }
        if (updatedStockUser.getBirthday() == null) {
            updatedStockUser.setBirthday(oldStockUser.getBirthday());
        }

        Optional<StockUser> user = Optional.empty();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement =
                        connection.prepareStatement(updateUserQuery, Statement.RETURN_GENERATED_KEYS)
        ) {
            preparedStatement.setString(1, updatedStockUser.getFirstName());
            preparedStatement.setString(2, updatedStockUser.getSecondName());
            preparedStatement.setDate(3, updatedStockUser.getBirthday());
            preparedStatement.setInt(4, userId);

            preparedStatement.executeUpdate();
            ResultSet res = preparedStatement.getGeneratedKeys();
            if (res.next()) {
                user = findById(res.getInt(1));
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return user;
    }

    @Transactional
    public void register(UserRegistrationDTO dto, Role role)
            throws EmailOrUsernameIsAlreadyUsedException, NotEnoughDataException {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement insertUser =
                        connection.prepareStatement(registerQuery1, Statement.RETURN_GENERATED_KEYS);
                PreparedStatement insertSecurityInfo =
                        connection.prepareStatement(registerQuery2)
        ) {
            try {
                connection.setAutoCommit(false);
                // check duplicate
                if (emailOrUsernameIsUsed(dto.getEmail(), dto.getUsername())) {
                    throw new EmailOrUsernameIsAlreadyUsedException();
                }
                // create if no duplication in login details
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
                insertSecurityInfo.setInt(5, role == Role.ADMIN? 1: 2); // USER
                insertSecurityInfo.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                log.error(e.getMessage());
                throw new NotEnoughDataException();
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    public List<EmailStockDTO> getAllFavouriteStocks(int userId) {
        List<EmailStockDTO> stocksList = new ArrayList<>();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(getAllFavouriteStocksQuery)
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
            log.error(e.getMessage());
        }
        return stocksList;
    }

    public List<Pair<Integer, String>> getPeopleWithFavStocks() {
        List<Pair<Integer, String>> integerList = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(getPeopleWithFavStocksQuery)
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
            log.error(e.getMessage());
        }
        return integerList;
    }

    public void addStockToFavourite(int userId, String symbol) throws NoSuchUserException, NoStockWithThisNameException {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement selectMeta = connection.prepareStatement(addStockToFavouriteQuery1);
                PreparedStatement insertIntoFavourites =
                        connection.prepareStatement(addStockToFavouriteQuery2)
        ) {
            try {
                connection.setAutoCommit(false);
                if (findById(userId).isEmpty()) {
                    throw new NoSuchUserException();
                }
                selectMeta.setString(1, symbol);
                ResultSet resultSet = selectMeta.executeQuery();
                if (!resultSet.next()) {
                    throw new NoStockWithThisNameException();
                }
                int metaId = resultSet.getInt("id");
                insertIntoFavourites.setInt(1, userId);
                insertIntoFavourites.setInt(2, metaId);
                insertIntoFavourites.executeUpdate();

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                log.error(e.getMessage());
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    public void deleteStockFromFavourite(int userId, String stockName)
            throws NoStockWithThisNameException, NoSuchUserException {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement selectMeta = connection.prepareStatement(deleteStockFromFavouriteQuery1);
                PreparedStatement insertIntoFavourites =
                        connection.prepareStatement(deleteStockFromFavouriteQuery2)
        ) {
            try {
                connection.setAutoCommit(false);
                if (findById(userId).isEmpty()) {
                    throw new NoSuchUserException();
                }
                selectMeta.setString(1, stockName);
                ResultSet resultSet = selectMeta.executeQuery();
                if (!resultSet.next()) {
                    throw new NoStockWithThisNameException();
                }
                int metaId = resultSet.getInt("id");
                insertIntoFavourites.setInt(1, userId);
                insertIntoFavourites.setInt(2, metaId);
                insertIntoFavourites.executeUpdate();

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                log.error(e.getMessage());
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }
}
