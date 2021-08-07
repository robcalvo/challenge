package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.InsufficientBalanceException;
import com.db.awmd.challenge.exception.InvalidTransferException;
import com.db.awmd.challenge.service.TransfersValidations;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TransfersValidationsTest {

    @Autowired
    private TransfersValidations transfersValidations;

    private final String accountFromId = "Id-999";
    private final String accountToId = "Id-998";

    @Test
    public void validateAccountFromExists() {
        Account accountFrom = null;
        Account accountTo = null;
        Transfer transfer = new Transfer(accountFromId, accountToId, BigDecimal.valueOf(22));
        try {
            this.transfersValidations.validate(accountFrom, accountTo, transfer);
            fail("Should have failed when tranfering to non-existing account");
        } catch (AccountNotFoundException anfe) {
            assertThat(anfe.getMessage()).isEqualTo("Account not found: " + accountFromId);
        }
    }

    @Test
    public void validateAccountToExists() {
        Account accountFrom = new Account(accountFromId);
        Account accountTo = null;
        Transfer transfer = new Transfer(accountFromId, accountToId, BigDecimal.valueOf(22));
        try {
            this.transfersValidations.validate(accountFrom, accountTo, transfer);
            fail("Should have failed when tranfering to non-existing account");
        } catch (AccountNotFoundException anfe) {
            assertThat(anfe.getMessage()).isEqualTo("Account not found: " + accountToId);
        }
    }

    @Test
    public void validateEnoughBalance() {
        Account accountFrom = new Account(accountFromId, BigDecimal.valueOf(10));
        Account accountTo = new Account(accountToId);
        Transfer transfer = new Transfer(accountFromId, accountToId, BigDecimal.valueOf(12));
        try {
            this.transfersValidations.validate(accountFrom, accountTo, transfer);
            fail("Should have failed when trying to transfer and amount higher than existing balance.");
        } catch (InsufficientBalanceException ibe) {
            assertThat(ibe.getMessage()).isEqualTo("Insufficient balance in account: " + accountFromId);
        }
    }

    @Test
    public void validatePositiveAmountToTransfer() {
        BigDecimal amountToTransfer = BigDecimal.valueOf(-5);
        Account accountFrom = new Account(accountFromId, BigDecimal.valueOf(10));
        Account accountTo = new Account(accountToId);
        Transfer transfer = new Transfer(accountFromId, accountToId, amountToTransfer);
        try {
            this.transfersValidations.validate(accountFrom, accountTo, transfer);
            fail("Should have failed when trying to transfer a negative ammount.");
        } catch (InvalidTransferException ite) {
            assertThat(ite.getMessage()).isEqualTo("Negative amount to transfer: " + amountToTransfer);
        }
    }

    @Test
    public void validateNonZeroAmountToTransfer() {
        BigDecimal amountToTransfer = BigDecimal.ZERO;
        Account accountFrom = new Account(accountFromId, BigDecimal.valueOf(10));
        Account accountTo = new Account(accountToId);
        Transfer transfer = new Transfer(accountFromId, accountToId, amountToTransfer);
        try {
            this.transfersValidations.validate(accountFrom, accountTo, transfer);
            fail("Should have failed when trying to transfer amount equals to zero.");
        } catch (InvalidTransferException ite) {
            assertThat(ite.getMessage()).isEqualTo("Amount to transfer is zero");
        }
    }

    @Test
    public void validateNotSameAccountInTransfer() {
        String accountToId = accountFromId;
        Account accountFrom = new Account(accountFromId, BigDecimal.valueOf(100));
        Account accountTo = new Account(accountToId);
        Transfer transfer = new Transfer(accountFromId, accountToId, BigDecimal.valueOf(10));
        try {
            this.transfersValidations.validate(accountFrom, accountTo, transfer);
            fail("Should have failed when trying to transfer to same account as origin.");
        } catch (InvalidTransferException ite) {
            assertThat(ite.getMessage()).isEqualTo("Origin account is equals to target account");
        }
    }


}
