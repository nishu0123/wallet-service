package com.rs.payments.wallet.controller;

import java.util.Optional;
import java.util.UUID;
import com.rs.payments.wallet.BaseIntegrationTest;
import com.rs.payments.wallet.dto.CreateUserRequest;
import com.rs.payments.wallet.dto.CreateWalletRequest;
import com.rs.payments.wallet.model.User;
import com.rs.payments.wallet.model.Wallet;
import com.rs.payments.wallet.repository.UserRepository;
import com.rs.payments.wallet.repository.WalletRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class WalletIntegrationTest extends BaseIntegrationTest {

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;



    @Test
    void shouldCreateWalletForExistingUser() {
        User user = new User();
        user.setUsername("walletuser");
        user.setEmail("wallet@example.com");
        user = userRepository.save(user);

        CreateWalletRequest request = new CreateWalletRequest();
        request.setUserId(user.getId());

        String url = "http://localhost:" + port + "/wallets";
        ResponseEntity<Wallet> response = restTemplate.postForEntity(url, request, Wallet.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    void shouldReturnNotFoundForNonExistentUser() {
        CreateWalletRequest request = new CreateWalletRequest();
        request.setUserId(UUID.randomUUID());

        String url = "http://localhost:" + port + "/wallets";
        try {
            restTemplate.postForEntity(url, request, String.class);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Test
    @DisplayName("Should automatically create a wallet when a user is registered")
    void shouldCreateWalletAutomatically() {
        // 1. Arrange
        CreateUserRequest request = new CreateUserRequest("auto_wallet_user", "wallet@test.com");
        String userUrl = "http://localhost:" + port + "/users";

        // 2. Act: Create the User
        ResponseEntity<User> userResponse = restTemplate.postForEntity(userUrl, request, User.class);
        UUID createdUserId = userResponse.getBody().getId();

        // 3. Assert: Check the Database directly for the Wallet
        // We expect the WalletService to have been triggered automatically
        Optional<Wallet> wallet = walletRepository.findByUserId(createdUserId);

        assertThat(wallet).isPresent();
        assertThat(wallet.get().getBalance()).isEqualByComparingTo("0.00");
    }
}
