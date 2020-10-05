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

public class Program extends Application {
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
        holidaysPerWeekday = countHolidaysForEachWeekday(holidays, beginningYear);

        holidaysVariable = getAllHolidaysFromFile(duration, beginningYear);
        holidaysVariable = addVariableHolidays(holidaysVariable, duration, beginningYear);
        holidaysPerWeekdayVariable = countHolidaysForEachWeekday(holidaysVariable, beginningYear);

        printHolidays(holidaysVariable);

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
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Weekdays");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount");

        // Create a BarChart
        BarChart<String, Number> barChart = new BarChart<String, Number>(xAxis, yAxis);

        // Series 1 - Data of 2014
        XYChart.Series<String, Number> dataSeries1 = new XYChart.Series<String, Number>();
        dataSeries1.setName(beginningYear + " - " + (beginningYear + duration));

        dataSeries1.getData().add(new XYChart.Data<String, Number>("Monday", holidaysPerWeekday[0]));
        dataSeries1.getData().add(new XYChart.Data<String, Number>("Tuesday", holidaysPerWeekday[1]));
        dataSeries1.getData().add(new XYChart.Data<String, Number>("Wednesday", holidaysPerWeekday[2]));
        dataSeries1.getData().add(new XYChart.Data<String, Number>("Thursday", holidaysPerWeekday[3]));
        dataSeries1.getData().add(new XYChart.Data<String, Number>("Friday", holidaysPerWeekday[4]));

        // Add Series to BarChart.
        barChart.getData().add(dataSeries1);

        barChart.setTitle("Holidays per weekday from " + beginningYear + " over " + duration + " years.");

        VBox vbox = new VBox(barChart);

        primaryStage.setTitle("JavaFX BarChart");
        Scene scene = new Scene(vbox, 400, 200);

        primaryStage.setScene(scene);
        primaryStage.setHeight(300);
        primaryStage.setWidth(400);

        primaryStage.show();
    }

    private static LinkedHashMap<LocalDate, String> addVariableHolidays(LinkedHashMap<LocalDate, String> holidays,
                                                                        int duration, int beginningYear) {
        int year;

        String date;
        for (int i = 0; i < duration; i++) {
            year = beginningYear + i;
            addDay(holidays, year, "Ostermontag");
            addDay(holidays, year, "Christi Himmelfahrt");
            addDay(holidays, year, "Pfingstmontag");
            addDay(holidays, year, "Fronleichnam");
        }

        return holidays;
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

    private static int[] countHolidaysForEachWeekday(LinkedHashMap<LocalDate, String> holidays, int beginningYear) {
        int monday = 0, tuesday = 0, wednesday = 0, thursday = 0, friday = 0;
        int[] holidaysPerWeekday = new int[5];

        for (Map.Entry<LocalDate, String> e : holidays.entrySet()) {
            switch (e.getKey().getDayOfWeek()) {
                case MONDAY -> monday++;
                case TUESDAY -> tuesday++;
                case WEDNESDAY -> wednesday++;
                case THURSDAY -> thursday++;
                case FRIDAY -> friday++;
            }
        }

        holidaysPerWeekday[0] = monday;
        holidaysPerWeekday[1] = tuesday;
        holidaysPerWeekday[2] = wednesday;
        holidaysPerWeekday[3] = thursday;
        holidaysPerWeekday[4] = friday;

        return holidaysPerWeekday;
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
}