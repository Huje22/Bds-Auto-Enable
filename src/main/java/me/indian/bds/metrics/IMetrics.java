package me.indian.bds.metrics;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;
import javax.net.ssl.HttpsURLConnection;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.MetricsConfig;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.DefaultsVariables;
import me.indian.bds.util.GsonUtil;
import me.indian.util.DateUtil;
import me.indian.util.ThreadUtil;

/**
 * bStats collects some data for plugin authors.
 * <p>
 * Check out <a href="https://bStats.org/">...</a> to learn more about bStats!
 * <p>
 * Edited from <a href="https://github.com/wode490390/bStats-Nukkit">...</a>
 * Still needed improvements
 * By: IndianBartonka ,
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class IMetrics {

    // The version of this bStats class
    public static final int B_STATS_VERSION = 1;
    // The url to which the data is sent
    private static final String URL = "https://bStats.org/submitData/bukkit";
    // Should failed requests be logged?
    private static boolean logFailedRequests;
    // Should the sent data be logged?
    private static boolean logSentData;
    // Should the response text be logged?
    private static boolean logResponseStatusText;
    // The uuid of the server
    private static String serverUUID;
    // Instance of "Random" 
    private static Random random;
    // Instance of server 
    private static ServerProcess server;
    // Instance of app
    private static BDSAutoEnable bdsAutoEnable;

    static {
        // You can use the property to disable the check in your test environment
        if (System.getProperty("bstats.relocatecheck") == null || !System.getProperty("bstats.relocatecheck").equals("false")) {
            // Maven's Relocate is clever and changes strings, too. So we have to use this little "trick" ... :D
            final String defaultPackage = new String(new byte[]{'o', 'r', 'g', '.', 'b', 's', 't', 'a', 't', 's', '.', 'n', 'u', 'k', 'k', 'i', 't'});
            final String examplePackage = new String(new byte[]{'y', 'o', 'u', 'r', '.', 'p', 'a', 'c', 'k', 'a', 'g', 'e'});
            // We want to make sure nobody just copy & pastes the example and use the wrong package names
            if (IMetrics.class.getPackage().getName().equals(defaultPackage) || IMetrics.class.getPackage().getName().equals(examplePackage)) {
                throw new IllegalStateException("bStats Metrics class has not been relocated correctly!");
            }
        }
    }

    // A list with all custom charts
    private final List<CustomChart> charts = new ArrayList<>();
    // Is bStats enabled on this server?
    private boolean enabled;

    public IMetrics(final BDSAutoEnable bdsAutoEnable) {
        if (bdsAutoEnable == null) throw new IllegalArgumentException("Instancjia aplikacji jest nullem!");

        IMetrics.bdsAutoEnable = bdsAutoEnable;
        IMetrics.server = bdsAutoEnable.getServerProcess();
        IMetrics.random = new Random();
        try {
            this.loadConfig();
        } catch (final IOException exception) {
            bdsAutoEnable.getLogger().warning("Nie udało załadować się configu bStats!", exception);
        }

        if (this.enabled) {
            this.startSubmitting();
        }
    }

    /**
     * Sends the data to the bStats server.
     *
     * @param data The data to send.
     * @throws Exception If the request failed.
     */
    private static void sendData(final JsonObject data) throws Exception {
        if (data == null) throw new IllegalArgumentException("Data nie może być nullem!");
        if (ThreadUtil.isImportantThread()) throw new IllegalAccessError("Nie możesz wykonac tego na tym wątku!");
        if (logSentData) bdsAutoEnable.getLogger().info("Wysyłanie danych do bStats: " + data);

        final HttpsURLConnection connection = (HttpsURLConnection) new URL(URL).openConnection();

        // Compress the data to save bandwidth
        final byte[] compressedData = compress(data.toString());

        // Add headers
        connection.setRequestMethod("POST");
        connection.addRequestProperty("Accept", "application/json");
        connection.addRequestProperty("Connection", "close");
        connection.addRequestProperty("Content-Encoding", "gzip");
        connection.addRequestProperty("Content-Length", String.valueOf(compressedData.length));
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "MC-Server/" + B_STATS_VERSION);

        // Send data
        connection.setDoOutput(true);
        try (final DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
            outputStream.write(compressedData);
        }

        final StringBuilder builder = new StringBuilder();
        try (final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
        }

        if (logResponseStatusText) {
            bdsAutoEnable.getLogger().info("Wysłano dane do bStats i otrzyman (Z kodem&1 "
                    + connection.getResponseCode() + "&r) odpowiedź: " + builder);
        }
    }

    /**
     * Gzips the given String.
     *
     * @param str The string to gzip.
     * @return The gzipped String.
     * @throws IOException If the compression failed.
     */
    private static byte[] compress(final String str) throws IOException {
        if (str == null) return null;

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (final GZIPOutputStream gzip = new GZIPOutputStream(outputStream)) {
            gzip.write(str.getBytes(StandardCharsets.UTF_8));
        }
        return outputStream.toByteArray();
    }

    /**
     * Checks if bStats is enabled.
     *
     * @return Whether bStats is enabled or not.
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Adds a custom chart.
     *
     * @param chart The chart to add.
     */
    public void addCustomChart(final CustomChart chart) {
        if (chart == null) throw new IllegalArgumentException("Chart nie może być nullem!");
        this.charts.add(chart);
    }

    /**
     * Starts the Scheduler which submits our data every 30 minutes.
     */
    private void startSubmitting() {
        final Timer timer = new Timer("bStats-data-sender", true);

        final TimerTask submitTask = new TimerTask() {
            @Override
            public void run() {
                if (server.isEnabled()) IMetrics.this.submitData();
            }
        };

        // Many servers tend to restart at a fixed time at xx:00 which causes an uneven distribution of requests on the
        // bStats backend. To circumvent this problem, we introduce some randomness into the initial and second delay.
        // WARNING: You must not modify any part of this Metrics class, including the submit delay or frequency!
        // WARNING: Modifying this code will get your plugin banned on bStats. Just don't do it!

        final int randomMinutes = 3 + random.nextInt(6);
        final int secondRandomMinutes = 3 + random.nextInt(10);

        timer.scheduleAtFixedRate(submitTask, DateUtil.minutesTo(randomMinutes + secondRandomMinutes, TimeUnit.MILLISECONDS),
                DateUtil.minutesTo(30, TimeUnit.MILLISECONDS));
    }

    /**
     * Gets the app specific data.
     * This method is called using Reflection.
     *
     * @return The app specific data.
     */
    public JsonObject getPluginData() {
        final JsonObject data = new JsonObject();
        final String appName = "BDS-Auto-Enable";
        final String appVersion = bdsAutoEnable.getProjectVersion();

        data.addProperty("pluginName", appName); // Append the name of the app
        data.addProperty("id", 19727); // Append the id of the app
        data.addProperty("pluginVersion", appVersion); // Append the version of the app

        final JsonArray customCharts = new JsonArray();
        for (final CustomChart customChart : this.charts) {
            // Add the data of the custom charts
            final JsonObject chart = customChart.getRequestJsonObject();
            // If the chart is null, we skip it
            if (chart == null) continue;
            customCharts.add(chart);
        }
        data.add("customCharts", customCharts);

        return data;
    }

    /**
     * Gets the server specific data.
     *
     * @return The server specific data.
     */
    private JsonObject getServerData() {
        // Minecraft specific data
        final int playerAmount = bdsAutoEnable.getServerManager().getOnlinePlayers().size();
        final int onlineMode = bdsAutoEnable.getServerProperties().isOnlineMode() ? 1 : 0;
        final String softwareVersion = bdsAutoEnable.getAppConfigManager().getVersionManagerConfig().getVersion();
        final String softwareName = "BDS-Auto-Enable";

        // OS/Java specific data
        final String javaVersion = System.getProperty("java.version");
        final String osName = System.getProperty("os.name");
        final String osArch = System.getProperty("os.arch");
        final String osVersion = System.getProperty("os.version");
        final int coreCount = Runtime.getRuntime().availableProcessors();

        final JsonObject data = new JsonObject();

        data.addProperty("serverUUID", serverUUID);

        data.addProperty("playerAmount", playerAmount);
        data.addProperty("onlineMode", onlineMode);
        data.addProperty("bukkitVersion", softwareVersion);
        data.addProperty("bukkitName", softwareName);
        data.addProperty("serverSoftware", "Okkkk");

        data.addProperty("javaVersion", javaVersion);
        data.addProperty("osName", osName);
        data.addProperty("osArch", osArch);
        data.addProperty("osVersion", osVersion);
        data.addProperty("coreCount", coreCount);

        return data;
    }

    /**
     * Collects the data and sends it afterward.
     */
    private void submitData() {
        final JsonObject data = this.getServerData();
        final JsonArray extensionsData = new JsonArray();
        final JsonObject appObject = new JsonObject();

        appObject.addProperty("pluginName", "BDS-Auto-Enable");
        appObject.addProperty("pluginVersion", bdsAutoEnable.getProjectVersion());
        appObject.addProperty("author", "IndianBartonka");

        extensionsData.add(appObject);

        if (!bdsAutoEnable.getExtensionManager().getExtensions().isEmpty()) {
            bdsAutoEnable.getExtensionManager().getExtensions().forEach((s, extension) -> {
                final JsonObject extensionData = new JsonObject();

                extensionData.addProperty("pluginName", extension.getName());
                extensionData.addProperty("pluginVersion", extension.getVersion());
                extensionData.addProperty("author", extension.getAuthor());

                extensionsData.add(extensionData);
            });
        }

        data.add("plugins", extensionsData);


        new ThreadUtil("Data sender Thread", () -> {
            try {
                // Send the data
                sendData(data);
            } catch (final Exception exception) {
                // Something went wrong! :(
                if (logFailedRequests) {
                    bdsAutoEnable.getLogger().warning("Nie można wysłać danych do&b bstats", exception);
                }
            }
        }).newThread().start();
    }

    /**
     * Loads the bStats configuration.
     *
     * @throws IOException If something did not work :(
     */
    private void loadConfig() throws IOException {
        final File configFile = new File(DefaultsVariables.getAppDir() + "bstats.json");
        final Gson gson = GsonUtil.getGson();

        serverUUID = bdsAutoEnable.getAppUUID();

        // Check if the config file exists
        if (!configFile.exists()) {
            // Create a default configuration object
            final MetricsConfig defaultConfig = new MetricsConfig(true, false, false, false);

            // Serialize the default configuration to JSON and save it to the config file
            try (final FileWriter writer = new FileWriter(configFile)) {
                gson.toJson(defaultConfig, writer);
            }

            // Log that the default config was created
            bdsAutoEnable.getLogger().debug("Utworzono domyślną konfigurację&1 bstats");
        }

        // Deserialize the configuration from JSON
        try (final FileReader reader = new FileReader(configFile)) {
            final MetricsConfig configData = gson.fromJson(reader, MetricsConfig.class);

            // Load the data from the deserialized object
            this.enabled = configData.enabled();
            logFailedRequests = configData.logFailedRequests();
            logSentData = configData.logSentData();
            logResponseStatusText = configData.logResponseStatusText();
        }
    }

    /**
     * Writes a String to a file. It also adds a note for the user.
     *
     * @param file  The file to write to. Cannot be null.
     * @param lines The lines to write.
     * @throws IOException If something did not work :(
     */
    private void writeFile(final File file, final String... lines) throws IOException {
        try (final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
            for (final String line : lines) {
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }
        }
    }

    /**
     * Represents a custom chart.
     */
    public static abstract class CustomChart {

        // The id of the chart
        final String chartId;

        /**
         * Class constructor.
         *
         * @param chartId The id of the chart.
         */
        CustomChart(final String chartId) {
            if (chartId == null || chartId.isEmpty()) {
                throw new IllegalArgumentException("ChartId cannot be null or empty!");
            }
            this.chartId = chartId;
        }

        private JsonObject getRequestJsonObject() {
            final JsonObject chart = new JsonObject();
            chart.addProperty("chartId", this.chartId);
            try {
                final JsonObject data = this.getChartData();
                if (data == null) {
                    // If the data is null we don't send the chart.
                    return null;
                }
                chart.add("data", data);
            } catch (final Throwable throwable) {
                if (logFailedRequests) {
                    bdsAutoEnable.getLogger().warning("Failed to get data for custom chart with id " + this.chartId, throwable);
                }
                return null;
            }
            return chart;
        }

        protected abstract JsonObject getChartData() throws Exception;

    }

    /**
     * Represents a custom simple pie.
     */
    public static class SimplePie extends CustomChart {

        private final Callable<String> callable;

        /**
         * Class constructor.
         *
         * @param chartId  The id of the chart.
         * @param callable The callable which is used to request the chart data.
         */
        public SimplePie(final String chartId, final Callable<String> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        protected JsonObject getChartData() throws Exception {
            final JsonObject data = new JsonObject();
            final String value = this.callable.call();
            if (value == null || value.isEmpty()) {
                // Null = skip the chart
                return null;
            }
            data.addProperty("value", value);
            return data;
        }
    }

    /**
     * Represents a custom advanced pie.
     */
    public static class AdvancedPie extends CustomChart {

        private final Callable<Map<String, Integer>> callable;

        /**
         * Class constructor.
         *
         * @param chartId  The id of the chart.
         * @param callable The callable which is used to request the chart data.
         */
        public AdvancedPie(final String chartId, final Callable<Map<String, Integer>> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        protected JsonObject getChartData() throws Exception {
            final JsonObject data = new JsonObject();
            final JsonObject values = new JsonObject();
            final Map<String, Integer> map = this.callable.call();
            if (map == null || map.isEmpty()) {
                // Null = skip the chart
                return null;
            }
            boolean allSkipped = true;
            for (final Map.Entry<String, Integer> entry : map.entrySet()) {
                if (entry.getValue() == 0) {
                    continue; // Skip this invalid
                }
                allSkipped = false;
                values.addProperty(entry.getKey(), entry.getValue());
            }
            if (allSkipped) {
                // Null = skip the chart
                return null;
            }
            data.add("values", values);
            return data;
        }
    }

    /**
     * Represents a custom drilldown pie.
     */
    public static class DrilldownPie extends CustomChart {

        private final Callable<Map<String, Map<String, Integer>>> callable;

        /**
         * Class constructor.
         *
         * @param chartId  The id of the chart.
         * @param callable The callable which is used to request the chart data.
         */
        public DrilldownPie(final String chartId, final Callable<Map<String, Map<String, Integer>>> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        public JsonObject getChartData() throws Exception {
            final JsonObject data = new JsonObject();
            final JsonObject values = new JsonObject();
            final Map<String, Map<String, Integer>> map = this.callable.call();
            if (map == null || map.isEmpty()) {
                // Null = skip the chart
                return null;
            }
            boolean reallyAllSkipped = true;
            for (final Map.Entry<String, Map<String, Integer>> entryValues : map.entrySet()) {
                final JsonObject value = new JsonObject();
                boolean allSkipped = true;
                for (final Map.Entry<String, Integer> valueEntry : map.get(entryValues.getKey()).entrySet()) {
                    value.addProperty(valueEntry.getKey(), valueEntry.getValue());
                    allSkipped = false;
                }
                if (!allSkipped) {
                    reallyAllSkipped = false;
                    values.add(entryValues.getKey(), value);
                }
            }
            if (reallyAllSkipped) {
                // Null = skip the chart
                return null;
            }
            data.add("values", values);
            return data;
        }
    }

    /**
     * Represents a custom single line chart.
     */
    public static class SingleLineChart extends CustomChart {

        private final Callable<Integer> callable;

        /**
         * Class constructor.
         *
         * @param chartId  The id of the chart.
         * @param callable The callable which is used to request the chart data.
         */
        public SingleLineChart(final String chartId, final Callable<Integer> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        protected JsonObject getChartData() throws Exception {
            final JsonObject data = new JsonObject();
            final int value = this.callable.call();
            if (value == 0) {
                // Null = skip the chart
                return null;
            }
            data.addProperty("value", value);
            return data;
        }

    }

    /**
     * Represents a custom multi line chart.
     */
    public static class MultiLineChart extends CustomChart {

        private final Callable<Map<String, Integer>> callable;

        /**
         * Class constructor.
         *
         * @param chartId  The id of the chart.
         * @param callable The callable which is used to request the chart data.
         */
        public MultiLineChart(final String chartId, final Callable<Map<String, Integer>> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        protected JsonObject getChartData() throws Exception {
            final JsonObject data = new JsonObject();
            final JsonObject values = new JsonObject();
            final Map<String, Integer> map = this.callable.call();
            if (map == null || map.isEmpty()) {
                // Null = skip the chart
                return null;
            }
            boolean allSkipped = true;
            for (final Map.Entry<String, Integer> entry : map.entrySet()) {
                if (entry.getValue() == 0) {
                    continue; // Skip this invalid
                }
                allSkipped = false;
                values.addProperty(entry.getKey(), entry.getValue());
            }
            if (allSkipped) {
                // Null = skip the chart
                return null;
            }
            data.add("values", values);
            return data;
        }

    }

    /**
     * Represents a custom simple bar chart.
     */
    public static class SimpleBarChart extends CustomChart {

        private final Callable<Map<String, Integer>> callable;

        /**
         * Class constructor.
         *
         * @param chartId  The id of the chart.
         * @param callable The callable which is used to request the chart data.
         */
        public SimpleBarChart(final String chartId, final Callable<Map<String, Integer>> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        protected JsonObject getChartData() throws Exception {
            final JsonObject data = new JsonObject();
            final JsonObject values = new JsonObject();
            final Map<String, Integer> map = this.callable.call();
            if (map == null || map.isEmpty()) {
                // Null = skip the chart
                return null;
            }
            for (final Map.Entry<String, Integer> entry : map.entrySet()) {
                final JsonArray categoryValues = new JsonArray();
                categoryValues.add(new JsonPrimitive(entry.getValue()));
                values.add(entry.getKey(), categoryValues);
            }
            data.add("values", values);
            return data;
        }

    }

    /**
     * Represents a custom advanced bar chart.
     */
    public static class AdvancedBarChart extends CustomChart {

        private final Callable<Map<String, int[]>> callable;

        /**
         * Class constructor.
         *
         * @param chartId  The id of the chart.
         * @param callable The callable which is used to request the chart data.
         */
        public AdvancedBarChart(final String chartId, final Callable<Map<String, int[]>> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        protected JsonObject getChartData() throws Exception {
            final JsonObject data = new JsonObject();
            final JsonObject values = new JsonObject();
            final Map<String, int[]> map = this.callable.call();
            if (map == null || map.isEmpty()) {
                // Null = skip the chart
                return null;
            }
            boolean allSkipped = true;
            for (final Map.Entry<String, int[]> entry : map.entrySet()) {
                if (entry.getValue().length == 0) {
                    continue; // Skip this invalid
                }
                allSkipped = false;
                final JsonArray categoryValues = new JsonArray();
                for (final int categoryValue : entry.getValue()) {
                    categoryValues.add(new JsonPrimitive(categoryValue));
                }
                values.add(entry.getKey(), categoryValues);
            }
            if (allSkipped) {
                // Null = skip the chart
                return null;
            }
            data.add("values", values);
            return data;
        }
    }

}
