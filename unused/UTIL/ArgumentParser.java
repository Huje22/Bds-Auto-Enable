package pl.indianbartonka.bds;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.Nullable;


public final class ArgumentParser {

    private final Set<Arg> argsList = new LinkedHashSet<>();

    public List<Arg> parse(final String[] args) {
        final List<Arg> parsedArgs = new ArrayList<>();

        for (final String arg : args) {
            if (arg.startsWith("-")) {
                final String[] splitArg = arg.substring(1).split(":", 2);
                if (splitArg.length == 2) {
                    parsedArgs.add(new Arg(splitArg[0], splitArg[1]));
                } else {
                    parsedArgs.add(new Arg(splitArg[0], null));
                }
            }
        }

        this.argsList.addAll(parsedArgs);
        return parsedArgs;
    }

    @Nullable
    public Arg getArgByName(final String name) {
        return this.argsList.stream()
                .filter(arg -> arg.name().equalsIgnoreCase(name))
                .findAny()
                .orElse(null);
    }

    public boolean contains(final String name) {
        return this.argsList.stream().anyMatch(arg -> arg.name().equalsIgnoreCase(name));
    }

    public static void main(final String[] args) {
        final ArgumentParser parser = new ArgumentParser();
        final List<Arg> parsed = parser.parse(args);
        for (final Arg arg : parsed) {
            System.out.println("Name: " + arg.name() + ", Value: " + arg.value());
        }
        System.out.println();


        System.out.println(parser.contains("debug"));
        System.out.println(parser.getArgByName("serverport"));
    }
}

record Arg(String name, String value) {
}