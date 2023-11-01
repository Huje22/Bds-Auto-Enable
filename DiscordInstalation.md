<div align="center">

W `config.yml` który znajduje się w folderze `katalog z jar/BDS-Auto-Enable` ,<br>znajdziesz sekcje `Discord`, masz tam
dwa typy integracji, polecamy `JDA` lecz gdy nie potrzebujesz czatu `Diecord <-> Minecraft` a
samo `Minecraft -> Discord` możesz wybrać `WEBHOOK` <br>
</div>

### Dodatkowe info

Nie tylko otrzymasz czat `Discord <-> Minecraft` , a także niektóre funkcje mogą posiadać alerty od innych funkcji (
które można wyłączyć w `config.yml`) i alerty o błędach krytycznych które mogą zapobiec działaniu servera Minecraft

### WEBHOOK

TODO: Dokończyć to

### JDA

* Musisz utworzyć bota, tu
  znajdziesz [poradnik](https://www.appki.com.pl/jak-stworzyc-bota-discord-i-dodac-go-do-swojego-serwera) <br>
* I dalej wypełniasz Config danymy informacjami, czym jest co znajdziesz opis w komentarzach `#`
* Jak nie wiesz jak skopiować id kanału to tu
  znajdziesz [poradniki](https://www.google.com/amp/s/pl.jugomobile.com/jak-znalezc-identyfikator-serwera-w-discord-na-komputerze-pc-lub-smartfonie/%3famp)<br>
* Aby bot działał poprawnie będziesz musiał włączyć wszystkie <br>
  `Privileged Gateway Intents`: <br>
    * MESSAGE CONTENT INTENT
    * PRESENCE INTENT
    * SERVER MEMBERS INTENT

![Discord](https://github.com/Huje22/.github/blob/main/assets/Discord-Privileged-Gateway-Intents.jpg)
