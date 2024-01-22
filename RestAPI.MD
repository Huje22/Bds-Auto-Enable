# Dokumentacja API

---
## Statystyki

### Pobierz informacje o śmierciach graczy

```
GET /api/stats/deaths
```

Zwraca statystyki dotyczące liczby śmierci graczy.

### Pobierz informacje o czasie gry graczy

```
GET /api/stats/playtime
```

Zwraca statystyki dotyczące czasu gry graczy w milisekundach.

### Pobierz informacje o graczach online i offline

```
GET /api/stats/players
```

Zwraca informacje o graczach, którzy są obecnie online oraz offline.

---

## Backup

### Pobierz backup za pomocą klucza autoryzacji

```
GET /api/{api-key}/backup/{filename}
```

Pobiera dostępny backup o określonej nazwie przy użyciu klucza autoryzacji.

---
## Konsola serwera

### Wykonaj polecenie w konsoli serwera

```
POST /command/{api-key}
```

Wykonuje polecenie w konsoli serwera i zwraca ostatnią linię z konsoli.

#### Przykładowy JSON

```json
{
  "command": "say To wiadomość oddana za pomocą REST API"
}
```

#### Przykład wysłania JSON z poziomu konsoli

```
curl -X POST -H "Content-Type: application/json" -d "{ \"command\": \"say To wiadomość oddana za pomocą REST API\"}" http://localhost:PORT/command/KLUCZ_API
```

---

# Discord

### Wyślij wiadomość do kanału czatu jako gracz

```
POST /discord/message/{api-key}
```

#### Przykładowy JSON

```json
{
  "name": "REST API",
  "message": "Wysyłam wiadomość na discord!"
}

```
#### Przykład wysłania JSON z poziomu konsoli

```
curl -X POST -H "Content-Type: application/json" -d "{ \"name\": \"REST API\",\"message\": \"Wysyłam wiadomość na discord!\"}" http://localhost:PORT/command/KLUCZ_API
```

---

# Dodatkowe

### Wyślij wiadomość do kanału czatu

```
POST /playerInfo/{api-key}
```

#### Przykładowy JSON

WORK IN PROGRESS

```json
{}
```

---

### Ostrzeżenie

Nie udostępniaj kluczy API
