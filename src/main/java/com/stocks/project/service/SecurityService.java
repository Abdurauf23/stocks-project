package com.stocks.project.service;

import com.stocks.project.model.SecurityInfo;
import com.stocks.project.repository.SecurityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SecurityService {
    private final SecurityRepository securityRepository;

    @Autowired
    public SecurityService(SecurityRepository securityRepository) {
        this.securityRepository = securityRepository;
    }

    public List<SecurityInfo> findAll() {
        return securityRepository.findAll();
    }

    public SecurityInfo findById(int id) {
        return securityRepository.findById(id);
    }

    public SecurityInfo create(SecurityInfo securityInfo, int userId) {
        return securityRepository.createSecurityInfo(securityInfo, userId);
    }

    public void delete(int userId) {
        securityRepository.delete(userId);
    }

    public SecurityInfo update(SecurityInfo updatedInfo, int userId) {
        return securityRepository.update(updatedInfo, userId);
    }
}
