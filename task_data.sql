create table users
(
    id       bigint auto_increment
        primary key,
    name     varchar(255) not null,
    password varchar(255) null,
    constraint UK_3g1j96g94xpk3lpxl2qbl985x
        unique (name)
);

create table messages
(
    id      bigint auto_increment
        primary key,
    text    varchar(255) null,
    user_id bigint       null,
    constraint FKpsmh6clh3csorw43eaodlqvkn
        foreign key (user_id) references users (id)
);