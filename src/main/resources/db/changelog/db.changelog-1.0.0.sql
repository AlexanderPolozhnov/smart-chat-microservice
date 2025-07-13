create table users
(
    id         uuid                     default gen_random_uuid() not null primary key,
    username   varchar(50)                                        not null unique,
    password   varchar(255)                                       not null,
    email      varchar(100) unique,
    role       varchar(20)                                        not null,
    created_at timestamp with time zone default now()             not null,
    updated_at timestamp with time zone default now()             not null
);