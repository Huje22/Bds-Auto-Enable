package me.indian.bds.extension;

import java.util.List;

public record ExtensionDescription(String mainClass, String version, String name,
                                   String author, List<String> authors, String description

) {
}