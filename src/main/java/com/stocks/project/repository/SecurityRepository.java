package com.stocks.project.repository;

import com.stocks.project.model.Role;
import com.stocks.project.model.SecurityInfo;
import com.stocks.project.utils.SecurityMapper;
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
public class SecurityRepository {
    private final DataSource dataSource;
    private final SecurityMapper securityMapper;

    @Autowired
    public SecurityRepository(DataSource dataSource, SecurityMapper securityMapper) {
        this.dataSource = dataSource;
        this.securityMapper = securityMapper;
    }

    public List<SecurityInfo> findAll() {
        List<SecurityInfo> securityInfos = new ArrayList<>();
        String query = "SELECT * FROM security_info INNER JOIN public.role r ON r.role_id = security_info.role_id;";
        try(
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query)
        ) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                securityInfos.add(securityMapper.mapRow(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return securityInfos;
    }

    public SecurityInfo findById(int id) {
        SecurityInfo securityInfo = null;
        String query = "SELECT * FROM security_info INNER JOIN public.role r " +
                "ON r.role_id = security_info.role_id WHERE id = ?;";
        try(
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                securityInfo = securityMapper.mapRow(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return securityInfo;
    }

    public SecurityInfo createSecurityInfo(SecurityInfo newSecurityInfo, int userId) {
        SecurityInfo securityInfo = null;
        String query = "INSERT INTO security_info (id, username, password, email, role_id) " +
                "VALUES (?, ?, ?, ?, ?);";
        try(
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement =
                        connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
        ) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, newSecurityInfo.getUsername());
            preparedStatement.setString(3, newSecurityInfo.getPassword());
            preparedStatement.setString(4, newSecurityInfo.getEmail());
            Role role = newSecurityInfo.getRole();
            preparedStatement.setInt(5, role == Role.ADMIN? 1 : 2);

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

    public void delete(int userId) {
        String query = "DELETE FROM security_info WHERE id = ?;";
        try(
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement =
                        connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
        ) {
            preparedStatement.setInt(1, userId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public SecurityInfo update(SecurityInfo updatedInfo, int userId) {
        SecurityInfo securityInfo = null;
        String query = "UPDATE security_info SET email = ?, password = ?, username = ?, role_id = ? WHERE id = ?;";
        try(
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement =
                        connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
        ) {
            preparedStatement.setString(1, updatedInfo.getEmail());
            preparedStatement.setString(2, updatedInfo.getPassword());
            preparedStatement.setString(3, updatedInfo.getUsername());
            Role role = updatedInfo.getRole();
            preparedStatement.setInt(4, role == Role.ADMIN? 1 : 2);
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
