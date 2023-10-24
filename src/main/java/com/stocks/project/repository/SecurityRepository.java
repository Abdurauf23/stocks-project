package com.stocks.project.repository;

import com.stocks.project.exception.EmailOrUsernameIsAlreadyUsedException;
import com.stocks.project.exception.NoSuchUserException;
import com.stocks.project.exception.NotEnoughDataException;
import com.stocks.project.model.Role;
import com.stocks.project.model.SecurityInfo;
import com.stocks.project.utils.SecurityMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class SecurityRepository {
    private final DataSource dataSource;
    private final SecurityMapper securityMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<SecurityInfo> findAll() {
        List<SecurityInfo> securityInfos = new ArrayList<>();
        String query = "SELECT * FROM security_info INNER JOIN public.role r " +
                "ON r.role_id = security_info.role_id;";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                securityInfos.add(securityMapper.mapRow(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return securityInfos;
    }

    public Optional<SecurityInfo> findById(int id) {
        SecurityInfo securityInfo = null;
        String query = "SELECT * FROM security_info INNER JOIN public.role r " +
                "ON r.role_id = security_info.role_id WHERE id = ?;";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                securityInfo = securityMapper.mapRow(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
        String query = "INSERT INTO security_info (id, username, password, email, role_id) " +
                "VALUES (?, ?, ?, ?, ?);";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
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
            e.printStackTrace();
        }

        return securityInfo;
    }

    public void deleteForAdmin(int userId) throws NoSuchUserException {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NoSuchUserException();
        }
        String query = "DELETE FROM security_info WHERE id = ?;";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
        ) {
            preparedStatement.setInt(1, userId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Optional<SecurityInfo> update(SecurityInfo updatedInfo, int userId)
            throws NoSuchUserException, EmailOrUsernameIsAlreadyUsedException {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NoSuchUserException();
        }
        SecurityInfo oldSecurityInfo = findById(userId).get();
        if (updatedInfo.getPassword() == null) {
            updatedInfo.setPassword(oldSecurityInfo.getPassword());
        }
        else {
            updatedInfo.setPassword(passwordEncoder.encode(updatedInfo.getPassword()));
        }
        if (updatedInfo.getEmail() == null) {
            updatedInfo.setEmail(oldSecurityInfo.getEmail());
        }
        if (updatedInfo.getUsername() == null) {
            updatedInfo.setUsername(oldSecurityInfo.getUsername());
        }
        if (updatedInfo.getRole() == null) {
            updatedInfo.setRole(oldSecurityInfo.getRole());
        }
        if (userRepository.emailOrUsernameIsUsed(updatedInfo.getEmail(), updatedInfo.getUsername())) {
            throw new EmailOrUsernameIsAlreadyUsedException();
        }
        Optional<SecurityInfo> securityInfo = Optional.empty();
        String query = "UPDATE security_info SET email = ?, password = ?, username = ?, role_id = ?" +
                " WHERE id = ?;";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
        ) {
            preparedStatement.setString(1, updatedInfo.getEmail());
            preparedStatement.setString(2, updatedInfo.getPassword());
            preparedStatement.setString(3, updatedInfo.getUsername());
            Role role = updatedInfo.getRole();
            preparedStatement.setInt(4, role == Role.ADMIN ? 1 : 2);
            preparedStatement.setInt(5, userId);

            preparedStatement.executeUpdate();
            ResultSet res = preparedStatement.getGeneratedKeys();
            if (res.next()) {
                securityInfo = findById(res.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return securityInfo;
    }
}
