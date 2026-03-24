# SpelPlanetService - Spring Expression Language Implementation

## Przegląd

`SpelPlanetService` jest alternatywną implementacją parsowania wyrażeń dla danych planet przy użyciu **Spring Expression Language (SpEL)**. 

W stosunku do oryginału `PlanetService`:
- Wyrażenia piszemy jako stringi SpEL
- Wbudowana obsługa zmiennych i funkcji
- Łatwa rozszerzalność o nowe funkcje
- Type-safe evaluation

## Zmienne (Wartości)

Następujące zmienne są dostępne w kontekście SpEL:

| Zmienna | Typ | Opis |
|---------|-----|------|
| `ALL` | `List<PlanetData>` | Wszystkie planety i księżyce |
| `PLANETS` | `List<PlanetData>` | Tylko planety |
| `MOONS` | `List<PlanetData>` | Tylko księżyce |
| `EACH_POSITION` | `List<PlanetData>` | Planety na każdej pozycji |
| `NONE` | `List<PlanetData>` | Pusta lista |

### Dostęp do zmiennych

W wyrażeniach SpEL zmienne poprzedzamy `$`:

```java
// Dostęp do zmiennej ALL
spelPlanetService.evaluate("ALL")

// Dostęp do zmiennej PLANETS
spelPlanetService.evaluate("PLANETS")

// Pobieranie elementu z listy
spelPlanetService.evaluate("PLANETS.get(0)")
```

## Funkcje

Dostępne funkcje działające na `List<PlanetData>`:

### next(List<PlanetData>)
Zwraca następną planetę z listy.

```java
// Następna planeta z listy PLANETS
spelPlanetService.evaluate("#next(PLANETS)")

// Następna planeta spośród MOONS
spelPlanetService.evaluate("#next(MOONS)")
```

### prev(List<PlanetData>)
Zwraca poprzednią planetę z listy.

```java
// Poprzednia planeta z listy PLANETS
spelPlanetService.evaluate("#prev(PLANETS)")

// Poprzednia planeta spośród MOONS
spelPlanetService.evaluate("#prev(MOONS)")
```

### swap(PlanetData)
Zwraca planetę z zamieniony typem (planeta ↔ księżyc).

```java
// Zamień typ pierwszej planety z PLANETS
spelPlanetService.evaluate("#swap(PLANETS.get(0))")

// Zamień typ każdej planety (wymagane zaawansowane wyrażenie)
spelPlanetService.evaluate("PLANETS.stream().map(T(org.enoch.snark.instance.service.SpelPlanetFunctions).swap(_)).toList()")
```

## Przykłady Wyrażeń

### Proste wyrażenia zmiennych

```java
// Zwróć wszystkie planety i księżyce
spelPlanetService.evaluate("ALL")

// Zwróć tylko planety
spelPlanetService.evaluate("PLANETS")

// Zwróć pustą listę
spelPlanetService.evaluate("NONE")
```

### Wyrażenia z funkcjami

```java
// Następna planeta
spelPlanetService.evaluate("#next(PLANETS)")

// Poprzednia planeta spośród księżyców
spelPlanetService.evaluate("#prev(MOONS)")

// Zamień typ pierwszej planety
spelPlanetService.evaluate("#swap(PLANETS.get(0))")
```

### Złożone wyrażenia

```java
// Pierwszy element ze wszystkich planet
spelPlanetService.evaluate("PLANETS.get(0)")

// Rozmiar listy planet
spelPlanetService.evaluate("PLANETS.size()")

// Filtrowanie (wymaga dodatkowych metod)
spelPlanetService.evaluate("PLANETS.stream().filter(_).toList()")
```

## Implementacja

### Klasa SpelPlanetService
- Tworzy parser SpEL (`SpelExpressionParser`)
- Rejestruje zmienne i funkcje w `EvaluationContext`
- Parsuje i ewaluuje wyrażenia

### Klasa SpelPlanetFunctions
Statyczne metody dostępne jako funkcje SpEL:
- `next(List<PlanetData>)` - zwraca następną planetę
- `prev(List<PlanetData>)` - zwraca poprzednią planetę
- `swap(PlanetData)` - zwraca planetę z zamiennym typem

## Rozszerzanie Funkcjonalności

Aby dodać nową funkcję:

1. **Dodaj metodę** w `SpelPlanetFunctions`:
```java
public static List<PlanetData> filterByGalaxy(List<PlanetData> planets, int galaxy) {
    return planets.stream()
        .filter(p -> p.getPlanet().galaxy == galaxy)
        .toList();
}
```

2. **Zarejestruj funkcję** w `SpelPlanetService.createEvaluationContext()`:
```java
Method filterByGalaxyMethod = SpelPlanetFunctions.class
    .getDeclaredMethod("filterByGalaxy", List.class, int.class);
context.registerFunction("filterByGalaxy", filterByGalaxyMethod);
```

3. **Użyj w wyrażeniu**:
```java
spelPlanetService.evaluate("#filterByGalaxy(ALL, 1)")
```

## Obsługa Błędów

Następujące sytuacje rzucają wyjątki:

- **Wyrażenie zwraca niewłaściwy typ**: `IllegalArgumentException`
- **Błąd parsowania SpEL**: `IllegalStateException`
- **Brak funkcji**: `NoSuchMethodException` (podczas rejestracji)

```java
try {
    List<PlanetData> result = spelPlanetService.evaluate("INVALID_EXPR");
} catch (IllegalStateException e) {
    // Obsłuż błąd parsowania
    System.err.println("Błąd wyrażenia: " + e.getMessage());
}
```

## TODO - Implementacja Pól

Poniższe metody wymagają implementacji w oparciu o Twoje repozytoria:

- `getPlanetsList()` - pobranie listy planet
- `getMoonsList()` - pobranie listy księżyców
- `getEachPositionList()` - pobranie planet na każdej pozycji
- `SpelPlanetFunctions.swap()` - implementacja zamiany typu planety

## Porównanie z oryginału PlanetService

| Aspekt | PlanetService | SpelPlanetService |
|--------|--------------|-------------------|
| Parsowanie | Ręczne parsowanie stringa | SpEL Parser |
| Wyrażenia | Specjalny format (`;`, `-`) | Standardowy SpEL |
| Funkcje | Hardcoded w metodach | Rejestrowane funkcje |
| Rozszerzalność | Wymagane zmiany kodu | Dodaj metodę + rejestrację |
| Typy wyrażeń | Ograniczone | Dowolne SpEL |

## Integracja

Aby użyć `SpelPlanetService` zamiast `PlanetService`:

```java
@Autowired
private SpelPlanetService spelPlanetService;

public void process() {
    List<PlanetData> targets = spelPlanetService.evaluate("#next(PLANETS)");
    // ... przetwarzaj rezultat
}
```

Lub jako alternatywa:

```java
@Autowired
private PlanetService planetService;  // Oryginalna implementacja

@Autowired
private SpelPlanetService spelPlanetService;  // Nowa implementacja

// Wybierz odpowiednią implementację w runtime
```

