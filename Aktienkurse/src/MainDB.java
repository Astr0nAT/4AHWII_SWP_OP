import java.sql.*;
import java.util.*;

public class MainDB {

    public static String path = System.getProperty("user.dir") + "\\db\\";
    public static String filename = "MainDB.db";

    public static void main(String[] args) {
        Connection conn = open();

        for(String s : getDescSortedKeys(getClose(conn, "TSLA"))){
            System.out.println(s);
        }

        System.out.println("\n");

        for(String s : getAscSortedKeys(getClose(conn, "TSLA"))){
            System.out.println(s);
        }

        close(conn);
    }

    public static String[] getDescSortedKeys(HashMap<String, Double> hm) {
        String[] keys = getAscSortedKeys(hm);
        Arrays.sort(keys, Collections.reverseOrder());
        return keys;
    }

    public static HashMap<String, Double> getmA200(Connection conn, String symbol){
        HashMap<String, Double> hm = new HashMap<>();
        String statement = "SELECT * FROM " + symbol;

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(statement);

            while(rs.next()){
                hm.put(rs.getString("date"), rs.getDouble("ma200Days"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return hm;
    }

    public static String[] getAscSortedKeys(HashMap<String, Double> hm){
        String[] keys = hm.keySet().toArray(new String[hm.size()]);
        Arrays.sort(keys);

        return keys;
    }

    public static HashMap<String, Double> getClose(Connection conn, String symbol){
        HashMap<String, Double> hm = new HashMap<>();
        String statement = "SELECT * FROM " + symbol;

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(statement);

            while(rs.next()){
                hm.put(rs.getString("date"), rs.getDouble("value"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return hm;
    }

    public static Connection open() {
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
                "CREATE TABLE IF NOT EXISTS " + symbol + " (date char(10) PRIMARY KEY, value REAL, mA200Days REAL);";
        try {
            Statement stmt = conn.createStatement();
            stmt.execute(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertOrReplace(Connection conn, String symbol, String date, double close, double mA200Days) {
        String statement =
                "INSERT OR REPLACE INTO " + symbol + " VALUES (?, ?, ?)";

        try {
            PreparedStatement stmt = conn.prepareStatement(statement);
            stmt.setString(1, date);
            stmt.setDouble(2, close);
            stmt.setDouble(3, mA200Days);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
