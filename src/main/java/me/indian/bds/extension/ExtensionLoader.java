package me.indian.bds.extension;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.DefaultsVariables;
import me.indian.bds.util.GsonUtil;

import java.io.File;
import java.io.FileInputStream;
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
import java.util.jar.JarInputStream;

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
                this.loadExtension(jarFile);
            }
        }
    }

    public void loadExtension(final File file) {
        final ExtensionDescription extensionDescription = this.getExtensionDescription(file);
        if (extensionDescription == null) {

            this.logger.critical("(&a" + file.getName() + "&r) Plik '&bExtension.json&r' ma nieprawidłową składnie albo nie istnieje");
            return;
        }

        try (final URLClassLoader classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()})) {
            final Class<?> extensionClass = classLoader.loadClass(extensionDescription.mainClass());

            this.loadClasses(file, classLoader);


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

    private void loadClasses(final File file, final ClassLoader classLoader) throws IOException, ClassNotFoundException, IllegalAccessException {
        try (final JarInputStream jarInputStream = new JarInputStream(new FileInputStream(file))) {
            JarEntry entry;
            while ((entry = jarInputStream.getNextJarEntry()) != null) {
                if (entry.getName().endsWith(".class")) {

                    final String classPath = entry.getName()
                            .replaceAll("/", ".")
                            .replaceAll(".class", "");


                    System.out.println(classPath);

                    final Class<?> loadedClass = classLoader.loadClass(classPath);

                    try {
                        final Object instance = loadedClass.newInstance();
                    } catch (final InstantiationException exception) {
                        System.out.println(classPath + " nie udalo sie");
                    }
                }
            }
        }
    }
    
    public String getExtensionsDir() {
        return this.extensionsDir;
    }

    public List<Extension> getExtensions() {
        return this.extensions;
    }
}