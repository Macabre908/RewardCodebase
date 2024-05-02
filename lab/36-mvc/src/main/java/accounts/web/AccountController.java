package accounts.web;

import accounts.AccountManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import rewards.internal.account.Account;

import java.util.List;

/**
 * A Spring MVC REST Controller handling requests to retrieve Account information.
 *
 * Note that some of the Account related classes are imported from the
 * rewards-db project:
 *
 * -Domain objects: Account and Beneficiary
 * -Service layer: AccountManager interface
 * -Repository layer: AccountRepository interface
 *
 */

@RestController
public class AccountController {

	private final AccountManager accountManager;

	/**
	 * Creates a new AccountController with a given account manager.
	 */
	@Autowired
	public AccountController(AccountManager accountManager) {
		this.accountManager = accountManager;
	}

	@GetMapping("/accounts")
	public List<Account> accountList() {

		return this.accountManager.getAllAccounts();

	}

	@GetMapping("/accounts/{entityId}")
	public Account accountDetails(@PathVariable Long entityId){

		return this.accountManager.getAccount(entityId);

	}

}
