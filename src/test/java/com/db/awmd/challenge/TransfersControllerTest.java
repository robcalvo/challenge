package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.repository.AccountsRepository;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.TransfersService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class TransfersControllerTest {
    private final String accountFromId = "Id-101";
    private final BigDecimal initialBalanceAccountFrom = BigDecimal.valueOf(120);
    private final Account accountFrom = new Account(accountFromId, initialBalanceAccountFrom);
    private final String accountToId = "Id-102";
    private final BigDecimal initialBalanceAccountTo = BigDecimal.valueOf(50);
    private final Account accountTo = new Account(accountToId, initialBalanceAccountTo);
    private MockMvc mockMvc;
    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private AccountsService accountsService;

    @SpyBean
    private TransfersService transfersService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void prepareEnvironment() {
        this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

        accountsRepository.createAccount(accountFrom);
        accountsRepository.createAccount(accountTo);
    }

    @After
    public void clearAccounts() {
        accountsRepository.clearAccounts();
    }

    @Test
    public void makeOKTransfer() throws Exception {
        BigDecimal amount = BigDecimal.valueOf(25);
        this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountFromId\":\"" + accountFromId + "\",\"accountToId\":\"" + accountToId + "\",\"amount\":" + amount + "}"))
                .andExpect(status().isCreated());

        Transfer transfer = new Transfer(accountFromId, accountToId, amount);
        Mockito.verify(transfersService).makeTransfer(transfer);

        assertThat(accountFrom.getBalance()).isEqualTo(initialBalanceAccountFrom.subtract(amount));
        assertThat(accountTo.getBalance()).isEqualByComparingTo(initialBalanceAccountTo.add(amount));
    }

    @Test
    public void transferFailsWithAccountNotFoundException() throws Exception {
        String nonExistingAccountId = "Id-999";
        BigDecimal amount = BigDecimal.valueOf(25);
        this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountFromId\":\"" + nonExistingAccountId + "\",\"accountToId\":\"" + accountToId + "\",\"amount\":" + amount + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Account not found: " + nonExistingAccountId));

        Transfer transfer = new Transfer(nonExistingAccountId, accountToId, amount);
        Mockito.verify(transfersService).makeTransfer(transfer);
    }

    @Test
    public void transferFailsWithInsufficientBalanceException() throws Exception {
        BigDecimal amount = BigDecimal.valueOf(10000);
        this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountFromId\":\"" + accountFromId + "\",\"accountToId\":\"" + accountToId + "\",\"amount\":" + amount + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Insufficient balance in account: " + accountFromId));

        Transfer transfer = new Transfer(accountFromId, accountToId, amount);
        Mockito.verify(transfersService).makeTransfer(transfer);
    }

    @Test
    public void transferFailsWithInvalidTransferExceptionForNegativeAmount() throws Exception {
        BigDecimal amount = BigDecimal.valueOf(-5);
        this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountFromId\":\"" + accountFromId + "\",\"accountToId\":\"" + accountToId + "\",\"amount\":" + amount + "}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void transferFailsWithInvalidTransferExceptionForSameAccount() throws Exception {
        BigDecimal amount = BigDecimal.valueOf(5);
        this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountFromId\":\"" + accountFromId + "\",\"accountToId\":\"" + accountFromId + "\",\"amount\":" + amount + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Origin account is equals to target account"));

        Transfer transfer = new Transfer(accountFromId, accountFromId, amount);
        Mockito.verify(transfersService).makeTransfer(transfer);
    }
}
