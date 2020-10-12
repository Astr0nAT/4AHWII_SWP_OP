import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

public class ProgramWithVacation extends Application {
    private static final Scanner reader = new Scanner(System.in);
    private static int[] holidaysPerWeekday;
    private static int[] holidaysPerWeekdayVariable;
    private static int beginningYear;
    private static int duration;

    public static void main(String[] args) {
        LinkedHashMap<LocalDate, String> holidays;
        LinkedHashMap<LocalDate, String> holidaysVariable;

        System.out.print("Beginning year (1920-2120) (inclusive): ");
        beginningYear = inputInt(1920, 2120);
        System.out.println("How many years do you want to calculate (1-200)?");
        duration = inputInt(1, 200);

        holidays = getAllHolidaysFromFile(duration, beginningYear);
        holidaysPerWeekday = countHolidaysForEachWeekday(holidays);

        holidaysVariable = getAllHolidaysFromFile(duration, beginningYear);
        addVariableHolidays(holidaysVariable, duration, beginningYear);
        holidaysPerWeekdayVariable = countHolidaysForEachWeekday(holidaysVariable);

        printHolidays(holidaysVariable);

        for (int i = 0; i < 5; i++) {
            System.out.printf("%9s:   %d%n", DayOfWeek.of(i + 1), holidaysPerWeekday[i]);
        }

        System.out.println();

        for (int i = 0; i < 5; i++) {
            System.out.printf("%9s:   %d%n", DayOfWeek.of(i + 1), holidaysPerWeekdayVariable[i]);
        }

        Application.launch(args);
    }

    private static void printHolidays(LinkedHashMap<LocalDate, String> holidays) {
        LocalDate beginningDate = (LocalDate) holidays.keySet().toArray()[0];
        int currentYear = beginningDate.getYear();
        SortedSet<LocalDate> keys = new TreeSet<>(holidays.keySet());

        for (LocalDate key : keys) {
            if (key.getYear() > currentYear) {
                currentYear = key.getYear();
                System.out.println();
            }
            System.out.printf("%s   %19s   %9s%n", key.toString(), holidays.get(key), key.getDayOfWeek());
        }
        System.out.println();
    }

    private static void addVariableHolidays(LinkedHashMap<LocalDate, String> holidays,
                                            int duration, int beginningYear) {
        int year;

        for (int i = 0; i < duration; i++) {
            year = beginningYear + i;
            addDay(holidays, year, "Ostermontag");
            addDay(holidays, year, "Christi Himmelfahrt");
            addDay(holidays, year, "Pfingstmontag");
            addDay(holidays, year, "Fronleichnam");
        }
    }

    private static int[] countHolidaysForEachWeekday(LinkedHashMap<LocalDate, String> holidays) {
        int[] holidaysPerWeekday = new int[5];

        for (Map.Entry<LocalDate, String> e : holidays.entrySet()) {
            switch (e.getKey().getDayOfWeek()) {
                case MONDAY -> holidaysPerWeekday[0]++;
                case TUESDAY -> holidaysPerWeekday[1]++;
                case WEDNESDAY -> holidaysPerWeekday[2]++;
                case THURSDAY -> holidaysPerWeekday[3]++;
                case FRIDAY -> holidaysPerWeekday[4]++;
            }
        }

        return holidaysPerWeekday;
    }

    private static void addDay(LinkedHashMap<LocalDate, String> holidays, int year, String name) {
        String urlBase = "https://feiertage-api.de/api/?jahr=";
        JSONObject json = getAPI(urlBase + year);
        String date = json.getJSONObject("BY").getJSONObject(name).get("datum").toString();
        holidays.put(LocalDate.parse(date), name);
    }

    private static LinkedHashMap<LocalDate, String> getAllHolidaysFromFile(int duration, int beginningYear) {
        LinkedHashMap<LocalDate, String> holidays = new LinkedHashMap<>();

        String path = "C:\\Users\\schul\\OneDrive - HTL Anichstrasse\\4AHWII\\SWP Rubner Szabolcs\\4AHWII_SWP_OP\\Holidays\\src\\holidays.json";
        String jsonString = readFile(path).toString();

        JSONObject date = new JSONObject(jsonString);
        JSONArray array = date.getJSONArray("holidays");

        for (int j = 0; j < duration; j++) {
            for (int i = 0; i < array.length(); i++) {
                date = array.getJSONObject(i);
                holidays.put(LocalDate.of(beginningYear + j, (int) date.get("month"), (int) date.get("day")),
                        date.get("name").toString());
            }
        }

        return holidays;
    }

    private static StringBuilder readFile(String filename) {
        StringBuilder s = new StringBuilder();
        File file = new File(filename);

        try {
            Scanner fileReader = new Scanner(file);
            while (fileReader.hasNextLine()) {
                String data = fileReader.nextLine();
                s.append(data);
            }

            fileReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return s;
    }

    private static int inputInt(int lower, int upper) {
        boolean retry = true;
        int integer = 0;

        do {
            try {
                integer = Integer.parseInt(reader.nextLine());
                System.out.println();
            } catch (Exception e) {
                System.out.println("NumberFormatException");
            }
            if (integer >= lower && integer <= upper) {
                retry = false;
            } else {
                System.out.println("Only " + lower + " to " + upper + ".");
            }
        } while (retry);
        System.out.println();

        return integer;
    }

    private static JSONObject getAPI(String url) {
        JSONObject json = new JSONObject();
        try {
            json = new JSONObject(IOUtils.toString(new URL(url), StandardCharsets.UTF_8));
        } catch (MalformedURLException e) {
            System.out.println("MalformedURLException");
        } catch (IOException e) {
            System.out.println("IOException");
        }
        return json;
    }

    @Override
    public void start(Stage primaryStage) {

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Weekdays");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount");

        // Create a BarChart
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);

        addDatasetToBarchart(barChart, holidaysPerWeekday, "Without variable holidays");
        addDatasetToBarchart(barChart, holidaysPerWeekdayVariable, "With varibale holidays");

        barChart.setTitle("Holidays per weekday from " + beginningYear + " over " + duration + " years.");

        VBox vbox = new VBox(barChart);

        primaryStage.setTitle("JavaFX BarChart Holidays");
        Scene scene = new Scene(vbox, 400, 400);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void addDatasetToBarchart(BarChart<String, Number> barChart, int[] holidays, String name) {
        XYChart.Series<String, Number> dataSeries = new XYChart.Series<>();

        dataSeries.setName(name);
        dataSeries.getData().add(new XYChart.Data<>("Monday", holidays[0]));
        dataSeries.getData().add(new XYChart.Data<>("Tuesday", holidays[1]));
        dataSeries.getData().add(new XYChart.Data<>("Wednesday", holidays[2]));
        dataSeries.getData().add(new XYChart.Data<>("Thursday", holidays[3]));
        dataSeries.getData().add(new XYChart.Data<>("Friday", holidays[4]));

        barChart.getData().add(dataSeries);
    }
}