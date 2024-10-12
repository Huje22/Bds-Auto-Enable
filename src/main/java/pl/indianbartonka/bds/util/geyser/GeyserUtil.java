package pl.indianbartonka.bds.util.geyser;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.bds.util.GsonUtil;
import pl.indianbartonka.bds.util.HTTPUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class GeyserUtil {

    private static final OkHttpClient CLIENT = HTTPUtil.getOkHttpClient();
    private static final Gson GSON = GsonUtil.getGson();
    private static final Map<Long, SkinResponse> SKIN_RESPONSE_MAP = new HashMap<>();
    private static BDSAutoEnable BDS_AUTO_ENABLE;

    private GeyserUtil() {

    }

    public static void init(final BDSAutoEnable bdsAutoEnable) {
        BDS_AUTO_ENABLE = bdsAutoEnable;
    }

    /**
     * Metoda, która pozwala uzyskać link do zdjęcia ciało gracza Bedrock Edition przy użyciu API Geyser i 'mc-heads.net'.
     *
     * @param xuid XUID gracza Bedrock Edition
     * @return Link do zdjęcia ciało gracza Bedrock Edition
     */
    public static String getBedrockSkinBody(final long xuid) {
        String playerName = BDS_AUTO_ENABLE.getServerManager().getStatsManager().getNameByXuid(xuid);

        if (playerName == null) playerName = "Steve";

        try {
            final SkinResponse skinResponse = sendGeyserSkinRequest(xuid);
            if (skinResponse.textureID() == null) {
                return "https://mineskin.eu/armor/bust/" + playerName + "/100.png";
            }

            return "https://mc-heads.net/body/" + skinResponse.textureID;
        } catch (final IOException ioException) {
            ioException.printStackTrace();
            return "https://mineskin.eu/armor/bust/" + playerName + "/100.png";
        }
    }

    /**
     * Metoda, która pozwala uzyskać link do zdjęcia głowy gracza Bedrock Edition przy użyciu API Geyser i 'mc-heads.net'.
     *
     * @param xuid XUID gracza Bedrock Edition
     * @return Link do zdjęcia głowy gracza Bedrock Edition
     */
    public static String getBedrockSkinHead(final long xuid) {
        String playerName = BDS_AUTO_ENABLE.getServerManager().getStatsManager().getNameByXuid(xuid);

        if (playerName == null) playerName = "Steve";

        try {
            final SkinResponse skinResponse = sendGeyserSkinRequest(xuid);
            if (skinResponse.textureID() == null) {
                return "https://mineskin.eu/headhelm/" + playerName + "/100.png";
            }

            return "https://mc-heads.net/head/" + skinResponse.textureID;
        } catch (final IOException ioException) {
            ioException.printStackTrace();
            return "https://mineskin.eu/headhelm/" + playerName + "/100.png";
        }
    }

    /**
     * Metoda wysyłająca żądanie o skórkę gracza Bedrock Edition do serwera Geyser.
     *
     * @param xuid XUID gracza Bedrock Edition
     * @return Odpowiedź zawierająca dane o skórce gracza
     * @throws IOException Wyjątek w przypadku błędu komunikacji
     */
    private static SkinResponse sendGeyserSkinRequest(final long xuid) throws IOException {
        if (SKIN_RESPONSE_MAP.containsKey(xuid)) return SKIN_RESPONSE_MAP.get(xuid);

        final Request request = new Request.Builder()
                .url("https://api.geysermc.org/v2/skin/" + xuid)
                .build();

        try (final Response response = CLIENT.newCall(request).execute()) {
            if (response.isSuccessful()) {
                final SkinResponse skinResponse = GSON.fromJson(response.body().string(), SkinResponse.class);

                if (skinResponse.textureID() != null) {
                    SKIN_RESPONSE_MAP.put(xuid, skinResponse);
                }

                return skinResponse;
            } else {
                return new SkinResponse(null);
            }
        }
    }

    record SkinResponse(@SerializedName("texture_id") String textureID) {
    }
}
