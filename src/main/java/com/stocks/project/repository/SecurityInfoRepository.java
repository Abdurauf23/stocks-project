package com.stocks.project.repository;

import com.stocks.project.exception.EmailOrUsernameIsAlreadyUsedException;
import com.stocks.project.exception.NoSuchUserException;
import com.stocks.project.exception.NotEnoughDataException;
import com.stocks.project.model.Role;
import com.stocks.project.model.SecurityInfo;
import com.stocks.project.utils.SecurityMapper;
import lombok.extern.slf4j.Slf4j;
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
public class SecurityInfoRepository {
    private final DataSource dataSource;
    private final SecurityMapper securityMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${securityInfo.findAllQuery}")
    private String findAllQuery;
    @Value("${securityInfo.findByIdQuery}")
    private String findByIdQuery;
    @Value("${securityInfo.createSecurityInfoQuery}")
    private String createSecurityInfoQuery;
    @Value("${securityInfo.updateSecurityInfoQuery}")
    private String updateSecurityInfoQuery;
    @Value("${securityInfo.deleteSecurityInfoQuery}")
    private String deleteSecurityInfoQuery;

    @Autowired
    public SecurityInfoRepository(DataSource dataSource, SecurityMapper securityMapper,
                                  UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.dataSource = dataSource;
        this.securityMapper = securityMapper;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<SecurityInfo> findAll() {
        List<SecurityInfo> securityInfos = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(findAllQuery)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                securityInfos.add(securityMapper.mapRow(resultSet));
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }

        return securityInfos;
    }

    public Optional<SecurityInfo> findById(int id) {
        SecurityInfo securityInfo = null;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(findByIdQuery)) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                securityInfo = securityMapper.mapRow(resultSet);
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }

        return Optional.ofNullable(securityInfo);
    }

    public Optional<SecurityInfo> createSecurityInfo(SecurityInfo newSecurityInfo, int userId)
            throws NoSuchUserException, NotEnoughDataException, EmailOrUsernameIsAlreadyUsedException {
        if (findById(userId).isPresent()) {
            return Optional.empty();
        }
        if (newSecurityInfo.getUsername() == null ||
                newSecurityInfo.getPassword() == null ||
                newSecurityInfo.getEmail() == null) {
            throw new NotEnoughDataException();
        }
        if (userRepository.findById(userId).isEmpty()) {
            throw new NoSuchUserException();
        }
        if (userRepository.emailOrUsernameIsUsed(newSecurityInfo.getEmail(), newSecurityInfo.getUsername())) {
            throw new EmailOrUsernameIsAlreadyUsedException();
        }
        Optional<SecurityInfo> securityInfo = Optional.empty();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(createSecurityInfoQuery, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, newSecurityInfo.getUsername());
            preparedStatement.setString(3, passwordEncoder.encode(newSecurityInfo.getPassword()));
            preparedStatement.setString(4, newSecurityInfo.getEmail());
            Role role = newSecurityInfo.getRole();
            preparedStatement.setInt(5, role == Role.ADMIN ? 1 : 2);

            preparedStatement.executeUpdate();
            ResultSet res = preparedStatement.getGeneratedKeys();
            if (res.next()) {
                securityInfo = findById(res.getInt(1));
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }

        return securityInfo;
    }

    public void deleteForAdmin(int userId) throws NoSuchUserException {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NoSuchUserException();
        }
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(deleteSecurityInfoQuery, Statement.RETURN_GENERATED_KEYS)
        ) {
            preparedStatement.setInt(1, userId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    public Optional<SecurityInfo> update(SecurityInfo updatedInfo, int userId)
            throws NoSuchUserException, EmailOrUsernameIsAlreadyUsedException {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NoSuchUserException();
        }
        Optional<SecurityInfo> securityInfoByID = findById(userId);
        if (securityInfoByID.isEmpty()) {
            throw new NoSuchUserException();
        }
        SecurityInfo oldSecurityInfo = securityInfoByID.get();

        if (updatedInfo.getPassword() == null) {
            updatedInfo.setPassword(oldSecurityInfo.getPassword());
        } else {
            updatedInfo.setPassword(passwordEncoder.encode(updatedInfo.getPassword()));
        }
        if (userRepository.emailOrUsernameIsUsed(updatedInfo.getEmail(), updatedInfo.getUsername())) {
            throw new EmailOrUsernameIsAlreadyUsedException();
        }
        if (updatedInfo.getEmail() == null) {
            updatedInfo.setEmail(oldSecurityInfo.getEmail());
        }
        if (updatedInfo.getUsername() == null) {
            updatedInfo.setUsername(oldSecurityInfo.getUsername());
        }
        Optional<SecurityInfo> securityInfo = Optional.empty();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(updateSecurityInfoQuery, Statement.RETURN_GENERATED_KEYS)
        ) {
            preparedStatement.setString(1, updatedInfo.getEmail());
            preparedStatement.setString(2, updatedInfo.getPassword());
            preparedStatement.setString(3, updatedInfo.getUsername());
            preparedStatement.setInt(4, userId);

            preparedStatement.executeUpdate();
            ResultSet res = preparedStatement.getGeneratedKeys();
            if (res.next()) {
                securityInfo = findById(res.getInt(1));
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }

        return securityInfo;
    }
}
