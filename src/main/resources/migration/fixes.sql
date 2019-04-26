insert into sources (universe_id, galaxy, "system", "position", cp)
values (1, 1, 119, 9, 33630331);
insert into sources (universe_id, galaxy, "system", "position", cp)
values (1, 1, 249, 9, 33630570);
insert into sources (universe_id, galaxy, "system", "position", cp)
values (1, 1, 389, 7, 33632327);
insert into sources (universe_id, galaxy, "system", "position", cp)
values (1, 2, 121, 9, 33637337);
insert into sources (universe_id, galaxy, "system", "position", cp)
values (1, 2, 254, 9, 33634233);
insert into sources (universe_id, galaxy, "system", "position", cp)
values (1, 2, 365, 9, 33676477);
insert into sources (universe_id, galaxy, "system", "position", cp)
values (1, 3, 129, 9, 33745997);
insert into sources (universe_id, galaxy, "system", "position", cp)
values (1, 3, 379, 9,33721019);

update targets set fleet_sum = 0 , defense_sum = 0
where type = 'IN_ACTIVE';

alter table targets add column spy_level integer default 4;
alter table targets add column lm int;
alter table targets add column cm int;
alter table targets add column kr int;
alter table targets add column ow int;
alter table targets add column pan int;
alter table targets add column bom int;
alter table targets add column ni int;
alter table targets add column gs int;
alter table targets add column mt int;
alter table targets add column dt int;
alter table targets add column kol int;
alter table targets add column rec int;
alter table targets add column son int;

alter table targets add column sat int;
alter table targets add column wr int;
alter table targets add column ldl int;
alter table targets add column cdl int;
alter table targets add column dg int;
alter table targets add column mr int;
alter table targets add column dj int;
alter table targets add column wp int;
alter table targets add column mpo int;
alter table targets add column dpo int;
alter table targets add column pr int;

alter table sources add column lm int;
alter table sources add column cm int;
alter table sources add column kr int;
alter table sources add column ow int;
alter table sources add column pan int;
alter table sources add column bom int;
alter table sources add column ni int;
alter table sources add column gs int;
alter table sources add column mt int;
alter table sources add column dt int;
alter table sources add column kol int;
alter table sources add column rec int;
alter table sources add column son int;

alter table sources add column sat int;
alter table sources add column wr int;
alter table sources add column ldl int;
alter table sources add column cdl int;
alter table sources add column dg int;
alter table sources add column mr int;
alter table sources add column dj int;
alter table sources add column wp int;
alter table sources add column mpo int;
alter table sources add column dpo int;
alter table sources add column pr int;


