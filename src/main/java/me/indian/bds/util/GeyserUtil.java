package me.indian.bds.util;

import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class GeyserUtil {

    /*
    Test Util
    Test Util
    Test Util
    Test Util
    Test Util
    Test Util
    Test Util
    Test Util

     */


    private static final StringBuilder xuidResponse = new StringBuilder();
    private static final StringBuilder nameResponse = new StringBuilder();

    public static long getXuid(final String name) {
        try {
            xuidResponse.setLength(0);
            final URL url = new URL("https://api.geysermc.org/v2/xbox/xuid/" + name);
            final HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json");

            final int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                final BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    xuidResponse.append(inputLine);
                }
                in.close();
                final JsonObject jsonObject = GsonUtil.getGson().fromJson(xuidResponse.toString(), JsonObject.class);

                if (jsonObject.has("xuid")) {
                    return jsonObject.get("xuid").getAsLong();
                } else {
                    System.out.println("Klucz 'xuid' nie istnieje w JSON-ie.");
                }
            } else {
                System.out.println("Błąd przy pobieraniu danych. Kod odpowiedzi: " + responseCode);
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String getName(final long xuid) {
        try {
            xuidResponse.setLength(0);
            final URL url = new URL("https://api.geysermc.org/v2/xbox/gamertag/" + xuid);
            final HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json");

            final int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                final BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    nameResponse.append(inputLine);
                }
                in.close();
                final JsonObject jsonObject = GsonUtil.getGson().fromJson(nameResponse.toString(), JsonObject.class);

                if (jsonObject.has("gamertag")) {
                    return jsonObject.get("gamertag").getAsString();
                } else {
                    System.out.println("Klucz 'gamertag' nie istnieje w JSON-ie.");
                }
            } else {
                System.out.println("Błąd przy pobieraniu danych. Kod odpowiedzi: " + responseCode);
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return "";
    }


    public static void main(final String[] args) {
        final String playerName = "JndjanBartonka";
        final long xuid = getXuid(playerName);
        System.out.println("XUID gracza " + playerName + ": " + xuid);
        final String nameFromXuid = getName(xuid);
        System.out.println(nameFromXuid);
    }

}
