package rewards.internal.account;

import common.money.MonetaryAmount;
import common.money.Percentage;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Loads accounts from a data source using the JDBC API.
 */

public class JdbcAccountRepository implements AccountRepository {

	private DataSource dataSource;

	private JdbcTemplate jdbcTemplate;

	public JdbcAccountRepository(DataSource dataSource) {
		this.dataSource = dataSource;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	public Account findByCreditCard(String creditCardNumber) {
		String sql = "select a.ID as ID, a.NUMBER as ACCOUNT_NUMBER, a.NAME as ACCOUNT_NAME, c.NUMBER as CREDIT_CARD_NUMBER, " +
			"	b.NAME as BENEFICIARY_NAME, b.ALLOCATION_PERCENTAGE as BENEFICIARY_ALLOCATION_PERCENTAGE, b.SAVINGS as BENEFICIARY_SAVINGS " +
			"from T_ACCOUNT a, T_ACCOUNT_CREDIT_CARD c " +
			"left outer join T_ACCOUNT_BENEFICIARY b " +
			"on a.ID = b.ACCOUNT_ID " +
			"where c.ACCOUNT_ID = a.ID and c.NUMBER = ?";
		
		Account account = null;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = dataSource.getConnection();
			ps = conn.prepareStatement(sql);
			ps.setString(1, creditCardNumber);
			rs = ps.executeQuery();
			account = mapAccount(rs);
		} catch (SQLException e) {
			throw new RuntimeException("SQL exception occurred finding by credit card number", e);
		} finally {
			if (rs != null) {
				try {
					// Close to prevent database cursor exhaustion
					rs.close();
				} catch (SQLException ex) {
				}
			}
			if (ps != null) {
				try {
					// Close to prevent database cursor exhaustion
					ps.close();
				} catch (SQLException ex) {
				}
			}
			if (conn != null) {
				try {
					// Close to prevent database connection exhaustion
					conn.close();
				} catch (SQLException ex) {
				}
			}
		}
		return account;
	}
	public void updateBeneficiaries(Account account) {
		String sql = "update T_ACCOUNT_BENEFICIARY SET SAVINGS = ? where ACCOUNT_ID = ? and NAME = ?";

		for(Beneficiary beneficiary: account.getBeneficiaries()) {
			this.jdbcTemplate.update(
					sql,
					beneficiary.getSavings().asBigDecimal(),
					account.getEntityId(),
					beneficiary.getName()
			);
		}

	}

	/**
	 * Map the rows returned from the join of T_ACCOUNT and T_ACCOUNT_BENEFICIARY to an fully-reconstituted Account
	 * aggregate.
	 * 
	 * @param rs the set of rows returned from the query
	 * @return the mapped Account aggregate
	 * @throws SQLException an exception occurred extracting data from the result set
	 */
	private Account mapAccount(ResultSet rs) throws SQLException {
		Account account = null;
		while (rs.next()) {
			if (account == null) {
				String number = rs.getString("ACCOUNT_NUMBER");
				String name = rs.getString("ACCOUNT_NAME");
				account = new Account(number, name);
				// set internal entity identifier (primary key)
				account.setEntityId(rs.getLong("ID"));
			}
			account.restoreBeneficiary(mapBeneficiary(rs));
		}
		if (account == null) {
			// no rows returned - throw an empty result exception
			throw new EmptyResultDataAccessException(1);
		}
		return account;
	}

	/**
	 * Maps the beneficiary columns in a single row to an AllocatedBeneficiary object.
	 * 
	 * @param rs the result set with its cursor positioned at the current row
	 * @return an allocated beneficiary
	 * @throws SQLException an exception occurred extracting data from the result set
	 */
	private Beneficiary mapBeneficiary(ResultSet rs) throws SQLException {
		String name = rs.getString("BENEFICIARY_NAME");
		MonetaryAmount savings = MonetaryAmount.valueOf(rs.getString("BENEFICIARY_SAVINGS"));
		Percentage allocationPercentage = Percentage.valueOf(rs.getString("BENEFICIARY_ALLOCATION_PERCENTAGE"));
		return new Beneficiary(name, allocationPercentage, savings);
	}
}
