create table farm_waves(
    id bigserial not null primary key,
    start timestamp not null,
    spy_requests_code bigint,
    war_requests_code bigint
);
