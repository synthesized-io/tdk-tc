create table speaker (
  id INT not null primary key,
  name VARCHAR(50)
);

create table conference (
  id INT not null primary key,
  name VARCHAR(50)
);

create table talk (
  id INT not null primary key,
  name VARCHAR(500) not null,
  conferenceid INT not null,
  status VARCHAR(100) not null,
  feedback VARCHAR(1000),
  foreign key (conferenceid) references conference(id)
);


create table talkspeakers (
 talkid INT not null ,
 speakerid INT not null,
 primary key (talkid, speakerid),
 foreign key (talkid) references talk(id),
 foreign key (speakerid) references speaker(id)
);

/* "SEED" RECORDS */
insert into conference(id, name) VALUES (1, 'conf1');
insert into speaker(id, name) values (2, 'speaker2');
insert into talk(id, name, conferenceid, status) VALUES (1, 'talk1', 1, 'IN_REVIEW');
