package org.enoch.snark.instance.model.technology;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.enoch.snark.gi.types.UrlComponent;

import static org.enoch.snark.gi.types.UrlComponent.DEFENSES;

@Getter
@AllArgsConstructor
public enum Defense implements Technology {
    rocketLauncher(401L),
    laserCannonLight(402L),
    laserCannonHeavy(403L),
    gaussCannon(404L),
    ionCannon(405L),
    plasmaCannon(406L),
    shieldDomeSmall(407L),
    shieldDomeLarge(408L),
    missileInterceptor(502L),
    missileInterplanetary(503L);

    private Long id;

    @Override
    public UrlComponent getPage() {
        return DEFENSES;
    }
}
