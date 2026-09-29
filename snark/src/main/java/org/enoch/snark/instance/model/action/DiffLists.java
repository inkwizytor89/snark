package org.enoch.snark.instance.model.action;

import java.util.ArrayList;
import java.util.List;

public class DiffLists<V> {

    private final List<V> oldList;
    private final List<V> newList;

    public DiffLists(List<V> oldList, List<V> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    public List<V> added() {
        List<V> addedElements = new ArrayList<>(newList);
        addedElements.removeAll(oldList);
        return addedElements;
    }

    public List<V> removed() {
        List<V> removedElements = new ArrayList<>(oldList);
        removedElements.removeAll(newList);
        return removedElements;
    }
}
