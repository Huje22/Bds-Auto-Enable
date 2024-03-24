package me.indian.bds.util;

import com.google.gson.JsonObject;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


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


    private static final OkHttpClient client = HTTPUtil.getOkHttpClient();

    public static long getXuid(final String name) {
        try {
            Request request = new Request.Builder()
                    .url("https://api.geysermc.org/v2/xbox/xuid/" + name)
                    .addHeader("Accept", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.out.println("Błąd przy pobieraniu danych. Kod odpowiedzi: " + response.code());
                    return -1;
                }

                String responseBody = response.body().string();
                JsonObject jsonObject = GsonUtil.getGson().fromJson(responseBody, JsonObject.class);

                if (jsonObject.has("xuid")) {
                    return jsonObject.get("xuid").getAsLong();
                } else {
                    System.out.println("Klucz 'xuid' nie istnieje w JSON-ie.");
                    return -1;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static String getName(final long xuid) {
        try {
            Request request = new Request.Builder()
                    .url("https://api.geysermc.org/v2/xbox/gamertag/" + xuid)
                    .addHeader("Accept", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.out.println("Błąd przy pobieraniu danych. Kod odpowiedzi: " + response.code());
                    return "";
                }

                String responseBody = response.body().string();
                JsonObject jsonObject = GsonUtil.getGson().fromJson(responseBody, JsonObject.class);

                if (jsonObject.has("gamertag")) {
                    return jsonObject.get("gamertag").getAsString();
                } else {
                    System.out.println("Klucz 'gamertag' nie istnieje w JSON-ie.");
                    return "";
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void main(final String[] args) {
        final String playerName = "JndjanBartonka";
        final long xuid = getXuid(playerName);
        System.out.println("XUID gracza " + playerName + ": " + xuid);
        final String nameFromXuid = getName(xuid);
        System.out.println(nameFromXuid);
    }

}
