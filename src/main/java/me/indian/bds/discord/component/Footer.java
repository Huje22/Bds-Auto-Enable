package me.indian.bds.discord.component;

public record Footer(String text, String imageURL) {

    public Footer(final String text) {
        this(text, null);
    }
}

