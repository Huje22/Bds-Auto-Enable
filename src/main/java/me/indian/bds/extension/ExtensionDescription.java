package me.indian.bds.extension;

import java.util.List;

public record ExtensionDescription(String mainClass, String version, String name,
                                   String author, String description, List<String> authors, List<String> dependencies,
                                   List<String> softDependencies

) {
}