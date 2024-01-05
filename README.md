<div align="center">

# BDS-Auto-Enable

Jest to program do zarządzania BDS wykorzystywany na serverze **Huje22**

</div>

# Uwaga

* Program wymaga przynajmniej `1GB` RAM do działania
  * Jeśli chcesz zmieniać coś w `NoDiscordConsole` w `Log.yml` wypisywanie większej ilości tekstu może wymagać więcej ramu z powodu RateLimitu Discord
* Zaleca się używać integracji z Discord
  * Dla wygodniejszego zarządzania serwerem</details>
* Program wspiera użycie [**WINE**](https://github.com/wine-mirror/wine)
  * Jest to nie zalecane
* Jedyna wersja Minecraft, którą wspieramy, to:
  [![BDS - Version](https://img.shields.io/badge/Bedrock%20Dedicated%20Server-1.20.51.01-brightgreen)](https://www.minecraft.net/download/server/bedrock)
  * Inne nadal mogą być kompatybilne 

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
* [**Rest API**](RestAPI.MD) również wymaga paczki

<details>
  <summary>Jak działa to z Paczką?</summary>
  <p>Aplikacja komunikuje się z serwerem BDS za pomocą wysyłania komend do konsoli oraz czytania ważnych informacji z konsoli, na przykład:</p>

  <p>Paczka <a href="https://github.com/Huje22/BDS-Auto-Enable-Management-Pack">BDS-Auto-Enable-Management-Pack</a> wysyła do konsoli log:</p>

  <pre>PlayerChat:JndjanBartonka Message:Witaj</pre>

  <p>Aplikacja odczytuje nick gracza z <code>PlayerChat</code> i wiadomość z <code>Message</code>. Następnie wysyła tę wiadomość do Discorda, podobnie z dołączaniem gracza (w tym wypadku <code>PlayerJoin</code>). W tym przypadku dodaje gracza do listy graczy online, a timer działający co 1s dodaje mu 1s czasu gry.</p>

  <p>Większość takich akcji odbywa się w klasie <a href="https://github.com/Huje22/Bds-Auto-Enable/blob/master/src/main/java/me/indian/bds/manager/server/ServerManager.java">ServerManager.java</a></p>
</details>

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

# Szybkie info

* Paczka sama się pobierze do twojego świata i załaduje , potrzebujesz jedynie włączonych experymentów w tym świecie!

# Użyte biblioteki

[Okaeri Configs - Do configów yml](https://github.com/OkaeriPoland/okaeri-configs) <br>
[Gson - Do plików i samych Json](https://github.com/google/gson)<br>
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
