package me.indian.bds;

import me.indian.bds.config.Config;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerProperties;
import me.indian.bds.util.ScannerUtil;
import me.indian.bds.util.SystemOs;

import java.util.Arrays;
import java.util.Scanner;

public class Settings {

    private final BDSAutoEnable bdsAutoEnable;
    private final ServerProperties serverProperties;
    private final Logger logger;
    private final Config config;
    private final String enter;

    public Settings(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.serverProperties = this.bdsAutoEnable.getServerProperties();
        this.logger = this.bdsAutoEnable.getLogger();
        this.config = this.bdsAutoEnable.getConfig();
        this.enter = "[Enter = Domyślnie]";
    }

    public void loadSettings(final Scanner scanner) {
        final ScannerUtil scannerUtil = new ScannerUtil(scanner);
        if (!this.config.isFirstRun()) {
            scannerUtil.addQuestion((defaultValue) -> this.logger.info("Zastosować wcześniejsze ustawienia? (true/false) (Enter = true) "),
                    true,
                    (settings) -> {
                        if (settings) {
                            this.serverProperties.loadProperties();
                            if (this.config.isLoaded()) {
                                this.anotherVersionQuestion(scannerUtil);
                            }
                            this.currentSettings(scanner);
                        } else {
                            this.logger.info("Zaczynamy od nowa");
                            this.init(scannerUtil);
                        }
                    });
        } else {
            this.init(scannerUtil);
        }
    }

    private void init(final ScannerUtil scannerUtil) {
        final long startTime = System.currentTimeMillis();
        SystemOs system;
        try {
            system = SystemOs.valueOf(scannerUtil.addQuestion((defaultValue) -> {
                        this.logger.info("&lPodaj system&r (Wykryty system: " + defaultValue + "): " + this.enter);
                        this.logger.info("Obsługiwane systemy: " + Arrays.toString(SystemOs.values()));
                    }, Defaults.getSystem(),
                    (input) -> this.logger.info("System ustawiony na:&1 " + input.toUpperCase())
            ).toUpperCase());
        } catch (final IllegalArgumentException exception) {
            this.logger.error("Podano nie znany system , ustawiono domyślnie na: LINUX");
            system = SystemOs.LINUX;
        }
        this.config.setSystem(system);
        this.config.setFileName(scannerUtil.addQuestion((defaultValue) -> this.logger.info("&lPodaj nazwę pliku&r (Domyślnie: " + defaultValue + "): " + this.enter),
                Defaults.getDefaultFileName(),
                (input) -> this.logger.info("Nazwa pliku ustawiona na:&1 " + input)));

        if (Defaults.hasWine()) {
            this.config.setWine(scannerUtil.addQuestion(
                    (defaultValue) -> {
                        this.logger.info("&lWykryliśmy &r&bWINE&r&l czy użyć go? (Domyślnie: " + defaultValue + "): " + this.enter);
                        this.logger.alert("Jeśli chcesz użyć&b WINE&r plik musi kończyć się na&1 .exe");
                    },
                    false,
                    (input) -> {
                        if (input) {
                            if (!this.config.getFileName().contains(".exe")) {
                                this.logger.alert("Plik  musi mieć końcówkę&1 .exe&r aby&b WINE&r mogło go wykonać.");
                                this.logger.alert("Zmieniliśmy domyślną nazwe pliku na&1 bedrock_server.exe&r dla ciebie.");
                                this.config.setFileName("bedrock_server.exe");
                            }
                            this.logger.info("&bWINE&r ustawione na:&1 " + input);
                        }
                    }
            ));
        }

        this.config.setFilesPath(scannerUtil.addQuestion((defaultValue) -> this.logger.info("&lPodaj ścieżkę do plików servera&r  (Domyślnie: " + defaultValue + "): " + this.enter),
                Defaults.getJarDir(),
                (input) -> this.logger.info("Ścieżke do plików servera ustawiona na: " + input)));
        this.config.save();

        if (!this.config.isLoaded()) this.versionQuestion(scannerUtil);
        this.serverProperties.loadProperties();

        final boolean backup = scannerUtil.addQuestion((defaultValue) -> this.logger.info("&lWłączyć Backupy&r (Domyślnie: " + defaultValue + ")? " + this.enter),
                true,
                (input) -> this.logger.info("Backupy ustawione na:&1 " + input));
        this.config.getWatchDogConfig().getBackup().setBackup(backup);
        if (backup) {
            final int backupFrequency = scannerUtil.addQuestion((defaultValue) -> this.logger.info("&lCo ile minut robić backup?&r (Domyślnie: " + defaultValue + ")? " + this.enter),
                    60,
                    (input) -> this.logger.info("Backup bedzie robiony co:&1 " + (input == 0 ? 60 : input + "&a minut")));
            this.config.getWatchDogConfig().getBackup().setBackupFrequency(backupFrequency == 0 ? 60 : backupFrequency);
        }

        this.serverSettings(scannerUtil);

        this.logger.info("Ukończono odpowiedzi w&a " + ((System.currentTimeMillis() - startTime) / 1000.0) + "&r sekund");
        this.config.save();
        this.currentSettings(scannerUtil.getScanner());
    }

    private void serverSettings(final ScannerUtil scannerUtil) {
           /*
        TODO:
            Dodać pełne wspracje dla:
            "default-player-permission-level" ( 0 = VISITOR , 1 = MEMBER , 2 = OPERATOR),
            "server-authoritative-movement" wraz z "correct-player-movement",
            "difficulty" (0 = peaceful, 1 = easy, 2 = normal, 3 = hard) METODA JUZ ISTNIEJE W "ServerProperties"
         */

        this.logger.info("&aKonfiguracja servera&r");
        System.out.println();

        this.serverProperties.setServerPort(scannerUtil.addQuestion((defaultValue) -> {
            this.logger.info("&lUstaw port v4?&r (Aktualny z &bserver.properties&r to: " + defaultValue + ") " + this.enter);
            this.logger.info("&cPamiętaj że twoja sieć musi miec dostępny ten port");
        }, this.serverProperties.getServerPort(), (input) -> this.logger.info("Port v4 ustawiony na:&1 " + input)));

        this.serverProperties.setServerPortV6(scannerUtil.addQuestion((defaultValue) -> {
            this.logger.info("&lUstaw port v6?&r (Aktualny z &bserver.properties&r to: " + defaultValue + ") " + this.enter);
            this.logger.info("&cJeśli twoja sieć obsługuje&b ipv6&c ustaw go na dostępny z puli portów");
        }, this.serverProperties.getServerPortV6(), (input) -> this.logger.info("Port v6 ustawiony na:&1 " + input)));

        this.serverProperties.setMaxThreads(scannerUtil.addQuestion((defaultValue) -> {
                    this.logger.info("&lLiczba wątków używana przez server&r ");
                    this.logger.info("Maksymalna liczba wątków, jakie serwer będzie próbował wykorzystać, Jeśli ustawione na&b 0&r wtedy będzie używać najwięcej jak to możliwe.");
                }, 0,
                (input) -> this.logger.info("Liczba wątków ustawiona na:&1 " + input)));

        this.serverProperties.setAllowCheats(scannerUtil.addQuestion(
                (defaultValue) -> {
                    this.logger.info("&lAllow Cheats&r (Aktualny z &bserver.properties&r to: " + defaultValue + ") " + this.enter);
                    this.logger.alert("Aby integracja z discord za pomocą&1 JDA&r działała poprawnie wymagamy tego na&b true&r i włączenie&b experymentów&r!");
                },
                this.serverProperties.isAllowCheats(),
                (input) -> this.logger.info("Allow Cheats ustawione na:&1 " + input)));

        this.serverProperties.setPlayerIdleTimeout(scannerUtil.addQuestion((defaultValue) -> {
                    this.logger.info("&lUstaw Timeout &r (Aktualny z &bserver.properties&r to: " + defaultValue + ") " + this.enter);
                    this.logger.info("Gdy gracz będzie bezczynny przez tyle minut, zostanie wyrzucony.");
                    this.logger.info("Jeśli ustawione na 0, gracze mogą pozostawać bezczynni przez czas nieokreślony.");
                },
                this.serverProperties.getPlayerIdleTimeout(),
                (input) -> this.logger.info("Timeout ustawiony na:&1 " + input)));

        this.serverProperties.setClientSideChunkGeneration(scannerUtil.addQuestion(
                (defaultValue) -> {
                    this.logger.info("&lClient Side Chunks&r (Domyślnie: " + defaultValue + ")? " + this.enter);
                    this.logger.alert("Może być to bardzo lagujące dla starszych i słabszych urządzeń");
                    this.logger.alert("Także gdy server przeznaczony jest pod większą ilość osób jest zalecane to włączyć!");
                    this.logger.info("Jeśli jest &1true&r serwer poinformuje klientów, " + "że mają możliwość generowania chunków poziomu wizualnego poza odległościami interakcji graczy. ");
                },
                true,
                (input) -> this.logger.info("Ustawiono Client Side Chunks na:&1 " + input)));

        if (this.serverProperties.isClientSideChunkGeneration()) {
            this.serverProperties.setServerBuildRadiusRatio(scannerUtil.addDoubleQuestion(
                    (defaultValue) -> {
                        this.logger.info("&lUstaw Server Build Radius Ratio&r (Domyślnie&r to: " + defaultValue + ") " + this.enter);
                        this.logger.info("Jeśli&1 Disabled&r, serwer dynamicznie obliczy, ile z widoku gracza zostanie wygenerowane, pozostawiając resztę do zbudowania przez klienta.");
                        this.logger.info("W przeciwnym razie, z nadpisanej proporcji, serwerowi zostanie powiedziane, ile z widoku gracza wygenerować, pomijając zdolności sprzętowe klienta.");
                        this.logger.info("Zakres to od&b 0.0 &rdo &b 1.0");
                        this.logger.alert("Aby wyłączyć i zostawić generowanie czank całkowicie po stronie klienta wpisz&b -1.0&r a my zrobimy to za ciebie! .");
                    },
                    1.0,
                    (input) -> this.logger.info("Server Build Radius Ratio ustawione na:&1 " + input)
            ));
        }

        this.serverProperties.setViewDistance(scannerUtil.addQuestion(
                (defaultValue) -> {
                    this.logger.info("&lUstaw View Distance&r (Aktualny z &bserver.properties&r to: " + defaultValue + ") " + this.enter);
                    this.logger.info("Maksymalna dozwolona odległość widoku wyrażona w liczbie chunk");
                    this.logger.alert("Jeśli&1 Client Side Chunks&r jest na&b false&r lub&1 server-build-radius-ratio&r na coś innego niż&b Disabled ");
                    this.logger.alert("Większe liczby niż 12-32 mogę obciążać server gdy klient ma duży render distance");
                },
                this.serverProperties.getViewDistance(),
                (input) -> this.logger.info("Maksymalny View Distance na:&1 " + input)));

        this.serverProperties.setTickDistance(scannerUtil.addQuestion((defaultValue) -> {
                    this.logger.info("&lUstaw Tick Distance &r (Aktualna z &bserver.properties&r to: " + defaultValue + ") " + this.enter);
                    this.logger.info("Świat zostanie wstrzymany po tylu chunk od gracza");
                    this.logger.alert("Duże ilości mogą prowadzić do lagów servera!");
                },
                this.serverProperties.getTickDistance(),
                (input) -> this.logger.info("Tick Distance na:&1 " + input)));


        /*
        TODO:
            Dodać pełne wspracje dla:
            "default-player-permission-level" ( 0 = VISITOR , 1 = MEMBER , 2 = OPERATOR),
            "server-authoritative-movement" wraz z "correct-player-movement",
            "difficulty" (0 = peaceful, 1 = easy, 2 = normal, 3 = hard) METODA JUZ ISTNIEJE W "ServerProperties"
         */
    }


    private void currentSettings(final Scanner scanner) {
        System.out.println();
        this.logger.info("&lAktualne Dane");
        System.out.println();
        this.logger.info("&e----------&bAplikacja&e----------");
        this.logger.info("System:&1 " + this.config.getSystem());
        this.logger.info("Nazwa pliku:&1 " + this.config.getFileName());
        this.logger.info("Wine:&1 " + this.config.isWine() + (Defaults.hasWine() ? " &d(&bPosiadasz&d)" : ""));
        this.logger.info("Ścieżka plików:&b " + this.config.getFilesPath());
        this.logger.info("Wersja:&1 " + this.config.getVersion());

        final boolean backup = this.config.getWatchDogConfig().getBackup().isBackup();
        this.logger.info("Backup:&1 " + backup);

        if (backup) {
            this.logger.info("Częstotliwość robienia backup:&1 " + this.config.getWatchDogConfig().getBackup().getBackupFrequency() + "&a minut");
            this.logger.info("Nazwa świata:&1 " + this.serverProperties.getWorldName());
        }

        System.out.println();
        this.logger.info("&e-----------&bserver.properties&e----------");
        this.logger.info("Port v4:&1 " + this.serverProperties.getServerPort());
        this.logger.info("Port v6:&1 " + this.serverProperties.getServerPortV6());
        this.logger.info("Liczba wątków używana przez server:&1 " + this.serverProperties.getMaxThreads());
        this.logger.info("Allow Cheats:&1 " + this.serverProperties.isAllowCheats());
        this.logger.info("Timeout:&1 " + this.serverProperties.getPlayerIdleTimeout() + " &aminut");
        this.logger.info("Czy klient generuje chunki:&1 " + this.serverProperties.isClientSideChunkGeneration());

        final double serverBuildRadiusRatio = this.serverProperties.getServerBuildRadiusRatio();
        this.logger.info("Server Build Radius Ratio:&1 " + (serverBuildRadiusRatio == -1.0 ? "Disabled" : serverBuildRadiusRatio));

        this.logger.info("View Distance:&1 " + this.serverProperties.getViewDistance());
        this.logger.info("Tick Distance:&1 " + this.serverProperties.getTickDistance());

        System.out.println();
        this.logger.alert("&cWięcej opcij&e konfiguracji&c znajdziesz w config");
        this.logger.info("Kliknij enter aby kontynuować");
        scanner.nextLine();

        if (this.config.isFirstRun()) {
            this.config.setFirstRun(false);
        }
        this.config.save();
    }

    private void versionQuestion(final ScannerUtil scannerUtil) {
        this.config.setVersion(scannerUtil.addQuestion(
                (defaultValue) -> {
                    this.logger.info("&lJaką versie załadować?&r (Domyślnie: " + defaultValue + "): " + this.enter);
                    if (this.bdsAutoEnable.getVersionManager().getAvailableVersions().isEmpty()) {
                        this.logger.info("Nie znaleziono żadnej wersji");
                    } else {
                        this.logger.info("Pobrane wersje: " + this.bdsAutoEnable.getVersionManager().getAvailableVersions());
                    }
                    this.logger.info("Aby pobrać jakąś versie wpisz jej numer (niektóre mogą mieć .01 / .02 na końcu)");
                },
                this.config.getVersion(), (input) -> this.logger.info("Wersja do załadowania ustawiona na:&1 " + input)
        ));
        this.bdsAutoEnable.getVersionManager().loadVersion();
    }

    private void anotherVersionQuestion(final ScannerUtil scannerUtil) {
        scannerUtil.addQuestion((defaultValue) -> this.logger.info("&lZaładować jakąś inną versie&r (Domyślnie: " + defaultValue + "): " + this.enter),
                false,
                (input) -> {
                    if (input) {
                        this.config.setLoaded(false);
                        this.config.save();
                        this.versionQuestion(scannerUtil);
                    }
                });
    }
}