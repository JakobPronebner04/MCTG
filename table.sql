create table if not exists packages
(
    package_id text not null
        primary key,
    created_at timestamp default CURRENT_TIMESTAMP
);

alter table packages
    owner to postgres;

create table if not exists packagecards
(
    card_id    text not null
        primary key,
    package_id text
        references packages,
    name       text,
    damage     numeric(10, 2)
);

alter table packagecards
    owner to postgres;

create table if not exists users
(
    user_id text         not null
        primary key,
    uname   varchar(50)  not null
        unique,
    upw     varchar(255) not null,
    coins   integer default 0,
    token   text
);

alter table users
    owner to postgres;

create table if not exists usercards
(
    card_id text           not null
        primary key,
    user_id text           not null,
    name    text           not null,
    damage  numeric(10, 2) not null
);

alter table usercards
    owner to postgres;

create table if not exists userdecks
(
    user_id  varchar(255) not null
        primary key
        references users
            on delete cascade,
    card_id1 text         not null,
    card_id2 text         not null,
    card_id3 text         not null,
    card_id4 text         not null
);

alter table userdecks
    owner to postgres;

create table if not exists userproperties
(
    user_id varchar(255) not null
        primary key
        constraint fk_user
            references users
            on delete cascade,
    name    varchar(255),
    bio     text,
    image   text
);

alter table userproperties
    owner to postgres;

create table if not exists userstats
(
    user_id text not null
        primary key
        references users
            on delete cascade,
    wins    integer default 0,
    losses  integer default 0,
    elo     integer default 0
);

alter table userstats
    owner to postgres;

create table if not exists trades
(
    id            text not null
        constraint table_name_pkey
            primary key,
    cardtotrade   text not null,
    type          text not null,
    minimumdamage double precision
);

alter table trades
    owner to postgres;

