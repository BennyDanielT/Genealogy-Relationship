# Genealogy-Relationship
A JAVA, console based application to determine the Biological relationship between individuals in terms of Degree of Cousinship and Level of Removal.

## Classes:
1. PersonIdentity – Manages data relevant to individuals in the system.
  Data Structures:
  i. Set<PersonIdentity> Children – To store the information of children for each parent.
  ii. Map<Integer, List<String>> Notes – To store notes on an individual
  iii. Map< Integer, List<String>> References – To store references on an individual
  iv. Map<String, String> Attributes – To store attributes of an individual
2. FileIdentifier– Manages data relevant to media files in a archive.
  Data Structures:
  i. Map< Integer,List<String>> Tags – To store Tags relevant to a media file.
  ii. Set<PersonIdentity> PeopleInMedia – To store the identifiers for people who appear
  in a media file.
  iii. Map<String, String> Attributes – To store attributes of a media file.
  
3. BiologicalRelation – Manages Data relevant to relationships between individuals in the system.
  Data Structures:
  i. Set<PersonIdentity> Descendants.
  ii. Set<PersonIdentity> Ancestors.
  iii. Map< List<Integer>, String> Relationships – To store the relationship between any two
  individuals.
4. Genealogy – Parent Class which encompasses all other classes.
  Data Structures:
  i. Set<PersonIdentity> People – To add a person to the family tree.
  ii. Set<FileIdentifier>Media – To add Media files to an archive.
  iii. Set<FileIdentifier> MediaByTag – To store Media files for a given tag or location.
  iv. List<FileIdentifier> MediaByPeople – To store Media files for a given set of people or a
  person’s children.
