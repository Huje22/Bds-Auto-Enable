package me.indian.bds.util;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
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
            final Request request = new Request.Builder()
                    .url("https://api.geysermc.org/v2/xbox/xuid/" + name)
                    .addHeader("Accept", "application/json")
                    .build();

            try (final Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.out.println("Błąd przy pobieraniu danych. Kod odpowiedzi: " + response.code());
                    return -1;
                }

                final String responseBody = response.body().string();
                final JsonObject jsonObject = GsonUtil.getGson().fromJson(responseBody, JsonObject.class);

                if (jsonObject.has("xuid")) {
                    return jsonObject.get("xuid").getAsLong();
                } else {
                    System.out.println("Klucz 'xuid' nie istnieje w JSON-ie.");
                    return -1;
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static String getName(final long xuid) {
        try {
            final Request request = new Request.Builder()
                    .url("https://api.geysermc.org/v2/xbox/gamertag/" + xuid)
                    .addHeader("Accept", "application/json")
                    .build();

            try (final Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.out.println("Błąd przy pobieraniu danych. Kod odpowiedzi: " + response.code());
                    return "";
                }

                final String responseBody = response.body().string();
                final JsonObject jsonObject = GsonUtil.getGson().fromJson(responseBody, JsonObject.class);

                if (jsonObject.has("gamertag")) {
                    return jsonObject.get("gamertag").getAsString();
                } else {
                    System.out.println("Klucz 'gamertag' nie istnieje w JSON-ie.");
                    return "";
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void main(final String[] args) throws IOException {
        final String playerName = "JndjanBartonka";
        final long xuid = getXuid(playerName);
        System.out.println("XUID gracza " + playerName + ": " + xuid);
        final String nameFromXuid = getName(xuid);
        System.out.println(nameFromXuid);

        System.out.println(getBedrockSkin(xuid));
    }


    public static String getBedrockSkin(final long xuid) throws IOException {
        final String geyserAPIResponse = sendHttpGETRequest(xuid);
        if (geyserAPIResponse.equals("")) throw new RuntimeException();

        return "https://textures.minecraft.net/texture/" + GsonUtil.getGson().fromJson(geyserAPIResponse, SkinResponse.class).getTextureId();
    }

    private static String sendHttpGETRequest(final long xuid) throws IOException {
        final Request request = new Request.Builder()
                .url("https://api.geysermc.org/v2/skin/" + xuid)
                .build();

        try (final Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                return "";
            }
        }
    }

    static class SkinResponse {
        @SerializedName("texture_id")
        private String textureId;

        public String getTextureId() {
            return this.textureId;
        }
    }
}