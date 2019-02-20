create table fleet(
    id bigserial not null primary key,
    visited timestamp default null,
    back timestamp default null
);

create table ships(
    id bigserial not null primary key,
    fleet_id bigint not null references fleet,
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