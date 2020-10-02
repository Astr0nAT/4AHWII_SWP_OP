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

public class Program {
    private static final Scanner reader = new Scanner(System.in);

    public static void main(String[] args) {
        LinkedHashMap<LocalDate, String> holidays;
        int[] holidaysPerWeekday;
        int duration;
        int beginningYear;

        System.out.print("Beginning year (1920-2120): ");
        beginningYear = inputInt(1920, 2120);
        System.out.print("How many years do you want to calculate: ");
        duration = inputInt(1, 500);

        holidays = getAllHolidaysFromFile(duration, beginningYear);
        holidays = addVariableHolidays(holidays, duration, beginningYear);
        holidaysPerWeekday = countHolidaysForEachWeekday(holidays, beginningYear);

        printHolidays(holidays);

        System.out.println();

        for (int i = 0; i < 5; i++) {
            System.out.printf("%9s:   %d%n", DayOfWeek.of(i + 1), holidaysPerWeekday[i]);
        }
    }

    private static void printHolidays(LinkedHashMap<LocalDate, String> holidays) {
        LocalDate beginningDate = (LocalDate) holidays.keySet().toArray()[0];
        int currentYear = beginningDate.getYear();
        SortedSet<LocalDate> keys = new TreeSet<>(holidays.keySet());

        for (LocalDate key : keys) {
            System.out.printf("%s   %19s   %9s%n", key.toString(), holidays.get(key), key.getDayOfWeek());
            if (key.getYear() > currentYear) {
                currentYear = key.getYear();
                System.out.println();
            }
        }
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