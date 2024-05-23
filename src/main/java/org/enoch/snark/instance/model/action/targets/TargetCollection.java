package org.enoch.snark.instance.model.action.targets;

import org.enoch.snark.common.DateUtil;
import org.enoch.snark.db.entity.TargetEntity;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class TargetCollection {

    private List<TargetEntity> targets = new ArrayList<>();
    private Deque<TargetEntity> spyTargets = new LinkedList<>();
    private Deque<TargetEntity> attackTargets = new LinkedList<>();

    public TargetCollection(List<TargetEntity> targets) {
        if(targets == null) return;
        this.targets = targets;
        setOnSpy(targets);
        setOnAttack(targets);
    }

    private void setOnSpy(List<TargetEntity> targets) {
        this.spyTargets = targets.stream()
                .filter(targetEntity -> DateUtil.isExpired2H(targetEntity.updated))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private void setOnAttack(List<TargetEntity> targets) {
        this.attackTargets = targets.stream()
                .filter(targetEntity -> DateUtil.isExpired2H(targetEntity.lastAttacked))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public TargetEntity pollSpy(){
        return spyTargets.poll();
    }

    public TargetEntity pollAttack(){
        return attackTargets.poll();
    }
}
