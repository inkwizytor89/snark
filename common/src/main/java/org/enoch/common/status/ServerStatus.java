package org.enoch.common.status;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerStatus {

    private NavigatorStatus navigatorStatus;
    private List<String> activeThreads;
    private String commandDeque;
    private String notifications;
    private CoordinatesStatus coordinates;
}
