package com.stocks.project.service;

import com.stocks.project.exception.EmailOrUsernameIsAlreadyUsedException;
import com.stocks.project.exception.NoSuchUserException;
import com.stocks.project.exception.NotEnoughDataException;
import com.stocks.project.model.SecurityInfo;
import com.stocks.project.repository.SecurityInfoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SecurityInfoService {
    private final SecurityInfoRepository securityInfoRepository;

    public SecurityInfoService(SecurityInfoRepository securityInfoRepository) {
        this.securityInfoRepository = securityInfoRepository;
    }

    public List<SecurityInfo> findAll() {
        return securityInfoRepository.findAll();
    }

    public Optional<SecurityInfo> findById(int id) {
        return securityInfoRepository.findById(id);
    }

    public Optional<SecurityInfo> create(SecurityInfo securityInfo, int userId)
            throws NoSuchUserException, NotEnoughDataException, EmailOrUsernameIsAlreadyUsedException {
        return securityInfoRepository.createSecurityInfo(securityInfo, userId);
    }

    public void delete(int userId) throws NoSuchUserException {
        securityInfoRepository.deleteForAdmin(userId);
    }

    public Optional<SecurityInfo> update(SecurityInfo updatedInfo, int userId)
            throws NoSuchUserException, EmailOrUsernameIsAlreadyUsedException {
        return securityInfoRepository.update(updatedInfo, userId);
    }
}
