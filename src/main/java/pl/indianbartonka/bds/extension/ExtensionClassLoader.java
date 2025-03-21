package pl.indianbartonka.bds.extension;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Kod użyty z <a href="https://github.com/CloudburstMC/Nukkit/blob/master/src/main/java/cn/nukkit/plugin/PluginClassLoader.java">...</a>
 */

public class ExtensionClassLoader extends URLClassLoader {
    private final Map<String, Class<?>> classes = new HashMap<>();
    private final ExtensionManager manager;

    public ExtensionClassLoader(final ExtensionManager manager, final ClassLoader parent, final File file) throws MalformedURLException {
        super(new URL[]{file.toURI().toURL()}, parent);
        this.manager = manager;
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        return this.findClass(name, true);
    }

    protected Class<?> findClass(final String name, final boolean checkGlobal) throws ClassNotFoundException {
        if (name.startsWith("pl.indianbartonka.bds")) { //Klasy aplikacji powinny być znane
            throw new ClassNotFoundException(name);
        }

        Class<?> result = this.classes.get(name);

        if (result == null) {
            if (checkGlobal) {
                result = this.manager.getClassByName(name);
            }

            if (result == null) {
                result = super.findClass(name);

                if (result != null) {
                    this.manager.setClass(name, result);
                }
            }

            this.classes.put(name, result);
        }

        return result;
    }

    public Set<String> getClasses() {
        return this.classes.keySet();
    }
}
