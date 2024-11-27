package org.enoch.snark.instance.model.action.find;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.PlanetEntity;
import org.enoch.snark.instance.model.to.Planet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomFinder {
    public static List<Planet> find(String query, ColonyEntity colonyEntity) {

        List<QueryParam> params = extractQueryParams(query);
        if(params.isEmpty()) return null;


        return null;
    }

    private static List<QueryParam> extractQueryParams(String query) {
        List<String> terms = Arrays.asList(query.split("_"));
        return terms.stream().map(QueryParam::parse).toList();
    }
}
