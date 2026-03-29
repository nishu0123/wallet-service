package com.rs.payments.wallet.service.impl;

import com.rs.payments.wallet.model.User;
import com.rs.payments.wallet.repository.UserRepository;
import com.rs.payments.wallet.service.UserService;
import com.rs.payments.wallet.service.WalletService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final WalletService walletService;

    public UserServiceImpl(UserRepository userRepository, WalletService walletService) {
        this.userRepository = userRepository;
        this.walletService = walletService;
    }

    @Transactional
    @Override
    public User createUser(User user) {
        User savedUser = userRepository.save(user);
        walletService.createWalletForUser(savedUser.getId()); // Must call this!
        return savedUser;
    }
}