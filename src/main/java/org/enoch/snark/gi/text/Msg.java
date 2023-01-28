package org.enoch.snark.gi.text;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Msg {
    public static Map<String, String[]> msg = new HashMap<>();

    public static final String BAZINGA_EN = "bazinga_en";

    public static final String BAZINGA_PL = "bazinga_pl";

    static {
        msg.put(BAZINGA_PL, new String[]{":)"});//"Bazinga!", "a kuku", "lepiej to zawrocic", "ping", "jestem", "szkoda czasu", "chyba nie dzisiaj", "elegancko"});
        msg.put(BAZINGA_EN, new String[]{":)"});//"Bazinga!", "I am, you can turn it back", "I am", "Not today"});
    }

    public static String get(String key) {
        String[] posibility = msg.get(key);
        return posibility[new Random().nextInt(posibility.length)];
    }
}
