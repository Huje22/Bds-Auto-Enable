# Dokumentacja API

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

### Pobierz backup za pomocą klucza autoryzacji

```
GET /api/{api-key}/backup/{filename}
```

Pobiera dostępny backup o określonej nazwie przy użyciu klucza autoryzacji.

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
curl -X POST -H "Content-Type: application/json" -d "{ \"command\": \"say To wiadomość oddana za pomocą REST API\"}" http://localhost:PORT_STRONY/command/KLUCZ_API
```

Pamiętaj o podmianie `PORT_STRONY` i `KLUCZ_API`, i zmianie `localhost` na ip twojego servera jeśli nie robisz tego
lokalnie

### Ostrzeżenie

Nie udostępniaj kluczy API

