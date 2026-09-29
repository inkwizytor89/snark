package org.enoch.snark.instance.model.uc;

import org.enoch.snark.instance.model.to.SystemView;

import java.util.ArrayList;
import java.util.List;

public class SystemUC {

    public static List<SystemView> range(int galaxy, int from, int to, int systemMax, boolean wrap) {
        ArrayList<SystemView> result = new ArrayList<>();
        if (from > to) {
            result.addAll(range(galaxy, from, systemMax, systemMax, wrap));
            result.addAll(range(galaxy, 1, to, systemMax, wrap));
            return result;
        }

        for (int i = from; i <= to; i++) {
            int system = i;
            if (wrap) {
                system = systemNormalize(i, systemMax);
            } else {
                if (i < 1 || i > systemMax) continue;
            }
            result.add(new SystemView(galaxy, system));
        }
        return result;
    }

    public static int systemNormalize(int x, int systemMax) {
            return (((x - 1) % systemMax + systemMax) % systemMax) + 1;
    }
}
