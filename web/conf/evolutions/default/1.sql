# --- !Ups
create table qr_user(name varchar(20) not null unique,password char(32));

# ---!Downs
drop table qr_user;