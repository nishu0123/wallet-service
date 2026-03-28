package com.rs.payments.wallet.service.impl;

import com.rs.payments.wallet.exception.BadRequestException;
import com.rs.payments.wallet.exception.ResourceNotFoundException;
import com.rs.payments.wallet.model.User;
import com.rs.payments.wallet.model.Wallet;
import com.rs.payments.wallet.repository.UserRepository;
import com.rs.payments.wallet.repository.WalletRepository;
import com.rs.payments.wallet.service.WalletService;
import java.util.UUID;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class WalletServiceImpl implements WalletService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    public WalletServiceImpl(UserRepository userRepository, WalletRepository walletRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
    }

    /*
    @Override
    public Wallet createWalletForUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wallet wallet = new Wallet();
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setUser(user);
        user.setWallet(wallet);

        user = userRepository.save(user); // Cascade saves wallet
        return user.getWallet();
    }
}

*/


    @Override
    @Transactional
    public Wallet createWalletForUser(UUID userId) {
        // 1. Requirement: User not found returns 404
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2. Requirement: User already has wallet returns 400
        // Check the existing user object before creating a new one

        if (user.getWallet() != null) {
            throw new BadRequestException("User already has a wallet");
        }

        // 3. Requirement: balance = 0
        Wallet wallet = new Wallet();
        wallet.setBalance(BigDecimal.ZERO);

        // Establishing the Bi-directional link
        wallet.setUser(user);
        user.setWallet(wallet);

        // 4. Save and Return
        user = userRepository.save(user); // Cascade saves wallet
        return user.getWallet();
    }


}