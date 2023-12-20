package me.indian.bds.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class GsonUtil {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

    private GsonUtil() {
    }

    public static Gson getGson() {
        return GSON;
    }
}