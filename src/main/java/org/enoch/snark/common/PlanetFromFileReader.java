package org.enoch.snark.common;

import org.enoch.snark.model.Planet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlanetFromFileReader {

    public static List<Planet> get(String pathtoFile){
        try (Stream<String> stream = Files.lines(Paths.get(pathtoFile))) {

            return stream
                    .map(Planet::new)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
