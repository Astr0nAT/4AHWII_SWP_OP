import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Main extends Application {
    static Scanner reader = new Scanner(System.in);

    static String baseUrl = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&outputsize=full&symbol=";
    static String apikey = readFile("src/apikey.txt");
    static Connection conn;
    static LocalDate currentDate = LocalDate.now();

    static StringBuilder url = new StringBuilder();
    static String symbol;

    public static void main(String[] args) {
        conn = MainDB.open();
        JSONObject json = new JSONObject();

        System.out.print("How many days should be updated?: ");
        ArrayList<LocalDate> dates = getNeededDates(reader.nextInt());
        System.out.println();

        boolean repeatGetAPI;
        double splitCoefficient = 1.0;

        do {
            try
            {
                repeatGetAPI = false;
                json = getAPI();    // fetches the json-Object from the api

                // get the latest date
                System.out.println("Last refreshed: " + json.getJSONObject("Meta Data").get("3. Last Refreshed").toString() + "\n");
            }

            catch (JSONException e)
            {
                System.err.println("Unknown ticker symbol\n");
                repeatGetAPI = true;
                sleep(30);    // sleep for 30ms, due to the asynchronicity of System.err.print and System.out.print
            }
        } while (repeatGetAPI);

        json = json.getJSONObject("Time Series (Daily)");    // remove metadata from json object
        MainDB.createTable(conn, symbol);

        for(LocalDate d : dates) {
            try {
                // close is the value which the stock had at the end of the day
                double close = Double.parseDouble(json.getJSONObject(d.toString()).get("4. close").toString());
                String split = json.getJSONObject(d.toString()).get("8. split coefficient").toString();
                double prevSplit = splitCoefficient;

                if(!split.equals("1.0")){
                    splitCoefficient *= Double.parseDouble(split);
                    close = round(close / prevSplit, 2);
                }
                else {
                    close = round(close / splitCoefficient, 2);
                }

                // printing the result and saving it to the database
                System.out.printf("%s: %s%n", d.toString(), close);
                MainDB.insertOrReplace(conn, symbol, d.toString(), close, 0.0);
            } catch (JSONException e) {
                System.err.println("Missing data on " + d.toString());
                sleep(30);
            }
            finally {
                if(d.getDayOfWeek() == DayOfWeek.MONDAY){
                    System.out.println();
                }
            }
        }

        HashMap<String, Double> data = MainDB.getClose(conn, symbol);
        String[] sortedKeys = MainDB.getDescSortedKeys(data);

        for(int i = 0; i < sortedKeys.length - 200; i++){
            MainDB.insertOrReplace(conn, symbol, sortedKeys[i], data.get(sortedKeys[i]), mA200Days(i, data, sortedKeys));
        }

        Application.launch(args);
    }
    @Override
    public void start(Stage stage) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Dates");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Value");

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);

        addClose(lineChart, MainDB.getClose(conn, symbol));
        addmA200(lineChart, MainDB.getmA200(conn, symbol));

        lineChart.setTitle("Aktiendaten für " + symbol);
        lineChart.setPrefHeight(900.0);
        lineChart.setPrefWidth(1600.0);
        lineChart.setCreateSymbols(false);
        VBox vBox = new VBox(lineChart);

        stage.setTitle("Aktiendaten mit API request an ALPHAVANTAGE");
        Scene scene = new Scene(vBox, 1600, 900);

        stage.setScene(scene);
        stage.show();
    }

    private void addmA200(LineChart<String, Number> lineChart, HashMap<String, Double> getmA200) {
        XYChart.Series<String, Number> ds = new XYChart.Series<>();

        ds.setName("mA200");
        insertData(lineChart, getmA200, ds);
    }

    private void addClose(LineChart<String, Number> lineChart, HashMap<String, Double> close) {
        XYChart.Series<String, Number> ds = new XYChart.Series<>();

        ds.setName("close");
        insertData(lineChart, close, ds);
    }

    private void insertData(LineChart<String, Number> lineChart, HashMap<String, Double> hm, XYChart.Series<String, Number> ds) {
        String[] ascSortedKeys = MainDB.getAscSortedKeys(hm);

        for(int i = 200; i < ascSortedKeys.length; i++){
            ds.getData().add(new XYChart.Data<>(ascSortedKeys[i], hm.get(ascSortedKeys[i])));
        }

        lineChart.getData().add(ds);
    }

    private static double mA200Days(int index, HashMap<String, Double> data, String[] sortedKeys){
        double avg = 0.0;

        for(int i = 0; i < 200; i++){
            avg += data.get(sortedKeys[index + i]);
        }

        avg /= 200;

        return round(avg, 2);
    }

    private static double round(double val, int num){
        double pow = Math.pow(10, num);
        return Math.round(val * pow) / pow;
    }

    private static ArrayList<LocalDate> getNeededDates(int amount) {
        LocalDate iter = currentDate;
        ArrayList<LocalDate> days = new ArrayList<>();
        DayOfWeek dow;

        do {
            dow = iter.getDayOfWeek();
            while(dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
                iter = iter.minusDays(1);
                dow = iter.getDayOfWeek();
            }
            days.add(iter);
            iter = iter.minusDays(1);
        } while(iter.isAfter(currentDate.minusDays(amount)));

        return days;
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

        System.out.println(url);

        System.out.flush();
    }

    private static JSONObject getAPI() {
        buildUrl();    // creates the url which is used to call the api

        JSONObject json = new JSONObject();
        try {
            json = new JSONObject(IOUtils.toString(new URL(url.toString()), StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.out.println("IOException");
        }
        return json;
    }
}