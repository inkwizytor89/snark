create table universes
(
  id               bigserial           not null
    constraint universes_pkey
    primary key,
  login            text,
  pass             text,
  name             text,
  tag              text default "run",
  url              text,
  galaxy_max       integer default 6   not null,
  exploration_area integer default 9   not null,
  system_max       integer default 499 not null
);