<div align="center">

# BDS-Auto-Enable

Jest to program do zarządzania BDS wykorzystywany na serverze **Huje22**

</div>

# Uwaga

* Wymaga Javy 17 bądź wyższej
* Program wymaga przynajmniej `1GB` RAM do działania
* Program wspiera użycie [**WINE**](https://github.com/wine-mirror/wine)
* Program wspiera proste
  rozszerzenia: [ExampleExtension](https://github.com/Huje22/BDS-AE-Extensions/tree/master/ExampleExtension)

# Program zawiera

* Dosyć proste
  rozszerzenia: [ExampleExtension](https://github.com/Huje22/BDS-AE-Extensions/tree/master/ExampleExtension)
* Automatyczne włączenie servera po crashu
* Łatwe załadowanie innej wersji
* Tworzenie backupów świata co dany czas i manualnie
* Pisanie w konsoli w 99% (**Mogą wystąpić małe błędy podczas dłuższego działania, lecz już nie powinny**)
* Licznik czasu gry gracza (
  Wymaga [BDS-Auto-Enable-Management-Pack](https://github.com/Huje22/BDS-Auto-Enable-Management-Pack))
* Licznik śmierci (Wymaga [BDS-Auto-Enable-Management-Pack](https://github.com/Huje22/BDS-Auto-Enable-Management-Pack))

<details>
  <summary>Jak działa to z Paczką?</summary>
  <p>Aplikacja komunikuje się z serwerem BDS za pomocą wysyłania komend do konsoli oraz czytania ważnych informacji z konsoli, na przykład:</p>
  <p>Paczka <a href="https://github.com/Huje22/BDS-Auto-Enable-Management-Pack">BDS-Auto-Enable-Management-Pack</a> wysyła do konsoli log np <code>PlayerChat:JndjanBartonka Message:Witaj</code>Aplikacja odczytuje nick gracza z <code>PlayerChat</code> i wiadomość z <code>Message</code>. Następnie wywołuje event `PlayerChatEvent` w każdym zarejestrowany listenerze , podobnie z dołączaniem gracza (w tym wypadku <code>PlayerJoin</code>).<br>
  W tym przypadku dodaje gracza do listy graczy online, a timer działający co 1s dodaje mu 1s czasu gry.<br>
  Większość takich akcji odbywa się w klasie <a href="https://github.com/Huje22/Bds-Auto-Enable/blob/master/src/main/java/me/indian/bds/manager/server/ServerManager.java">ServerManager.java</a></p>
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

[Poradnik ustawienia integracji z Discord](../BDS-AE-Extensions/DiscordExtension/DiscordInstalation.md)

* Wszystkie je jak i także ich opisy znajdziesz po wpisaniu `/` (Wymaga dodania bota z
  ___&scope=bot+applications.commands___ inaczej mogą wystąpić problemy)

# Program nie wspiera

* Wtyczek do Minecraft ani czytania pakietów z Minecraft

# Szybkie info

* Paczka sama się pobierze do twojego świata i załaduje , potrzebujesz jedynie włączonych experymentów w tym świecie!

# Użyte biblioteki

[Okaeri Configs - Do configów yml](https://github.com/OkaeriPoland/okaeri-configs) <br>
[Gson - Do plików i samych Json](https://github.com/google/gson)<br>
[Logback - Tylko dla developerów rozszerzeń](https://github.com/qos-ch/logback)<br>

  ----

<div align="center">

[![Bstats](https://bstats.org/signatures/bukkit/BDS-Auto-Enable.svg)](https://bstats.org/plugin/bukkit/BDS-Auto-Enable/19727)

[![bStats Servers](https://img.shields.io/bstats/servers/19727?style=for-the-badge)](https://bstats.org/plugin/bukkit/BDS-Auto-Enable/19727)
[![bStats Players](https://img.shields.io/bstats/players/19727?style=for-the-badge)](https://bstats.org/plugin/bukkit/BDS-Auto-Enable/19727) <br>
![Latest Tag](https://img.shields.io/github/v/tag/Huje22/Bds-Auto-Enable?label=LATEST%20TAG&style=for-the-badge)
![GitHub commit activity (branch)](https://img.shields.io/github/commit-activity/m/Huje22/BDS-Auto-Enable?style=for-the-badge)
