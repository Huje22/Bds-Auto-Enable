package me.indian.bds;

import java.util.List;
import java.util.Scanner;
import me.indian.bds.config.AppConfig;
import me.indian.bds.config.sub.version.VersionManagerConfig;
import me.indian.bds.config.sub.watchdog.WatchDogConfig;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.properties.ServerProperties;
import me.indian.bds.server.properties.component.CompressionAlgorithm;
import me.indian.bds.server.properties.component.PlayerPermissionLevel;
import me.indian.bds.server.properties.component.ServerMovementAuth;
import me.indian.bds.util.DefaultsVariables;
import me.indian.bds.util.MessageUtil;
import me.indian.bds.util.ScannerUtil;
import me.indian.bds.util.system.SystemUtil;

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

    public void loadSettings(final Scanner scanner) {
        final ScannerUtil scannerUtil = new ScannerUtil(scanner);
        if (!this.appConfig.isFirstRun()) {
            if (!this.appConfig.isQuestions()) {
                this.serverProperties.loadProperties();
                this.logger.info("Ominięto pytania");
                return;
            }
            scannerUtil.addBooleanQuestion((defaultValue) -> this.logger.info("&n&lZastosować wcześniejsze ustawienia?&r (true/false) " + this.enter),
                    true,
                    (settings) -> {
                        if (settings) {
                            this.serverProperties.loadProperties();
                            if (this.versionManagerConfig.isLoaded()) {
                                this.anotherVersionQuestion(scannerUtil);
                            }
                            this.againSetupServer(scannerUtil);
                            this.questionsSetting(scannerUtil);
                            this.currentSettings(scanner, true);
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
        final String appDir = DefaultsVariables.getJarDir();

        this.logger.print();

        if (DefaultsVariables.WINE) {
            this.appConfig.setWine(scannerUtil.addBooleanQuestion(
                    (defaultValue) -> {
                        this.logger.info("&n&lWykryliśmy &r&bWINE&r&n&l czy użyć go?&r (Domyślnie: " + defaultValue + ")" + this.enter);
                        this.logger.alert("Jeśli chcesz użyć&b WINE&r plik musi kończyć się na&1 .exe");
                    },
                    false,
                    (input) -> this.logger.info("&bWINE&r ustawione na:&1 " + input)
            ));
            this.logger.print();
        }

        this.appConfig.setFilesPath(scannerUtil.addStringQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lPodaj ścieżkę do plików servera&r (Domyślnie: " + defaultValue + ")" + this.enter);
                    this.logger.info("&a./&e =&b " + appDir);
                },
                appDir,
                (input) -> this.logger.info("Ścieżke do plików servera ustawiona na:&1 " + input)
        ).replaceAll("./", appDir));

        this.appConfig.save();
        this.logger.print();

        this.appConfig.setLogFile(
                scannerUtil.addBooleanQuestion(
                        (defaultValue) -> {
                            this.logger.info("&n&lCzy tworzyć pliki z logami servera?&r (Domyślnie: " + defaultValue + ")" + this.enter);
                            this.logger.info("Zostanie w pełni zastosowane dopiero przy następnym uruchomieniu aplikacji");
                        },
                        true,
                        (input) -> this.logger.info("Tworzenie logów servera ustawione na:&1 " + input)
                )
        );
        this.logger.print();

        if (!this.versionManagerConfig.isLoaded()) this.versionQuestion(scannerUtil);
        this.serverProperties.loadProperties();

        final boolean backup = scannerUtil.addBooleanQuestion(
                (defaultValue) -> this.logger.info("&n&lWłączyć Backupy&r (Domyślnie: " + defaultValue + ")?" + this.enter),
                true,
                (input) -> this.logger.info("Backupy ustawione na:&1 " + input));
        this.watchDogConfig.getBackupConfig().setEnabled(backup);
        if (backup) {
            final int backupFrequency = scannerUtil.addIntQuestion((defaultValue) -> this.logger.info("&n&lCo ile minut robić backup?&r (Domyślnie: " + defaultValue + ")? " + this.enter),
                    60,
                    (input) -> this.logger.info("Backup bedzie robiony co:&1 " + (input == 0 ? 60 : input + "&a minut")));
            this.watchDogConfig.getBackupConfig().setBackupFrequency(backupFrequency == 0 ? 60 : backupFrequency);
        }
        this.logger.print();

        final boolean autoRestart = scannerUtil.addBooleanQuestion(
                (defaultValue) -> this.logger.info("&n&lWłączyć AutoRestart Servera?&r (Domyślnie: " + defaultValue + ")? " + this.enter),
                true,
                (input) -> this.logger.info("AutoRestart Servera ustawione na:&1 " + input));
        this.logger.print();

        this.watchDogConfig.getAutoRestartConfig().setEnabled(autoRestart);

        if (autoRestart) {
            this.watchDogConfig.getAutoRestartConfig().setRestartTime(scannerUtil.addIntQuestion(
                    (defaultValue) -> this.logger.info("&n&lCo ile godzin restartować server?&r (Polecane: " + defaultValue + ")? " + this.enter),
                    4,
                    (input) -> this.logger.info("Server będzie restartowany co&1 " + input + "&a godziny")));
            this.logger.print();
        }

        scannerUtil.addBooleanQuestion(
                (defaultValue) -> this.logger.info("&n&lRozpocząć częściową konfiguracje servera?&r (Domyślnie: " + defaultValue + ")" + this.enter),
                true,
                (input) -> {
                    if (input) {
                        this.serverSettings(scannerUtil);
                    }
                });

        this.questionsSetting(scannerUtil);
        this.logger.info("Ukończono odpowiedzi w&a " + ((System.currentTimeMillis() - startTime) / 1000.0) + "&r sekund");
        this.appConfig.save();
        this.currentSettings(scannerUtil.getScanner(), true);
    }

    private void serverSettings(final ScannerUtil scannerUtil) {
        this.logger.info("&aKonfiguracja servera&r");
        this.logger.print();

        this.serverProperties.setServerPort(scannerUtil.addIntQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lUstaw port v4&r (Aktualny z &bserver.properties&r to: " + defaultValue + ")" + this.enter);
                    this.logger.info("&cPamiętaj że twoja sieć musi miec dostępny ten port");
                }, this.serverProperties.getServerPort(), (input) -> this.logger.info("Port v4 ustawiony na:&1 " + input)
        ));
        this.logger.print();

        this.serverProperties.setServerPortV6(scannerUtil.addIntQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lUstaw port v6&r (Aktualny z &bserver.properties&r to: " + defaultValue + ")" + this.enter);
                    this.logger.info("&cJeśli twoja sieć obsługuje&b ipv6&c ustaw go na dostępny z puli portów");
                }, this.serverProperties.getServerPortV6(), (input) -> this.logger.info("Port v6 ustawiony na:&1 " + input)
        ));
        this.logger.print();

        this.addCompressionAlgorithmQuestion(scannerUtil);
        this.logger.print();

        this.serverProperties.setMaxThreads(scannerUtil.addIntQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lLiczba wątków używana przez server&r ");
                    this.logger.info("Maksymalna liczba wątków, jakie serwer będzie próbował wykorzystać, Jeśli ustawione na&b 0&r wtedy będzie używać najwięcej jak to możliwe.");
                    this.logger.alert("Z doświadczenia nie polecam ustawiać na więcej niż 8, i także nie zbyt mało");
                }, 8,
                (input) -> this.logger.info("Liczba wątków ustawiona na:&1 " + input)
        ));
        this.logger.print();
        this.addPlayerPermissionQuestion(scannerUtil);
        this.logger.print();

        this.serverProperties.setPlayerIdleTimeout(scannerUtil.addIntQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lUstaw Timeout&r (Aktualny z &bserver.properties&r to: " + defaultValue + ")" + this.enter);
                    this.logger.info("Gdy gracz będzie bezczynny przez tyle minut, zostanie wyrzucony.");
                    this.logger.info("Jeśli ustawione na 0, gracze mogą pozostawać bezczynni przez czas nieokreślony.");
                },
                this.serverProperties.getPlayerIdleTimeout(),
                (input) -> this.logger.info("Timeout ustawiony na:&1 " + input)
        ));
        this.logger.print();

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
        this.logger.print();

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
        this.logger.print();

        this.serverProperties.setTickDistance(scannerUtil.addIntQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lUstaw Tick Distance&r (Aktualnie z &bserver.properties&r to: " + defaultValue + ")" + this.enter);
                    this.logger.info("Świat zostanie wstrzymany po tylu chunk od gracza");
                    this.logger.alert("Duże ilości mogą prowadzić do lagów servera!");
                },
                this.serverProperties.getTickDistance(),
                (input) -> this.logger.info("Tick Distance na:&1 " + input)
        ));

        this.logger.print();
        this.addServerAuthQuestion(scannerUtil);
        this.logger.print();

        if (this.serverProperties.getServerMovementAuth() != ServerMovementAuth.CLIENT_AUTH) {
            this.serverProperties.setCorrectPlayerMovement(scannerUtil.addBooleanQuestion(
                    (defaultValue) -> {
                        this.logger.info("&n&lUstaw Correct Player Movement&r (Polecamy ustawić na: " + defaultValue + ")" + this.enter);
                        this.logger.info("Jeśli ustawione na&b true&r, pozycja klienta zostanie poprawiona do pozycji serwera, jeśli wynik ruchu przekroczy próg.");
                        this.logger.info("Najlepiej działa z &b " + ServerMovementAuth.SERVER_AUTH_REWIND.getAuthName());
                    },
                    true,
                    (input) -> this.logger.info("Correct Player Movement ustawiono na:&1 " + input)
            ));
            this.logger.print();
        }

        this.serverProperties.setServerAuthoritativeBlockBreaking(scannerUtil.addBooleanQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lUstaw Server Authoritative Block Breaking&r (Polecamy ustawić na: " + defaultValue + ")" + this.enter);
                    this.logger.info("Jeśli ustawione na&b true&r, serwer będzie obliczać operacje wydobywania bloków synchronicznie z klientem, aby móc zweryfikować," +
                            " czy klient powinien mieć możliwość niszczenia bloków wtedy, kiedy uważa, że może to zrobić.");
                    this.logger.info("Działa jak anty nuker");
                }, true,
                (input) -> this.logger.info("Server Authoritative Block Breaking ustawiono na:&1 " + input)
        ));
        this.logger.print();

        this.serverProperties.setAllowCheats(scannerUtil.addBooleanQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lAllow Cheats&r (Aktualny z &bserver.properties&r to: " + defaultValue + ")" + this.enter);
                    this.logger.alert("Aby integracja z discord działała poprawnie wymagamy tego na&b true&r i włączenie&b experymentów&r!");
                },
                this.serverProperties.isAllowCheats(),
                (input) -> this.logger.info("Allow Cheats ustawione na:&1 " + input)
        ));
        this.logger.print();

        this.serverProperties.setTexturePackRequired(scannerUtil.addBooleanQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lWymagać tekstur?&r (Aktualny z &bserver.properties&r to: " + defaultValue + ")" + this.enter);
                    this.logger.info("Gdy jest to włączone klient musi pobrać tekstury i nie może używać własnych tekstur");

                },
                this.serverProperties.isTexturePackRequired(),
                (input) -> this.logger.info("Allow Cheats ustawione na:&1 " + input)
        ));
        this.logger.print();

        this.serverProperties.setServerTelemetry(scannerUtil.addBooleanQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lUstaw Telemetrie servera&r (Aktualnie z &bserver.properties&r to: " + defaultValue + ")" + this.enter);
                    this.logger.info("Prawdopodobnie&b crash reporty&r będą dzięki temu wysyłane do&4 mojang");
                },
                this.serverProperties.isServerTelemetry(),
                (input) -> this.logger.info("Telemetria servera ustawiona na:&1 " + input)
        ));
        this.logger.print();

    }

    private void questionsSetting(final ScannerUtil scannerUtil) {
        this.appConfig.setQuestions(scannerUtil.addBooleanQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lCzy powtórzyć następnym razem pytania?&r (Domyślnie: " + defaultValue + ")" + this.enter);
                    this.logger.info("&aJeśli ustawisz na&b false&a następnym razem aplikacja uruchomi się bez zadawania żadnych pytań!");
                    this.logger.info("&aMożesz potem zmienić to w&e config.yml");
                },
                true,
                (input) -> {
                }));
    }

    public void currentSettings(final Scanner scanner, final boolean waitForUser) {

        this.serverProperties.loadProperties();

        this.logger.print();
        this.logger.info("&n&lAktualne Dane");
        this.logger.print();
        this.logger.info("&e----------&bAplikacja&e----------");
        this.logger.info("System:&1 " + SystemUtil.getSystem());
        this.logger.info("Wine:&1 " + this.appConfig.isWine() + (DefaultsVariables.WINE ? " &d(&bPosiadasz&d)" : ""));
        this.logger.info("Ścieżka plików:&1 " + this.appConfig.getFilesPath());
        this.logger.info("Wersja:&1 " + this.versionManagerConfig.getVersion());

        final boolean backup = this.watchDogConfig.getBackupConfig().isEnabled();
        this.logger.info("Backup:&1 " + backup);

        if (backup) {
            this.logger.info("Częstotliwość robienia backup:&1 " + this.watchDogConfig.getBackupConfig().getBackupFrequency() + "&a minut");
        }

        this.logger.info("Nazwa świata:&1 " + this.serverProperties.getWorldName());
        this.logger.print();
        this.logger.info("&e-----------&bserver.properties&e----------");
        this.logger.info("Port v4:&1 " + this.serverProperties.getServerPort());
        this.logger.info("Port v6:&1 " + this.serverProperties.getServerPortV6());
        this.logger.info("Algorytm kompresji:&1 " + this.serverProperties.getCompressionAlgorithm());
        this.logger.print();

        this.logger.info("Liczba wątków używana przez server:&1 " + this.serverProperties.getMaxThreads());
        this.logger.info("Allow Cheats:&1 " + this.serverProperties.isAllowCheats());
        this.logger.info("Timeout:&1 " + this.serverProperties.getPlayerIdleTimeout() + " &aminut");
        this.logger.print();

        final double serverBuildRadiusRatio = this.serverProperties.getServerBuildRadiusRatio();
        this.logger.info("Server Build Radius Ratio:&1 " + (serverBuildRadiusRatio == -1.0 ? "Disabled" : serverBuildRadiusRatio));
        this.logger.info("View Distance:&1 " + this.serverProperties.getViewDistance());
        this.logger.info("Tick Distance:&1 " + this.serverProperties.getTickDistance());
        this.logger.print();

        this.logger.info("Default Player Permission Level:&1 " + this.serverProperties.getPlayerPermissionLevel().getPermissionName());
        this.logger.info("Server Authoritative Movement:&1 " + this.serverProperties.getServerMovementAuth().getAuthName());
        this.logger.info("Server Authoritative Block Breaking:&1 " + this.serverProperties.isServerAuthoritativeBlockBreaking());
        this.logger.info("Correct Player Movement:&1 " + this.serverProperties.isCorrectPlayerMovement());
        this.logger.info("Wymug tekstur:&1 " + this.serverProperties.isTexturePackRequired());
        this.logger.info("Allow Cheats:&1 " + this.serverProperties.isAllowCheats());
        this.logger.info("Emit server telemetry:&1 " + this.serverProperties.isServerTelemetry());
        this.logger.print();

        if (waitForUser) {
            this.logger.alert("&cWięcej opcji&e konfiguracji&c znajdziesz w config");
            this.logger.info("Kliknij enter aby kontynuować");
            scanner.nextLine();
        }

        if (this.appConfig.isFirstRun()) {
            this.appConfig.setFirstRun(false);
        }
        this.appConfig.save();
    }

    private void versionQuestion(final ScannerUtil scannerUtil) {
        final String latest = this.bdsAutoEnable.getVersionManager().getLatestVersion();
        final String check = (latest.equals("") ? "Nie udało odnaleźć się najnowszej wersji" : latest);

        this.versionManagerConfig.setVersion(scannerUtil.addStringQuestion(
                (defaultValue) -> {
                    this.logger.info("&n&lJaką wersje załadować?&r (Najnowsza: " + check + ")");
                    if (this.bdsAutoEnable.getVersionManager().getAvailableVersions().isEmpty()) {
                        this.logger.info("Nie znaleziono żadnej wersji");
                    } else {
                        this.logger.info("Pobrane wersje: " + this.bdsAutoEnable.getVersionManager().getAvailableVersions());
                    }
                    this.logger.info("Aby pobrać jakąś wersje wpisz jej numer (niektóre mogą mieć .01 / .02 na końcu)");
                },
                (latest.equals("") ? this.bdsAutoEnable.getVersionManager().getLoadedVersion() : latest),
                (input) -> this.logger.info("Wersja do załadowania ustawiona na:&1 " + input)
        ));
        this.bdsAutoEnable.getVersionManager().loadVersion();
        this.logger.print();
    }

    private void anotherVersionQuestion(final ScannerUtil scannerUtil) {
        scannerUtil.addBooleanQuestion(
                (defaultValue) -> this.logger.info("&n&lZaładować jakąś inną wersje?&r (Domyślnie: " + defaultValue + ")" + this.enter),
                false,
                (input) -> {
                    if (input) {
                        this.bdsAutoEnable.getVersionManager().setLoaded(false);
                        this.versionQuestion(scannerUtil);
                    }
                });
    }

    private void againSetupServer(final ScannerUtil scannerUtil) {
        scannerUtil.addBooleanQuestion(
                (defaultValue) -> this.logger.info("&n&lSkonfigurować ponownie server?&r (Domyślnie: " + defaultValue + ")" + this.enter),
                false,
                (input) -> {
                    if (input) {
                        this.serverSettings(scannerUtil);
                    }
                });
    }

    private void addCompressionAlgorithmQuestion(final ScannerUtil scannerUtil) {
        CompressionAlgorithm compressionAlgorithm;
        try {
            compressionAlgorithm = CompressionAlgorithm.valueOf(
                    scannerUtil.addStringQuestion(
                            (defaultValue) -> {
                                this.logger.info("&n&lUstaw Algorytm kompresji&r (Aktualnie z &bserver.properties&r to: " + defaultValue + ")" + this.enter);
                                this.logger.info("&b" + CompressionAlgorithm.SNAPPY + "&a Szybki algorytm kompresji z niską latencją, idealny do zastosowań czasu rzeczywistego, choć pliki mogą być nieco większe.");
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

    private void addPlayerPermissionQuestion(final ScannerUtil scannerUtil) {
        PlayerPermissionLevel playerPermissionLevel;

        try {
            playerPermissionLevel = PlayerPermissionLevel.valueOf(
                    scannerUtil.addStringQuestion(
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
