package huorong.cockroachDB;

import org.postgresql.ds.PGSimpleDataSource;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author lzx
 * @date 2023/7/18 13:58
 * @description: TODO
 */

public class BasicExample {
    public static void main(String[] args) {
        // Configure the database connection.
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setApplicationName("docs_simplecrud_jdbc");
        ds.setUrl(System.getenv("JDBC_DATABASE_URL"));

        // Create DAO.
        BasicExampleDAO dao = new BasicExampleDAO(ds);

        // Test our retry handling logic if FORCE_RETRY is true.  This
        // method is only used to test the retry logic.  It is not
        // necessary in production code.
        dao.testRetryHandling();

        // Create the accounts table if it doesn't exist
        dao.createAccountsTable();

        // Insert a few accounts "by hand", using INSERTs on the backend.
        Map<String, String> balances = new HashMap<>();
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        balances.put(id1.toString(), "1000");
        balances.put(id2.toString(), "250");
        int updatedAccounts = dao.updateAccounts(balances);
        System.out.printf("BasicExampleDAO.updateAccounts:\n    => %s total updated accounts\n", updatedAccounts);

        // How much money is in these accounts?
        BigDecimal balance1 = dao.getAccountBalance(id1);
        BigDecimal balance2 = dao.getAccountBalance(id2);
        System.out.printf("main:\n    => Account balances at time '%s':\n    ID %s => $%s\n    ID %s => $%s\n", LocalTime.now(), 1, balance1, 2, balance2);

        // Transfer $100 from account 1 to account 2
        UUID fromAccount = UUID.randomUUID();
        UUID toAccount = UUID.randomUUID();
        BigDecimal transferAmount = BigDecimal.valueOf(100);
        int transferredAccounts = dao.transferFunds(fromAccount, toAccount, transferAmount);
        if (transferredAccounts != -1) {
            System.out.printf("BasicExampleDAO.transferFunds:\n    => $%s transferred between accounts %s and %s, %s rows updated\n", transferAmount, fromAccount, toAccount, transferredAccounts);
        }

        balance1 = dao.getAccountBalance(id1);
        balance2 = dao.getAccountBalance(id2);
        System.out.printf("main:\n    => Account balances at time '%s':\n    ID %s => $%s\n    ID %s => $%s\n", LocalTime.now(), 1, balance1, 2, balance2);

        // Bulk insertion example using JDBC's batching support.
        int totalRowsInserted = dao.bulkInsertRandomAccountData();
        System.out.printf("\nBasicExampleDAO.bulkInsertRandomAccountData:\n    => finished, %s total rows inserted\n", totalRowsInserted);

        // Print out 10 account values.
        int accountsRead = dao.readAccounts(10);

    }
}
