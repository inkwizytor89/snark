package org.enoch.snark.instance.model.uc;

import org.apache.commons.lang3.NotImplementedException;
import org.enoch.snark.db.dao.PlayerDAO;
import org.enoch.snark.db.entity.PlayerEntity;
import org.enoch.snark.instance.model.technology.Ship;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.model.to.FleetPromise;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.model.to.ShipsMap;

import static org.enoch.snark.instance.model.to.ShipsMap.ALL_SHIPS;
import static org.enoch.snark.instance.model.to.ShipsMap.TRANSPORT_COUNT;
import static org.enoch.snark.instance.si.module.ConfigMap.TRANSPORTER_SMALL_CAPACITY;

public class ShipUC {

    public static ShipsMap fromExpressionToValues(ShipsMap requestedShips, FleetPromise promise) {
        ShipsMap valuedMap = changeExpressionCountsToLong(requestedShips, promise.getTarget());
        ShipsMap sourceShipsMap = promise.getSource().getShipsMap();
        ShipsMap maxToSend = sourceShipsMap.leave(promise.getLeaveShipsMap());

        if(ALL_SHIPS.equals(requestedShips)) valuedMap = maxToSend;
        return valuedMap;
    }

    private static ShipsMap changeExpressionCountsToLong(ShipsMap shipsMap, Planet target) {
        ShipsMap result = new ShipsMap();

        if(shipsMap != null) shipsMap.forEach((key, value) -> {
            if (TRANSPORT_COUNT.equals(value))
                result.put(key, calculateShipCountForTransport(key, target));
            else result.put(key, value);
        });
        return result;
    }

    public static Long calculateShipCountForTransport(Ship ship, Planet planet) {
        return calculateShipCountForTransport(ship, PlanetUC.fetch(planet).getResources());
    }

    public static Long calculateShipCountForTransport(Ship ship, Resources resources) {
        Long capacity = ShipUC.calculateCapacity(ship);
        return resources.count() / capacity;
    }

    public static Long calculateCapacity(Ship ship) {
        Long configCapacity = Instance.getGlobalMainConfigMap().getConfigLong(TRANSPORTER_SMALL_CAPACITY, -1L);
        long amount;
        if(configCapacity == -1L) {
            Long hyperspaceTechnology = PlayerDAO.getInstance().fetch(PlayerEntity.mainPlayer()).hyperspaceTechnology;
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
