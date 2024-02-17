package me.indian.bds.extension;

import java.util.List;

public record ExtensionDescription(String mainClass, String version, String name, String prefix,
                                   String description, String author, List<String> authors, List<String> dependencies,
                                   List<String> softDependencies) {
}