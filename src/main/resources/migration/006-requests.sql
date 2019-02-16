create table spy_requests(
    id bigserial not null,
    universe_id bigint not null references universes,
    planet_id bigint not null references planets,
    visited timestamp default null,
    back timestamp default null
);

create table war_requests(
    id bigserial not null,
    universe_id bigint not null references universes,
    planet_id bigint not null references planets,
    spy_info_id bigint not null references spy_info,
    visited timestamp default null,
    back timestamp default null
);

