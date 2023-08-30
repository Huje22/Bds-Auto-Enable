package me.indian.bds.player;

import me.indian.bds.util.DateUtil;

//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
import java.io.Serializable;
//import java.nio.file.DirectoryStream;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.time.LocalDate;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;

public class Player implements Serializable {
//
//    private final String name;
//    private final long xuid;
//    private long lastPlayed;
//    private static final Map<String, Player> players = new HashMap<>();
//
//    public Player(final String name, final long xuid) {
//        this.name = name;
//        this.xuid = xuid;
//        players.put(this.name, this);
//    }
//
//    public Player(final String name) {
//        this.name = name;
//        this.xuid = -1L;
//        players.put(this.name, this);
//    }
//
//    public void serializePlayer() throws IOException {
//        final String directoryPath = Defaults.getAppDir() + File.separator +"players/";
//        final String filePath = directoryPath + this.xuid + ".ser";
//
//        final Path path = Paths.get(filePath);
//        Files.createDirectories(path.getParent()); // Tworzenie brakujących katalogów
//
//        try (ObjectOutputStream outputStream = new ObjectOutputStream(Files.newOutputStream(path))) {
//            outputStream.writeObject(this);
//        }
//    }
//
//    public static Map<String, Player> deserializePlayers() throws IOException, ClassNotFoundException {
//        try (final DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(Defaults.getAppDir() + File.separator +"players/"))) {
//            for (final Path path : directoryStream) {
//                if (Files.isRegularFile(path)) {
//                    final String name = String.valueOf(path.getFileName());
//                    if (name.endsWith(".ser")) {
//                        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(Defaults.getAppDir() + File.separator +"players/" + name))) {
//                            final Player player = (Player) inputStream.readObject();
//                            players.put(player.getName(), player);
//                        }
//                    }
//                }
//            }
//        } catch (final IOException exception) {
//            exception.printStackTrace();
//        }
//        return players;
//    }
//
//
//    public static void main(String[] args) {
//
//        try {
//
//
//            Map<String, Player> playerMap = Player.deserializePlayers();
//
//            Player j = getPlayerByName("John");
//            long lastpl = j.getLastPlayed();
//            j.setLastPlayed(DateUtil.localDateToLong(LocalDate.of(2032 ,10 ,1)));
//
//            System.out.println(lastpl);
//            System.out.println(DateUtil.longToLocalDate(lastpl));
//
//            j.setLastPlayed(DateUtil.localDateToLong(LocalDate.now()));
//
//            long lastpl3 = j.getLastPlayed();
//            System.out.println(lastpl3);
//            System.out.println(DateUtil.longToLocalDate(lastpl3));
//
//
//            for (Player player : playerMap.values()) {
//                System.out.println(player.name);
//
//            }
//        } catch (IOException | ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    public String getName() {
//        return this.name;
//    }
//
//    public long getXuid() {
//        return this.xuid;
//    }
//
//    public long getLastPlayed() {
//        return this.lastPlayed;
//    }
//
//    public void setLastPlayed(final long lastPlayed) {
//        this.lastPlayed = lastPlayed;
//    }
//
//    public static Player getPlayerByName(final String name) {
//        return players.get(name);
//    }
}
