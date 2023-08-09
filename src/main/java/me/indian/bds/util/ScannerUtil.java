package me.indian.bds.util;

import java.util.Scanner;

public class ScannerUtil {
    private final Scanner scanner;


    public ScannerUtil(final Scanner scanner) {
        this.scanner = scanner;
    }

    public String addQuestion(final ResponseConsumer question, final String defaultValue, final ResponseConsumer response) {
        question.accept(defaultValue);
        String input = getInput();
        input = input.isEmpty() ? defaultValue : input;
        response.accept(input);

        System.out.println();
        return input;
    }


    public boolean addQuestion(final ResponseConsumer question, final boolean defaultValue, final ResponseConsumer response) {
        question.accept(String.valueOf(defaultValue));
        String input = getInput();
        boolean userInput = input.isEmpty() ? defaultValue : Boolean.parseBoolean(input);
        response.accept(String.valueOf(userInput));

        System.out.println();
        return userInput;
    }

    public int addQuestion(final ResponseConsumer question, final int defaultValue, final ResponseConsumer response) {
        question.accept(String.valueOf(defaultValue));
        String input = getInput();
        int userInput = input.isEmpty() ? defaultValue : Integer.parseInt(input);
        response.accept(String.valueOf(userInput));

        System.out.println();
        return userInput;
    }

    private String getInput() {
        return scanner.nextLine();
    }

    @FunctionalInterface
    public interface ResponseConsumer {
        void accept(final String string);
    }
}
