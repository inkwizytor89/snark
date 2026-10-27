package org.enoch.common.status;

import java.util.List;

/**
 * One bounded page of colony data returned for an on-demand status details request.
 */
public record StatusDetails(
        int offset,
        int limit,
        int total,
        int planetCount,
        int moonCount,
        List<ColonyStatus> colonies
) {
}
