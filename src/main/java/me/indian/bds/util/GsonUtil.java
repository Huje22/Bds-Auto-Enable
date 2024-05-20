package me.indian.bds.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.lang.reflect.Modifier;

public final class GsonUtil {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting().serializeNulls()
            .disableHtmlEscaping().setLenient()
            .excludeFieldsWithModifiers(Modifier.PRIVATE)
            .create();
    //TODO: detLenient() jest przestarzałe, trzeba użyć innej formy z docs gsona

    private GsonUtil() {
    }

    public static Gson getGson() {
        return GSON;
    }
}
