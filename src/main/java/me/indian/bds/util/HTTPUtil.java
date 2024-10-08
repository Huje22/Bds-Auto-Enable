package me.indian.bds.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.exception.DownloadException;
import me.indian.util.DateUtil;
import me.indian.util.MathUtil;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.Nullable;

public final class HTTPUtil {

    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient.Builder()
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

    public static void download(final String url, final String path, final BDSAutoEnable bdsAutoEnable) throws IOException, DownloadException {
        final File file = new File(path);
        final Request request = new Request.Builder().url(url).get().build();

        try (final Response response = OK_HTTP_CLIENT.newCall(request).execute()) {
            final int responseCode = response.code();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                final long fileSize = response.body().contentLength();

                try (final InputStream inputStream = new BufferedInputStream(response.body().byteStream())) {
                    try (final FileOutputStream outputStream = new FileOutputStream(file)) {
                        final byte[] buffer = new byte[1024];
                        int bytesRead;
                        long totalBytesRead = 0;

                        long lastTime = System.currentTimeMillis();
                        long lastBytesRead = 0;
                        int lastProgress = -1;

                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                            totalBytesRead += bytesRead;

                            final long currentTime = System.currentTimeMillis();
                            final double elapsedTime = (currentTime - lastTime) / 1000.0;
                            if (elapsedTime >= 1.0) {
                                final long bytesSinceLastTime = totalBytesRead - lastBytesRead;
                                final double speedBytesPerSecond = (double) bytesSinceLastTime / elapsedTime;
                                final double speedMBps = speedBytesPerSecond / (1024.0 * 1024.0);
                                lastTime = currentTime;
                                lastBytesRead = totalBytesRead;

                                final int progress = Math.round((float) totalBytesRead / (float) fileSize * 100.0f);

                                final double formatedSpeed = MathUtil.format(speedMBps, 3);
                                final long remainingTimeSeconds = (long) (MathUtil.bytesToMB(fileSize) / formatedSpeed);
                                final String remainingTimeString = DateUtil.formatTimeDynamic(remainingTimeSeconds * 1000, true);

                                bdsAutoEnable.setAppWindowName(String.format("%-1s %-2s %-4s %-4s", "Pobrano w:", progress + "%", formatedSpeed + " MB/s", "Pozostało " + remainingTimeString));

                                if (progress != lastProgress) {
                                    lastProgress = progress;
                                    bdsAutoEnable.getLogger().info(String.format("%-1s &b%-2s &a%-4s &d%-4s", "Pobrano w:", progress + "%", formatedSpeed + " MB/s", "Pozostało " + remainingTimeString));
                                }
                            }
                        }
                    }
                }
            } else {
                throw new DownloadException(responseCode);
            }
        }
    }
}