package accounts.client;

import common.money.Percentage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import rewards.internal.account.Account;
import rewards.internal.account.Beneficiary;

import java.net.URI;
import java.util.Random;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class AccountClientTests {

	@Autowired
	private TestRestTemplate restTemplate;

	private static final String BASE_URL = "";

	private Random random = new Random();

	@Test
	public void listAccounts() {
		String url = BASE_URL + "/accounts";
		// we have to use Account[] instead of List<Account>, or Jackson won't know what
		// type to unmarshal to
		Account[] accounts = restTemplate.getForObject(url, Account[].class);
		assertThat(accounts.length >= 21).isTrue();
		assertThat(accounts[0].getName()).isEqualTo("Keith and Keri Donald");
		assertThat(accounts[0].getBeneficiaries().size()).isEqualTo(2);
		assertThat(accounts[0].getBeneficiary("Annabelle").getAllocationPercentage()).isEqualTo(Percentage.valueOf("50%"));
	}

	@Test
	public void getAccount() {
		String url = BASE_URL + "/accounts/{accountId}";
		Account account = restTemplate.getForObject(url, Account.class, 0);
		assertThat(account.getName()).isEqualTo("Keith and Keri Donald");
		assertThat(account.getBeneficiaries().size()).isEqualTo(2);
		assertThat(account.getBeneficiary("Annabelle").getAllocationPercentage()).isEqualTo(Percentage.valueOf("50%"));
	}

	@Test
	public void createAccount() {
		String url = BASE_URL + "/accounts";
		// use a random account number to avoid conflict
		String number = String.format("12345%4d", random.nextInt(10000));
		Account account = new Account(number, "John Doe");
		account.addBeneficiary("Jane Doe");
		URI newAccountLocation = restTemplate.postForLocation(url, account);

		Account retrievedAccount = restTemplate.getForObject(newAccountLocation, Account.class);
		assertThat(retrievedAccount.getNumber()).isEqualTo(account.getNumber());

		Beneficiary accountBeneficiary = account.getBeneficiaries().iterator().next();
		Beneficiary retrievedAccountBeneficiary = retrievedAccount.getBeneficiaries().iterator().next();

		assertThat(retrievedAccountBeneficiary.getName()).isEqualTo(accountBeneficiary.getName());
		assertThat(retrievedAccount.getEntityId()).isNotNull();
	}

	@Test
	public void addAndDeleteBeneficiary() {
		// perform both add and delete to avoid issues with side effects
		String addUrl = BASE_URL + "/accounts/{accountId}/beneficiaries";
		URI newBeneficiaryLocation = restTemplate.postForLocation(addUrl, "David", 1);
		Beneficiary newBeneficiary = restTemplate.getForObject(newBeneficiaryLocation, Beneficiary.class);
		assertThat(newBeneficiary.getName()).isEqualTo("David");

		restTemplate.delete(newBeneficiaryLocation);

		ResponseEntity<Beneficiary> beneficiaryResponseEntity = restTemplate.getForEntity(newBeneficiaryLocation, Beneficiary.class);

		assertThat(beneficiaryResponseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}


}
