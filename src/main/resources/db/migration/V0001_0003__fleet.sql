create table fleet(
    id bigserial not null primary key,
    universe_id bigint not null references universes,
    type text not null,
    target_id bigint not null references targets,
    space_target text,
    source_id bigint not null references sources,
    start timestamp default null,
    visited timestamp default null,
    back timestamp default null,
    code bigint,
    lm int default 0,
    cm int default 0,
    kr int default 0,
    ow int default 0,
    pan int default 0,
    bom int default 0,
    ni int default 0,
    gs int default 0,
    lt int default 0,
    dt int default 0,
    kol int default 0,
    rec int default 0,
    son int default 0
);