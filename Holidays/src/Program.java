import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;

public class Program {
    public static void main(String[] args) {
        ArrayList<LocalDate> holidays = new ArrayList<LocalDate>();
        holidays.add(LocalDate.of(2020, Month.JANUARY, 1));
        holidays.add(LocalDate.of(2020, Month.JANUARY, 6));
        holidays.add(LocalDate.of(2020, Month.MARCH, 19));

    }
}
