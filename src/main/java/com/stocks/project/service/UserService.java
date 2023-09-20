package com.stocks.project.service;

import com.stocks.project.model.User;
import com.stocks.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public User findById(int id) {
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        return userRepository.createUser(user);
    }

    public void deleteUser(int userId) {
        userRepository.delete(userId);
    }

    public User updateUser(User updatedUser, int userId) {
        return userRepository.updateUser(updatedUser, userId);
    }
}
