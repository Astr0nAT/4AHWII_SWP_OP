import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Main {
    static Scanner reader = new Scanner(System.in);

    static String baseUrl = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol=";
    static String apikey = readFile("src/apikey.txt");
    static Connection conn;

    static StringBuilder url = new StringBuilder();
    static String symbol;

    public static void main(String[] args) {
        conn = MainDB.connect();
        JSONObject json;
        String refreshed = "";
        boolean repeat;

        do {
            repeat = false;

            buildUrl();    // creates the url which is used to call the api
            json = getAPI();    // fetches the json-Object from the api

            try {
                // get the latest date
                refreshed = json.getJSONObject("Meta Data").get("3. Last Refreshed").toString();
            } catch (JSONException e) {
                System.err.println("Unknown ticker symbol\n");
                repeat = true;
                sleep(30);    // sleep for 30ms, due to the asynchronicity of System.err.print and System.out.print
            }
        } while (repeat);

        json = json.getJSONObject("Time Series (Daily)");    // remove metadata from json object
        LocalDate date = LocalDate.parse(refreshed);

        int errors = 0;

        while(errors < 3){
            try {
                // close is the value which the stock had at the end of the day
                double close = Double.parseDouble(json.getJSONObject(date.toString()).get("4. close").toString());

                // printing the result and saving it to the database
                System.out.printf("%s: %s%n", date.toString(), close);
                MainDB.createTable(conn, symbol);
                MainDB.insertOrReplace(conn, date.toString(), close, symbol);

                errors = 0;
            } catch (JSONException e) {
                // if 3 days in a row have no data, the program terminates
                errors++;
                System.err.printf("Error %d on %s%n", errors, date.toString());
                sleep(30);
            }

            // skipping weekends
            if (date.getDayOfWeek() == DayOfWeek.MONDAY) {
                date = date.minusDays(3);
                System.out.println();
            } else {
                date = date.minusDays(1);
            }
        }
    }

    private static void sleep(int time) {
        try {
            TimeUnit.MILLISECONDS.sleep(time);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
    }

    private static String readFile(String filepath) {
        try {
            return IOUtils.toString(new FileInputStream(filepath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void buildUrl() {
        System.out.println("Which stock do you want to analyze?");
        System.out.println("(E.g. TSLA , AAPL , AMD, INTC (Intel), T (AT&T))");
        System.out.print("Input: ");

        symbol = reader.next().toUpperCase();

        url.append(baseUrl);
        url.append(symbol);
        url.append("&apikey=");
        url.append(apikey);

        System.out.flush();
    }

    private static JSONObject getAPI() {
        JSONObject json = new JSONObject();
        try {
            json = new JSONObject(IOUtils.toString(new URL(url.toString()), StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.out.println("IOException");
        }
        return json;
    }

}
