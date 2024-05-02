package accounts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import config.AccountsConfig;

/**
 * Spring Boot application.
 */
@SpringBootApplication
@Import(AccountsConfig.class)
public class AccountsApplication {

	public static void main(String[] args) {
		SpringApplication.run(AccountsApplication.class, args);
	}

}

/**
 * TODO-12: Make this server listen on port 8088.
 * - Go to "application.properties" and set the appropriate property
 * - Once the application restarts, access http://localhost:8088
 */
