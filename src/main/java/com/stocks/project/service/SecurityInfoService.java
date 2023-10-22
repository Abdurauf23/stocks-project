package com.stocks.project.service;

import com.stocks.project.exception.EmailOrUsernameIsAlreadyUsedException;
import com.stocks.project.exception.NoSuchUserException;
import com.stocks.project.exception.NotEnoughDataException;
import com.stocks.project.model.SecurityInfo;
import com.stocks.project.repository.SecurityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SecurityInfoService {
    private final SecurityRepository securityRepository;

    @Autowired
    public SecurityInfoService(SecurityRepository securityRepository) {
        this.securityRepository = securityRepository;
    }

    public List<SecurityInfo> findAll() {
        return securityRepository.findAll();
    }

    public Optional<SecurityInfo> findById(int id) {
        return securityRepository.findById(id);
    }

    public Optional<SecurityInfo> create(SecurityInfo securityInfo, int userId)
            throws NoSuchUserException, NotEnoughDataException, EmailOrUsernameIsAlreadyUsedException {
        return securityRepository.createSecurityInfo(securityInfo, userId);
    }

    public void delete(int userId) throws NoSuchUserException {
        securityRepository.delete(userId);
    }

    public Optional<SecurityInfo> update(SecurityInfo updatedInfo, int userId)
            throws NoSuchUserException, EmailOrUsernameIsAlreadyUsedException {
        return securityRepository.update(updatedInfo, userId);
    }
}
