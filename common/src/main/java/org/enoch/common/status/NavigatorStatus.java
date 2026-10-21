package org.enoch.common.status;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NavigatorStatus {

    private String fleetStatus;
    private LocalDateTime lastUpdate;
}
