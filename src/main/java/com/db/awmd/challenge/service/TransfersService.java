package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.InsufficientBalanceException;
import com.db.awmd.challenge.exception.InvalidTransferException;
import com.db.awmd.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransfersService {

    @Getter
    private final AccountsRepository accountsRepository;

    @Getter
    private final NotificationService notificationService;
    @Autowired
    private TransfersValidations transfersValidations;

    @Autowired
    public TransfersService(AccountsRepository accountsRepository, NotificationService notificationService) {
        this.accountsRepository = accountsRepository;
        this.notificationService = notificationService;
    }

    public void makeTransfer(Transfer transfer) throws AccountNotFoundException, InsufficientBalanceException, InvalidTransferException {
        final Account accountFrom = accountsRepository.getAccount(transfer.getAccountFromId());
        final Account accountTo = accountsRepository.getAccount(transfer.getAccountToId());

        transfersValidations.validate(accountFrom, accountTo, transfer);

        Transaction transaction = new Transaction(accountFrom, accountTo, transfer);
        transaction.run();
    }

    //TODO Consider refactor to move this inner class to an external class
    private class Transaction extends Thread {

        private final Account accountFrom;
        private final Account accountTo;
        private final Transfer transfer;

        public Transaction(Account accountFrom, Account accountTo, Transfer transfer) {
            this.accountFrom = accountFrom;
            this.accountTo = accountTo;
            this.transfer = transfer;
        }

        public void run() {
            // Acquire the lock of accountFrom
            synchronized (accountFrom) {
                accountFrom.withdraw(transfer.getAmount());

                // Acquire the lock of accountTo
                synchronized (accountTo) {
                    accountTo.deposit(transfer.getAmount());
                }
                // Release the lock of accountTo
            }
            // Release the lock of accountFrom

            notificationService.notifyAboutTransfer(accountFrom, "New transfer to account: " + accountTo.getAccountId() + ", amount: " + transfer.getAmount());
            notificationService.notifyAboutTransfer(accountTo, "New transfer from account: " + accountFrom.getAccountId() + ", amount: " + transfer.getAmount());
        }
    }

}