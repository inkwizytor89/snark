package org.enoch.common.status;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColonyStatus {

    private String coordinate;
    private Long metal;
    private Long crystal;
    private Long deuterium;
    private Long energy;
    private Map<String, Long> ships;
}
