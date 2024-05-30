package org.enoch.snark.instance.model.uc;

import org.apache.commons.lang3.NotImplementedException;
import org.enoch.snark.db.dao.PlayerDAO;
import org.enoch.snark.db.entity.PlanetEntity;
import org.enoch.snark.db.entity.PlayerEntity;
import org.enoch.snark.gi.types.ShipEnum;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.model.to.Planet;

import static org.enoch.snark.instance.si.module.ConfigMap.TRANSPORTER_SMALL_CAPACITY;

public class ShipUC {

    public static Long calculateShipCountForTransport(ShipEnum shipEnum, Planet planet) {
        Long capacity = ShipUC.calculateCapacity(shipEnum);
        PlanetEntity planetEntity = PlanetUC.fetch(planet);
        return planetEntity.getResources().count() / capacity;
    }

    public static Long calculateCapacity(ShipEnum shipEnum) {
        Long configCapacity = Instance.getMainConfigMap().getConfigLong(TRANSPORTER_SMALL_CAPACITY, -1L);
        long amount;
        if(configCapacity == -1L) {
            Long hyperspaceTechnology = PlayerDAO.getInstance().fetch(PlayerEntity.mainPlayer()).hyperspaceTechnology;
            amount = 5000 + (250 * (hyperspaceTechnology +1));
        } else {
            amount = configCapacity;
        }
        switch (shipEnum) {
            case espionageProbe -> {
                return amount / 1000L;
            }
            case transporterSmall -> {
                return amount;
            }
            case transporterLarge -> {
                return amount * 5L;
            }
            default -> throw new NotImplementedException("For "+shipEnum+" missing calculateCapacity");
        }
    }
}
