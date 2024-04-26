package me.indian.bds.exception;

import java.net.HttpURLConnection;

public class DownloadException extends Exception {

    public DownloadException(final String message, final int code) {
        super(message + getCodeMessage(code));
    }

    private static String getCodeMessage(final int responseCode) {
        return switch (responseCode) {
            case HttpURLConnection.HTTP_BAD_REQUEST -> "Nieprawidłowe zapytanie";
            case HttpURLConnection.HTTP_UNAUTHORIZED -> "Brak autoryzacji";
            case HttpURLConnection.HTTP_FORBIDDEN -> "Odmowa dostępu";
            case HttpURLConnection.HTTP_NOT_FOUND -> "Nie znaleziono";
            case HttpURLConnection.HTTP_INTERNAL_ERROR -> "Wewnętrzny błąd serwera";
            case HttpURLConnection.HTTP_BAD_GATEWAY -> "Błąd bramy";
            case HttpURLConnection.HTTP_UNAVAILABLE -> "Serwis niedostępny";
            case HttpURLConnection.HTTP_GATEWAY_TIMEOUT -> "Przekroczony czas oczekiwania na bramę";
            case 429, 439 -> "Zbyt wiele żądań";
            default -> "Nie obsłużony kod odpowiedzi: " + responseCode;
        };
    }
}
