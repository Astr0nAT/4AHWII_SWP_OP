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
    private static XYChart.Series<String, Number> oldData1 = new XYChart.Series<>();
    private static XYChart.Series<String, Number> oldData2 = new XYChart.Series<>();
    private static XYChart.Series<String, Number> updatedData1 = new XYChart.Series<>();
    private static XYChart.Series<String, Number> updatedData2 = new XYChart.Series<>();
    private static int beginningYear;
    private static int duration;

    public static void main(String[] args) {
        LinkedHashMap<LocalDate, String> holidays;
        LinkedHashMap<LocalDate, String> holidaysVariable;

        int[] holidaysPerWeekday;
        int[] holidaysPerWeekdayVariable;

        System.out.println("Using 2017 - 2022 due to missing data in the API.\n");
        beginningYear = 2017;
        duration = 6;

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

        oldData1 = createDataset(holidaysPerWeekday, "hol. without var., with invalid");
        oldData2 = createDataset(holidaysPerWeekdayVariable, "hol. with var., with invalid");

        removeInvalidDays(holidays);
        removeInvalidDays(holidaysVariable);

        holidaysPerWeekday = countHolidaysForEachWeekday(holidays);
        holidaysPerWeekdayVariable = countHolidaysForEachWeekday(holidaysVariable);

        updatedData1 = createDataset(holidaysPerWeekday, "hol. without var., without invalid");
        updatedData2 = createDataset(holidaysPerWeekdayVariable, "hol. with var., without invalid");

        Application.launch(args);
    }

    private static void removeInvalidDays(LinkedHashMap<LocalDate, String> holidays) {
        String url;
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonO;
        LocalDate start;
        LocalDate end;
        String vacationName;

        for (int i = 0; i < duration; i++) {
            url = "https://ferien-api.de/api/v1/holidays/BY/" + (beginningYear + i);
            try {
                jsonArray = new JSONArray(IOUtils.toString(new URL(url), StandardCharsets.UTF_8));
            } catch (Exception e) {
                e.printStackTrace();
            }

            for (int j = 0; j < duration; j++) {
                jsonO = jsonArray.getJSONObject(j);
                start = LocalDate.parse(jsonO.get("start").toString().substring(0, 10));
                end = LocalDate.parse(jsonO.get("end").toString().substring(0, 10));
                vacationName = jsonO.get("name").toString();
                vacationName = vacationName.substring(0, 1).toUpperCase() + vacationName.substring(1);

                LocalDate finalStart = start;
                LocalDate finalEnd = end;
                String finalVacationName = vacationName;

                ArrayList<LocalDate> holidaysDuringVacations = new ArrayList<>();

                holidays.forEach((key, value) -> {
                    if (key.isAfter(finalStart) && key.isBefore(finalEnd)) {
                        // System.out.println(value + " am " + key + " ist in den " + finalVacationName);
                        holidaysDuringVacations.add(key);
                    }
                });

                holidaysDuringVacations.forEach(holidays::remove);
            }
        }
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

    private static XYChart.Series<String, Number> createDataset(int[] holidays, String name) {
        XYChart.Series<String, Number> dataSeries = new XYChart.Series<>();

        dataSeries.setName(name);
        dataSeries.getData().add(new XYChart.Data<>("Monday", holidays[0]));
        dataSeries.getData().add(new XYChart.Data<>("Tuesday", holidays[1]));
        dataSeries.getData().add(new XYChart.Data<>("Wednesday", holidays[2]));
        dataSeries.getData().add(new XYChart.Data<>("Thursday", holidays[3]));
        dataSeries.getData().add(new XYChart.Data<>("Friday", holidays[4]));

        return dataSeries;
    }

    @Override
    public void start(Stage primaryStage) {

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Weekdays");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount");

        // Create a BarChart
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);

        barChart.getData().add(oldData1);
        barChart.getData().add(oldData2);

        barChart.getData().add(updatedData1);
        barChart.getData().add(updatedData2);

        barChart.setTitle("Holidays per weekday from " + beginningYear + " over " + duration + " years.");

        VBox vbox = new VBox(barChart);

        primaryStage.setTitle("JavaFX BarChart Holidays");
        Scene scene = new Scene(vbox, 800, 400);

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}