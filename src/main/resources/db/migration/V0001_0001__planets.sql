create table colonies
(
  id          bigserial   not null
    constraint colonies_pkey
    primary key,
  galaxy      integer     not null,
  system      integer     not null,
  position    integer     not null,
  metal       bigint,
  crystal     bigint,
  deuterium   bigint,
  power       integer,
  level       integer,
  updated    timestamp default now(),

  cp          integer   not null,
  cpm          integer,
  is_planet boolean default true,
  collecting_order    integer,

  -- fleet
  fighterLight    integer default 0,
  fighterHeavy    integer default 0,
  cruiser    integer default 0,
  battleship    integer default 0,
  interceptor   integer default 0,
  bomber   integer default 0,
  destroyer    integer default 0,
  deathstar    integer default 0,
  recycler    integer default 0,
  explorer    integer default 0,
  transporterSmall    integer default 0,
  transporterLarge    integer default 0,
  colonyShip   integer default 0,
  reaper   integer default 0,
  espionageProbe   integer default 0,
  sat   integer default 0,

  --defence
  rocketLauncher    integer default 0,
  laserCannonLight   integer default 0,
  laserCannonHeavy   integer default 0,
  gaussCannon    integer default 0,
  ionCannon    integer default 0,
  plasmaCannon    integer default 0,
  shieldDomeSmall   integer default 0,
  shieldDomeLarge   integer default 0,
  missileInterceptor    integer default 0,
  missileInterplanetary    integer default 0,

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

  moonbase              integer,
  sensorPhalanx         integer,
  jumpGate              integer,

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
  metal       bigint,
  crystal     bigint,
  deuterium   bigint,
  power       integer,
  player_id bigint not null references players,
  updated    timestamp default now(),
  last_attacked    timestamp default now(),
  is_planet boolean default true,

  -- fleet
  fighterLight    integer default 0,
  fighterHeavy    integer default 0,
  cruiser    integer default 0,
  battleship    integer default 0,
  interceptor   integer default 0,
  bomber   integer default 0,
  destroyer    integer default 0,
  deathstar    integer default 0,
  recycler    integer default 0,
  explorer    integer default 0,
  transporterSmall    integer default 0,
  transporterLarge    integer default 0,
  colonyShip   integer default 0,
  reaper   integer default 0,
  espionageProbe   integer default 0,
  sat   integer default 0,

  --defence
  rocketLauncher    integer default 0,
  laserCannonLight   integer default 0,
  laserCannonHeavy   integer default 0,
  gaussCannon    integer default 0,
  ionCannon    integer default 0,
  plasmaCannon    integer default 0,
  shieldDomeSmall   integer default 0,
  shieldDomeLarge   integer default 0,
  missileInterceptor    integer default 0,
  missileInterplanetary    integer default 0,

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

  moonbase              integer,
  sensorPhalanx         integer,
  jumpGate              integer,

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
