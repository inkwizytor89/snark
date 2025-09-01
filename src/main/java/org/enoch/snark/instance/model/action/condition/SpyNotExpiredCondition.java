package org.enoch.snark.instance.model.action.condition;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.instance.model.action.promisecondition.ExpiredPromiseCondition;
import org.enoch.snark.instance.model.to.Planet;

import java.time.LocalDateTime;

//@SuperBuilder
@Getter
public class SpyNotExpiredCondition extends ExpiredCondition {
}
