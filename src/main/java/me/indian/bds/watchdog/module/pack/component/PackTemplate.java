package me.indian.bds.watchdog.module.pack.component;
import java.util.ArrayList;
import java.util.List;
public record PackTemplate(String pack_id, String subpack, int[] version) {

    public static void main(String[] args) {
        // Tworzenie listy
        List<String> lista = new ArrayList<>();

        // Dodanie elementów do listy (opcjonalne)
        lista.add("Element 1");
        lista.add("Element 2");
        lista.add("Element 3");

        // Dodanie nowego elementu na początku listy
        String nowyElement = "Nowy element";
        lista.add(0, nowyElement);

        // Wyświetlenie listy po dodaniu nowego elementu
        System.out.println("Lista po dodaniu nowego elementu:");
        for (String element : lista) {
            System.out.println(element);
        }
    }

}
