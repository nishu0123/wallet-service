package com.rs.payments.wallet.model;

import java.util.List;
import java.util.UUID;

import com.rs.payments.wallet.dto.DepositRequest;
import com.rs.payments.wallet.repository.TransactionRepository;
import com.rs.payments.wallet.service.UserService;
import com.rs.payments.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ActiveProfiles("test")
//@AutoConfigureTestDatabase
class TransactionTest {

    @Autowired
    private TestRestTemplate restTemplate; // This fixes the first error

    private UUID createdWalletId; // This will hold the ID for our tests

    @Autowired
    private UserService userService; // You'll need this to create the user

    @Autowired
    private WalletService walletService; // You'll need this to create the wallet

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    @DisplayName("Should create transaction with data")
    void shouldCreateTransaction() {
        Wallet wallet = new Wallet();
        wallet.setId(UUID.randomUUID());
        LocalDateTime now = LocalDateTime.now();
        
        UUID txId = UUID.randomUUID();
        Transaction tx = new Transaction(txId, wallet, BigDecimal.TEN, TransactionType.DEPOSIT, now, "Test");

        assertEquals(txId, tx.getId());
        assertEquals(wallet, tx.getWallet());
        assertEquals(BigDecimal.TEN, tx.getAmount());
        assertEquals(TransactionType.DEPOSIT, tx.getType());
        assertEquals(now, tx.getTimestamp());
        assertEquals("Test", tx.getDescription());
    }

    @Test
    @DisplayName("Should update transaction fields")
    void shouldUpdateTransactionFields() {
        Transaction tx = new Transaction();
        Wallet wallet = new Wallet();
        LocalDateTime now = LocalDateTime.now();

        UUID txId = UUID.randomUUID();
        tx.setId(txId);
        tx.setWallet(wallet);
        tx.setAmount(BigDecimal.ONE);
        tx.setType(TransactionType.WITHDRAWAL);
        tx.setTimestamp(now);
        tx.setDescription("Desc");

        assertEquals(txId, tx.getId());
        assertEquals(wallet, tx.getWallet());
        assertEquals(BigDecimal.ONE, tx.getAmount());
        assertEquals(TransactionType.WITHDRAWAL, tx.getType());
        assertEquals(now, tx.getTimestamp());
        assertEquals("Desc", tx.getDescription());
    }

    @Test
    @DisplayName("Should test equals and hashCode")
    void shouldTestEqualsAndHashCode() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Transaction tx1 = new Transaction();
        tx1.setId(id1);
        Transaction tx2 = new Transaction();
        tx2.setId(id1);
        Transaction tx3 = new Transaction();
        tx3.setId(id2);

        assertEquals(tx1, tx2);
        assertNotEquals(tx1, tx3);
        assertEquals(tx1.hashCode(), tx2.hashCode());
    }

    @BeforeEach
    void setUp() {
        // 1. Create a User first (since Wallet needs a User)
        User user = new User();
        user.setUsername("Test User");
        user.setEmail("test@example.com");

        // Save via your actual service or repository
        User savedUser = userService.createUser(user);

        // 2. Create the Wallet for that user
        Wallet wallet = walletService.createWalletForUser(savedUser.getId());
        this.createdWalletId = wallet.getId(); // This fixes the second error
    }

    @Test
    @DisplayName("Should return 400 when deposit amount is zero or negative")
    void shouldReturn400ForInvalidAmount() {
        UUID walletId = UUID.randomUUID();
        // Amount is -50.00, which should violate @Positive
        DepositRequest invalidRequest = new DepositRequest(new BigDecimal("-50.00"));

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/wallets/" + walletId + "/deposit",
                invalidRequest,
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Concurrency: Should handle multiple simultaneous deposits correctly")
    void shouldHandleConcurrentDeposits() throws InterruptedException {
        UUID walletId = createdWalletId; // A wallet with 0 balance
        BigDecimal depositAmount = new BigDecimal("10.00");
        int threads = 10;

        ExecutorService service = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            service.execute(() -> {
                restTemplate.postForEntity("/api/wallets/" + walletId + "/deposit",
                        new DepositRequest(depositAmount), Wallet.class);
                latch.countDown();
            });
        }
        latch.await();

        // If not atomic, balance might be less than 100.00 (lost updates)
        Wallet finalWallet = restTemplate.getForObject("/api/wallets/" + walletId, Wallet.class);
        assertEquals(0, new BigDecimal("100.00").compareTo(finalWallet.getBalance()));
    }


    @Test
    @DisplayName("POST /wallets/{id}/deposit - Should update balance and record transaction")
    void shouldDepositFundsSuccessfully() {
        // 1. Arrange
        BigDecimal depositAmount = new BigDecimal("100.00");
        DepositRequest request = new DepositRequest(depositAmount);

        // 2. Act
        ResponseEntity<Wallet> response = restTemplate.postForEntity(
                "/api/wallets/" + createdWalletId + "/deposit",
                request,
                Wallet.class
        );

        // 3. Assert - Phase 1: Check the API Response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, depositAmount.compareTo(response.getBody().getBalance()));

        // 4. Assert - Phase 2: Check the Database (The "Transaction Record" requirement)
        List<Transaction> transactions = transactionRepository.findByWalletId(createdWalletId);
        assertFalse(transactions.isEmpty(), "Transaction record should have been created");
        assertEquals(TransactionType.DEPOSIT, transactions.get(0).getType());
        assertEquals(0, depositAmount.compareTo(transactions.get(0).getAmount()));
    }
}
