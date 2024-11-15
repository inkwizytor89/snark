package org.enoch.snark.instance.model.technology;

import org.enoch.snark.gi.types.UrlComponent;

public interface Technology {

    public Long getId();
    public String name();
    UrlComponent getPage();
}
