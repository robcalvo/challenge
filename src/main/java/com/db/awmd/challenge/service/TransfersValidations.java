package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.InsufficientBalanceException;
import com.db.awmd.challenge.exception.InvalidTransferException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TransfersValidations {

    public void validate(final Account accountFrom, final Account accountTo, final Transfer transfer) {
        validateAccountExists(transfer.getAccountFromId(), accountFrom);
        validateAccountExists(transfer.getAccountToId(), accountTo);
        validateEnoughBalance(transfer, accountFrom);
        validatePositiveAmountToTransfer(transfer);
        validateNonZeroAmountToTransfer(transfer);
        validateNotSameAccountInTransfer(transfer);
    }

    private void validateAccountExists(final String accountId, final Account account) {
        if (account == null) throw new AccountNotFoundException("Account not found: " + accountId);
    }

    private void validateEnoughBalance(final Transfer transfer, final Account accountFrom) {
        if (accountFrom.getBalance().compareTo(transfer.getAmount()) < 0)
            throw new InsufficientBalanceException("Insufficient balance in account: " + transfer.getAccountFromId());
    }

    private void validatePositiveAmountToTransfer(final Transfer transfer) {
        if (BigDecimal.ZERO.compareTo(transfer.getAmount()) > 0)
            throw new InvalidTransferException("Negative amount to transfer: " + transfer.getAmount());
    }

    private void validateNonZeroAmountToTransfer(final Transfer transfer) {
        if (BigDecimal.ZERO.compareTo(transfer.getAmount()) == 0)
            throw new InvalidTransferException("Amount to transfer is zero");
    }

    private void validateNotSameAccountInTransfer(final Transfer transfer) {
        if (transfer.getAccountToId().equals(transfer.getAccountFromId()))
            throw new InvalidTransferException("Origin account is equals to target account");
    }


}
