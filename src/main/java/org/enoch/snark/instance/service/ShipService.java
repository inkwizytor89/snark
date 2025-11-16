package org.enoch.snark.instance.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.enoch.snark.action.command.SendCommand;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.PlanetEntity;
import org.enoch.snark.db.repository.CacheEntryRepository;
import org.enoch.snark.db.repository.PlayerRepository;
import org.enoch.snark.instance.model.technology.Ship;
import org.enoch.snark.instance.model.to.FleetContext;
import org.enoch.snark.instance.model.to.FleetPromise;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.model.to.ShipsMap;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.enoch.snark.instance.model.to.ShipsMap.*;
import static org.enoch.snark.instance.si.module.ThreadMap.TRANSPORTER_SMALL_CAPACITY;

@RequiredArgsConstructor
@Component
@Scope("prototype")
public class ShipService {

    private final CacheEntryRepository cacheEntryRepository;
    private final PlayerRepository playerRepository;

    public ShipsMap fromExpressionToValues(SendCommand command) {
        return fromExpressionToValues(command.getShipsMap(), command.getSource(), command.getLeaveShipsMap());
    }


    @Deprecated
    public ShipsMap fromExpressionToValues(ShipsMap requestedShips, FleetPromise promise) {
        throw new NotImplementedException(" to remove");
//        ShipsMap valuedMap = changeExpressionCountsToLong(requestedShips, Planet.fromString(promise.getTarget()).getFirst());
//        ShipsMap sourceShipsMap = promise.getSource().getShipsMap();
//        ShipsMap maxToSend = sourceShipsMap.leave(promise.getLeaveShipsMap());
//
//        if(ALL_SHIPS.equals(requestedShips)) valuedMap = maxToSend;
//        return valuedMap;
    }
    @Deprecated
    public ShipsMap fromExpressionToValues(ShipsMap requestedShips, PlanetEntity source, ShipsMap leaveShipsMap) {
        ShipsMap valuedMap = changeExpressionCountsToLong(requestedShips, source);
        ShipsMap sourceShipsMap = source.getShipsMap();
        ShipsMap maxToSend = sourceShipsMap.leave(leaveShipsMap);

        valuedMap = valuedMap.reduce(maxToSend);
        if(ALL_SHIPS.equals(requestedShips)) valuedMap = maxToSend;
        return valuedMap;
    }

    @Deprecated
    private ShipsMap changeExpressionCountsToLong(ShipsMap shipsMap, PlanetEntity planet) {
        ShipsMap result = new ShipsMap();

        if(shipsMap != null) shipsMap.forEach((key, value) -> {
            if (TRANSPORT_COUNT.equals(value))
                result.put(key, calculateShipCountForTransport(key, planet.getResources()));
            else if (ATTACK_COUNT.equals(value))
//                target tutaj
                result.put(key, calculateShipCountForTransport(key, planet.getResources()));
            else result.put(key, value);
        });
        return result;
    }

    public ShipsMap fromExpressionToValues(ShipsMap requestedShips, FleetContext context) {
        ColonyEntity source = context.getSource().getColony();

        ShipsMap valuedMap = changeExpressionCountsToLong(requestedShips, source);
        ShipsMap sourceShipsMap = source.getShipsMap();
        ShipsMap maxToSend = sourceShipsMap.leave(context.getLeaveShipsMap());

        valuedMap = valuedMap.reduce(maxToSend);
        if(ALL_SHIPS.equals(requestedShips)) valuedMap = maxToSend;
        return valuedMap;
    }

//    private ShipsMap changeExpressionCountsToLong(ShipsMap shipsMap, FleetContext context) {
//        ShipsMap result = new ShipsMap();
//
//        if(shipsMap != null) shipsMap.forEach((key, value) -> {
//            if (TRANSPORT_COUNT.equals(value)) {
//                PlanetEntity source = context.getSource().planetData();
//                result.put(key, calculateShipCountForTransport(key, source.getResources()));
//            } else if (ATTACK_COUNT.equals(value))
//                PlanetEntity source = context.getSource().planetData();
//                result.put(key, calculateShipCountForTransport(key, planet.getResources()));
//            else result.put(key, value);
//        });
//        return result;
//    }

    public Long calculateShipCountForTransport(Ship ship, Resources resources) {
        Long capacity = calculateCapacity(ship);
        return (long) Math.ceil((double) resources.count() / capacity);
    }

    private Long calculateCapacity(Ship ship) {
        long configCapacity = Long.parseLong(cacheEntryRepository.getValue(TRANSPORTER_SMALL_CAPACITY, "-1"));
        long amount;
        if(configCapacity == -1L) {
            Long hyperspaceTechnology = playerRepository.mainPlayer().hyperspaceTechnology;
            amount = 5000 + (250 * (hyperspaceTechnology +1));
        } else {
            amount = configCapacity;
        }
        switch (ship) {
            case espionageProbe -> {
                return amount / 1000L;
            }
            case transporterSmall -> {
                return amount;
            }
            case transporterLarge -> {
                return amount * 5L;
            }
            default -> throw new NotImplementedException("For "+ ship +" missing calculateCapacity");
        }
    }
}
