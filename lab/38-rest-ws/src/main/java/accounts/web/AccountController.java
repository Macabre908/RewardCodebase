package accounts.web;

import accounts.AccountManager;
import common.money.Percentage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import rewards.internal.account.Account;
import rewards.internal.account.Beneficiary;

import java.net.URI;
import java.util.HashMap;
import java.util.List;

/**
 * A controller handling requests for CRUD operations on Accounts and their
 * Beneficiaries.
 */
@RestController
public class AccountController {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private AccountManager accountManager;

	/**
	 * Creates a new AccountController with a given account manager.
	 */
	@Autowired
	public AccountController(AccountManager accountManager) {
		this.accountManager = accountManager;
	}

	@GetMapping(value = "/accounts")
	public List<Account> accountSummary() {
		return accountManager.getAllAccounts();
	}

	@GetMapping(value = "/accounts/{id}")
	@ResponseBody
	public Account accountDetails(@PathVariable int id) {
		return retrieveAccount(id);
	}

	/**
	 * Creates a new Account, setting its URL as the Location header on the
	 * response.
	 */
	@PostMapping("/accounts")
	public ResponseEntity<Void> createAccount(@RequestBody Account newAccount) {
		// Saving the account also sets its entity Id
		Account account = accountManager.save(newAccount);

		// Return a ResponseEntity - it will be used to build the
		// HttpServletResponse.

		return entityWithLocation(account.getEntityId());
	}

	private ResponseEntity<Void> entityWithLocation(Object resourceId) {

		URI uri = ServletUriComponentsBuilder
				.fromCurrentRequestUri()
				.path("/{id}")
				.buildAndExpand(resourceId)
				.toUri();

		return ResponseEntity.created(uri).build();
	}

	@GetMapping(value = "/accounts/{accountId}/beneficiaries/{beneficiaryName}")
	public Beneficiary getBeneficiary(@PathVariable("accountId") int accountId,
			@PathVariable("beneficiaryName") String beneficiaryName) {
		return retrieveAccount(accountId).getBeneficiary(beneficiaryName);
	}

	@PostMapping("/accounts/{accountId}/beneficiaries")
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<Void> addBeneficiary(@PathVariable long accountId, @RequestBody String beneficiaryName) {

		accountManager.addBeneficiary(accountId, beneficiaryName);
		
		return entityWithLocation(beneficiaryName);
	}

	@DeleteMapping("/accounts/{accountId}/beneficiaries/{beneficiaryName}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void removeBeneficiary(@PathVariable long accountId, @PathVariable String beneficiaryName) {
		Account account = accountManager.getAccount(accountId);
		if (account == null) {
			throw new IllegalArgumentException("No such account with id " + accountId);
		}
		Beneficiary b = account.getBeneficiary(beneficiaryName);
		if (account.getBeneficiaries().size() != 1 && (!b.getAllocationPercentage().equals(Percentage.zero()))) {
			throw new RuntimeException("Logic to rebalance Beneficiaries not defined.");
		}

		accountManager.removeBeneficiary(accountId, beneficiaryName, new HashMap<String, Percentage>());
	}

	/**
	 * Maps IllegalArgumentExceptions to a 404 Not Found HTTP status code.
	 */
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler({ IllegalArgumentException.class })
	public void handleNotFound(Exception ex) {
		logger.error("Exception is: ", ex);
		// just return empty 404
	}
	@ResponseStatus(HttpStatus.CONFLICT)
	@ExceptionHandler({DataIntegrityViolationException.class})
	public void dataIntegrityException(Exception ex) {
		logger.error("Exception is: " + ex);
	}
	
	/**
	 * Finds the Account with the given id, throwing an IllegalArgumentException
	 * if there is no such Account.
	 */
	private Account retrieveAccount(long accountId) throws IllegalArgumentException {
		Account account = accountManager.getAccount(accountId);
		if (account == null) {
			throw new IllegalArgumentException("No such account with id " + accountId);
		}
		return account;
	}

}
