use benny;
/*!40101 SET NAMES utf8 */;
/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
-- truncate table people;
truncate table children_information;
truncate table media_archive;
truncate table media_attributes;
truncate table media_tag;
truncate table partner_information_history;
truncate table people_attributes;
truncate table people_in_media;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- ***CREATE STATEMENTS*** --
create table If not exists people(person_id int not null auto_increment primary key, 
person_name nvarchar (100) not null);

create table If not exists people_attributes(
person_id int not null,
attribute_name nvarchar (25) not null, 
attribute_value nvarchar (50) not null,
primary key(person_id,attribute_name),
foreign key (person_id) references people(person_id));

create table If not exists people_notes(notes_id int not null auto_increment primary key,
person_id int not null,
notes varchar(250),
note_date datetime default NOW(),
foreign key (person_id) references people(person_id));


create table If not exists people_reference(reference_id int not null auto_increment primary key,
person_id int not null,
reference_material varchar(250),
note_date datetime default NOW(),
foreign key (person_id) references people(person_id));

-- ****************************************************************** --

create table If not exists media_archive(media_id int not null auto_increment primary key, 
media_path nvarchar (700) not null);

create table If not exists media_attributes(
media_id int not null,
attribute_name nvarchar (25) not null, 
attribute_value nvarchar (50) not null,
primary key(media_id,attribute_name),
foreign key (media_id) references media_archive(media_id));

create table If not exists media_tag(tag_id int not null auto_increment primary key,
media_id int not null,
tag nvarchar (50) not null,
foreign key (media_id) references media_archive(media_id));
-- ****************************************************************** --
create table If not exists children_information(
parent_id int not null,
child_id int not null,
primary key(parent_id,child_id),
foreign key (parent_id) references people(person_id),
foreign key (child_id) references people(person_id));

create table If not exists partner_information_history(
partner1 int not null,
partner2 int not null,
event_information nvarchar (50) not null,
record_date datetime default NOW(),
primary key(partner1,partner2,record_date),
foreign key (partner1) references people(person_id),
foreign key (partner2) references people(person_id));


create table If not exists people_in_media(record_id int not null auto_increment primary key,
media_id int not null,
person_id int not null,
foreign key (media_id) references media_archive(media_id),
foreign key (person_id) references people(person_id));


select * from people;