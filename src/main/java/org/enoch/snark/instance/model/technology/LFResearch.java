package org.enoch.snark.instance.model.technology;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.enoch.snark.gi.types.UrlComponent;

@Getter
@AllArgsConstructor
public enum LFResearch implements Technology {

    ;

    private final Long id;

    @Override
    public UrlComponent getPage() {
        return null;
    }
}
