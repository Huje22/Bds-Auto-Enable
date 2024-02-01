package me.indian.bds.extension;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.DefaultsVariables;
import me.indian.bds.util.GsonUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ExtensionLoader {


    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final List<Extension> extensions;
    private final String extensionsDir;

    public ExtensionLoader(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.extensions = new ArrayList<>();
        this.extensionsDir = DefaultsVariables.getAppDir() + "extensions";
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
                final ExtensionDescription extensionDescription = this.getExtensionDescription(jarFile);
                if (extensionDescription == null)
                    throw new NullPointerException("Plik 'Extension.json' ma nieprawidłową składnie albo nie istnieje");

                try (final URLClassLoader classLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()})) {

                    final Class<?> extensionClass = classLoader.loadClass(extensionDescription.mainClass());

                    if (!Extension.class.isAssignableFrom(extensionClass)) {
                        throw new IllegalAccessException(extensionDescription.mainClass() + " nie rozszerza 'Extension'");
                    }

                    final Extension extension = (Extension) extensionClass.newInstance();

                    extension.init(this.bdsAutoEnable, extensionDescription, this);

                    this.logger.info("Ładowanie&b " + extensionDescription.name() + "&r...");
                    extension.onLoad();
                    this.extensions.add(extension);

                } catch (final Exception exception) {
                    this.logger.error("Nie udało się załadować&b " + extensionDescription.name(), exception);
                }
            }
        }
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
                System.out.println(extension);
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
}