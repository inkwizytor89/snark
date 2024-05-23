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
  forms             text,
  forms_level       integer,
  updated    timestamp default now(),
  created       timestamp default now(),

  debris_metal       bigint,
  debris_crystal     bigint,
  debris_deuterium   bigint,

  cp          integer   not null,
  cpm          integer,
  type          text default 'PLANET',
  tags              text,

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
  metalMine             integer default 0,
  crystalMine           integer default 0,
  deuteriumSynthesizer  integer default 0,
  solarPlant            integer default 0,
  fusionPlant           integer default 0,
  solarSatellite        integer default 0,
  metalStorage          integer default 0,
  crystalStorage        integer default 0,
  deuteriumStorage      integer default 0,

  --Facilities
  roboticsFactory       integer default 0,
  shipyard              integer default 0,
  researchLaboratory    integer default 0,
  allianceDepot         integer default 0,
  missileSilo           integer default 0,
  naniteFactory         integer default 0,
  terraformer           integer default 0,
  repairDock            integer default 0,

  moonbase              integer default 0,
  sensorPhalanx         integer default 0,
  jumpGate              integer default 0,

  --Lifeform
  --Humans
  lifeformTech11101 integer default 0,
  lifeformTech11102 integer default 0,
  lifeformTech11103 integer default 0,
  lifeformTech11104 integer default 0,
  lifeformTech11105 integer default 0,
  lifeformTech11106 integer default 0,
  lifeformTech11107 integer default 0,
  lifeformTech11108 integer default 0,
  lifeformTech11109 integer default 0,
  lifeformTech11110 integer default 0,
  lifeformTech11111 integer default 0,
  lifeformTech11112 integer default 0,
-- Rock´tal
  lifeformTech12101 integer default 0,
  lifeformTech12102 integer default 0,
  lifeformTech12103 integer default 0,
  lifeformTech12104 integer default 0,
  lifeformTech12105 integer default 0,
  lifeformTech12106 integer default 0,
  lifeformTech12107 integer default 0,
  lifeformTech12108 integer default 0,
  lifeformTech12109 integer default 0,
  lifeformTech12110 integer default 0,
  lifeformTech12111 integer default 0,
  lifeformTech12112 integer default 0,
-- Mecha
  lifeformTech13101 integer default 0,
  lifeformTech13102 integer default 0,
  lifeformTech13103 integer default 0,
  lifeformTech13104 integer default 0,
  lifeformTech13105 integer default 0,
  lifeformTech13106 integer default 0,
  lifeformTech13107 integer default 0,
  lifeformTech13108 integer default 0,
  lifeformTech13109 integer default 0,
  lifeformTech13110 integer default 0,
  lifeformTech13111 integer default 0,
  lifeformTech13112 integer default 0,
-- Kaelesh
  lifeformTech14101 integer default 0,
  lifeformTech14102 integer default 0,
  lifeformTech14103 integer default 0,
  lifeformTech14104 integer default 0,
  lifeformTech14105 integer default 0,
  lifeformTech14106 integer default 0,
  lifeformTech14107 integer default 0,
  lifeformTech14108 integer default 0,
  lifeformTech14109 integer default 0,
  lifeformTech14110 integer default 0,
  lifeformTech14111 integer default 0,
  lifeformTech14112 integer default 0
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
  created       timestamp default now(),
  last_attacked    timestamp,
  last_spied_on    timestamp,
  type        varchar(15) not null default 'PLANET',
  tags              text,

  debris_metal       bigint,
  debris_crystal     bigint,
  debris_deuterium   bigint,

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
  metalMine             integer default 0,
  crystalMine           integer default 0,
  deuteriumSynthesizer  integer default 0,
  solarPlant            integer default 0,
  fusionPlant           integer default 0,
  solarSatellite        integer default 0,
  metalStorage          integer default 0,
  crystalStorage        integer default 0,
  deuteriumStorage      integer default 0,

  --Facilities
  roboticsFactory       integer default 0,
  shipyard              integer default 0,
  researchLaboratory    integer default 0,
  allianceDepot         integer default 0,
  missileSilo           integer default 0,
  naniteFactory         integer default 0,
  terraformer           integer default 0,
  repairDock            integer default 0,

  moonbase              integer default 0,
  sensorPhalanx         integer default 0,
  jumpGate              integer default 0,

  --Lifeform
    --Humans
  lifeformTech11101 integer default 0,
  lifeformTech11102 integer default 0,
  lifeformTech11103 integer default 0,
  lifeformTech11104 integer default 0,
  lifeformTech11105 integer default 0,
  lifeformTech11106 integer default 0,
  lifeformTech11107 integer default 0,
  lifeformTech11108 integer default 0,
  lifeformTech11109 integer default 0,
  lifeformTech11110 integer default 0,
  lifeformTech11111 integer default 0,
  lifeformTech11112 integer default 0,
    -- Rock´tal
  lifeformTech12101 integer default 0,
  lifeformTech12102 integer default 0,
  lifeformTech12103 integer default 0,
  lifeformTech12104 integer default 0,
  lifeformTech12105 integer default 0,
  lifeformTech12106 integer default 0,
  lifeformTech12107 integer default 0,
  lifeformTech12108 integer default 0,
  lifeformTech12109 integer default 0,
  lifeformTech12110 integer default 0,
  lifeformTech12111 integer default 0,
  lifeformTech12112 integer default 0,
    -- Mecha
  lifeformTech13101 integer default 0,
  lifeformTech13102 integer default 0,
  lifeformTech13103 integer default 0,
  lifeformTech13104 integer default 0,
  lifeformTech13105 integer default 0,
  lifeformTech13106 integer default 0,
  lifeformTech13107 integer default 0,
  lifeformTech13108 integer default 0,
  lifeformTech13109 integer default 0,
  lifeformTech13110 integer default 0,
  lifeformTech13111 integer default 0,
  lifeformTech13112 integer default 0,
    -- Kaelesh
  lifeformTech14101 integer default 0,
  lifeformTech14102 integer default 0,
  lifeformTech14103 integer default 0,
  lifeformTech14104 integer default 0,
  lifeformTech14105 integer default 0,
  lifeformTech14106 integer default 0,
  lifeformTech14107 integer default 0,
  lifeformTech14108 integer default 0,
  lifeformTech14109 integer default 0,
  lifeformTech14110 integer default 0,
  lifeformTech14111 integer default 0,
  lifeformTech14112 integer default 0,

  resources   integer,
  fleet_sum   bigint default 0,
  defense_sum bigint default 0,
  spy_level integer default 4
);

create table historic_targets
(
  id          bigserial   not null
    constraint historic_targets_pkey
    primary key,
  target_id bigint not null references targets,
  metal       bigint,
  crystal     bigint,
  deuterium   bigint,
  updated    timestamp default now(),
  tags              text,

  resources   integer,
  fleet_sum   bigint default 0,
  defense_sum bigint default 0,

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
  missileInterplanetary    integer default 0
);

create table targets_activity
(
  id          bigserial   not null
    constraint targets_activities_pkey
    primary key,
  target_id bigint not null references targets,
  counter       integer,
  tags              text,
  updated    timestamp default now()
);
