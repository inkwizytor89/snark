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
  energyTechnology              integer,
  laserTechnology               integer,
  ionTechnology                 integer,
  hyperspaceTechnology          integer,
  plasmaTechnology              integer,
  combustionDriveTechnology     integer,
  impulseDriveTechnology        integer,
  hyperspaceDriveTechnology     integer,
  espionageTechnology           integer,
  computerTechnology            integer,
  astrophysicsTechnology        integer,
  researchNetworkTechnology     integer,
  gravitonTechnology            integer,
  weaponsTechnology             integer,
  shieldingTechnology           integer,
  armorTechnology               integer
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