package org.enoch.common.status;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoordinatesStatus {

    private int planetCount;
    private int moonCount;
    private List<ColonyStatus> colonies;
}
