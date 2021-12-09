use benny;


-- ***CREATE STATEMENTS*** --
create table If not exists people(person_id int not null auto_increment primary key, 
person_name nvarchar (100) not null);

drop table people;

create table If not exists people_attributes(attribute_id int not null auto_increment primary key,
person_id int not null,
attribute_name nvarchar (25) not null, 
attribute_value nvarchar (50) not null,
foreign key (person_id) references people(person_id));
-- drop table people_attributes;

create table If not exists people_notes(notes_id int not null auto_increment primary key,
person_id int not null,
notes varchar(250),
foreign key (person_id) references people(person_id));

create table If not exists people_reference(reference_id int not null auto_increment primary key,
person_id int not null,
reference_material varchar(250),
foreign key (person_id) references people(person_id));

-- ****************************************************************** --

create table If not exists media_archive(media_id int not null auto_increment primary key, 
media_path nvarchar (150) not null);

create table If not exists media_attributes(attribute_id int not null auto_increment primary key,
media_id int not null,
attribute_name nvarchar (25) not null, 
attribute_value nvarchar (50) not null,
foreign key (media_id) references media_archive(media_id));

create table If not exists media_tag(tag_id int not null auto_increment primary key,
media_id int not null,
tag nvarchar (50) not null,
foreign key (media_id) references media_archive(media_id));
-- ****************************************************************** --
create table If not exists children_information(record_id int not null auto_increment primary key,
parent int not null,
child int not null,
foreign key (parent) references people(person_id),
foreign key (child) references people(person_id));

create table If not exists partner_information(record_id int not null auto_increment primary key,
partner1 int not null,
partner2 int not null,
status boolean not null,
foreign key (partner1) references people(person_id),
foreign key (partner2) references people(person_id));

create table If not exists people_in_media(record_id int not null auto_increment primary key,
media_id int not null,
person_id int not null,
foreign key (media_id) references media_archive(media_id),
foreign key (person_id) references people(person_id));


