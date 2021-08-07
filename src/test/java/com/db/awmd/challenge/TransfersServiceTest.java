package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.InsufficientBalanceException;
import com.db.awmd.challenge.repository.AccountsRepository;
import com.db.awmd.challenge.service.NotificationService;
import com.db.awmd.challenge.service.TransfersService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TransfersServiceTest {

    private final String accountId1 = "Id-101";
    private final BigDecimal initialBalanceAccount1 = BigDecimal.valueOf(120);
    private final Account account1 = new Account(accountId1, initialBalanceAccount1);


    private final String accountId2 = "Id-102";
    private final BigDecimal initialBalanceAccount2 = BigDecimal.valueOf(50);
    private final Account account2 = new Account(accountId2, initialBalanceAccount2);

    @Autowired
    private AccountsRepository accountsRepository;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private TransfersService transfersService;


    @Before
    public void prepareAccounts() {
        accountsRepository.createAccount(account1);
        accountsRepository.createAccount(account2);
    }

    @After
    public void clearAccounts() {
        accountsRepository.clearAccounts();
    }

    @Test
    public void makeOneOKTransferAndNotify() {
        BigDecimal transferAmount = BigDecimal.valueOf(10);
        Transfer transfer = new Transfer(accountId1, accountId2, transferAmount);
        transfersService.makeTransfer(transfer);

        BigDecimal balanceAccount1 = accountsRepository.getAccount(accountId1).getBalance();
        assertThat(balanceAccount1).isEqualTo(initialBalanceAccount1.subtract(transferAmount));

        BigDecimal balanceAccount2 = accountsRepository.getAccount(accountId2).getBalance();
        assertThat(balanceAccount2).isEqualTo(initialBalanceAccount2.add(transferAmount));

        verify(notificationService, times(1)).notifyAboutTransfer(account1, "New transfer to account: " + account2.getAccountId() + ", amount: " + transfer.getAmount());
        verify(notificationService, times(1)).notifyAboutTransfer(account2, "New transfer from account: " + account1.getAccountId() + ", amount: " + transfer.getAmount());
    }

    @Test
    public void makeOneFailingTransferAndNotNotify() {
        BigDecimal transferAmount = BigDecimal.valueOf(200);
        Transfer transfer = new Transfer(accountId1, accountId2, transferAmount);

        try {
            transfersService.makeTransfer(transfer);
            fail("Should have failed when trying to transfer and amount higher than existing balance.");
        } catch (InsufficientBalanceException ibe) {
            assertThat(ibe.getMessage()).isEqualTo("Insufficient balance in account: " + accountId1);
        }

        BigDecimal balanceAccount1 = accountsRepository.getAccount(accountId1).getBalance();
        assertThat(balanceAccount1).isEqualTo(initialBalanceAccount1);

        BigDecimal balanceAccount2 = accountsRepository.getAccount(accountId2).getBalance();
        assertThat(balanceAccount2).isEqualTo(initialBalanceAccount2);

        verify(notificationService, times(0)).notifyAboutTransfer(account1, "New transfer to account: " + account2.getAccountId() + ", amount: " + transfer.getAmount());
        verify(notificationService, times(0)).notifyAboutTransfer(account2, "New transfer from account: " + account1.getAccountId() + ", amount: " + transfer.getAmount());
    }


    @Test
    public void makeManyConsecutiveTransfersSameDirection() {
        BigDecimal transferAmount = BigDecimal.valueOf(10);
        Transfer transfer = new Transfer(accountId1, accountId2, transferAmount);
        int numberOfTransfers = 10;

        //This loop will create several threads as method 'makeTransfer' generates a new Thread for each iteration
        for (int i = 0; i < numberOfTransfers; i++) {
            transfersService.makeTransfer(transfer);
        }

        BigDecimal balanceAccount1 = accountsRepository.getAccount(accountId1).getBalance();
        assertThat(balanceAccount1).isEqualTo(initialBalanceAccount1.subtract(transferAmount.multiply(BigDecimal.valueOf(numberOfTransfers))));

        BigDecimal balanceAccount2 = accountsRepository.getAccount(accountId2).getBalance();
        assertThat(balanceAccount2).isEqualTo(initialBalanceAccount2.add(transferAmount.multiply(BigDecimal.valueOf(numberOfTransfers))));
    }

    @Test
    public void makeManyConsecutiveTransfersBothDirections() {
        BigDecimal transferAmount1 = BigDecimal.valueOf(10);
        Transfer transfer1 = new Transfer(accountId1, accountId2, transferAmount1);
        BigDecimal transferAmount2 = BigDecimal.valueOf(2);
        Transfer transfer2 = new Transfer(accountId2, accountId1, transferAmount2);

        int numberOfPairTransfers = 10;

        //This loop will create several threads as method 'makeTransfer' generates a new Thread for each iteration
        for (int i = 0; i < numberOfPairTransfers; i++) {
            transfersService.makeTransfer(transfer1);
            transfersService.makeTransfer(transfer2);
        }

        BigDecimal balanceAccount1 = accountsRepository.getAccount(accountId1).getBalance();
        assertThat(balanceAccount1).isEqualTo(initialBalanceAccount1
                .subtract(transferAmount1.multiply(BigDecimal.valueOf(numberOfPairTransfers)))
                .add(transferAmount2.multiply(BigDecimal.valueOf(numberOfPairTransfers))));

        BigDecimal balanceAccount2 = accountsRepository.getAccount(accountId2).getBalance();
        assertThat(balanceAccount2).isEqualTo(initialBalanceAccount2
                .subtract(transferAmount2.multiply(BigDecimal.valueOf(numberOfPairTransfers)))
                .add(transferAmount1.multiply(BigDecimal.valueOf(numberOfPairTransfers))));
    }

    @Test
    public void makeManyMultiThreadedTransfersBothDirections() throws InterruptedException {
        BigDecimal transferAmount1 = BigDecimal.valueOf(10);
        Transfer transfer1 = new Transfer(accountId1, accountId2, transferAmount1);
        BigDecimal transferAmount2 = BigDecimal.valueOf(2);
        Transfer transfer2 = new Transfer(accountId2, accountId1, transferAmount2);

        int numberOfPairThreads = 0;
        List<Thread> listOfTreads = new ArrayList<>();

        for (int i = 0; i < numberOfPairThreads; i++) {
            Thread th1 = new Thread(() -> {
                transfersService.makeTransfer(transfer1);
            });
            listOfTreads.add(th1);
            Thread th2 = new Thread(() -> {
                transfersService.makeTransfer(transfer2);
            });
            listOfTreads.add(th2);
        }

        for(Thread thread: listOfTreads) {
            thread.join();
        }

        BigDecimal balanceAccount1 = accountsRepository.getAccount(accountId1).getBalance();
        assertThat(balanceAccount1).isEqualTo(initialBalanceAccount1
                .subtract(transferAmount1.multiply(BigDecimal.valueOf(numberOfPairThreads)))
                .add(transferAmount2.multiply(BigDecimal.valueOf(numberOfPairThreads))));

        BigDecimal balanceAccount2 = accountsRepository.getAccount(accountId2).getBalance();
        assertThat(balanceAccount2).isEqualTo(initialBalanceAccount2
                .subtract(transferAmount2.multiply(BigDecimal.valueOf(numberOfPairThreads)))
                .add(transferAmount1.multiply(BigDecimal.valueOf(numberOfPairThreads))));

    }



}
