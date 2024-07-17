package org.enoch.snark.db.dao;

import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.JPAUtility;
import org.enoch.snark.instance.service.PlanetCache;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.types.ColonyType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.enoch.snark.instance.model.action.PlanetExpression.MOON;
import static org.enoch.snark.instance.model.action.PlanetExpression.PLANET;

public class ColonyDAO extends AbstractDAO<ColonyEntity> {

    private static ColonyDAO INSTANCE;

    private ColonyDAO() {
        super();
    }

    public static ColonyDAO getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new ColonyDAO();
        }
        return INSTANCE;
    }

    @Override
    protected Class<ColonyEntity> getEntityClass() {
        return ColonyEntity.class;
    }

    public ColonyEntity find(String code) {
        return find(Planet.parse(code));
    }
    public ColonyEntity find(Planet planet) {
        if(planet == null) return null;
        synchronized (JPAUtility.dbSynchro) {
            List<ColonyEntity> resultList = entityManager.createQuery("" +
                    "from ColonyEntity " +
                    "where galaxy = :galaxy and " +
                    "system = :system and " +
                    "position = :position and " +
                    "type = :type", ColonyEntity.class)
                    .setParameter("galaxy", planet.galaxy)
                    .setParameter("system", planet.system)
                    .setParameter("position", planet.position)
                    .setParameter("type", planet.type)
                    .getResultList();
            if(resultList.isEmpty()) {
//                System.err.println("Error: ColonyDAO.get missing colony "+planet);
                return null;
            }
            return resultList.get(0);
        }
    }
    public ColonyEntity find(Integer cp) {
        synchronized (JPAUtility.dbSynchro) {
            List<ColonyEntity> resultList = entityManager.createQuery("" +
                    "from ColonyEntity " +
                    "where cp = :cp", ColonyEntity.class)
                    .setParameter("cp", cp)
                    .getResultList();
            if(resultList.isEmpty()) {
                return null;
            }
            return resultList.get(0);
        }
    }

    public ColonyEntity getOldestUpdated() {
        synchronized (JPAUtility.dbSynchro) {
            List<ColonyEntity> resultList = entityManager.createQuery(
                    "from ColonyEntity " +
                            "order by updated asc", ColonyEntity.class)
                    .getResultList();
            return resultList.stream().findFirst().get();
        }
    }

    public List<ColonyEntity> getColonies(String code) {
        String lowerCode = code.toLowerCase();
        List<ColonyEntity> colonies;
        if(lowerCode.contains(MOON)) {
            colonies = fetchAll()
                    .stream()
                    .filter(colonyEntity -> !colonyEntity.is(ColonyType.PLANET))
                    .sorted(Comparator.comparing(o -> -o.galaxy))
                    .collect(Collectors.toList());
        } else if(lowerCode.contains(PLANET)) {
            colonies = fetchAll()
                    .stream()
                    .filter(colonyEntity -> colonyEntity.is(ColonyType.PLANET))
                    .sorted(Comparator.comparing(o -> -o.galaxy))
                    .collect(Collectors.toList());
        } else if(lowerCode.equals(StringUtils.EMPTY)) {
            colonies = new ArrayList<>();
            List<ColonyEntity> planetList = fetchAll()
                    .stream()
                    .filter(colonyEntity -> colonyEntity.is(ColonyType.PLANET))
                    .sorted(Comparator.comparing(o -> -o.galaxy))
                    .collect(Collectors.toList());
            for (ColonyEntity planet : planetList) {
                ColonyEntity colony = planet;
                if (planet.cpm != null) {
                    colony = find(planet.cpm);
                }
                colonies.add(colony);
            }
        } else {
            colonies = Planet.fromString(lowerCode).stream()
                    .map(this::find)
                    .collect(Collectors.toList());
        }
        PlanetCache.put(code, colonies);
        return colonies;
    }
}
