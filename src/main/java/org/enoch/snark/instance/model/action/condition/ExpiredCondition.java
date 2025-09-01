package org.enoch.snark.instance.model.action.condition;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.enoch.snark.common.DateUtil;
import org.enoch.snark.db.dao.TargetDAO;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.instance.model.action.promisecondition.AbstractPromiseCondition;
import org.enoch.snark.instance.model.to.FleetPromise;
import org.enoch.snark.instance.model.to.Planet;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

//@SuperBuilder
@Getter
public abstract class ExpiredCondition extends AbstractCondition {
    protected Long seconds;
    protected Boolean is;
    protected Planet planet;

}
