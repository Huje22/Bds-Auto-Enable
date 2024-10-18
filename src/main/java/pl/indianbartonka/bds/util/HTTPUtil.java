package pl.indianbartonka.bds.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.Nullable;
import pl.indianbartonka.bds.exception.DownloadException;
import pl.indianbartonka.util.download.DownloadListener;
import pl.indianbartonka.util.download.DownloadTask;
import pl.indianbartonka.util.http.UserAgent;

public final class HTTPUtil {

    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient.Builder()
            .protocols(List.of(Protocol.HTTP_3, Protocol.HTTP_2, Protocol.HTTP_1_1))
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
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

    public static void download(final String url, final String path) throws DownloadException, IOException, TimeoutException {
        download(url, path, null);
    }

    public static void download(final String url, final String path, final DownloadListener downloadListener) throws IOException, DownloadException, TimeoutException {
        final File file = new File(path);
        final Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", UserAgent.buildUserAgent())
                .get()
                .build();

        try (final Response response = OK_HTTP_CLIENT.newCall(request).execute()) {
            final int responseCode = response.code();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                final DownloadTask downloadTask = new DownloadTask(response.body().byteStream(), file, response.body().contentLength(), 30, downloadListener);

                downloadTask.downloadFile();
            } else {
                throw new DownloadException(responseCode);
            }
        }
    }
}