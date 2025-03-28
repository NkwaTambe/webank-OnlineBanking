package com.adorsys.webank.obs.serviceimpl;

import com.adorsys.webank.obs.dto.BalanceRequest;
import de.adorsys.webank.bank.api.domain.AmountBO;
import de.adorsys.webank.bank.api.domain.BalanceBO;
import de.adorsys.webank.bank.api.domain.BankAccountDetailsBO;
import de.adorsys.webank.bank.api.service.BankAccountService;
import com.adorsys.webank.obs.security.JwtCertValidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class BalanceServiceImplTest {

    @Mock
    private BankAccountService bankAccountService;

    @InjectMocks
    private BalanceServiceImpl balanceService;

    @Mock
    private JwtCertValidator jwtCertValidator;  // Mocking JwtCertValidator

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetBalance_WithValidBalance() {
        // Arrange
        BalanceRequest request = new BalanceRequest();
        request.setAccountID("12345");
        String accountCertificateJwt = "valid-jwt-token"; // Provide a mock valid JWT token

        AmountBO amount = new AmountBO();
        amount.setCurrency(Currency.getInstance("XAF"));
        amount.setAmount(new BigDecimal("1000"));
        BalanceBO balance = new BalanceBO();
        balance.setAmount(amount);
        BankAccountDetailsBO accountDetails = new BankAccountDetailsBO();
        accountDetails.setBalances(List.of(balance));

        when(bankAccountService.getAccountDetailsById(anyString(), any(LocalDateTime.class), anyBoolean()))
                .thenReturn(accountDetails);
        when(jwtCertValidator.validateJWT(accountCertificateJwt)).thenReturn(true);

        // Act
        String result = balanceService.getBalance(request, accountCertificateJwt);

        // Assert
        assertEquals("1000", result, "Balance should be '1000' when account has a valid balance");
    }

    @Test
    void testGetBalance_WithEmptyBalance() {
        // Arrange
        BalanceRequest request = new BalanceRequest();
        request.setAccountID("12345");
        String accountCertificateJwt = "valid-jwt-token"; // Provide a mock valid JWT token

        BankAccountDetailsBO accountDetails = new BankAccountDetailsBO();
        accountDetails.setBalances(Collections.emptyList());

        when(bankAccountService.getAccountDetailsById(anyString(), any(LocalDateTime.class), anyBoolean()))
                .thenReturn(accountDetails);
        when(jwtCertValidator.validateJWT(accountCertificateJwt)).thenReturn(true);

        // Act
        String result = balanceService.getBalance(request, accountCertificateJwt);

        // Assert
        assertEquals("Balance empty", result, "Balance should be empty when no balance is found.");
    }

    @Test
    void testGetBalance_WithNullAccountDetails() {
        // Arrange
        BalanceRequest request = new BalanceRequest();
        request.setAccountID("12345");
        String accountCertificateJwt = "valid-jwt-token"; // Provide a mock valid JWT token

        when(bankAccountService.getAccountDetailsById(anyString(), any(LocalDateTime.class), anyBoolean()))
                .thenReturn(null);
        when(jwtCertValidator.validateJWT(accountCertificateJwt)).thenReturn(true);

        // Act
        String result = balanceService.getBalance(request, accountCertificateJwt);

        // Assert
        assertEquals("Balance empty", result, "Balance should be empty when account details are null.");
    }

    @Test
    void testGetBalance_WithInvalidJWT() {
        // Arrange
        BalanceRequest request = new BalanceRequest();
        request.setAccountID("12345");
        String accountCertificateJwt = "invalid-jwt-token"; // Provide an invalid JWT token

        when(jwtCertValidator.validateJWT(accountCertificateJwt)).thenReturn(false);

        // Act
        String result = balanceService.getBalance(request, accountCertificateJwt);

        // Assert
        assertEquals("Invalid certificate or JWT. Account creation failed", result, "Should return error message for invalid JWT.");
    }
}
