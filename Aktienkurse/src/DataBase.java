import java.sql.*;

public class DataBase {
    public static String path = "C:\\Users\\schul\\OneDrive - HTL Anichstrasse\\4AHWII\\SWP Rubner Szabolcs\\4AHWII_SWP_OP\\Aktienkurse\\db\\";
    public static String filename = "DataBase.db";

    public static void main(String[] args) {
        Connection conn = connect();

        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static Connection connect() {
        Connection conn = null;
        try {
            String url = "jdbc:sqlite:" + path + filename;

            conn = DriverManager.getConnection(url);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return conn;
    }
}
