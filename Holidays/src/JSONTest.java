import org.apache.commons.io.IOUtils;
import org.json.JSONArray;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class JSONTest {
    public static void main(String[] args) {
        String url = "https://ferien-api.de/api/v1/holidays/BY/2020";

        JSONArray json = new JSONArray();
        try {
            json = new JSONArray(IOUtils.toString(new URL(url), StandardCharsets.UTF_8));
        } catch (MalformedURLException e) {
            System.out.println("MalformedURLException");
        } catch (IOException e) {
            System.out.println("IOException");
        }

        for (int i = 0; i < json.length(); i++) {
            System.out.println(json.getJSONObject(i).get("start").toString());

        }
    }
}
