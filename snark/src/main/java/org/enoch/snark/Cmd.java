package org.enoch.snark;

import lombok.RequiredArgsConstructor;
import org.enoch.snark.config.ConfigTerm;
import org.enoch.snark.instance.si.RemotePropertiesMap;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class Cmd {

    private final RemotePropertiesMap propertiesMap;
    private Map<String,String> shortcuts =  new HashMap<>() {{
        put("on", "config add time=on");
        put("off", "config add time=off");
    }};

    /**
     * Przetwarza linię poleceń i wykonuje odpowiednią akcję
     * @param command linia poleceń do przetworzenia
     */
    public String execute(String command) {
        if (command == null || command.trim().isEmpty())  return "Empty command";

        // obsługa skrótów: jeśli cała linia to skrót (np. "on"/"off"), zastąp go pełną komendą
        String trimmed = command.trim();
        if (shortcuts.containsKey(trimmed)) {
            String mapped = shortcuts.get(trimmed);
//            if("on".equals(trimmed))
//            if("off".equals(trimmed))
                command = mapped;
        }

        String[] parts = command.trim().split("\\s+");
        String result;
        try {
            String cmd = parts[0];
            if ("help".equals(cmd)) {
                result = handleHelp();
            } else if ("module".equals(cmd)) {
                result = handleModule(parts);
            } else if ("thread".equals(cmd)) {
                result = handleThread(parts);
            } else if ("config".equals(cmd)) {
                result = handleConfig(parts);
            } else if ("queue".equals(cmd)) {
                result = handleQueue(parts);
            } else {
                result = "Błąd: Nieznana komenda '" + parts[0] + "'. Wpisz 'help' aby zobaczyć dostępne komendy.";
            }
        } catch (Exception e) {
            result = "Błąd podczas przetwarzania komendy: " + e.getMessage();
        }

        // Jeśli oryginalnie wpisany skrót (trimmed) był "on" lub "off", doklej odpowiedni suffix
        if ("on".equals(trimmed)) {
            result = result + "\nhttps://tenor.com/view/star-wars-r2d2-beeps-provocatively-annoying-gif-4059356";
        } else if ("off".equals(trimmed)) {
            result = result + "\nhttps://tenor.com/view/star-wars-r2d2-fall-tired-exhausted-gif-4778748";
        }

        return result;
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
     * Wspierane formy:
     * - config add <value>...     -> wypisuje przekazane wartości (połączone spacją)
     * - config reset              -> zwraca "reset"
     * - config show               -> zwraca "all"
     * - config show <value>...    -> zwraca "show <value...>"
     */
    private String handleConfig(String[] parts) {
        if (parts.length < 2) {
            return "Błąd: Nieprawidłowa składnia. Użyj: config add <value> | config reset | config show [value]";
        }

        String op = parts[1];
        switch (op) {
            case "add":
                if (parts.length < 3) {
                    return "Błąd: Brak wartości do dodania. Użyj: config add <value>";
                }
                ConfigTerm config = ConfigTerm.parse(parts[2]);
                propertiesMap.addRemoteConfig(config);
                return "added config "+config;
            case "reset":
                propertiesMap.resetRemoteConfig();
                return "reset";
            case "show":
                if (parts.length == 2) {
                    String all = propertiesMap.showConfig("all");
                    return all;
                } else {
                    String configs = propertiesMap.showConfig(parts[2]);
                    return configs;
                }
            default:
                return "Błąd: Nieznana operacja dla config. Użyj: add | reset | show";
        }
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
