package me.indian.bds.rest;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.server.manager.StatsManager;
import me.indian.bds.util.DateUtil;

public final class HTMLUtil {

    private HTMLUtil() {

    }

    public static String getExampleBody() {
        return """                         
                  <!DOCTYPE html>
                 <html lang="pl">
                 <meta name="theme-color" content="#38761d">
                 <meta name="viewport" content="width=device-width, initial-scale=1.0">
                 <meta charset="utf-8">
                  <body>
                <a target="_blank" class="guzik" href="https://github.com/Huje22/Bds-Auto-Enable/blob/main/RestAPI.MD">Dostępne EndPointy</a><br>
                 <a target="_blank" class="guzik" href="minecraft://?addExternalServer=Huje22|127.0.0.1:19132">Kliknij aby dołączyć na server!</a><br>
                         Ta Strona odświeża się co 1min<br>
                         <br>
                         Tak możesz uzyskać osoby online na serwerze <br>
                         <online> / <max><br>
                         <br><br> 
                         Osoby online i ich statystyki    
                         <br><br>
                         <online-with-stats>
                  <style>
                  a { text-decoration: none; }
                 .guzik:hover { box-shadow: 0px 4px 8px rgb(45 35 66 / 40%), 0px 7px 13px -3px rgb(45 35 66 / 30%), inset 0px -3px 0px #d6d6e7;
                    transform: translateY(-2px);}
                 .guzik:active { box-shadow: inset 0px 3px 7px #d6d6e7;
                    transform: translateY(2px);}                 
                  </style>
                  </body>
                  </html>                 
                  """;
    }

    public static String getOnlineWithStats(final BDSAutoEnable bdsAutoEnable) {
        String online = "<b>Brak osób online</b>";
        final StatsManager statsManager = bdsAutoEnable.getServerManager().getStatsManager();

        for (final String player : bdsAutoEnable.getServerManager().getOnlinePlayers()) {
            online += """                      
                    <details>
                      <summary><playerName></summary>
                      <p><playTime></p>
                      <p><deaths></p>
                    </details>                     
                    """.replaceAll("<playerName>", player)
                    .replaceAll("<playTime>",
                            quote("Czas gry: " + b(DateUtil.formatTime(statsManager.getPlayTimeByName(player), "days hours minutes seconds"))))
                    .replaceAll("<deaths>",
                            quote("Śmierci: " + b(String.valueOf(statsManager.getDeathsByName(player)))));
        }

        return online;
    }

    public static String b(final String message) {
        return "<b>" + message + "</b>";
    }

    public static String quote(final String message) {
        return "<blockquote>" + message + "</blockquote>";
    }
}