package me.indian.bds.extension;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.exception.ExtensionException;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.DefaultsVariables;
import me.indian.bds.util.GsonUtil;
import org.jetbrains.annotations.Nullable;

public class ExtensionLoader {

    //TODO: Zmienic nazwe z Loader na Manager


    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final Map<String, Extension> extensions;
    private final String extensionsDir;
    private final File[] jarFiles;
    private final Map<String, Class<?>> classes;
    private final Map<String, ExtensionClassLoader> classLoaders;


    public ExtensionLoader(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.extensions = new LinkedHashMap<>();
        this.extensionsDir = DefaultsVariables.getAppDir() + "extensions";
        this.jarFiles = new File(this.extensionsDir).listFiles(pathname -> pathname.getName().endsWith(".jar"));
        this.classes = new HashMap<>();
        this.classLoaders = new HashMap<>();

        try {
            Files.createDirectories(Paths.get(this.extensionsDir));
        } catch (final IOException exception) {
            this.logger.critical("Nie można utworzyć katalogu dla rozszerzeń");
            throw new RuntimeException(exception);
        }
    }

    @Nullable
    public Extension loadExtension(final File file) throws Exception {
        final ExtensionDescription extensionDescription = this.getExtensionDescription(file);
        if (extensionDescription == null) {
            this.logger.critical("(&2" + file.getName() + "&r) Plik &bExtension.json&r ma nieprawidłową składnie albo nie istnieje");
            return null;
        }

        if (extensionDescription.name().contains(" ")) {
            throw new ExtensionException("'" + file.getName() + "' Nazwa rozszerzenia nie może zawierać spacji");
        }

        final Extension ex = this.getExtension(extensionDescription.name());

        if (ex != null) {
            if (ex.isLoaded()) return ex;
            throw new ExtensionException("Rozserzenie o nazwie: `" + extensionDescription.name() + "` już istnieje");
        }

        this.loadDependencies(file);
        this.loadSoftDependencies(file);

        final String className = extensionDescription.mainClass();
        final ExtensionClassLoader classLoader = new ExtensionClassLoader(this, this.getClass().getClassLoader(), file);

        this.classLoaders.put(extensionDescription.name(), classLoader);
        final Extension extension;
        try {
            final Class<?> javaClass = classLoader.loadClass(className);

            if (!Extension.class.isAssignableFrom(javaClass)) {
                throw new ExtensionException("'" + extensionDescription.mainClass() + "' nie rozszerza Extension");
            }

            try {
                final Class<? extends Extension> pluginClass = javaClass.asSubclass(Extension.class);

                extension = pluginClass.getDeclaredConstructor().newInstance();

                try {
                    extension.init(this.bdsAutoEnable, extensionDescription, this);
                } catch (final IOException exception) {
                    throw new ExtensionException("Nie udało się zainicjalizować `" + extensionDescription.name() + "`");
                }

                this.logger.info("Ładowanie&b " + extensionDescription.name() + "&r...");
                extension.onLoad();
                extension.setLoaded(true);

                this.extensions.put(extensionDescription.name(), extension);

                return extension;
            } catch (final InstantiationException | IllegalAccessException exception) {
                throw new ExtensionException(exception);
            }

        } catch (final ClassNotFoundException exception) {
            throw new ExtensionException("Nie można załadować rozszerzenia `" + extensionDescription.name() + "` główna klasa nie została odnaleziona");
        }
    }

    /**
     * To rozwiązanie ładowania i włączania soft dependencies i dependencies
     * może prowadzić do zapętlenia uruchamiania rozszerzeń, lecz na razie to najlepsze, jakie zrobiłem
     */

    private void loadSoftDependencies(final File file) throws Exception {
        final List<String> softDependencies = this.getExtensionDescription(file).softDependencies();
        if (softDependencies.isEmpty()) return;

        for (final String depend : softDependencies) {
            final File dependencyFile = this.findJarFileByName(depend);
            if (dependencyFile != null) {
                this.loadExtension(dependencyFile);
            } else {
                this.logger.error("Nie znaleziono zależności `" + depend + "`");
            }
        }
    }

    private void loadDependencies(final File file) throws Exception {
        final List<String> dependencies = this.getExtensionDescription(file).dependencies();
        if (dependencies.isEmpty()) return;

        for (final String depend : dependencies) {
            final File dependencyFile = this.findJarFileByName(depend);
            if (dependencyFile != null) {
                this.loadExtension(dependencyFile);
            } else {
                throw new ExtensionException("Nie znaleziono zależności `" + depend + "`");
            }
        }
    }

    public void loadExtensions() {
        if (this.jarFiles != null) {
            for (final File jarFile : this.jarFiles) {
                try {
                        this.loadExtension(jarFile);
                } catch (final Exception exception) {
                    this.logger.error("&c Nie udało załadować się &b" + jarFile.getName(), exception);
                }
            }
        }
    }

    private boolean isLoaded(final File file) {
        final ExtensionDescription description = this.getExtensionDescription(file);

        if (description == null) {
            throw new ExtensionException("(" + file.getName() + ") Plik Extension.json ma nieprawidłową składnie albo nie istnieje");
        }

        final Extension extension = this.getExtension(description.name());
        return (extension != null && extension.isLoaded());
    }

    public void enableExtension(final Extension extension) {
        if (extension.isEnabled()) return;
        try {
            this.enableDependencies(extension);
            this.enableSoftDependencies(extension);
            this.logger.info("Włączanie&b " + extension.getName());
            extension.onEnable();
            extension.setEnabled(true);
        } catch (final Exception exception) {
            extension.setEnabled(false);
            this.logger.error("Nie udało się włączyć&b " + extension.getName(), exception);
        }
    }

    private void enableSoftDependencies(final Extension extension) {
        final List<String> softDependencies = extension.getExtensionDescription().softDependencies();
        if (softDependencies.isEmpty()) return;

        for (final String depend : softDependencies) {
            final Extension dependency = this.extensions.get(depend);
            if (dependency != null) {
                if (!dependency.isEnabled()) {
                    this.enableExtension(dependency);
                }
            } else {
                extension.setEnabled(false);
                this.logger.error("Nie znaleziono zależności `" + depend + "`");
            }
        }
    }

    private void enableDependencies(final Extension extension) {
        final List<String> dependencies = extension.getExtensionDescription().dependencies();
        if (dependencies.isEmpty()) return;

        for (final String depend : dependencies) {
            final Extension dependency = this.extensions.get(depend);
            if (dependency != null) {
                if (!dependency.isEnabled()) {
                    this.enableExtension(dependency);
                }
            } else {
                extension.setEnabled(false);
                throw new ExtensionException("Nie znaleziono zależności `" + depend + "`");
            }
        }
    }

    public void enableExtensions() {
        for (final Map.Entry<String, Extension> entry : this.extensions.entrySet()) {
            this.enableExtension(entry.getValue());
        }
    }

    public void disableExtension(final Extension extension) {
        try {
            if (!extension.isEnabled()) return;
            this.logger.info("Wyłączanie&b " + extension.getName());
            extension.onDisable();
            extension.setEnabled(false);
        } catch (final Exception exception) {
            this.logger.error("Nie udało się wyłączyć&b " + extension.getName(), exception);
        }
    }

    public void disableExtensions() {
        for (final Map.Entry<String, Extension> entry : this.extensions.entrySet()) {
            this.disableExtension(entry.getValue());
        }
    }

    @Nullable
    public Extension getExtension(final String name) {
        return this.extensions.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    public ExtensionDescription getExtensionDescription(final File file) {
        try (final JarFile jar = new JarFile(file)) {
            JarEntry entry = jar.getJarEntry("Extension.json");
            if (entry == null) {
                entry = jar.getJarEntry("extension.json");

                if (entry == null) return null;
            }
            try (final InputStreamReader reader = new InputStreamReader(jar.getInputStream(entry))) {
                final ExtensionDescription description = GsonUtil.getGson().fromJson(reader, ExtensionDescription.class);

                final String author = description.author();
                final List<String> authors = description.authors();
                List<String> dependencies = description.dependencies();
                List<String> softDependencies = description.softDependencies();

                if (authors.isEmpty() || !authors.contains(author)) authors.add(author);
                if (dependencies == null) dependencies = new ArrayList<>();
                if (softDependencies == null) softDependencies = new ArrayList<>();

                return new ExtensionDescription(description.mainClass(), description.version(), description.name(),
                        author, description.description(), authors, dependencies, softDependencies);
            }
        } catch (final IOException exception) {
            return null;
        }
    }

    public String getExtensionsDir() {
        return this.extensionsDir;
    }

    public Map<String, Extension> getExtensions() {
        return this.extensions;
    }

    private File findJarFileByName(final String fileName) {
        for (final File file : this.jarFiles) {
            if (file.getName().contains(fileName)) {
                return file;
            }
        }
        return null;
    }

    public Class<?> getClassByName(final String name) {
        Class<?> cachedClass = this.classes.get(name);

        if (cachedClass != null) {
            return cachedClass;
        } else {
            for (final ExtensionClassLoader loader : this.classLoaders.values()) {
                try {
                    cachedClass = loader.findClass(name, false);
                } catch (final ClassNotFoundException ignore) {
                }
                if (cachedClass != null) {
                    return cachedClass;
                }
            }
        }
        return null;
    }

    public void setClass(final String name, final Class<?> clazz) {
        if (!this.classes.containsKey(name)) {
            this.classes.put(name, clazz);
        }
    }
}
