import java.sql.*;

public class ProgramDB {
    public static String path = "C:\\Users\\schul\\OneDrive - HTL Anichstrasse\\4AHWII\\SWP Rubner Szabolcs\\4AHWII_SWP_OP\\Holidays\\db\\";
    public static String filename = "ProgramDB.db";

    public static void main(String[] args) {
        System.out.println("Connected\n");

        Connection conn = connect();
        dropTable(conn);
        createTable(conn);
        selectAll(conn);

        System.out.print("\nClosed");
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

    public static void createTable(Connection conn){
        String statement =
                "CREATE TABLE IF NOT EXISTS holidays (timestamp varchar(25), yearB INT, duration INT, yearE INT, " +
                        "monday int, tuesday int, wednesday int, thursday int, friday int);";

        try{
            Statement stmt = conn.createStatement();
            stmt.execute(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    public static void dropTable(Connection conn){
        String statement =
                "DROP TABLE IF EXISTS holidays;";

        try{
            Statement stmt = conn.createStatement();
            stmt.execute(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertData(Connection conn, int yearB, int duration, int yearE, int monday, int tuesday,
                                  int wednesday, int thursday, int friday){
        String statement =
                "INSERT INTO holidays(timestamp, yearB, duration, yearE, " +
                        "monday, " +
                        "tuesday, " +
                        "wednesday, " +
                        "thursday, " +
                        "friday)" +
                        "values (?, ?, ?, ?, ?, ?, ?, ?, ?);";
        String timestampString = new Timestamp(System.currentTimeMillis()).toString();
        timestampString = timestampString.substring(0, 19);

        try{
            PreparedStatement stmt = conn.prepareStatement(statement);
            stmt.setString(1, timestampString);
            stmt.setInt   (2, yearB);
            stmt.setInt   (3, duration);
            stmt.setInt   (4, yearE);

            stmt.setInt   (5, monday);
            stmt.setInt   (6, tuesday);
            stmt.setInt   (7, wednesday);
            stmt.setInt   (8, thursday);
            stmt.setInt   (9, friday);

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void selectAll(Connection conn){
        System.out.printf("%19s   %5s   %3s   %5s   %4s %4s %4s %4s %4s%n",
                "Timestamp", "yearB", "dur", "yearE",
                "mon",
                "tue",
                "wed",
                "thu",
                "fri");
        String statement = "SELECT timestamp, yearB, duration, yearE, monday, tuesday, wednesday, thursday, friday " +
                "FROM holidays;";

        try {
            Statement stmt  = conn.createStatement();
            ResultSet rs    = stmt.executeQuery(statement);

            while (rs.next()) {
                System.out.printf("%19s   %5d   %3d   %5d   %4d %4d %4d %4d %4d%n",
                        rs.getString(1),
                        rs.getInt(2),
                        rs.getInt(3),
                        rs.getInt(4),
                        rs.getInt(5),
                        rs.getInt(6),
                        rs.getInt(7),
                        rs.getInt(8),
                        rs.getInt(9)
                );
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void execute(Connection conn, String statement){
        try {
            Statement stmt  = conn.createStatement();
            stmt.execute(statement);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

}  