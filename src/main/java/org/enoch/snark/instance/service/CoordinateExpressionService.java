package org.enoch.snark.instance.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@RequiredArgsConstructor
@Component
@Scope("prototype")
@Deprecated
public class CoordinateExpressionService {


    public static final String PLANETS = "planets";
    public static final String MOONS = "moons";
    public static final String ALL = "all";
    public static final String EACH_POSITION = EMPTY;
    public static final String NONE = "none";

    public static final String SWAP = "swap";
    public static final String NEXT = "next";
    public static final String PREV = "prev";
    public static final String SPACE = "space";

}
