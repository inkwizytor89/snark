package org.enoch.snark.instance.model.types;

public enum LifeFormType {
    HUMAN,
    ROCKTAL,
    MECHA,
    KAELESH;

    public boolean equalsTo(String lifeForm) {
        if (lifeForm == null) return false;
        return name().toLowerCase().equals(lifeForm.toLowerCase());
    }
}
