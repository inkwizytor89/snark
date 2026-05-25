package org.enoch.snark;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Cmd {

    /**
     * Przetwarza linię poleceń i wykonuje odpowiednią akcję
     * @param command linia poleceń do przetworzenia
     */
    public String execute(String command) {
        if (command == null || command.trim().isEmpty())  return "Empty command";

        String[] parts = command.trim().split("\\s+");

        try {
           return  switch (parts[0]) {
                case "help":
                    handleHelp();
                case "module":
                    handleModule(parts);
                case "thread":
                    handleThread(parts);
                case "config":
                    handleConfig(parts);
                case "queue":
                   handleQueue(parts);
                default:
                    yield "Błąd: Nieznana komenda '" + parts[0] + "'. Wpisz 'help' aby zobaczyć dostępne komendy.";
            };
        } catch (Exception e) {
            return "Błąd podczas przetwarzania komendy: " + e.getMessage();
        }
    }

    /**
     * Obsługuje komendę 'help'
     */
    private String handleHelp() {
        StringBuilder message = new StringBuilder();
        message.append("\n=== Dostępne komendy ===");
        message.append("help                           - Wyświetla tę pomoc");
        message.append("module show                    - Wyświetla listę wszystkich modułów");
        message.append("module <name> show             - Wyświetla informacje o konkretnym module");
        message.append("thread show                    - Wyświetla listę wszystkich wątków");
        message.append("thread <name> show             - Wyświetla informacje o konkretnym wątku");
        message.append("config add <param1=val1> ...   - Dodaje konfigurację z parametrami");
        message.append("queue show                     - Wyświetla zawartość kolejki");
        message.append("queue push <command.json>      - Dodaje komendę do kolejki");
        message.append("=============================\n");
        return message.toString();
    }

    /**
     * Obsługuje komendy 'module'
     */
    private String handleModule(String[] parts) {
        if (parts.length < 2) {
            return "Błąd: Nieprawidłowa składnia. Użyj: module show lub module <name> show";
        }

        StringBuilder message = new StringBuilder();
        if ("show".equals(parts[1])) {
            // module show
            message.append("\n=== Lista modułów ===");
            message.append("Moduł: ActionModule");
            message.append("Moduł: AIModule");
            message.append("Moduł: DiscordModule");
            message.append("Moduł: DatabaseModule");
            message.append("====================\n");
        } else {
            // module <name> show
            if (parts.length >= 3 && "show".equals(parts[2])) {
                String moduleName = parts[1];
                message.append("\n=== Informacje o module ===");
                message.append("Nazwa: ").append(moduleName);
                message.append("Status: Aktywny");
                message.append("Wersja: 1.0");
                message.append("===========================\n");
            } else {
                message.append("Błąd: Nieprawidłowa składnia. Użyj: module <name> show");
            }
        }
        return message.toString();
    }

    /**
     * Obsługuje komendy 'thread'
     */
    private String handleThread(String[] parts) {
        if (parts.length < 2) {
            return "Błąd: Nieprawidłowa składnia. Użyj: thread show lub thread <name> show";
        }
        StringBuilder message = new StringBuilder();
        if ("show".equals(parts[1])) {
            // thread show
            message.append("\n=== Lista wątków ===");
            message.append("Wątek: main");
            message.append("Wątek: executor-1");
            message.append("Wątek: scheduler-1");
            message.append("===================\n");
        } else {
            // thread <name> show
            if (parts.length >= 3 && "show".equals(parts[2])) {
                String threadName = parts[1];
                message.append("\n=== Informacje o wątku ===");
                message.append("Nazwa: " + threadName);
                message.append("ID: " + threadName.hashCode());
                message.append("Stan: RUNNABLE");
                message.append("===========================\n");
            } else {
                message.append("Błąd: Nieprawidłowa składnia. Użyj: thread <name> show");
            }
        }
        return message.toString();
    }

    /**
     * Obsługuje komendy 'config'
     */
    private String handleConfig(String[] parts) {
        if (parts.length < 2 || !"add".equals(parts[1])) {
            return "Błąd: Nieprawidłowa składnia. Użyj: config add <param1=val1> <param2=val2> ...";
        }

        if (parts.length < 3) {
            return "Błąd: Brak parametrów do dodania";
        }

        // Parsowanie parametrów w formacie key=value
        Map<String, String> config = new HashMap<>();
        for (int i = 2; i < parts.length; i++) {
            String param = parts[i];
            if (param.contains("=")) {
                String[] keyValue = param.split("=", 2);
                config.put(keyValue[0], keyValue[1]);
            } else {
                return "Ostrzeżenie: Parametr '" + param + "' ma nieprawidłowy format. Oczekiwano: klucz=wartość";
            }
        }

        StringBuilder message = new StringBuilder();
        message.append("\n=== Dodana konfiguracja ===");
        for (Map.Entry<String, String> entry : config.entrySet()) {
            message.append(entry.getKey()).append(" = ").append(entry.getValue());
        }
        message.append("============================\n");
        return message.toString();
    }

    /**
     * Obsługuje komendy 'queue'
     */
    private String handleQueue(String[] parts) {
        if (parts.length < 2) {
            return "Błąd: Nieprawidłowa składnia. Użyj: queue show lub queue push <command.json>";
        }

        StringBuilder message = new StringBuilder();
        if ("show".equals(parts[1])) {
            // queue show
            message.append("\n=== Zawartość kolejki ===");
            message.append("Liczba elementów: 0");
            message.append("Kolejka jest pusta");
            message.append("======================\n");
        } else if ("push".equals(parts[1])) {
            // queue push <command.json>
            if (parts.length < 3) {
                return "Błąd: Brak nazwy pliku. Użyj: queue push <command.json>";
            }

            String commandFile = parts[2];
            message.append("\n=== Komenda dodana do kolejki ===");
            message.append("Plik: " + commandFile);
            message.append("Status: Oczekiwanie na przetworzenie");
            message.append("=================================\n");
        } else {
            return "Błąd: Nieznana operacja na kolejce. Użyj: queue show lub queue push <command.json>";
        }
        return message.toString();
    }
}
