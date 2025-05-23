<div align="center">

# BDS-Auto-Enable

Jest to program do zarządzania BDS wykorzystywany na serverze **Huje22**
i [innych](#inne-servery-które-używały-tego-projektu-) <br>
Jak zbudować projekt? [Budowanie projektu za pomocą Maven Wrapper](./Wrapper%20Instruction.MD)
</div>

# Uwaga

* Wspieramy tylko
  tą [wersje](https://github.com/Huje22/Bds-Auto-Enable/blob/main/src/main/java/pl/indianbartonka/bds/config/sub/version/VersionManagerConfig.java#L17)
* Wymaga Javy 17 bądź wyższej
* Program wymaga przynajmniej `4GB` RAM (jest aktualne minimum dla BDS)
* Program wspiera użycie: **[WINE](https://github.com/wine-mirror/wine)**, **[Box64](https://github.com/ptitSeb/box64)**
* Program wspiera proste
  rozszerzenia: [ExampleExtension](https://github.com/Huje22/BDS-AE-Extensions/tree/master/ExampleExtension)

# Program zawiera

* Dosyć proste
  rozszerzenia: [ExampleExtension](https://github.com/Huje22/BDS-AE-Extensions/tree/master/ExampleExtension) (Nie zawsze
  zaktualizowane z najnowszym API)
* Automatyczne włączenie servera po crashu
* Łatwe załadowanie innej wersji
* Automatyczne ładowanie paczek `zachowań` i `tesktur` z `FOLDER_ŚWIATA/behavior_packs`/`resource_packs`
* Tworzenie backupów świata co dany czas i manualnie
* Statystyki graczy (
  Wymaga [BDS-Auto-Enable-Management-Pack](https://github.com/Huje22/BDS-Auto-Enable-Management-Pack))

<details>
  <summary>Jak działa to z Paczką?</summary>
  <p>Aplikacja komunikuje się z serwerem BDS za pomocą wysyłania komend do konsoli oraz czytania ważnych informacji z konsoli, na przykład:</p>
  <p>Paczka <a href="https://github.com/Huje22/BDS-Auto-Enable-Management-Pack">BDS-Auto-Enable-Management-Pack</a> wysyła do konsoli log np <code>PlayerChat:JndjanBartonka Message:Witaj</code><br>
Aplikacja odczytuje nick gracza z <code>PlayerChat</code> i wiadomość z <code>Message</code>. Następnie wywołuje event <code>PlayerChatEvent</code> w każdym zarejestrowany listenerze ,
podobnie z dołączaniem gracza (w tym wypadku <code>PlayerJoin</code>).<br>
  W tym przypadku wywołuje <code>PlayerJoinEvent</code> i on dodaje gracza do listy graczy online, a timer działający co 1s dodaje mu 1s czasu gry.<br>
  Większość takich akcji odbywa się w klasie <a href="https://github.com/Huje22/Bds-Auto-Enable/blob/main/src/main/java/me/indian/bds/server/ServerManager.java">ServerManager.java</a></p>
</details>

# Polecenia

### Na serwerze i w konsoli

**Polecenia dla gracza działają tylko gdy server ma
paczke [BDS-Auto-Enable-Management-Pack](https://github.com/Huje22/BDS-Auto-Enable-Management-Pack) (Działają tylko gdy
paczka jest najwyżej)**

* `!help` - lista poleceń

# Program nie wspiera

* Wtyczek do Minecraft z czytaniem pakietów i ich wysyłaniem (za to zawiera proste rozszerzenia)

# Szybkie info

* Paczka sama się pobierze do twojego świata i załaduje , potrzebujesz jedynie włączonych experymentów w tym świecie!

# Inne servery które używały tego projektu

* [MrowiskoSMP](https://github.com/mrowiskomc/) - Server przy którym pomagał Indian tworzony przez <b>Mruwe</b> i jego
  ekipe <br>
* <b>BetterRealms</b> - Server przy którym pomagał Indian tworzony
  przez [Adovskiego](https://www.youtube.com/@AdoVski) <br>
  ----

<div align="center">

[![Bstats](https://bstats.org/signatures/bukkit/BDS-Auto-Enable.svg)](https://bstats.org/plugin/bukkit/BDS-Auto-Enable/19727)

[![bStats Servers](https://img.shields.io/bstats/servers/19727?style=for-the-badge)](https://bstats.org/plugin/bukkit/BDS-Auto-Enable/19727)
[![bStats Players](https://img.shields.io/bstats/players/19727?style=for-the-badge)](https://bstats.org/plugin/bukkit/BDS-Auto-Enable/19727) <br>
![Latest Tag](https://img.shields.io/github/v/tag/Huje22/Bds-Auto-Enable?label=LATEST%20TAG&style=for-the-badge)
![GitHub commit activity (branch)](https://img.shields.io/github/commit-activity/m/Huje22/BDS-Auto-Enable?style=for-the-badge)
