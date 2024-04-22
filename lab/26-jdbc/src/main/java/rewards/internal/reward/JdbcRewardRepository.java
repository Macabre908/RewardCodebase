package rewards.internal.reward;

import common.datetime.SimpleDate;
import org.springframework.jdbc.core.JdbcTemplate;
import rewards.AccountContribution;
import rewards.Dining;
import rewards.RewardConfirmation;

import javax.sql.DataSource;
import java.sql.*;

/**
 * JDBC implementation of a reward repository that records the result
 * of a reward transaction by inserting a reward confirmation record.
 */

public class JdbcRewardRepository implements RewardRepository {

	private DataSource dataSource;

	private JdbcTemplate jdbcTemplate;

	public JdbcRewardRepository(DataSource dataSource) {
		this.dataSource = dataSource;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public RewardConfirmation confirmReward(AccountContribution contribution, Dining dining) {
		String sql = "insert into T_REWARD (CONFIRMATION_NUMBER, REWARD_AMOUNT, REWARD_DATE, ACCOUNT_NUMBER, DINING_MERCHANT_NUMBER, DINING_DATE, DINING_AMOUNT) values (?, ?, ?, ?, ?, ?, ?)";
		String confirmationNumber = nextConfirmationNumber();

		this.jdbcTemplate.update(sql,
				confirmationNumber,
				contribution.getAmount().asBigDecimal(),
				new Date(SimpleDate.today().inMilliseconds()),
				contribution.getAccountNumber(),
				dining.getMerchantNumber(),
				new Date(dining.getDate().inMilliseconds()),
				dining.getAmount().asBigDecimal()
				);
		// Update the T_REWARD table with the new Reward
		
		return new RewardConfirmation(confirmationNumber, contribution);
	}

	private String nextConfirmationNumber() {
		String sql = "select next value for S_REWARD_CONFIRMATION_NUMBER from DUAL_REWARD_CONFIRMATION_NUMBER";
		String nextValue;
		
		nextValue = this.jdbcTemplate.queryForObject(sql, String.class);
		
		return nextValue;
	}
}
