import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MainDB {

    public static String path = System.getProperty("user.dir") + "\\db\\";
    public static String filename = "MainDB.db";

    public static void main(String[] args) {
        Connection conn = connect();
        String symbol = "TSLA";

        createTable(conn, symbol);

        String[] keys = {"1111-01-01", "2222-02-02", "3333-03-03", "4444-04-04"};
        double[] values = {4.0, 3.0, 2.0, 1.0};

        for (int i = 0; i < 4; i++) {
            insertOrReplace(conn, keys[i], values[i], symbol);
        }

        close(conn);
    }

    public static Connection connect() {
        Connection conn = null;
        try {
            String url = "jdbc:sqlite:" + path + filename;
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return conn;
    }

    public static void close(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void createTable(Connection conn, String symbol) {
        String statement =
                "CREATE TABLE IF NOT EXISTS " + symbol + " (date char(10) PRIMARY KEY, value REAL);";

        try {
            Statement stmt = conn.createStatement();
            stmt.execute(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertOrReplace(Connection conn, String key, double value, String symbol) {
        String statement =
                "INSERT OR REPLACE INTO " + symbol + " VALUES ('" + key + "', " + value + ")";

        try {
            Statement stmt = conn.createStatement();
            stmt.execute(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
