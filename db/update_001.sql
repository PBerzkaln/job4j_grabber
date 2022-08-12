create table if not exists rabbit(
    id serial primary key,
    created_date timestamp
);

create table if not exists post(
    id serial primary key,
    name varchar(255),
    link text not null,
    text text,
    created timestamp,
    unique (link)
);