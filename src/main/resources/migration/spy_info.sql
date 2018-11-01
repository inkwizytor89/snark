create table spy_info
(
  id        bigint not null
    constraint spy_info_pkey
    primary key,
  planet_id bigint not null
    constraint fk8jx6jluu6fres4509nsubriko
    references planets
    constraint spy_info_targets_id_fk
    references planets,
  metal     integer,
  crystal   integer,
  deuterium integer,
  power     integer,
  update    timestamp default now(),
  source_id bigint
    constraint fk4pfvro0kuu3k48ity81ilw9cb
    references sources
    constraint spy_info_sources_id_fk
    references sources
);

create unique index spy_info_id_uindex
  on spy_info (id);