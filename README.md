<div align="center">

# BDS-Auto-Enable

Jest to program do zarządzania BDS wykorzystywany na serverze **Huje22**


</div>

# Uwaga

* Program wymaga przynajmniej `1GB` ram do działania
* Zaleca się używać integracji z discord (JDA)
* Program wspiera użycie [**WINE**](https://github.com/wine-mirror/wine)

# Program zawiera

* Automatyczne włączenie servera po crashu
* Łatwe załadowanie innej wersji
* Tworzenie backupów świata co dany czas i manualnie (
  ___Ładowanie backup wymaga nadal wielkiej poprawy, wymaga debugu właczonego___)
* Pisanie w konsoli w 99% (**Mogą wystąpić małe błędy**)
* AutoMessages
* Licznik czasu gry gracza (
  Wymaga [BDS-Auto-Enable-Management-Pack](https://github.com/Huje22/BDS-Auto-Enable-Management-Pack))
* Licznik śmierci (Wymaga [BDS-Auto-Enable-Management-Pack](https://github.com/Huje22/BDS-Auto-Enable-Management-Pack))
* Integracje z Discordem
  (Wymaga [BDS-Auto-Enable-Management-Pack](https://github.com/Huje22/BDS-Auto-Enable-Management-Pack) do obsługi
  większej
  ilości funkcji)
* **Rest API** z czasem gry , liczbą śmierci i graczami online/offline

# Jak to działa

Komunikuje się on z serverem BDS za pomocą wysyłania komend do konsoli a także czytania ważnych informacji z konsoli na
przykład: <br>
Paczka [BDS-Auto-Enable-Management-Pack](https://github.com/Huje22/BDS-Auto-Enable-Management-Pack) wysyła do konsoli
log

```
PlayerChat:JndjanBartonka Message:Witaj
```

A aplikacja odczytuje nick gracza z `PlayerChat` i wiadomość z `Message` , i dalej na przykład wysyła wiadomość tą do
discord , podobnie z dołączaniem gracza (w tym wypadku `PlayerJoin`) , i w tym wypadku dodaje gracza na listę graczy
online i timer działający co 1s dodaje mu wtedy 1s czasu gry.<br>
Większość takich akcji odbywa się w klasie [ServerManager.java](https://github.com/Huje22/Bds-Auto-Enable/blob/master/src/main/java/me/indian/bds/manager/server/ServerManager.java)

# Polecenia

### W konsoli

* `backup` - natychmiastowo wywołuje tworzenie backupa
* `version` - pokazuje załadowaną versie minecraft + versie oprogramowania (w konsoli i graczom online)
* `stats` - statystyki servera i aplikacji
* `playtime` - top 20 graczy z największym czasem gry
* `deaths` - top 20 graczy z największą ilością śmierci
* `end` - zamyka server i aplikacje

### W Bocie

* Wszystkie je jak i także ich opisy znajdziesz po wpisaniu `/` (Wymaga dodania bota z
  ___&scope=bot+applications.commands___ inaczej mogą wystąpić problemy)

### Na serwerze
  **Działają tylko gdy server ma paczke [BDS-Auto-Enable-Management-Pack](https://github.com/Huje22/BDS-Auto-Enable-Management-Pack) (Działają tylko gdy paczka jest najwyżej)**
* `!help` - lista poleceń
* `!tps` - Ticki na sekunde servera
* `!playtime` - Top 10 graczy z największym czasem gry
* `!deaths` - Top 10 graczy z największą ilością śmierci
* `!link` - Łączy nick z mc z kontem discord
 
# Użyte biblioteki

[Okaeri Configs - do configów yml](https://github.com/OkaeriPoland/okaeri-configs) <br>
[Gson - do plików i samych Json](https://github.com/google/gson)<br>
[JDA - Integracja z discord](https://github.com/discord-jda/JDA)<br>
[Javalin - Rest API](https://github.com/javalin/javalin)<br>
[Logback - Tylko do wyłączenia niektórych wiadomości z Javalin i JDA](https://github.com/qos-ch/logback)<br>

# Program nie wspiera

* Wtyczek do Minecraft

# Szybkie info

* Paczka sama się pobierze do twojego świata i załaduje , potrzebujesz jedynie włączonych experymentów w tym świecie!

  ----
  
<div align="center">

  ![Bstats](https://bstats.org/signatures/bukkit/BDS-Auto-Enable.svg)

![bStats Servers](https://img.shields.io/bstats/servers/19727?style=for-the-badge)
![bStats Players](https://img.shields.io/bstats/players/19727?style=for-the-badge) <br>
![Latest Tag](https://img.shields.io/github/v/tag/Huje22/Bds-Auto-Enable?label=LATEST%20TAG&style=for-the-badge)
![GitHub commit activity (branch)](https://img.shields.io/github/commit-activity/m/Huje22/BDS-Auto-Enable?style=for-the-badge)
