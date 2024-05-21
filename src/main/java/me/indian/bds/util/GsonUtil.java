package me.indian.bds.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import java.lang.reflect.Modifier;

public final class GsonUtil {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting().serializeNulls()
            .disableHtmlEscaping()
            .setStrictness(Strictness.LENIENT)
            .excludeFieldsWithModifiers(Modifier.PRIVATE)
            .create();

    private GsonUtil() {
    }

    public static Gson getGson() {
        return GSON;
    }
}