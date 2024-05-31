## Budowanie projektu za pomocą Maven Wrapper

### Uruchom Maven Wrapper

Aby uruchomić Maven Wrapper, wykonaj poniższe kroki w zależności od systemu operacyjnego:

- Na systemach Unix/Linux/MacOS:

  ```bash
  sudo bash ./mvnw
  ```

- Na systemie Windows:

  ```cmd
  ./mvnw.cmd
  ```

### Jak zbudować JAR

Po uruchomieniu Maven Wrapper, zbuduj projekt, wykonując poniższe polecenia:

- Na systemach Unix/Linux/MacOS:

  ```bash
  sudo bash ./mvnw clean install
  ```

- Na systemie Windows:

  ```cmd
  ./mvnw.cmd clean install
  ```

### Gdzie odnaleźć JAR

Po zbudowaniu projektu, przejdź do katalogu `./target` i odnajdź plik `BDS-Auto-Enable-WERSIA.jar`.