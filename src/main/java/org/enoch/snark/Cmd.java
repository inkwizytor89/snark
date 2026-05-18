package org.enoch.snark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cmd {

    /**
     * Przetwarza linię poleceń i wykonuje odpowiednią akcję
     * @param command linia poleceń do przetworzenia
     */
    public void execute(String command) {
        if (command == null || command.trim().isEmpty()) {
            System.out.println("Błąd: Pusta komenda");
            return;
        }

        String[] parts = command.trim().split("\\s+");

        try {
            switch (parts[0]) {
                case "help":
                    handleHelp();
                    break;
                case "module":
                    handleModule(parts);
                    break;
                case "thread":
                    handleThread(parts);
                    break;
                case "config":
                    handleConfig(parts);
                    break;
                case "queue":
                    handleQueue(parts);
                    break;
                default:
                    System.out.println("Błąd: Nieznana komenda '" + parts[0] + "'. Wpisz 'help' aby zobaczyć dostępne komendy.");
            }
        } catch (Exception e) {
            System.out.println("Błąd podczas przetwarzania komendy: " + e.getMessage());
        }
    }

    /**
     * Obsługuje komendę 'help'
     */
    private void handleHelp() {
        System.out.println("\n=== Dostępne komendy ===");
        System.out.println("help                           - Wyświetla tę pomoc");
        System.out.println("module show                    - Wyświetla listę wszystkich modułów");
        System.out.println("module <name> show             - Wyświetla informacje o konkretnym module");
        System.out.println("thread show                    - Wyświetla listę wszystkich wątków");
        System.out.println("thread <name> show             - Wyświetla informacje o konkretnym wątku");
        System.out.println("config add <param1=val1> ...   - Dodaje konfigurację z parametrami");
        System.out.println("queue show                     - Wyświetla zawartość kolejki");
        System.out.println("queue push <command.json>      - Dodaje komendę do kolejki");
        System.out.println("=============================\n");
    }

    /**
     * Obsługuje komendy 'module'
     */
    private void handleModule(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Błąd: Nieprawidłowa składnia. Użyj: module show lub module <name> show");
            return;
        }

        if ("show".equals(parts[1])) {
            // module show
            System.out.println("\n=== Lista modułów ===");
            System.out.println("Moduł: ActionModule");
            System.out.println("Moduł: AIModule");
            System.out.println("Moduł: DiscordModule");
            System.out.println("Moduł: DatabaseModule");
            System.out.println("====================\n");
        } else {
            // module <name> show
            if (parts.length >= 3 && "show".equals(parts[2])) {
                String moduleName = parts[1];
                System.out.println("\n=== Informacje o module ===");
                System.out.println("Nazwa: " + moduleName);
                System.out.println("Status: Aktywny");
                System.out.println("Wersja: 1.0");
                System.out.println("===========================\n");
            } else {
                System.out.println("Błąd: Nieprawidłowa składnia. Użyj: module <name> show");
            }
        }
    }

    /**
     * Obsługuje komendy 'thread'
     */
    private void handleThread(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Błąd: Nieprawidłowa składnia. Użyj: thread show lub thread <name> show");
            return;
        }

        if ("show".equals(parts[1])) {
            // thread show
            System.out.println("\n=== Lista wątków ===");
            System.out.println("Wątek: main");
            System.out.println("Wątek: executor-1");
            System.out.println("Wątek: scheduler-1");
            System.out.println("===================\n");
        } else {
            // thread <name> show
            if (parts.length >= 3 && "show".equals(parts[2])) {
                String threadName = parts[1];
                System.out.println("\n=== Informacje o wątku ===");
                System.out.println("Nazwa: " + threadName);
                System.out.println("ID: " + threadName.hashCode());
                System.out.println("Stan: RUNNABLE");
                System.out.println("===========================\n");
            } else {
                System.out.println("Błąd: Nieprawidłowa składnia. Użyj: thread <name> show");
            }
        }
    }

    /**
     * Obsługuje komendy 'config'
     */
    private void handleConfig(String[] parts) {
        if (parts.length < 2 || !"add".equals(parts[1])) {
            System.out.println("Błąd: Nieprawidłowa składnia. Użyj: config add <param1=val1> <param2=val2> ...");
            return;
        }

        if (parts.length < 3) {
            System.out.println("Błąd: Brak parametrów do dodania");
            return;
        }

        // Parsowanie parametrów w formacie key=value
        Map<String, String> config = new HashMap<>();
        for (int i = 2; i < parts.length; i++) {
            String param = parts[i];
            if (param.contains("=")) {
                String[] keyValue = param.split("=", 2);
                config.put(keyValue[0], keyValue[1]);
            } else {
                System.out.println("Ostrzeżenie: Parametr '" + param + "' ma nieprawidłowy format. Oczekiwano: klucz=wartość");
            }
        }

        System.out.println("\n=== Dodana konfiguracja ===");
        for (Map.Entry<String, String> entry : config.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
        System.out.println("============================\n");
    }

    /**
     * Obsługuje komendy 'queue'
     */
    private void handleQueue(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Błąd: Nieprawidłowa składnia. Użyj: queue show lub queue push <command.json>");
            return;
        }

        if ("show".equals(parts[1])) {
            // queue show
            System.out.println("\n=== Zawartość kolejki ===");
            System.out.println("Liczba elementów: 0");
            System.out.println("Kolejka jest pusta");
            System.out.println("======================\n");
        } else if ("push".equals(parts[1])) {
            // queue push <command.json>
            if (parts.length < 3) {
                System.out.println("Błąd: Brak nazwy pliku. Użyj: queue push <command.json>");
                return;
            }

            String commandFile = parts[2];
            System.out.println("\n=== Komenda dodana do kolejki ===");
            System.out.println("Plik: " + commandFile);
            System.out.println("Status: Oczekiwanie na przetworzenie");
            System.out.println("=================================\n");
        } else {
            System.out.println("Błąd: Nieznana operacja na kolejce. Użyj: queue show lub queue push <command.json>");
        }
    }
}
