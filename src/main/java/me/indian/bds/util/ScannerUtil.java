package me.indian.bds.util;

import me.indian.bds.logger.Logger;

import java.util.Scanner;

public class ScannerUtil {
    private final Scanner scanner;
    private Logger logger;


    public ScannerUtil(final Logger logger, final Scanner scanner) {
        this.logger = logger;
        this.scanner = scanner;
    }

//    public static void main(String[] args) {
//        Main main = new Main();
//        Logger logger = main.getLogger();
//        Config config = Main.getConfig();
//        ScannerUtil scannerUtil = new ScannerUtil(logger, new Scanner(System.in));
//    }


    public String addQuestion(ResponseConsumer question, String defaultValue, ResponseConsumer response) {
        question.accept(defaultValue);
        String input = getInput();
        input = input.isEmpty() ? defaultValue : input;
        response.accept(input);

        System.out.println();
        return input;
    }


    public boolean addQuestion(ResponseConsumer question, boolean defaultValue, ResponseConsumer response) {
        question.accept(String.valueOf(defaultValue));
        String input = getInput();
        boolean userInput = input.isEmpty() ? defaultValue : Boolean.parseBoolean(input);
        response.accept(String.valueOf(userInput));

        System.out.println();
        return userInput;
    }

    private String getInput() {
        return scanner.nextLine();
    }

    @FunctionalInterface
    public interface ResponseConsumer {
        void accept(String string);
    }
}
