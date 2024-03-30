package me.indian.bds.extension;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.event.EventManager;
import me.indian.bds.event.server.ExtensionDisableEvent;
import me.indian.bds.event.server.ExtensionEnableEvent;
import me.indian.bds.exception.ExtensionException;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.DateUtil;
import me.indian.bds.util.DefaultsVariables;
import me.indian.bds.util.GsonUtil;
import org.jetbrains.annotations.Nullable;

public class ExtensionManager {

    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final EventManager eventManager;
    private final Map<String, Extension> extensions;
    private final String extensionsDir;
    private final File[] jarFiles;
    private final Map<String, Class<?>> classes;
    private final Map<String, ExtensionClassLoader> classLoaders;


    public ExtensionManager(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.eventManager = this.bdsAutoEnable.getEventManager();
        this.extensions = new LinkedHashMap<>();
        this.extensionsDir = this.createExtensionDir();
        this.jarFiles = new File(this.extensionsDir).listFiles(pathname -> pathname.getName().endsWith(".jar"));
        this.classes = new HashMap<>();
        this.classLoaders = new HashMap<>();

    }

    @Nullable
    public Extension loadExtension(final File file) throws Exception {
        final ExtensionDescription description = this.getExtensionDescription(file);
        if (description == null) {
            this.logger.critical("(&2" + file.getName() + "&r) Plik &bExtension.json&r ma nieprawidłową składnie albo nie istnieje");
            return null;
        }

        if (description.name().contains(" ")) {
            throw new ExtensionException("'" + file.getName() + "' Nazwa rozszerzenia nie może zawierać spacji");
        }

        final Extension ex = this.getExtension(description.name());

        if (ex != null) {
            if (ex.isLoaded()) return ex;
            throw new ExtensionException("Rozszerzenie o nazwie: `" + description.name() + "` już istnieje");
        }

        final String className = description.mainClass();
        final ExtensionClassLoader classLoader = new ExtensionClassLoader(this, this.getClass().getClassLoader(), file);

        this.classLoaders.put(description.name(), classLoader);
        final Extension extension;
        try {
            final Class<?> javaClass = classLoader.loadClass(className);

            if (!Extension.class.isAssignableFrom(javaClass)) {
                throw new ExtensionException("'" + description.mainClass() + "' nie rozszerza Extension");
            }

            try {
                final Class<? extends Extension> extensionClass = javaClass.asSubclass(Extension.class);

                extension = extensionClass.getDeclaredConstructor().newInstance();

                try {
                    extension.init(this.bdsAutoEnable, description, this);
                } catch (final IOException exception) {
                    throw new ExtensionException("Nie udało się zainicjalizować `" + description.name() + "`");
                }

                extension.onLoad();
                extension.setLoaded(true);
                this.logger.info("Załadowano&b " + description.name());
                this.extensions.put(description.name(), extension);

                return extension;
            } catch (final InstantiationException | IllegalAccessException exception) {
                throw new ExtensionException(exception);
            }

        } catch (final ClassNotFoundException exception) {
            throw new ExtensionException("Nie można załadować rozszerzenia `" + description.name() + "` główna klasa nie została odnaleziona");
        }
    }

    public Set<String> dependencyGraph() {
        final Map<String, List<String>> map = new HashMap<>();
        for (final File jar : this.jarFiles) {
            final ExtensionDescription description = this.getExtensionDescription(jar);
            if (description != null) {
                final List<String> dependencies = new LinkedList<>();
                dependencies.addAll(description.dependencies());
                dependencies.addAll(description.softDependencies());
                map.put(description.name(), dependencies);
            }
        }

        final Set<String> sorted = new LinkedHashSet<>();
        for (final Map.Entry<String, List<String>> entry : map.entrySet()) {
            final String name = entry.getKey();
            sorted.addAll(entry.getValue());
            sorted.add(name);
        }

        return sorted;
    }

    public void loadExtensions() {
        for (final String extensionName : this.dependencyGraph()) {
            final File jarFile = this.findJarFileByName(extensionName);
            if (jarFile == null) {
                this.logger.error("&cNie udało się odnaleźć:&b " + extensionName);
                continue;
            }

            try {
                this.loadExtension(jarFile);
            } catch (final Exception | Error throwable) {
                this.logger.error("&cNie udało załadować się &b" + jarFile.getName(), throwable);
            }
        }
    }

    public void enableExtension(final Extension extension) {
        if (extension.isEnabled()) return;
        try {
            extension.onEnable();
            extension.setEnabled(true);
            this.eventManager.callEvent(new ExtensionEnableEvent(extension));
            this.logger.info("Włączono&b " + extension.getName() + "&r (Wersja:&a " + extension.getVersion() + "&r Autor:&a " + extension.getAuthor() + "&r)");
        } catch (final Exception | Error throwable) {
            extension.setEnabled(false);
            extension.onDisable();
            this.logger.error("Nie udało się włączyć&b " + extension.getName() + "&r (Wersja:&a " + extension.getVersion() + "&r Autor:&a " + extension.getAuthor() + "&r)", throwable);
        }
    }

    public void enableExtensions() {
        final long startTime = System.currentTimeMillis();
        for (final Map.Entry<String, Extension> entry : this.extensions.entrySet()) {
            this.enableExtension(entry.getValue());
        }

        final String formattedTime = DateUtil.formatTime((System.currentTimeMillis() - startTime), List.of('s', 'i'), true);
        this.logger.info("Włączono&b " + this.extensions.size() + "&r rozszerzeń w czasie&1 " + formattedTime);
    }

    public void disableExtension(final Extension extension) {
        try {
            if (!extension.isEnabled()) return;
            extension.onDisable();
            this.eventManager.unRegister(extension);
            this.bdsAutoEnable.getCommandManager().unRegister(extension);
            extension.setEnabled(false);
            this.eventManager.callEvent(new ExtensionDisableEvent(extension));
            this.logger.info("Wyłączono&b " + extension.getName() + "&r (Wersja:&a " + extension.getVersion() + "&r Autor:&a " + extension.getAuthor() + "&r)");
        } catch (final Exception | Error throwable) {
            this.logger.error("Nie udało się wyłączyć&b " + extension.getName() + "&r (Wersja:&a " + extension.getVersion() + "&r Autor:&a " + extension.getAuthor() + "&r)", throwable);
        }
    }

    public void disableExtensions() {
        final long startTime = System.currentTimeMillis();
        for (final Map.Entry<String, Extension> entry : this.extensions.entrySet()) {
            this.disableExtension(entry.getValue());
        }

        final String formattedTime = DateUtil.formatTime((System.currentTimeMillis() - startTime), List.of('s', 'i'), true);
        this.logger.info("Wyłączono&b " + this.extensions.size() + "&r rozszerzeń w czasie&1 " + formattedTime);
    }

    @Nullable
    public Extension getExtension(final String name) {
        return this.extensions.entrySet().stream()
                .filter(entry -> entry.getKey().toLowerCase().startsWith(name.toLowerCase()) ||
                        entry.getKey().equalsIgnoreCase(name))
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
                final String prefix = (description.prefix() == null ? description.name() : description.prefix());
                List<String> authors = description.authors();
                List<String> dependencies = description.dependencies();
                List<String> softDependencies = description.softDependencies();

                if (authors == null) authors = new ArrayList<>();
                if (authors.isEmpty() || !authors.contains(author)) authors.add(author);
                if (dependencies == null) dependencies = new ArrayList<>();
                if (softDependencies == null) softDependencies = new ArrayList<>();

                return new ExtensionDescription(description.mainClass(), description.version(), description.name(),
                        prefix, description.description(),
                        description.author(), authors, dependencies, softDependencies);
            }
        } catch (final IOException exception) {
            return null;
        }
    }

    private String createExtensionDir() {
        final String extensionsDir = DefaultsVariables.getAppDir() + "extensions";

        try {
            Files.createDirectories(Paths.get(extensionsDir));
        } catch (final IOException exception) {
            throw new RuntimeException("Nie można utworzyć katalogu dla rozszerzeń", exception);
        }

        return extensionsDir;
    }

    public String getExtensionsDir() {
        return this.extensionsDir;
    }

    public Map<String, Extension> getExtensions() {
        return this.extensions;
    }

    @Nullable
    private File findJarFileByName(final String fileName) {
        for (final File file : this.jarFiles) {
            final ExtensionDescription description = this.getExtensionDescription(file);
            if (description != null && description.name().contains(fileName)) return file;

            if (file.getName().contains(fileName)) {
                return file;
            }
        }
        return null;
    }

    @Nullable
    public Class<?> getClassByName(final String name) {
        Class<?> cachedClass = this.classes.get(name);

        if (cachedClass != null) {
            return cachedClass;
        } else {
            for (final ExtensionClassLoader manager : this.classLoaders.values()) {
                try {
                    cachedClass = manager.findClass(name, false);
                } catch (final ClassNotFoundException ignored) {
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