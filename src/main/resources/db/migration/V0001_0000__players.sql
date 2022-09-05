create table players
(
  id               bigserial           not null
    constraint player_pkey
    primary key,
  name             text,
  code              text,
  level             integer,

  --Research
  energyTechnology              integer,
  laserTechnology               integer,
  ionTechnology                 integer,
  hyperspaceTechnology          integer,
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