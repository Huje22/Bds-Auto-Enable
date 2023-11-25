package me.indian.bds.util;

import java.util.Scanner;

public final class ScannerUtil {
    private final Scanner scanner;

    public ScannerUtil(final Scanner scanner) {
        this.scanner = scanner;
    }

    public String addStringQuestion(final StringResponseConsumer question, final String defaultValue, final StringResponseConsumer response) {
        question.accept(defaultValue);
        String input = this.getInput();
        input = input.isEmpty() ? defaultValue : input;
        response.accept(input);

        return input;
    }

    public boolean addBooleanQuestion(final BooleanResponseConsumer question, final boolean defaultValue, final BooleanResponseConsumer response) {
        question.accept(defaultValue);
        final String input = this.getInput();
        final boolean userInput = input.isEmpty() ? defaultValue : Boolean.parseBoolean(input);
        response.accept(userInput);

        return userInput;
    }

    public int addIntQuestion(final IntegerResponseConsumer question, final int defaultValue, final IntegerResponseConsumer response) {
        question.accept(defaultValue);
        final String input = this.getInput();
        final int userInput = input.isEmpty() ? defaultValue : Integer.parseInt(input);
        response.accept(userInput);

        return userInput;
    }

    public double addDoubleQuestion(final DoubleResponseConsumer question, final double defaultValue, final DoubleResponseConsumer response) {
        question.accept(defaultValue);
        final String input = this.getInput();
        final double userInput = input.isEmpty() ? defaultValue : Double.parseDouble(input);
        response.accept(userInput);

        return userInput;
    }

    public Scanner getScanner() {
        return this.scanner;
    }

    private String getInput() {
        return this.scanner.nextLine();
    }

    @FunctionalInterface
    public interface StringResponseConsumer {
        void accept(final String consumer);
    }

    @FunctionalInterface
    public interface BooleanResponseConsumer {
        void accept(final boolean consumer);
    }

    @FunctionalInterface
    public interface IntegerResponseConsumer {
        void accept(final int consumer);
    }

    @FunctionalInterface
    public interface DoubleResponseConsumer {
        void accept(final double consumer);
    }
}