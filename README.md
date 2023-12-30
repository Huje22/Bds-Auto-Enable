<div align="center">

# BDS-Auto-Enable

Jest to program do zarządzania BDS wykorzystywany na serverze **Huje22**

</div>

# Uwaga

* Program wymaga przynajmniej `1GB` ram do działania
* Zaleca się używać integracji z discord (JDA)
* Program wspiera użycie [**WINE**](https://github.com/wine-mirror/wine)
* Jedyna wersia Minecraft jaką wspieramy
  to <br>
[![BDS - Version](https://img.shields.io/badge/Bedrock%20Dedicated%20Server-1.20.51.01-brightgreen)](https://www.minecraft.net/download/server/bedrock)


# Program zawiera

* Automatyczne włączenie servera po crashu
* Łatwe załadowanie innej wersji
* Tworzenie backupów świata co dany czas i manualnie 
* Pisanie w konsoli w 99% (**Mogą wystąpić małe błędy podczas dłuższego działania, lecz już nie powinny**)
* AutoMessages
* Licznik czasu gry gracza (
  Wymaga [BDS-Auto-Enable-Management-Pack](https://github.com/Huje22/BDS-Auto-Enable-Management-Pack))
* Licznik śmierci (Wymaga [BDS-Auto-Enable-Management-Pack](https://github.com/Huje22/BDS-Auto-Enable-Management-Pack))
* Integracje z Discordem
  (Wymaga [BDS-Auto-Enable-Management-Pack](https://github.com/Huje22/BDS-Auto-Enable-Management-Pack) do obsługi
  większej
  ilości funkcji)
* Formatowanie czatu (Wymaga [BDS-Auto-Enable-Management-Pack](https://github.com/Huje22/BDS-Auto-Enable-Management-Pack) działa tylko gdy paczka jest najwyżej)
* **Rest API** z czasem gry , liczbą śmierci i graczami online/offline (Również wymaga paczki)

# Jak to działa

Komunikuje się on z serverem BDS za pomocą wysyłania komend do konsoli a także czytania ważnych informacji z konsoli na
przykład: <br>
Paczka [BDS-Auto-Enable-Management-Pack](https://github.com/Huje22/BDS-Auto-Enable-Management-Pack) wysyła do konsoli log <br>
``
PlayerChat:JndjanBartonka Message:Witaj
``<br>
A aplikacja odczytuje nick gracza z `PlayerChat` i wiadomość z `Message` , i dalej na przykład wysyła wiadomość tą do
discord , podobnie z dołączaniem gracza (w tym wypadku `PlayerJoin`) , i w tym wypadku dodaje gracza na listę graczy
online i timer działający co 1s dodaje mu wtedy 1s czasu gry.<br>
Większość takich akcji odbywa się w
klasie [ServerManager.java](https://github.com/Huje22/Bds-Auto-Enable/blob/master/src/main/java/me/indian/bds/manager/server/ServerManager.java)

# Polecenia

### Na serwerze i w konsoli

**Polecenia dla gracza działają tylko gdy server ma
paczke [BDS-Auto-Enable-Management-Pack](https://github.com/Huje22/BDS-Auto-Enable-Management-Pack) (Działają tylko gdy
paczka jest najwyżej)**

* `!help` - lista poleceń
* `!tps` - Ticki na sekunde servera (działa tylko w minecraft)
  **Po resztę użyj `!help`**

### W Bocie

[Poradnik ustawienia integracji z Discord](DiscordInstalation.md)

* Wszystkie je jak i także ich opisy znajdziesz po wpisaniu `/` (Wymaga dodania bota z
  ___&scope=bot+applications.commands___ inaczej mogą wystąpić problemy)
  
# Program nie wspiera

* Wtyczek do Minecraft ani czytania pakietów z Minecraft

# Projekty które powinienem kiedyś uwzględnić
https://github.com/hesslink111/Minecraft-Telegram-Bot <br>

# Szybkie info

* Paczka sama się pobierze do twojego świata i załaduje , potrzebujesz jedynie włączonych experymentów w tym świecie!

# Użyte biblioteki

[Okaeri Configs - do configów yml](https://github.com/OkaeriPoland/okaeri-configs) <br>
[Gson - do plików i samych Json](https://github.com/google/gson)<br>
[JDA - Integracja z discord](https://github.com/discord-jda/JDA)<br>
[Javalin - Rest API](https://github.com/javalin/javalin)<br>
[Logback - Tylko do wyłączenia niektórych wiadomości z Javalin i JDA](https://github.com/qos-ch/logback)<br>

  ----
  
<div align="center">

[![Bstats](https://bstats.org/signatures/bukkit/BDS-Auto-Enable.svg)](https://bstats.org/plugin/bukkit/BDS-Auto-Enable/19727)

[![bStats Servers](https://img.shields.io/bstats/servers/19727?style=for-the-badge)](https://bstats.org/plugin/bukkit/BDS-Auto-Enable/19727)
[![bStats Players](https://img.shields.io/bstats/players/19727?style=for-the-badge)](https://bstats.org/plugin/bukkit/BDS-Auto-Enable/19727) <br>
![Latest Tag](https://img.shields.io/github/v/tag/Huje22/Bds-Auto-Enable?label=LATEST%20TAG&style=for-the-badge)
![GitHub commit activity (branch)](https://img.shields.io/github/commit-activity/m/Huje22/BDS-Auto-Enable?style=for-the-badge)
