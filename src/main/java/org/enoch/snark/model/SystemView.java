package org.enoch.snark.model;

public class SystemView {
    public Integer galaxy;
    public Integer system;

    public SystemView(Integer galaxy, Integer system) {
        this.galaxy = galaxy;
        this.system = system;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof SystemView)) {
            return false;
        }
        SystemView another = (SystemView) obj;
        return galaxy.equals(another.galaxy) && system.equals(another.system);
    }
}
