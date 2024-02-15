package me.indian.bds.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.concurrent.TimeUnit;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.Nullable;

public final class HTTPUtil {

    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .connectionPool(new ConnectionPool(5, 30, TimeUnit.SECONDS))
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .build();
    private static String LAST_KNOWN_IP = null;


    private HTTPUtil() {
    }

    public static OkHttpClient getOkHttpClient() {
        return OK_HTTP_CLIENT;
    }

    @Nullable
    public static String getOwnIP() {
        if (LAST_KNOWN_IP != null) {
            return LAST_KNOWN_IP;
        }

        final Request request = new Request.Builder()
                .url("https://api64.ipify.org?format=json")
                .build();

        try (final Response response = OK_HTTP_CLIENT.newCall(request).execute()) {
            if (response.isSuccessful()) {
                final JsonObject jsonObject = JsonParser.parseString(response.body().string())
                        .getAsJsonObject();

                if (jsonObject.has("ip")) {
                    LAST_KNOWN_IP = jsonObject.get("ip").getAsString();
                    return LAST_KNOWN_IP;
                }
            }
        } catch (final Exception ignored) {
        }

        return LAST_KNOWN_IP;
    }
}
