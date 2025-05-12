create table players
(
  id               bigserial           not null
    constraint player_pkey
    primary key,
  name             text,
  code              text,
  status              text,
  type              text,
  alliance              text,
  all_points       bigint,
  economy_points     bigint,
  research_points   bigint,
  fleet_points   bigint,
  ships_count   bigint,
  lifeform_points   bigint,
  tags              text,
  spy_level             integer,
  updated    timestamp default now(),

  --Research
  energy_technology              integer,
  laser_technology               integer,
  ion_technology                 integer,
  hyperspace_technology          integer,
  plasma_technology              integer,
  combustion_drive_technology     integer,
  impulse_drive_technology        integer,
  hyperspace_drive_technology     integer,
  espionage_technology           integer,
  computer_technology            integer,
  astrophysics_technology        integer,
  research_network_technology     integer,
  graviton_technology            integer,
  weapons_technology             integer,
  shielding_technology           integer,
  armor_technology               integer
);

create table players_activity
(
  id          bigserial   not null
    constraint players_activities_pkey
    primary key,
  player_id bigint not null references players,
  counter       integer,
  tags              text,
  updated    timestamp default now()
);