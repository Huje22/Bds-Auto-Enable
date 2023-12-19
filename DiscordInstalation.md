<div align="center">

## Obsługa Integracji z Discord

W `Discord.yml` który znajduje się w folderze `katalog z jar/BDS-Auto-Enable/config`. <br>
Masz tam trzy typy integracji, polecamy `JDA` lecz gdy nie potrzebujesz czatu `Discord <-> Minecraft` a
samo `Minecraft -> Discord` możesz wybrać `WEBHOOK` <br>
Gdy nie potrzebujesz integracji z discord wybierz `NONE` <br>

Aby aplikacja miała dostęp do informacji o graczu musisz
posiadać [BDS-Auto-Enable-Management-Pack](https://github.com/Huje22/BDS-Auto-Enable-Management-Pack)

</div>

## Uwaga

`Messages` i `MessagesOptions` w config są dla obu integracji

### NONE

Brak jakiej kolwiek integracji z discord

### WEBHOOK

Ta integracja jest dosyć prosta, pozwala na czat `Minecraft -> Discord`

#### Jak zacząć

[//]: # (* Możesz także ustawić `consoleUrl` wtedy zajrzyj do `Log.yml`)

* Pierw musisz utworzyć webhook na kanale , tu
  znajdziesz [poradnik](https://support.discord.com/hc/en-us/articles/228383668-Intro-to-Webhooks)
* Gdy masz już link ustaw `chatUrl` na link twojego webhooku kanału z wiadomościami
* Możesz także ustawić `avatarUrl` i `name` na jakie tylko chcesz
  * Jeśli link do avataru bezie niepoprawny prawdopodobnie webhook nie będzie działać
  * Sekcja `Bot` nie bedzie ci tu potrzebna

### JDA

Integracja za pomocą bota dodająca czat `Discord <-> Minecraft` i nie tylko

#### Jak zacząć

* Musisz utworzyć bota, tu
  znajdziesz [poradnik](https://www.appki.com.pl/jak-stworzyc-bota-discord-i-dodac-go-do-swojego-serwera) <br>
* I dalej wypełniasz Config danymy informacjami, czym jest co znajdziesz opis w komentarzach `#`
  * Sekcja `WebHook` nie bedzie ci tu potrzebna
* Jak nie wiesz jak skopiować id kanału to tu
  znajdziesz [poradniki](https://www.google.com/amp/s/pl.jugomobile.com/jak-znalezc-identyfikator-serwera-w-discord-na-komputerze-pc-lub-smartfonie/%3famp)<br>
  * Możesz także używać konsoli na discord , zajrzyj do `Log.yml` aby dowiedzieć się więcej o dostępnych tam
    wiadomościach
* Aby bot działał poprawnie będziesz musiał włączyć wszystkie <br>
  `Privileged Gateway Intents`: <br>
  * MESSAGE CONTENT INTENT
  * PRESENCE INTENT
  * SERVER MEMBERS INTENT

![Discord](https://github.com/Huje22/.github/blob/main/assets/Discord-Privileged-Gateway-Intents.jpg)
