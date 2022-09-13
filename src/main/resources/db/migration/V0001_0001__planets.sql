create table colonies
(
  id          bigserial   not null
    constraint colonies_pkey
    primary key,
  galaxy      integer     not null,
  system      integer     not null,
  position    integer     not null,
  metal       integer,
  crystal     integer,
  deuterium   integer,
  power       integer,
  level       integer,
  updated    timestamp default now(),

  cp          integer   not null,
  cpm          integer,
  collecting_order    integer,

  -- fleet
  lm    integer default 0,
  cm    integer default 0,
  kr    integer default 0,
  ow    integer default 0,
  pan   integer default 0,
  bom   integer default 0,
  ni    integer default 0,
  gs    integer default 0,
  re    integer default 0,
  pf    integer default 0,
  mt    integer default 0,
  dt    integer default 0,
  kol   integer default 0,
  rec   integer default 0,
  son   integer default 0,
  sat   integer default 0,

  --defence
  wr    integer default 0,
  ldl   integer default 0,
  cdl   integer default 0,
  dg    integer default 0,
  dj    integer default 0,
  wp    integer default 0,
  mpo   integer default 0,
  dpo   integer default 0,
  pr    integer default 0,
  mr    integer default 0,

  -- Resources
  metalMine             integer,
  crystalMine           integer,
  deuteriumSynthesizer  integer,
  solarPlant            integer,
  fusionPlant           integer,
  solarSatellite        integer,
  metalStorage          integer,
  crystalStorage        integer,
  deuteriumStorage      integer,

  --Facilities
  roboticsFactory       integer,
  shipyard              integer,
  researchLaboratory    integer,
  allianceDepot         integer,
  missileSilo           integer,
  naniteFactory         integer,
  terraformer           integer,
  repairDock            integer,

  --Lifeform
  lifeformTech14101 integer,
  lifeformTech14102 integer,
  lifeformTech14103 integer,
  lifeformTech14104 integer,
  lifeformTech14105 integer,
  lifeformTech14106 integer,
  lifeformTech14107 integer,
  lifeformTech14108 integer,
  lifeformTech14109 integer,
  lifeformTech14110 integer,
  lifeformTech14111 integer,
  lifeformTech14112 integer
);

create table targets
(
  id          bigserial   not null
    constraint targets_pkey
    primary key,
  galaxy      integer     not null,
  system      integer     not null,
  position    integer     not null,
  metal       integer,
  crystal     integer,
  deuterium   integer,
  power       integer,
  player_id bigint not null references players,
  updated    timestamp default now(),

  -- fleet
  lm    integer default 0,
  cm    integer default 0,
  kr    integer default 0,
  ow    integer default 0,
  pan   integer default 0,
  bom   integer default 0,
  ni    integer default 0,
  gs    integer default 0,
  re    integer default 0,
  pf    integer default 0,
  mt    integer default 0,
  dt    integer default 0,
  kol   integer default 0,
  rec   integer default 0,
  son   integer default 0,
  sat   integer default 0,

  --defence
  wr    integer default 0,
  ldl   integer default 0,
  cdl   integer default 0,
  dg    integer default 0,
  dj    integer default 0,
  wp    integer default 0,
  mpo   integer default 0,
  dpo   integer default 0,
  pr    integer default 0,
  mr    integer default 0,

  -- Resources
  metalMine             integer,
  crystalMine           integer,
  deuteriumSynthesizer  integer,
  solarPlant            integer,
  fusionPlant           integer,
  solarSatellite        integer,
  metalStorage          integer,
  crystalStorage        integer,
  deuteriumStorage      integer,

  --Facilities
  roboticsFactory       integer,
  shipyard              integer,
  researchLaboratory    integer,
  allianceDepot         integer,
  missileSilo           integer,
  naniteFactory         integer,
  terraformer           integer,
  repairDock            integer,

  --Lifeform
  lifeformTech14101 integer,
  lifeformTech14102 integer,
  lifeformTech14103 integer,
  lifeformTech14104 integer,
  lifeformTech14105 integer,
  lifeformTech14106 integer,
  lifeformTech14107 integer,
  lifeformTech14108 integer,
  lifeformTech14109 integer,
  lifeformTech14110 integer,
  lifeformTech14111 integer,
  lifeformTech14112 integer,

  resources   integer,
  type        varchar(15) not null,
  fleet_sum   bigint default 0,
  defense_sum bigint default 0,
  spy_level integer default 4
);
