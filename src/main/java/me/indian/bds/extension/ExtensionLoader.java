package me.indian.bds.extension;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.exception.ExtensionException;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.DefaultsVariables;
import me.indian.bds.util.GsonUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ExtensionLoader {


    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final List<Extension> extensions;
    private final String extensionsDir;
    private final Map<String, Class> classes;
    private final Map<String, ExtensionClassLoader> classLoaders;

    public ExtensionLoader(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.extensions = new ArrayList<>();
        this.extensionsDir = DefaultsVariables.getAppDir() + "extensions";
        this.classes = new HashMap<>();
        this.classLoaders = new HashMap<>();
    }

    public void loadExtensions() {

        try {
            Files.createDirectories(Paths.get(this.extensionsDir));
        } catch (final IOException exception) {
            this.logger.critical("Nie można utworzyć katalogu dla rozszerzeń");
            throw new RuntimeException(exception);
        }

        final File[] jarFiles = new File(this.extensionsDir).listFiles(pathname -> pathname.getName().endsWith(".jar"));

        if (jarFiles != null) {
            for (final File jarFile : jarFiles) {
                try {
                    this.loadExtension(jarFile);
                } catch (final Exception exception) {
                   exception.printStackTrace();
                }
            }
        }
    }

    public Extension loadExtension(final File file) throws Exception {
        final ExtensionDescription extensionDescription = this.getExtensionDescription(file);
        if (extensionDescription == null) {
            this.logger.critical("(&a" + file.getName() + "&r) Plik &bExtension.json&r ma nieprawidłową składnie albo nie istnieje");
            return null;
        }

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
                final Class<Extension> pluginClass = (Class<Extension>) javaClass.asSubclass(Extension.class);

                extension = pluginClass.newInstance();

                try {
                    extension.init(this.bdsAutoEnable, extensionDescription, this);
                } catch (final IOException exception) {
                    throw new ExtensionException("Nie udało się zainicjalizować `" + extensionDescription.name() + "`");
                }

                this.logger.info("Ładowanie&b " + extensionDescription.name() + "&r...");
                extension.onLoad();
                this.extensions.add(extension);

                return extension;
            } catch (final InstantiationException | IllegalAccessException exception) {
                this.bdsAutoEnable.getLogger().logThrowable(exception);
            }

        } catch (final ClassNotFoundException exception) {
            throw new ExtensionException("Nie można załadować rozszerzenia `" + extensionDescription.name() + "` główna klasa nie została odnaleziona");
        }
        return null;
    }


    public void enableExtensions() {
        for (final Extension extension : this.extensions) {
            try {
                this.logger.info("Włączanie&b " + extension.getName());
                extension.onEnable();
                extension.setEnabled(true);
            } catch (final Exception exception) {
                extension.setEnabled(false);
                this.logger.error("Nie udało się włączyć&b " + extension.getName(), exception);
            }
        }
    }

    public void disableExtensions() {
        for (final Extension extension : this.extensions) {
            try {
                this.logger.info("Wyłączanie&b " + extension.getName());
                extension.onDisable();
                extension.setEnabled(false);
            } catch (final Exception exception) {
                this.logger.error("Nie udało się wyłączyć&b " + extension.getName(), exception);
            }
        }
    }

    public ExtensionDescription getExtensionDescription(final File file) {
        try (final JarFile jar = new JarFile(file)) {
            final JarEntry entry = jar.getJarEntry("Extension.json");
            if (entry == null) return null;
            try (final InputStreamReader reader = new InputStreamReader(jar.getInputStream(entry))) {
                return GsonUtil.getGson().fromJson(reader, ExtensionDescription.class);
            }
        } catch (final IOException exception) {
            return null;
        }
    }


    public String getExtensionsDir() {
        return this.extensionsDir;
    }

    public List<Extension> getExtensions() {
        return this.extensions;
    }

    Class<?> getClassByName(final String name) {
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

    void setClass(final String name, final Class<?> clazz) {
        if (!this.classes.containsKey(name)) {
            this.classes.put(name, clazz);
        }
    }
}