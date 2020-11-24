import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.Scanner;
import org.apache.commons.io.IOUtils;
import org.json.*;

public class Main {
    static Scanner reader = new Scanner(System.in);

    static String baseUrl = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol=";
    static String apikey = readFile("src/apikey.txt");

    static StringBuilder url = new StringBuilder();
    static String symbol;

    public static void main(String[] args) {
        buildUrl();             // creates the url which is used to call the api

        JSONObject json = getAPI();

        String refreshed = json.getJSONObject("Meta Data").get("3. Last Refreshed").toString();
        LocalDate date = LocalDate.parse(refreshed);

        int errors = 0;

        while(errors < 3){
            try {
                String close = json.getJSONObject("Time Series (Daily)").getJSONObject(date.toString()).get("4. close").toString();
                System.out.printf("%s: %s%n", date.toString(), close);
                errors = 0;
            } catch (JSONException e){
                errors++;
                System.out.printf("Error %d on %s%n", errors, date.toString());
            }

            if(date.getDayOfWeek() == DayOfWeek.MONDAY){
                date = date.minusDays(3);
                System.out.println();
            } else {
                date = date.minusDays(1);
            }
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

        // TODO symbol = reader.next();
        symbol = "TSLA";
        System.out.println(symbol);

        url.append(baseUrl);
        url.append(symbol);
        url.append("&apikey=");
        url.append(apikey);
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
