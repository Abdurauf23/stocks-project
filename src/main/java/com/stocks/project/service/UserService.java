package com.stocks.project.service;

import com.stocks.project.exception.EmailOrUsernameIsAlreadyUsedException;
import com.stocks.project.exception.NoFirstNameException;
import com.stocks.project.exception.NoStockWithThisNameException;
import com.stocks.project.exception.NoSuchUserException;
import com.stocks.project.model.Role;
import com.stocks.project.model.StockUser;
import com.stocks.project.model.UserRegistrationDTO;
import com.stocks.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<StockUser> findAll() {
        return userRepository.findAll();
    }

    public Optional<StockUser> findById(int id) {
        return userRepository.findById(id);
    }

    public Optional<StockUser> createUser(StockUser stockUser) throws NoFirstNameException {
        return userRepository.createUser(stockUser);
    }

    public void delete(int userId) throws NoSuchUserException {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        if (isAdmin(login)) {
            userRepository.deleteForAdmin(userId);
        } else {
            userRepository.deleteForUser(userId);
        }
    }

    public Optional<StockUser> updateUser(StockUser updatedStockUser, int userId) throws NoSuchUserException {
        return userRepository.updateUser(updatedStockUser, userId);
    }

    public void register(UserRegistrationDTO dto, Role role) throws EmailOrUsernameIsAlreadyUsedException {
        userRepository.register(dto, role);
    }

    public List<?> getAllFavouriteStocks(int userId) {
        return userRepository.getAllFavouriteStocks(userId);
    }

    public void addStockToFavourite(int userId, String stockName) throws NoStockWithThisNameException, NoSuchUserException {
        userRepository.addStockToFavourite(userId, stockName);
    }

    public void deleteStockFromFavourite(int userId, String stockName)
            throws NoStockWithThisNameException, NoSuchUserException {
        userRepository.deleteStockFromFavourite(userId, stockName);
    }

    public boolean isAdmin(String login) {
        return userRepository.isAdminByLogin(login);
    }

    public boolean isSame(String login, int id) {
        return userRepository.isSamePerson(login, id);
    }
}
