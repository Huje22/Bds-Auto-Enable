package me.indian.bds;

import me.indian.bds.config.Config;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.properties.PlayerPermissionLevel;
import me.indian.bds.server.properties.ServerMovementAuth;
import me.indian.bds.server.properties.ServerProperties;
import me.indian.bds.util.MessageUtil;
import me.indian.bds.util.ScannerUtil;

import java.util.List;
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
        this.enter = " [Enter = Domyślnie]";
    }

    public void loadSettings(final Scanner scanner) {
        final ScannerUtil scannerUtil = new ScannerUtil(scanner);
        if (!this.config.isFirstRun()) {
            scannerUtil.addBooleanQuestion((defaultValue) -> this.logger.info("&n&lZastosować wcześniejsze ustawienia?&r (true/false) " + this.enter),
                    true,
                    (settings) -> {
                        if (settings) {
                            this.serverProperties.loadProperties();
                            if (this.config.getVersionManagerConfig().isLoaded()) {
                                this.anotherVersionQuestion(scannerUtil);
                            }
                            this.againSetupServer(scannerUtil);
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

        System.out.println();

        if (Defaults.hasWine()) {
            this.config.setWine(scannerUtil.addBooleanQuestion(
                    (defaultValue) -> {
                        this.logger.info("&n&lWykryliśmy &r&bWINE&r&n&l czy użyć go?&r (Domyślnie: " + defaultValue + ")" + this.enter);
                        this.logger.alert("Jeśli chcesz użyć&b WINE&r plik musi kończyć się na&1 .exe");
                    },
                    false,
                    (input) -> this.logger.info("&bWINE&r ustawione na:&1 " + input)
            ));
            System.out.println();
        }

        this.config.setFilesPath(scannerUtil.addStringQuestion(
                (defaultValue) -> this.logger.info("&n&lPodaj ścieżkę do plików servera&r (Domyślnie: " + defaultValue + ")" + this.enter),
                Defaults.getJarDir(),
                (input) -> this.logger.info("Ścieżke do plików servera ustawiona na: " + input)
        ));
        this.config.save();
        System.out.println();

        if (!this.config.getVersionManagerConfig().isLoaded()) this.versionQuestion(scannerUtil);
        this.serverProperties.loadProperties();

        final boolean backup = scannerUtil.addBooleanQuestion(
                (defaultValue) -> this.logger.info("&n&lWłączyć Backupy&r (Domyślnie: " + defaultValue + ")? " + this.enter),
                true,
                (input) -> this.logger.info("Backupy ustawione na:&1 " + input));
        this.config.getWatchDogConfig().getBackup().setBackup(backup);
        if (backup) {
            final int backupFrequency = scannerUtil.addIntQuestion((defaultValue) -> this.logger.info("&n&lCo ile minut robić backup?&r (Domyślnie: " + defaultValue + ")? " + this.enter),
                    60,
                    (input) -> this.logger.info("Backup bedzie robiony co:&1 " + (input == 0 ? 60 : input + "&a minut")));
            this.config.getWatchDogConfig().getBackup().setBackupFrequency(backupFrequency == 0 ? 60 : backupFrequency);
        }
        System.out.println();

        scannerUtil.addBooleanQuestion(
                (defaultValue) -> this.logger.info("&n&lRozpocząć częściową konfiguracje servera?&r (Domyślnie: " + defaultValue + ")" + this.enter),
                true,
                (input) -> {
                    if (input) {
                        this.serverSettings(scannerUtil);
                    }
                });

        this.logger.info("Ukończono odpowiedzi w&a " + ((System.currentTimeMillis() - startTime) / 1000.0) + "&r sekund");
        this.config.save();
        this.currentSettings(scannerUtil.getScanner());
    }

    private void serverSettings(final ScannerUtil scannerUtil) {
        this.logger.info("&aKonfiguracja servera&r");
        System.out.println();

        this.serverProperties.setServerPort(scannerUtil.addIntQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lUstaw port v4&r (Aktualny z &bserver.properties&r to: " + defaultValue + ")" + this.enter);
                    this.logger.info("&cPamiętaj że twoja sieć musi miec dostępny ten port");
                }, this.serverProperties.getServerPort(), (input) -> this.logger.info("Port v4 ustawiony na:&1 " + input)
        ));
        System.out.println();

        this.serverProperties.setServerPortV6(scannerUtil.addIntQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lUstaw port v6&r (Aktualny z &bserver.properties&r to: " + defaultValue + ")" + this.enter);
                    this.logger.info("&cJeśli twoja sieć obsługuje&b ipv6&c ustaw go na dostępny z puli portów");
                }, this.serverProperties.getServerPortV6(), (input) -> this.logger.info("Port v6 ustawiony na:&1 " + input)
        ));

        System.out.println();

        this.serverProperties.setMaxThreads(scannerUtil.addIntQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lLiczba wątków używana przez server&r ");
                    this.logger.info("Maksymalna liczba wątków, jakie serwer będzie próbował wykorzystać, Jeśli ustawione na&b 0&r wtedy będzie używać najwięcej jak to możliwe.");
                }, 0,
                (input) -> this.logger.info("Liczba wątków ustawiona na:&1 " + input)
        ));
        System.out.println();
        this.addPlayerPermissionQuestion(scannerUtil);
        System.out.println();

        this.serverProperties.setPlayerIdleTimeout(scannerUtil.addIntQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lUstaw Timeout&r (Aktualny z &bserver.properties&r to: " + defaultValue + ")" + this.enter);
                    this.logger.info("Gdy gracz będzie bezczynny przez tyle minut, zostanie wyrzucony.");
                    this.logger.info("Jeśli ustawione na 0, gracze mogą pozostawać bezczynni przez czas nieokreślony.");
                },
                this.serverProperties.getPlayerIdleTimeout(),
                (input) -> this.logger.info("Timeout ustawiony na:&1 " + input)
        ));
        System.out.println();

        this.serverProperties.setServerBuildRadiusRatio(scannerUtil.addDoubleQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lUstaw Server Build Radius Ratio&r (Domyślnie&r to: " + defaultValue + ")" + this.enter);
                    this.logger.info("Zakres to od&b 0.0 &rdo&b 1.0");
                    this.logger.info("Jeśli&1 Disabled&r, serwer dynamicznie obliczy, ile z widoku gracza zostanie wygenerowane, pozostawiając resztę do zbudowania przez klienta.");
                    this.logger.info("W przeciwnym razie, z nadpisanej proporcji, serwerowi zostanie powiedziane, ile z widoku gracza wygenerować, pomijając zdolności sprzętowe klienta.");
                    this.logger.info("W skrócie:&e Server generuje czanki dla klienta&r dla słabych urządzeń bedzie to pomocne ale obciąży server ");
                    this.logger.alert("Aby wyłączyć i zostawić generowanie czank całkowicie po stronie klienta wpisz&b -1.0&r a my ustawimy to dla ciebie na&b Disabled! .");
                },
                1.0,
                (input) -> {
                    this.logger.info("Server Build Radius Ratio ustawione na:&1 " + input);
                    this.serverProperties.setClientSideChunkGeneration(true);
                }
        ));
        System.out.println();

        this.serverProperties.setViewDistance(scannerUtil.addIntQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lUstaw View Distance&r (Aktualny z &bserver.properties&r to: " + defaultValue + ")" + this.enter);
                    this.logger.info("Maksymalna dozwolona odległość widoku wyrażona w liczbie chunk");
                    this.logger.alert("Jeśli&1 server-build-radius-ratio&r na coś innego niż&b Disabled ");
                    this.logger.alert("Wtedy większe liczby niż 12-32 mogę obciążać server gdy klient ma duży render distance");
                },
                this.serverProperties.getViewDistance(),
                (input) -> this.logger.info("Maksymalny View Distance na:&1 " + input)
        ));
        System.out.println();

        this.serverProperties.setTickDistance(scannerUtil.addIntQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lUstaw Tick Distance&r (Aktualnie z &bserver.properties&r to: " + defaultValue + ")" + this.enter);
                    this.logger.info("Świat zostanie wstrzymany po tylu chunk od gracza");
                    this.logger.alert("Duże ilości mogą prowadzić do lagów servera!");
                },
                this.serverProperties.getTickDistance(),
                (input) -> this.logger.info("Tick Distance na:&1 " + input)
        ));

        System.out.println();
        this.addServerAuthQuestion(scannerUtil);
        System.out.println();

        this.serverProperties.setServerAuthoritativeBlockBreaking(scannerUtil.addBooleanQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lUstaw Server Authoritative Block Breaking&r (Aktualnie z &bserver.properties&r to: " + defaultValue + ")" + this.enter);
                    this.logger.info("Jeśli ustawione na&b true&r, serwer będzie obliczać operacje wydobywania bloków synchronicznie z klientem, aby móc zweryfikować," +
                            " czy klient powinien mieć możliwość niszczenia bloków wtedy, kiedy uważa, że może to zrobić.");
                },
                this.serverProperties.isServerAuthoritativeBlockBreaking(),
                (input) -> this.logger.info("Server Authoritative Block Breaking ustawiono na:&1 " + input)
        ));
        System.out.println();

        if (this.serverProperties.getServerAuthoritativeMovement() == ServerMovementAuth.SERVER_AUTH_REWIND) {
            this.serverProperties.setCorrectPlayerMovement(scannerUtil.addBooleanQuestion(
                    (defaultValue) -> {
                        this.logger.info("&n&lUstaw Correct Player Movement&r (Aktualnie z &bserver.properties&r to: " + defaultValue + ")" + this.enter);
                        this.logger.info("jeśli ustawione na&b true&r, pozycja klienta zostanie poprawiona do pozycji serwera, jeśli wynik ruchu przekroczy próg.");
                    },
                    this.serverProperties.isCorrectPlayerMovement(),
                    (input) -> this.logger.info("Correct Player Movement ustawiono na:&1 " + input)
            ));
            System.out.println();
        }

        this.serverProperties.setAllowCheats(scannerUtil.addBooleanQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lAllow Cheats&r (Aktualny z &bserver.properties&r to: " + defaultValue + ")" + this.enter);
                    this.logger.alert("Aby integracja z discord działała poprawnie wymagamy tego na&b true&r i włączenie&b experymentów&r!");
                },
                this.serverProperties.isAllowCheats(),
                (input) -> this.logger.info("Allow Cheats ustawione na:&1 " + input)
        ));
        System.out.println();

        this.serverProperties.setTexturePackRequired(scannerUtil.addBooleanQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lWymagać tekstur?&r (Aktualny z &bserver.properties&r to: " + defaultValue + ")" + this.enter);
                    this.logger.info("Gdy jest to włączone klient musi pobrać tekstury i nie może używać własnych tekstur");

                },
                this.serverProperties.isTexturePackRequired(),
                (input) -> this.logger.info("Allow Cheats ustawione na:&1 " + input)
        ));
        System.out.println();

        this.serverProperties.setServerTelemetry(scannerUtil.addBooleanQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lUstaw Telemetrie servera&r (Aktualnie z &bserver.properties&r to: " + defaultValue + ")" + this.enter);
                    this.logger.info("Prawdopodobnie&b crash reporty&r będą dzięki temu wysyłane do&4 mojang");
                },
                this.serverProperties.isServerTelemetry(),
                (input) -> this.logger.info("Telemetria servera ustawiona na:&1 " + input)
        ));
        System.out.println();

    }

    private void currentSettings(final Scanner scanner) {
        System.out.println();
        this.logger.info("&n&lAktualne Dane");
        System.out.println();
        this.logger.info("&e----------&bAplikacja&e----------");
        this.logger.info("System:&1 " + Defaults.getSystem());
        this.logger.info("Wine:&1 " + this.config.isWine() + (Defaults.hasWine() ? " &d(&bPosiadasz&d)" : ""));
        this.logger.info("Ścieżka plików:&1 " + this.config.getFilesPath());
        this.logger.info("Wersja:&1 " + this.config.getVersionManagerConfig().getVersion());

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
        System.out.println();

        this.logger.info("Liczba wątków używana przez server:&1 " + this.serverProperties.getMaxThreads());
        this.logger.info("Allow Cheats:&1 " + this.serverProperties.isAllowCheats());
        this.logger.info("Timeout:&1 " + this.serverProperties.getPlayerIdleTimeout() + " &aminut");
        System.out.println();

        final double serverBuildRadiusRatio = this.serverProperties.getServerBuildRadiusRatio();
        this.logger.info("Server Build Radius Ratio:&1 " + (serverBuildRadiusRatio == -1.0 ? "Disabled" : serverBuildRadiusRatio));
        this.logger.info("View Distance:&1 " + this.serverProperties.getViewDistance());
        this.logger.info("Tick Distance:&1 " + this.serverProperties.getTickDistance());
        System.out.println();

        this.logger.info("Default Player Permission Level:&1 " + this.serverProperties.getDefaultPlayerPermissionLevel().getPermissionName());
        this.logger.info("Server Authoritative Movement:&1 " + this.serverProperties.getServerAuthoritativeMovement().getAuthName());
        this.logger.info("Server Authoritative Block Breaking:&1 " + this.serverProperties.isServerAuthoritativeBlockBreaking());
        this.logger.info("Correct Player Movement:&1 " + this.serverProperties.isCorrectPlayerMovement());
        this.logger.info("Wymug tekstur:&1 " + this.serverProperties.isTexturePackRequired());
        this.logger.info("Allow Cheats:&1 " + this.serverProperties.isAllowCheats());
        this.logger.info("Emit server telemetry:&1 " + this.serverProperties.isServerTelemetry());
        System.out.println();

        this.logger.alert("&cWięcej opcji&e konfiguracji&c znajdziesz w config");
        this.logger.info("Kliknij enter aby kontynuować");
        scanner.nextLine();

        if (this.config.isFirstRun()) {
            this.config.setFirstRun(false);
        }
        this.config.save();
    }

    private void versionQuestion(final ScannerUtil scannerUtil) {
        final String latest = this.bdsAutoEnable.getVersionManager().getLatestVersion();
        final String check = (latest.equals("") ? "Nie udało odnaleźć się najnowszej wersji" : latest);
        this.config.getVersionManagerConfig().setVersion(scannerUtil.addStringQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lJaką versie załadować?&r (Najnowsza: " + check + ")");
                    if (this.bdsAutoEnable.getVersionManager().getAvailableVersions().isEmpty()) {
                        this.logger.info("Nie znaleziono żadnej wersji");
                    } else {
                        this.logger.info("Pobrane wersje: " + this.bdsAutoEnable.getVersionManager().getAvailableVersions());
                    }
                    this.logger.info("Aby pobrać jakąś versie wpisz jej numer (niektóre mogą mieć .01 / .02 na końcu)");
                },
                this.config.getVersionManagerConfig().getVersion(), (input) -> this.logger.info("Wersja do załadowania ustawiona na:&1 " + input)
        ));
        this.bdsAutoEnable.getVersionManager().loadVersion();
        System.out.println();
    }

    private void anotherVersionQuestion(final ScannerUtil scannerUtil) {
        scannerUtil.addBooleanQuestion(
                (defaultValue) -> this.logger.info("&n&lZaładować jakąś inną versie?&r (Domyślnie: " + defaultValue + ")" + this.enter),
                false,
                (input) -> {
                    if (input) {
                        this.config.getVersionManagerConfig().setLoaded(false);
                        this.config.save();
                        this.versionQuestion(scannerUtil);
                    }
                });
    }
    
    private void againSetupServer(final ScannerUtil scannerUtil){
        scannerUtil.addBooleanQuestion(
                (defaultValue) -> this.logger.info("&n&lSkonfigurować ponownie server?&r (Domyślnie: " + defaultValue + ")" + this.enter),
                false,
                (input) -> {
                    if (input) {
                        this.serverSettings(scannerUtil);
                    }
                });
    }

    private void addPlayerPermissionQuestion(final ScannerUtil scannerUtil) {
        PlayerPermissionLevel playerPermissionLevel;

        try {
            playerPermissionLevel = PlayerPermissionLevel.valueOf(
                    scannerUtil.addStringQuestion(
                            (defaultValue) -> {
                                this.logger.info("&n&lUstaw Default Player Permission Level&r (Aktualnie z &bserver.properties&r to: " + defaultValue + ")" + this.enter);
                                this.logger.info("Dostępne:&b " + MessageUtil.objectListToString(List.of(PlayerPermissionLevel.values()), "&a, &b"));
                            },
                            this.serverProperties.getDefaultPlayerPermissionLevel().name(),
                            (input) -> this.logger.info("Default Player Permission Level ustawiono na:&1 " + input)
                    ).toUpperCase());
        } catch (final IllegalArgumentException exception) {
            this.logger.error("Podano nieznany poziom uprawnień gracza, ustawiliśmy go dla ciebie na:&1 " + PlayerPermissionLevel.MEMBER.getPermissionName());
            playerPermissionLevel = PlayerPermissionLevel.MEMBER;
        }
        this.serverProperties.setDefaultPlayerPermissionLevel(playerPermissionLevel);
    }

    private void addServerAuthQuestion(final ScannerUtil scannerUtil) {
        ServerMovementAuth serverMovementAuth;

        try {
            serverMovementAuth = ServerMovementAuth.valueOf(
                    scannerUtil.addStringQuestion(
                            (defaultValue) -> {
                                this.logger.info("&n&lUstaw Server Authoritative Movement&r (Aktualnie z &bserver.properties&r to: " + defaultValue + ")" + this.enter);
                                this.logger.info("Dostępne:&b " + MessageUtil.objectListToString(List.of(ServerMovementAuth.values()), "&a, &b"));
                                this.logger.alert("Jeśli chcesz uniknąć cheaterów włącz&b server-auth-with-rewind&r wraz z &bcorrect-player-movement&r ," +
                                        " lecz uważaj! Osoby z słabym połączeniem bedą się okropnie teleportować");
                            },
                            this.serverProperties.getServerAuthoritativeMovement().name(),
                            (input) -> this.logger.info("Server Authoritative Movement ustawiono na:&1 " + input)
                    ).toUpperCase());
        } catch (final Exception exception) {
            exception.printStackTrace();
            this.logger.error("Podano nieznany typ uwierzytelnienia poruszania sie , ustawiliśmy go dla ciebie na:&b " + ServerMovementAuth.SERVER_AUTH.getAuthName());
            serverMovementAuth = ServerMovementAuth.SERVER_AUTH;
        }
        this.serverProperties.setServerAuthoritativeMovement(serverMovementAuth);
    }
}