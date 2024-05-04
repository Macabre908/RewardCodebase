package accounts.client;

import common.money.Percentage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;
import rewards.internal.account.Account;
import rewards.internal.account.Beneficiary;

import java.net.URI;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class AccountClientTests {

	private static final String BASE_URL = "http://localhost:8080";
	
	private RestTemplate restTemplate = new RestTemplate();
	private Random random = new Random();
	
	@Test
	public void listAccounts() {

		Account[] accounts = restTemplate.getForObject(BASE_URL + "/accounts", Account[].class);
		
		assertNotNull(accounts);
		assertTrue(accounts.length >= 21);
		assertEquals("Keith and Keri Donald", accounts[0].getName());
		assertEquals(2, accounts[0].getBeneficiaries().size());
		assertEquals(Percentage.valueOf("50%"), accounts[0].getBeneficiary("Annabelle").getAllocationPercentage());
	}
	
	@Test
	public void getAccount() {

		UriTemplate uri = new UriTemplate(BASE_URL + "/accounts/{id}");

		Account account = restTemplate.getForObject(uri.expand(0), Account.class);
		
		assertNotNull(account);
		assertEquals("Keith and Keri Donald", account.getName());
		assertEquals(2, account.getBeneficiaries().size());
		assertEquals(Percentage.valueOf("50%"), account.getBeneficiary("Annabelle").getAllocationPercentage());
	}
	
	@Test
	public void createAccount() {
		// Use a unique number to avoid conflicts
		String number = String.format("12345%4d", random.nextInt(10000));
		Account account = new Account(number, "John Doe");
		account.addBeneficiary("Jane Doe");

		URI newAccountLocation = restTemplate.postForLocation(BASE_URL + "/accounts", account);

		Account retrievedAccount = restTemplate.getForObject(newAccountLocation, Account.class);
		
		assertEquals(account.getNumber(), retrievedAccount.getNumber());
		
		Beneficiary accountBeneficiary = account.getBeneficiaries().iterator().next();
		Beneficiary retrievedAccountBeneficiary = retrievedAccount.getBeneficiaries().iterator().next();
		
		assertEquals(accountBeneficiary.getName(), retrievedAccountBeneficiary.getName());
		assertNotNull(retrievedAccount.getEntityId());
	}
	
	@Test
	public void addAndDeleteBeneficiary() {


		URI uri  = restTemplate.postForLocation(BASE_URL + "/accounts/{accountId}/beneficiaries", "David", 1);

		Beneficiary newBeneficiary = restTemplate.getForObject(uri, Beneficiary.class);
		
		assertNotNull(newBeneficiary);
		assertEquals("David", newBeneficiary.getName());

		restTemplate.delete(uri);

		HttpClientErrorException httpClientErrorException = assertThrows(HttpClientErrorException.class, () -> {
			System.out.println("You SHOULD get the exception \"No such beneficiary with name 'David'\" in the server.");

			restTemplate.getForObject(uri, Beneficiary.class);

		});
		assertEquals(HttpStatus.NOT_FOUND, httpClientErrorException.getStatusCode());
	}
	
}
