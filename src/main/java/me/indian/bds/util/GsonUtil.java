package me.indian.bds.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class GsonUtil {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

    private GsonUtil() {
    }
}