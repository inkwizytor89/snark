package org.enoch.snark.expression.function.technology;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.PlayerEntity;
import org.enoch.snark.db.repository.CacheEntryRepository;
import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.db.repository.PlayerRepository;
import org.enoch.snark.db.repository.TargetRepository;
import org.enoch.snark.expression.ExpressionCache;
import org.enoch.snark.expression.ExpressionService;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.service.PlanetDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TechnologyFunctionsTest {

    private ExpressionService expressionService;
    private ColonyRepository colonyRepository;
    private PlayerRepository playerRepository;

    @BeforeEach
    void setUp() {
        CacheEntryRepository cacheEntryRepository = mock(CacheEntryRepository.class);
        colonyRepository = mock(ColonyRepository.class);
        playerRepository = mock(PlayerRepository.class);
        when(cacheEntryRepository.findAll()).thenReturn(List.of());
        expressionService = new ExpressionService(
                new ExpressionCache(),
                cacheEntryRepository,
                colonyRepository,
                playerRepository,
                mock(TargetRepository.class),
                mock(PlanetDataService.class)
        );
    }

    @Test
    void evaluatesStrictComparisonsForResearchWithoutCoordinate() {
        PlayerEntity player = new PlayerEntity();
        player.energyTechnology = 10L;
        when(playerRepository.mainPlayer()).thenReturn(player);

        assertTrue(evaluateBoolean("#hasTechnologyGreaterThan({name: 'energyTechnology', level: 9})"));
        assertFalse(evaluateBoolean("#hasTechnologyGreaterThan({name: 'energyTechnology', level: 10})"));
        assertTrue(evaluateBoolean("#hasTechnologyLessThan({name: 'energyTechnology', level: 11})"));
        assertFalse(evaluateBoolean("#hasTechnologyLessThan({name: 'energyTechnology', level: 10})"));
        verify(playerRepository, times(4)).mainPlayer();
    }

    @Test
    void evaluatesPlanetScopedTechnologyAtCoordinate() {
        ColonyEntity colony = new ColonyEntity();
        colony.metalMine = 20L;
        when(colonyRepository.byPlanet(Planet.parse("p[1:2:3]"))).thenReturn(colony);

        assertTrue(evaluateBoolean(
                "#hasTechnologyGreaterThan({name: 'metalMine', level: 19, coordinate: 'p[1:2:3]'})"));
        assertTrue(evaluateBoolean(
                "#hasTechnologyLessThan({name: 'metalMine', level: 21, coordinate: 'p[1:2:3]'})"));
    }

    @Test
    void usesMoonCoordinateAndSupportsLifeformBuildings() {
        ColonyEntity moon = new ColonyEntity();
        moon.lifeformTech11101 = 4L;
        when(colonyRepository.byPlanet(Planet.parse("m[1:2:3]"))).thenReturn(moon);

        assertTrue(evaluateBoolean(
                "#hasTechnologyGreaterThan({name: 'lifeformTech11101', level: 3, coordinate: 'm[1:2:3]'})"));
        verify(colonyRepository).byPlanet(Planet.parse("m[1:2:3]"));
    }

    @Test
    void treatsNullStoredTechnologyLevelsAsZero() {
        PlayerEntity player = new PlayerEntity();
        when(playerRepository.mainPlayer()).thenReturn(player);
        ColonyEntity colony = new ColonyEntity();
        when(colonyRepository.byPlanet(Planet.parse("p[1:2:3]"))).thenReturn(colony);

        assertTrue(evaluateBoolean("#hasTechnologyLessThan({name: 'energyTechnology', level: 1})"));
        assertTrue(evaluateBoolean(
                "#hasTechnologyLessThan({name: 'metalMine', level: 1, coordinate: 'p[1:2:3]'})"));
    }

    @Test
    void rejectsMissingAndInvalidTechnologyParameters() {
        assertThrows(IllegalArgumentException.class,
                () -> TechnologyFunctions.hasTechnologyGreaterThan(Map.of("level", 1)));
        assertThrows(IllegalArgumentException.class,
                () -> TechnologyFunctions.hasTechnologyLessThan(Map.of("name", "metalMine")));
        assertThrows(IllegalArgumentException.class,
                () -> TechnologyFunctions.hasTechnologyGreaterThan(Map.of("name", "metalMine", "level", 1)));
        assertThrows(IllegalArgumentException.class,
                () -> TechnologyFunctions.hasTechnologyLessThan(Map.of("name", "notATechnology", "level", 1)));
        assertThrows(IllegalArgumentException.class,
                () -> TechnologyFunctions.hasTechnologyGreaterThan(
                        Map.of("name", "energyTechnology", "level", "invalid")));
        assertThrows(IllegalArgumentException.class,
                () -> TechnologyFunctions.hasTechnologyLessThan(
                        Map.of("name", "metalMine", "level", 1)));
    }

    private boolean evaluateBoolean(String expression) {
        return expressionService.evaluateNoCached(expression, Map.of(), Boolean.class);
    }
}
