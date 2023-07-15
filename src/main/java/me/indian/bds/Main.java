package me.indian.bds;

import me.indian.bds.logger.impl.Logger;
import me.indian.bds.util.SystemOs;
import me.indian.bds.util.ThreadUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {


    private static Logger logger;
    private static ExecutorService service;
    private static String jarPath;
    private static String filePath;
    private static String name;
    private static SystemOs os;
    private static boolean wine;


    public Main() {
        jarPath = getJarPath();
        logger = new Logger();
        service = Executors.newScheduledThreadPool(ThreadUtil.getThreadsCount(), new ThreadUtil("Auto restart"));
    }

    public static void main(String[] args) {
        new Main();

        init();

        final File file = new File(filePath);

        if (file.exists()) {
            logger.info("Odnaleziono " + name);
        } else {
            logger.critical("Nie można odnaleźć pliku " + name);
            logger.alert("Ścieżka " + filePath);
            return;
        }

        startProcess();
    }

    private static void init() {
        final Scanner scanner = new Scanner(System.in);
        String input;

        logger.info("Podaj system: ");
        logger.info("Obsługiwane systemy: " + Arrays.toString(SystemOs.values()));

        input = scanner.nextLine();
        os = input.isEmpty() ? SystemOs.LINUX : SystemOs.valueOf(input.toUpperCase());
        logger.info("System ustawiony na: " + os);
        System.out.println();

        logger.info("Podaj nazwę pliku (Domyślnie: bedrock_server.exe): ");
        input = scanner.nextLine();
        name = input.isEmpty() ? "bedrock_server.exe" : input;
        logger.info("Nazwa pliku ustawiona na: " + name);
        System.out.println();


        if (os != SystemOs.WINDOWS) {
            logger.info("Uzyć wine? (true/false): ");
            wine = scanner.nextBoolean();
            logger.info("Użycie wine ustawione na: " + wine);
            System.out.println();
        } else {
            wine = false;
        }

        logger.info("Podaj ścieżkę do pliku (Domyślnie " + jarPath + File.separator + name + "): ");
        input = scanner.nextLine();
        filePath = input.isEmpty() ? jarPath + File.separator + name : input;
        System.out.println();

        logger.info("Podane informacje:");
        logger.info("Nazwa: " + name);
        logger.info("Pełnoletni: " + wine);
        logger.info("Ścieżka do pliku: " + filePath);

        logger.info("Kliknij enter przycisk aby kontunować");
        scanner.nextLine();


//        System.exit(0);
    }


    public static String getJarPath() {
        return System.getProperty("user.dir");
    }

    public static boolean isProcessRunning() {
        try {
            String command = "";

            switch (os) {
                case LINUX:
                    command = "pgrep -f " + name;
                    break;
                case WINDOWS:
                    command = "tasklist.exe /FI \"IMAGENAME eq " + filePath + "\"";
                    break;
                default:
                    logger.critical("Musisz podać odpowiedni system");
                    System.exit(0);

            }

            final Process process = Runtime.getRuntime().exec(command);
            process.waitFor();

            final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    return true;
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static void startProcess() {
        if (isProcessRunning()) {
            logger.info("Proces " + name + " jest już uruchomiony.");
        } else {
            logger.info("Proces " + name + " nie jest uruchomiony. Uruchamianie...");

            try {
                String command = filePath;
                ProcessBuilder processBuilder = null;

                switch (os) {
                    case LINUX:
                        if (wine) {
                            command = "wine";
                        } else {
                            command = "LD_LIBRARY_PATH=.";
                        }
                        processBuilder = new ProcessBuilder(command, filePath);
                        break;
                    case WINDOWS:
                        processBuilder = new ProcessBuilder(filePath);
                        break;
                    default:
                        logger.critical("Musisz podać odpowiedni system");
                        System.exit(0);
                }


                processBuilder.inheritIO();

                logger.info(processBuilder.inheritIO());

                final Process process = processBuilder.start();

                final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }

                process.waitFor(); 
                startProcess();

            } catch (Exception exception) {
                logger.critical("Nie można uruchomic procesu");
                logger.critical(exception);
                exception.printStackTrace();
                System.exit(0);
            }
        }
    }
}