package pl.indianbartonka.bds;

import java.util.List;
import java.util.Scanner;
import pl.indianbartonka.bds.config.AppConfig;
import pl.indianbartonka.bds.config.sub.version.VersionManagerConfig;
import pl.indianbartonka.bds.config.sub.watchdog.WatchDogConfig;
import pl.indianbartonka.bds.server.properties.ServerProperties;
import pl.indianbartonka.bds.server.properties.component.CompressionAlgorithm;
import pl.indianbartonka.bds.server.properties.component.PlayerPermissionLevel;
import pl.indianbartonka.bds.server.properties.component.ServerMovementAuth;
import pl.indianbartonka.bds.util.DefaultsVariables;
import pl.indianbartonka.bds.version.VersionManager;
import pl.indianbartonka.util.MessageUtil;
import pl.indianbartonka.util.ScannerUtil;
import pl.indianbartonka.util.ThreadUtil;
import pl.indianbartonka.util.logger.Logger;

public class Settings {

    private final BDSAutoEnable bdsAutoEnable;
    private final ServerProperties serverProperties;
    private final Logger logger;
    private final AppConfig appConfig;
    private final WatchDogConfig watchDogConfig;
    private final VersionManagerConfig versionManagerConfig;
    private final String enter;

    public Settings(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.serverProperties = this.bdsAutoEnable.getServerProperties();
        this.logger = this.bdsAutoEnable.getLogger();
        this.appConfig = this.bdsAutoEnable.getAppConfigManager().getAppConfig();
        this.watchDogConfig = this.bdsAutoEnable.getAppConfigManager().getWatchDogConfig();
        this.versionManagerConfig = this.bdsAutoEnable.getAppConfigManager().getVersionManagerConfig();
        this.enter = " [Enter = Domyślnie]";
    }

    public void loadSettings() {
        if (!this.appConfig.isFirstRun()) {
            if (!this.appConfig.isQuestions()) {
                this.serverProperties.loadProperties();
                this.logger.info("Ominięto pytania");
                return;
            }
            ScannerUtil.addBooleanQuestion((defaultValue) -> this.logger.info("&n&lZastosować wcześniejsze ustawienia?&r (Domyślnie: true) " + this.enter),
                    true,
                    (settings) -> {
                        if (settings) {
                            this.serverProperties.loadProperties();
                            if (this.versionManagerConfig.isLoaded()) {
                                this.anotherVersionQuestion();
                            }
                            this.againSetupServer();
                            this.questionsSetting();
                            this.currentSettings(true);
                        } else {
                            this.logger.info("Zaczynamy od nowa");
                            this.init();
                        }
                    });
        } else {
            this.init();
        }
    }

    private void init() {
        final long startTime = System.currentTimeMillis();
        final String appDir = DefaultsVariables.getJarDir();

        this.logger.println();

        if (DefaultsVariables.wine) {
            this.appConfig.setWine(ScannerUtil.addBooleanQuestion(
                    (defaultValue) -> {
                        this.logger.info("&n&lWykryliśmy &r&bWINE&r&n&l czy użyć go?&r (Domyślnie: " + defaultValue + ")" + this.enter);
                        this.logger.alert("Jeśli chcesz użyć&b WINE&r plik musi kończyć się na&1 .exe");
                    },
                    false,
                    (input) -> this.logger.info("&bWINE&r ustawione na:&1 " + input)
            ));
            this.logger.println();
        }

        if (DefaultsVariables.box64) {
            //TODO: Obczajc czy te pytanie ma wgl sens
            this.appConfig.setBox64(ScannerUtil.addBooleanQuestion(
                    (defaultValue) -> {
                        this.logger.info("&n&lWykryliśmy &r&bBox64&r&n&l czy użyć go?&r (Domyślnie: " + defaultValue + ")" + this.enter);
                        this.logger.alert("Jeśli chcesz użyć&b Box64");
                    },
                    false,
                    (input) -> this.logger.info("&bBox64&r ustawione na:&1 " + input)
            ));
            this.logger.println();
        }

        this.appConfig.setFilesPath(ScannerUtil.addStringQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lPodaj ścieżkę do plików servera&r (Domyślnie: " + defaultValue + ")" + this.enter);
                    this.logger.info("&aAPP_DIR&e =&b " + appDir);
                },
                appDir,
                (input) -> this.logger.info("Ścieżke do plików servera ustawiona na:&1 " + input)
        ).replaceAll("APP_DIR", appDir));

        this.appConfig.save();
        this.logger.println();

        if (!this.versionManagerConfig.isLoaded()) this.versionQuestion();
        this.serverProperties.loadProperties();

        final boolean backup = ScannerUtil.addBooleanQuestion(
                (defaultValue) -> this.logger.info("&n&lWłączyć Backupy&r (Domyślnie: " + defaultValue + ")?" + this.enter),
                true,
                (input) -> this.logger.info("Backupy ustawione na:&1 " + input));
        this.watchDogConfig.getBackupConfig().setEnabled(backup);
        if (backup) {
            final int backupFrequency = ScannerUtil.addIntQuestion((defaultValue) -> this.logger.info("&n&lCo ile minut robić backup?&r (Domyślnie: " + defaultValue + ")? " + this.enter),
                    60,
                    (input) -> this.logger.info("Backup bedzie robiony co:&1 " + (input == 0 ? 60 : input + "&a minut")));
            this.watchDogConfig.getBackupConfig().setBackupFrequency(backupFrequency == 0 ? 60 : backupFrequency);
        }
        this.logger.println();

        final boolean autoRestart = ScannerUtil.addBooleanQuestion(
                (defaultValue) -> this.logger.info("&n&lWłączyć AutoRestart Servera?&r (Domyślnie: " + defaultValue + ")? " + this.enter),
                true,
                (input) -> this.logger.info("AutoRestart Servera ustawione na:&1 " + input));
        this.logger.println();

        this.watchDogConfig.getAutoRestartConfig().setEnabled(autoRestart);

        if (autoRestart) {
            this.watchDogConfig.getAutoRestartConfig().setRestartTime(ScannerUtil.addIntQuestion(
                    (defaultValue) -> this.logger.info("&n&lCo ile godzin restartować server?&r (Polecane: " + defaultValue + ")? " + this.enter),
                    4,
                    (input) -> this.logger.info("Server będzie restartowany co&1 " + input + "&a godziny")));
            this.logger.println();
        }

        ScannerUtil.addBooleanQuestion(
                (defaultValue) -> this.logger.info("&n&lRozpocząć częściową konfiguracje servera?&r (Domyślnie: " + defaultValue + ")" + this.enter),
                true,
                (input) -> {
                    if (input) {
                        this.serverSettings();
                    }
                });

        this.questionsSetting();
        this.logger.info("Ukończono odpowiedzi w&a " + ((System.currentTimeMillis() - startTime) / 1000.0) + "&r sekund");
        this.appConfig.save();
        this.currentSettings(true);
    }

    private void serverSettings() {
        this.logger.info("&aKonfiguracja servera&r");
        this.logger.println();

        this.serverProperties.setServerPort(ScannerUtil.addIntQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lUstaw port v4&r (Aktualny z &bserver.properties&r to: " + defaultValue + ")" + this.enter);
                    this.logger.info("&cPamiętaj że twoja sieć musi miec dostępny ten port");
                }, this.serverProperties.getServerPort(), (input) -> this.logger.info("Port v4 ustawiony na:&1 " + input)
        ));
        this.logger.println();

        this.serverProperties.setServerPortV6(ScannerUtil.addIntQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lUstaw port v6&r (Aktualny z &bserver.properties&r to: " + defaultValue + ")" + this.enter);
                    this.logger.info("&cJeśli twoja sieć obsługuje&b ipv6&c ustaw go na dostępny z puli portów");
                }, this.serverProperties.getServerPortV6(), (input) -> this.logger.info("Port v6 ustawiony na:&1 " + input)
        ));
        this.logger.println();

        this.addCompressionAlgorithmQuestion();
        this.logger.println();

        this.serverProperties.setCompressionThreshold(ScannerUtil.addIntQuestion((defaultValue) -> {
                    this.logger.info("&n&lUstaw Próg kompresji&r (Aktualny z &bserver.properties&r to: " + defaultValue + ")" + this.enter);
                    this.logger.alert("Mniejszy próg kompresji = większe użycje internetu ale zmniejsza użycje procesora");
                    this.logger.alert("Większy próg kompresji = mniejsze użycje internetu ale zwiększa użycje procesora");
                    this.logger.alert("Ustawienie tego na&1 0&r jest nie zalecane!");

                }, this.serverProperties.getCompressionThreshold(),
                (input) -> this.logger.info("Próg kompresji ustawiono na:&1 " + input)
        ));
        this.logger.println();

        this.serverProperties.setMaxThreads(ScannerUtil.addIntQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lLiczba wątków używana przez server&r (Dostępna liczba to: " + defaultValue + ")" + this.enter);
                    this.logger.info("Maksymalna liczba wątków, jakie serwer będzie próbował wykorzystać, Jeśli ustawione na&b 0&r wtedy będzie używać najwięcej jak to możliwe.");
                    this.logger.alert("&cJeśli stawiasz tylko jeden server polecamy użyć wszystkich&b " + defaultValue + "&c wątków");
                }, ThreadUtil.getLogicalThreads(),
                (input) -> this.logger.info("Liczba wątków ustawiona na:&1 " + input)
        ));
        this.logger.println();
        this.addPlayerPermissionQuestion();
        this.logger.println();

        this.serverProperties.setPlayerIdleTimeout(ScannerUtil.addIntQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lUstaw Timeout&r (Aktualny z &bserver.properties&r to: " + defaultValue + ")" + this.enter);
                    this.logger.info("Gdy gracz będzie bezczynny przez tyle minut, zostanie wyrzucony.");
                    this.logger.info("Jeśli ustawione na 0, gracze mogą pozostawać bezczynni przez czas nieokreślony.");
                },
                this.serverProperties.getPlayerIdleTimeout(),
                (input) -> this.logger.info("Timeout ustawiony na:&1 " + input)
        ));
        this.logger.println();

        if (this.serverProperties.isClientSideChunkGeneration()) {
            this.serverProperties.setServerBuildRadiusRatio(ScannerUtil.addDoubleQuestion(
                    (defaultValue) -> {
                        this.logger.info("&n&lUstaw Server Build Radius Ratio&r (Domyślnie&r to: " + defaultValue + ")" + this.enter);
                        this.logger.info("Zakres to od&b 0.0 &rdo&b 1.0");
                        this.logger.info("Jeśli&1 Disabled&r, serwer dynamicznie obliczy, ile z widoku gracza zostanie wygenerowane, pozostawiając resztę do zbudowania przez klienta.");
                        this.logger.info("W przeciwnym razie, z nadpisanej proporcji, serwerowi zostanie powiedziane, ile z widoku gracza wygenerować, pomijając zdolności sprzętowe klienta.");
                        this.logger.info("W skrócie:&e Server generuje czanki dla klienta&r dla słabych urządzeń bedzie to pomocne ale obciąży server ");
                        this.logger.alert("Aby wyłączyć i zostawić generowanie czank całkowicie po stronie klienta wpisz&b -1.0&r a my ustawimy to dla ciebie na&b Disabled!");
                    },
                    1.0,
                    (input) -> {
                        this.logger.info("Server Build Radius Ratio ustawione na:&1 " + input);
                        this.serverProperties.setClientSideChunkGeneration(true);
                    }
            ));
            this.logger.println();
        }

        this.serverProperties.setViewDistance(ScannerUtil.addIntQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lUstaw View Distance&r (Aktualny z &bserver.properties&r to: " + defaultValue + ")" + this.enter);
                    this.logger.info("Maksymalna dozwolona odległość widoku wyrażona w liczbie chunk");
                    this.logger.alert("Jeśli&1 server-build-radius-ratio&r na coś innego niż&b Disabled ");
                    this.logger.alert("Wtedy większe liczby niż 12-32 mogę obciążać server gdy klient ma duży render distance");
                },
                this.serverProperties.getViewDistance(),
                (input) -> this.logger.info("Maksymalny View Distance na:&1 " + input)
        ));
        this.logger.println();

        this.serverProperties.setTickDistance(ScannerUtil.addIntQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lUstaw Tick Distance&r (Aktualnie z &bserver.properties&r to: " + defaultValue + ")" + this.enter);
                    this.logger.info("Świat zostanie wstrzymany po tylu chunk od gracza");
                    this.logger.alert("Duże ilości mogą prowadzić do lagów servera!");
                },
                this.serverProperties.getTickDistance(),
                (input) -> this.logger.info("Tick Distance na:&1 " + input)
        ));

        this.logger.println();
        this.addServerAuthQuestion();
        this.logger.println();

        this.serverProperties.setServerAuthoritativeBlockBreaking(ScannerUtil.addBooleanQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lUstaw Server Authoritative Block Breaking&r (Polecamy ustawić na: " + defaultValue + ")" + this.enter);
                    this.logger.info("Jeśli ustawione na&b true&r, serwer będzie obliczać operacje wydobywania bloków synchronicznie z klientem, aby móc zweryfikować," +
                            " czy klient powinien mieć możliwość niszczenia bloków wtedy, kiedy uważa, że może to zrobić.");
                    this.logger.info("Działa jak anty nuker");
                }, true,
                (input) -> this.logger.info("Server Authoritative Block Breaking ustawiono na:&1 " + input)
        ));
        this.logger.println();

        this.serverProperties.setAllowCheats(ScannerUtil.addBooleanQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lAllow Cheats&r (Aktualny z &bserver.properties&r to: " + defaultValue + ")" + this.enter);
                    this.logger.alert("Aby rozszerzenia działały poprawnie wymagamy tego na&b true&r i włączenie&b experymentów&r!");
                },
                this.serverProperties.isAllowCheats(),
                (input) -> this.logger.info("Allow Cheats ustawione na:&1 " + input)
        ));
        this.logger.println();

        this.serverProperties.setTexturePackRequired(ScannerUtil.addBooleanQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lWymagać tekstur?&r (Aktualny z &bserver.properties&r to: " + defaultValue + ")" + this.enter);
                    this.logger.info("Gdy jest to włączone klient musi pobrać tekstury i nie może używać własnych tekstur");

                },
                this.serverProperties.isTexturePackRequired(),
                (input) -> this.logger.info("Allow Cheats ustawione na:&1 " + input)
        ));
        this.logger.println();

        this.serverProperties.setServerTelemetry(ScannerUtil.addBooleanQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lUstaw Telemetrie servera&r (Aktualnie z &bserver.properties&r to: " + defaultValue + ")" + this.enter);
                    this.logger.info("Prawdopodobnie&b crash reporty&r będą dzięki temu wysyłane do&4 mojang");
                },
                this.serverProperties.isServerTelemetry(),
                (input) -> this.logger.info("Telemetria servera ustawiona na:&1 " + input)
        ));
        this.logger.println();

    }

    private void questionsSetting() {
        this.appConfig.setQuestions(ScannerUtil.addBooleanQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lCzy powtórzyć następnym razem pytania?&r (Domyślnie: " + defaultValue + ")" + this.enter);
                    this.logger.info("&aJeśli ustawisz na&b false&a następnym razem aplikacja uruchomi się bez zadawania żadnych pytań!");
                    this.logger.info("&aMożesz potem zmienić to w:&1 " + this.appConfig.getBindFile().toString().replaceAll("config.yml", "&econfig.yml"));
                },
                true,
                (input) -> {
                }));
    }

    public void currentSettings(final boolean waitForUser) {
        final VersionManager versionManager = this.bdsAutoEnable.getVersionManager();

        this.serverProperties.loadProperties();

        this.logger.println();
        this.logger.info("&n&lAktualne Dane");
        this.logger.println();
        this.logger.info("&e----------&bAplikacja&e----------");
        this.logger.info("Java:&1 " + System.getProperty("java.version"));
        this.logger.info("Wine:&1 " + this.appConfig.isWine() + (DefaultsVariables.wine ? " &d(&bPosiadasz&d)" : ""));
        this.logger.info("Box64:&1 " + this.appConfig.isBox64() + (DefaultsVariables.box64 ? " &d(&bPosiadasz&d)" : ""));
        this.logger.info("Ścieżka plików:&1 " + this.appConfig.getFilesPath());

        final boolean backup = this.watchDogConfig.getBackupConfig().isEnabled();
        this.logger.info("Backup:&1 " + backup);

        if (backup) {
            this.logger.info("Częstotliwość robienia backup:&1 " + this.watchDogConfig.getBackupConfig().getBackupFrequency() + "&a minut");
        }

        this.logger.info("Nazwa świata:&1 " + this.serverProperties.getWorldName());
        this.logger.println();
        this.logger.info("&e-----------&bServer&e----------");
        this.logger.info("Wersja:&1 " + versionManager.getLoadedVersion() + " &d(&b" + versionManager.getLastKnownProtocol() + "&d)");
        this.logger.info("Port v4:&1 " + this.serverProperties.getServerPort());
        this.logger.info("Port v6:&1 " + this.serverProperties.getServerPortV6());
        this.logger.info("Algorytm kompresji:&1 " + this.serverProperties.getCompressionAlgorithm());
        this.logger.info("Próg kompresji:&1 " + this.serverProperties.getCompressionThreshold());
        this.logger.println();


        final int threadsCount = this.serverProperties.getMaxThreads();
        final int logicalThreadsCount = ThreadUtil.getLogicalThreads();
        String threadsNote = "";

        if (threadsCount != 0 && threadsCount != logicalThreadsCount) {
            threadsNote = "&d (&bDostępne jest:&1 " + logicalThreadsCount + "&d)";
        } else if (threadsCount == 0) {
            threadsNote = "&d (&bPosiadasz:&1 " + logicalThreadsCount + "&d)";
        }

        this.logger.info("Liczba wątków używana przez server:&1 " + this.serverProperties.getMaxThreads() + threadsNote);
        this.logger.info("Allow Cheats:&1 " + this.serverProperties.isAllowCheats());
        this.logger.info("Timeout:&1 " + this.serverProperties.getPlayerIdleTimeout() + " &aminut");
        this.logger.println();

        if (this.serverProperties.isClientSideChunkGeneration()) {
            final double serverBuildRadiusRatio = this.serverProperties.getServerBuildRadiusRatio();
            this.logger.info("Server Build Radius Ratio:&1 " + (serverBuildRadiusRatio == -1.0 ? "Disabled" : serverBuildRadiusRatio));
        }

        this.logger.info("View Distance:&1 " + this.serverProperties.getViewDistance());
        this.logger.info("Tick Distance:&1 " + this.serverProperties.getTickDistance());
        this.logger.println();

        this.logger.info("Default Player Permission Level:&1 " + this.serverProperties.getPlayerPermissionLevel().getPermissionName());
        this.logger.info("Server Authoritative Movement:&1 " + this.serverProperties.getServerMovementAuth().getAuthName());

        if (this.serverProperties.getServerMovementAuth() != ServerMovementAuth.CLIENT_AUTH) {
            this.logger.info("Server Authoritative Block Breaking:&1 " + this.serverProperties.isServerAuthoritativeBlockBreaking());
        }

        this.logger.info("Wymug tekstur:&1 " + this.serverProperties.isTexturePackRequired());
        this.logger.info("Allow Cheats:&1 " + this.serverProperties.isAllowCheats());
        this.logger.info("Emitowanie telemetrii:&1 " + this.serverProperties.isServerTelemetry());
        this.logger.println();

        if (waitForUser) {
            this.logger.alert("&cWięcej opcji&e konfiguracji&c znajdziesz w config");
            this.logger.info("Kliknij enter aby kontynuować");
            try (final Scanner scanner = new Scanner(System.in)) {
                scanner.nextLine();
            }
        }

        if (this.appConfig.isFirstRun()) {
            this.appConfig.setFirstRun(false);
        }
        this.appConfig.save();
    }

    private void versionQuestion() {
        final String latest = this.bdsAutoEnable.getVersionManager().getLatestVersion();
        final String check = (latest.isEmpty() ? "Nie udało odnaleźć się najnowszej wersji" : latest);

        this.versionManagerConfig.setVersion(ScannerUtil.addStringQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lJaką wersje załadować?&r (Najnowsza: " + check + ")");
                    if (this.bdsAutoEnable.getVersionManager().getAvailableVersions().isEmpty()) {
                        this.logger.info("Nie znaleziono żadnej wersji");
                    } else {
                        this.logger.info("Pobrane wersje: " + this.bdsAutoEnable.getVersionManager().getAvailableVersions());
                    }
                    this.logger.info("Aby pobrać jakąś wersje wpisz jej numer (niektóre mogą mieć .01 / .02 na końcu)");
                },
                (latest.isEmpty() ? this.bdsAutoEnable.getVersionManager().getLoadedVersion() : latest),
                (input) -> this.logger.info("Wersja do załadowania ustawiona na:&1 " + input)
        ));
        this.bdsAutoEnable.getVersionManager().loadVersion();
        this.logger.println();
    }

    private void anotherVersionQuestion() {
        ScannerUtil.addBooleanQuestion(
                (defaultValue) -> this.logger.info("&n&lZaładować jakąś inną wersje?&r (Domyślnie: " + defaultValue + ")" + this.enter),
                false,
                (input) -> {
                    if (input) {
                        this.bdsAutoEnable.getVersionManager().setLoaded(false);
                        this.versionQuestion();
                    }
                });
    }

    private void againSetupServer() {
        ScannerUtil.addBooleanQuestion(
                (defaultValue) -> this.logger.info("&n&lSkonfigurować ponownie server?&r (Domyślnie: " + defaultValue + ")" + this.enter),
                false,
                (input) -> {
                    if (input) {
                        this.serverSettings();
                    }
                });
    }

    private void addCompressionAlgorithmQuestion() {
        CompressionAlgorithm compressionAlgorithm;
        try {
            compressionAlgorithm = CompressionAlgorithm.valueOf(
                    ScannerUtil.addStringQuestion(
                            (defaultValue) -> {
                                this.logger.info("&n&lUstaw Algorytm kompresji&r (Aktualnie z &bserver.properties&r to: " + defaultValue + ")" + this.enter);
                                this.logger.info("&b" + CompressionAlgorithm.SNAPPY + "&a Szybki algorytm kompresji z niską latencją, idealny do zastosowań czasu rzeczywistego, choć pliki mogą być nieco większe. &d(&bMoże się bagować&d)");
                                this.logger.info("&b" + CompressionAlgorithm.ZLIB + "&a Potężny algorytm, osiągający wysoki stosunek kompresji, nadający się do sytuacji wymagających znaczącej redukcji rozmiaru plików.");
                            },
                            String.valueOf(this.serverProperties.getCompressionAlgorithm()),
                            (input) -> this.logger.info("Algorytm kompresji ustawiono na:&1 " + input)
                    ).toUpperCase());
        } catch (final IllegalArgumentException exception) {
            this.logger.error("Podano nieznany algorytm kompresji, ustawiliśmy go dla ciebie na:&1 " + CompressionAlgorithm.ZLIB, exception);
            compressionAlgorithm = CompressionAlgorithm.ZLIB;
        }
        this.serverProperties.setCompressionAlgorithm(compressionAlgorithm);
    }

    private void addPlayerPermissionQuestion() {
        PlayerPermissionLevel playerPermissionLevel;

        try {
            playerPermissionLevel = PlayerPermissionLevel.valueOf(
                    ScannerUtil.addStringQuestion(
                            (defaultValue) -> {
                                this.logger.info("&n&lUstaw Default Player Permission Level&r (Aktualnie z &bserver.properties&r to: " + defaultValue + ")" + this.enter);
                                this.logger.info("Dostępne:&b " + MessageUtil.objectListToString(List.of(PlayerPermissionLevel.values()), "&a, &b"));
                            },
                            this.serverProperties.getPlayerPermissionLevel().name(),
                            (input) -> this.logger.info("Default Player Permission Level ustawiono na:&1 " + input)
                    ).toUpperCase());
        } catch (final IllegalArgumentException exception) {
            this.logger.error("Podano nieznany poziom uprawnień gracza, ustawiliśmy go dla ciebie na:&1 " + PlayerPermissionLevel.MEMBER.getPermissionName(), exception);
            playerPermissionLevel = PlayerPermissionLevel.MEMBER;
        }
        this.serverProperties.setPlayerPermissionLevel(playerPermissionLevel);
    }

    private void addServerAuthQuestion() {
        ServerMovementAuth serverMovementAuth;

        try {
            serverMovementAuth = ServerMovementAuth.valueOf(
                    ScannerUtil.addStringQuestion(
                            (defaultValue) -> {
                                this.logger.info("&n&lUstaw Server Authoritative Movement&r (Aktualnie z &bserver.properties&r to: " + defaultValue + ")" + this.enter);
                                this.logger.info("Dostępne:&b " + MessageUtil.objectListToString(List.of(ServerMovementAuth.values()), "&a, &b"));
                                this.logger.alert("Jeśli chcesz uniknąć cheaterów włącz&b server-auth-with-rewind&r wraz z &bcorrect-player-movement&r ," +
                                        " lecz uważaj! Osoby z słabym połączeniem bedą się okropnie teleportować");
                            },
                            this.serverProperties.getServerMovementAuth().name(),
                            (input) -> this.logger.info("Server Authoritative Movement ustawiono na:&1 " + input)
                    ).toUpperCase());
        } catch (final Exception exception) {
            this.logger.error("Podano nieznany typ uwierzytelnienia poruszania sie , ustawiliśmy go dla ciebie na:&b " + ServerMovementAuth.SERVER_AUTH.getAuthName(), exception);
            serverMovementAuth = ServerMovementAuth.SERVER_AUTH;
        }
        this.serverProperties.setServerMovementAuth(serverMovementAuth);
    }
}
