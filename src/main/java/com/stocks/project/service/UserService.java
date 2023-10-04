package com.stocks.project.service;

import com.stocks.project.exception.NoFirstNameException;
import com.stocks.project.exception.NoSuchUserException;
import com.stocks.project.model.User;
import com.stocks.project.model.UserSecurityDTO;
import com.stocks.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(int id) {
        return userRepository.findById(id);
    }

    public Optional<User> createUser(User user) throws NoFirstNameException {
        return userRepository.createUser(user);
    }

    public void deleteUser(int userId) throws NoSuchUserException {
        userRepository.delete(userId);
    }

    public Optional<User> updateUser(User updatedUser, int userId) throws NoSuchUserException {
        return userRepository.updateUser(updatedUser, userId);
    }

    public Optional<User> register(UserSecurityDTO dto) {
        return userRepository.register(dto);
    }

    public List<?> getAllFavouriteStocks(int userId) {
        return userRepository.getAllFavouriteStocks(userId);
    }
}
