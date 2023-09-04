package me.indian.bds.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class GsonUtil {

    private static final Gson gson = new Gson();
    private static final Gson gsonBuilder = new GsonBuilder().create();
    private static final Gson gsonPrettyBuilder = new GsonBuilder().setPrettyPrinting().create();

    public static Gson getGson() {
        return gson;
    }

    public static Gson getGsonBuilder() {
        return gsonBuilder;
    }

    public static Gson getGsonPrettyBuilder() {
        return gsonPrettyBuilder;
    }
}
