import java.sql.*;
import java.util.*;
public class Genealogy
{
    private ArrayList<PersonIdentity> people = new ArrayList<>(); // Array list of People
    private ArrayList<FileIdentifier> files = new ArrayList<>(); // Array list of Media Files

    public PersonIdentity addPerson(String name) {
        if(name!=null && !name.equalsIgnoreCase("")) //If fileLocation is null or an empty string do not insert!
        {
            PersonIdentity person = new PersonIdentity(name);

            Connection con=null;
            Statement stmt=null;
            ResultSet resultSet=null;
            String schema="benny";//Schema fileLocation, currently hardcoded. Will be modified later
            try
            {
                Class.forName("com.mysql.jdbc.Driver");
                con= DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306","benny","B00899629");
                System.out.println("Connection established with " + schema + " successfully");

                stmt=con.createStatement();
                stmt.execute ("use "+schema+";"); //Select a schema

                String query="insert into people values(?,?)"; /*insert the person's fileLocation into the database,
                the Unique Id for the person is automatically generated*/
                PreparedStatement prepStatement = con.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);
                prepStatement.setString(1,null); //Set as null for auto_increment
                prepStatement.setString(2, name); //Set fileLocation
                prepStatement.execute();

                int insertKey = 0; //Object to store the Unique Id
                resultSet = prepStatement.getGeneratedKeys();
                if (resultSet.next()) {
                    insertKey = resultSet.getInt(1); //Retrieve the Unique ID for the person
                }
                else
                {
                    throw new SQLException("Failed to add the person - " + name + " to the database!"); /* If the Id wasn't retrieved this means
                    that the insert operation failed*/
                }
                System.out.println("Person - " + name + " has been added to the database with Id: " + insertKey);

                //Close connection variables
                resultSet.close();
                con.close();
                stmt.close();
                prepStatement.close();
            }
            catch (ClassNotFoundException | SQLException e)
            {
                e.printStackTrace();
            }
            return person;
        }
        else
            System.out.println("Invalid fileLocation, person was not added to the database"); /*If invalid parameter for fileLocation is encountered
            do not add person to the database*/
            return null; //Throw an Exception later
    }

    public Boolean recordAttributes (PersonIdentity person, Map<String, String> attributes ) throws ClassNotFoundException,SQLException {
        String blanks=""; //Object to store blanks
        String attribute; //Object to store intermediate attribute names
        String attribute_value; //Object to store intermediate attribute values
        Set<String> keys = new HashSet<>(); //Set of keys in the Map
        keys=attributes.keySet();
        Collection<String> values = new ArrayList<>(); //Set of Values in the Map
        values=attributes.values();

        if(!keys.contains(null) && !keys.contains(blanks) && !values.contains(null) && !values.contains(blanks)) //If attribute key or value is null or an empty string do not insert!
        {
            Connection con = null;
            Statement stmt = null;
            ResultSet resultSet = null;
            String schema = "benny";//Schema fileLocation, currently hardcoded. Will be modified later
            try {
                int id = person.getId(); //Object to store the id of the personIdentity object passed in the method
                int exists = -1; //Object to store the count of Ids in the table people_reference
                String name= person.getName(); //Object to store the person's fileLocation
                Class.forName("com.mysql.jdbc.Driver");
                con = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306", "benny", "B00899629");
                System.out.println("Connection established with schema " + schema + " successfully");

                stmt = con.createStatement();
                stmt.execute("use " + schema + ";");

                String query = "select count(person_id) from people where person_id=? and person_name=?";//Check if person exists
                PreparedStatement prepStatement = con.prepareStatement(query);
                prepStatement.setInt(1, id);
                prepStatement.setString(2, name);
                resultSet = prepStatement.executeQuery();
                if (resultSet.next()) {
                    exists = resultSet.getInt(1); //retrieve the count of people with the given id (Maximum of 1 Minimum 0)
                }

                if (exists <= 0)//Throw an exception if the person does not exist
                {
                    throw new PersonNotFoundException("Person with Id - " + id + " and Name - " + name + " cannot be found");
                }

                System.out.println("List of Attributes for Person with Id - " + id + ":");
                //Display the attributes currently stored for this person in the database
                String queryCurrentAttributes = "select distinct attribute_name as Attribute,attribute_value as Value" +
                        " from people_attributes where person_id=?";
                prepStatement = con.prepareStatement(queryCurrentAttributes);
                prepStatement.setInt(1, id);
                resultSet = prepStatement.executeQuery();
                System.out.println("***************************************************");
                System.out.format("%16s%16s", "Attribute", "Value");
                System.out.println("");

                //Print the Attribute and values
                while(resultSet.next())
                {
                    attribute=resultSet.getNString(1);
                    attribute_value=resultSet.getNString(2);
                    System.out.format("%16s%16s", attribute,attribute_value); //Format the output by including padding
                    System.out.println("");
                }
                System.out.println("***************************************************");

                for(var entryset : attributes.entrySet())
                {
                    attribute=entryset.getKey();
                    attribute_value=entryset.getValue();
                    String queryAttributeExists = "select count(person_id) from people_attributes where person_id=? and attribute_name=?";
                    prepStatement = con.prepareStatement(queryAttributeExists);
                    prepStatement.setInt(1, id);
                    prepStatement.setString(2, attribute);
                    resultSet = prepStatement.executeQuery();
                    if (resultSet.next())
                    {
                        exists = resultSet.getInt(1); /*Check if the attribute already exists in the table
                        if it does, Update the value with the new value ELSE Insert the new Attribute-Value pair*/
                    }
                    if (exists > 0)//Attribute already exists in the table, therefore update the attribute value
                    {
                        String queryUpdateAttribute = "update people_attributes set attribute_value=? where person_id=? and attribute_name=?";
                        prepStatement = con.prepareStatement(queryUpdateAttribute);
                        prepStatement.setString(1, attribute_value);
                        prepStatement.setInt(2, id);
                        prepStatement.setString(3, attribute);
                        prepStatement.execute();
                        System.out.println("An attribute - " + attribute + " has been updated with value - " + attribute_value);
                    }
                    else //Attribute is new. Therefore, Insert it into the table
                    {
                        String queryInsertAttribute = "insert into people_attributes values(?,?,?)";
                        prepStatement = con.prepareStatement(queryInsertAttribute);
                        prepStatement.setInt(1, id);
                        prepStatement.setString(2, attribute);
                        prepStatement.setString(3, attribute_value);
                        prepStatement.execute();
                        System.out.println("An attribute - " + attribute + " has been added to the database with value - " + attribute_value);
                    }
                }

                prepStatement.close();
                return true;

            } catch (ClassNotFoundException e) {
                System.out.println("Class not found. Please verify that the appropriate .jar files and classes are set up");
                e.printStackTrace();
            } catch (SQLException e) {
                System.out.println("Error while trying to access the database!");
                e.printStackTrace();
            }
            finally {//Close Connection objects
                if (resultSet != null)
                {resultSet.close();}
                if (con != null)
                {con.close();}
                if (stmt != null)
                {stmt.close();}
            }
        }
        else
            System.out.println("Invalid Attribute Name and/or Value, Attribute was not added to the database!");
            return false;
    }

    public Boolean recordReference( PersonIdentity person, String reference ) throws PersonNotFoundException,ClassNotFoundException,SQLException {
        if(reference!=null && !reference.equalsIgnoreCase("")) //If reference is null or an empty string do not insert!
        {
            Connection con = null;
            Statement stmt = null;
            ResultSet resultSet = null;
            String schema = "benny";//Schema fileLocation, currently hardcoded. Will be modified later
            try {
                int id = person.getId(); //Object to store the id of the personIdentity object passed in the method
                int exists = -1; //Object to store the count of Ids in the table people_reference
                String name= person.getName(); //Object to store the person's fileLocation
                Class.forName("com.mysql.jdbc.Driver");
                con = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306", "benny", "B00899629");
                System.out.println("Connection established with schema " + schema + " successfully");

                stmt = con.createStatement();
                stmt.execute("use " + schema + ";");

                String query = "select count(person_id) from people where person_id=? and person_name=?";
                PreparedStatement prepStatement = con.prepareStatement(query);
                prepStatement.setInt(1, id);
                prepStatement.setString(2, name);
                resultSet = prepStatement.executeQuery();
                if (resultSet.next()) {
                    exists = resultSet.getInt(1); //retrieve the count of people with the given id (Maximum of 1 Minimum 0)
                }

                if (exists <= 0)//Throw an exception if the person does not exist
                {
                    throw new PersonNotFoundException("Person with Id - " + id + " and Name - " + name + " cannot be found");
                }

                String queryInsertNotes = "insert into people_reference values(?,?,?)";
                prepStatement = con.prepareStatement(queryInsertNotes,Statement.RETURN_GENERATED_KEYS);
                prepStatement.setString(1, null);
                prepStatement.setInt(2, id);
                prepStatement.setString(3, reference);
                prepStatement.execute();

                int insertKey = 0; //Object to store the Unique Id
                resultSet = prepStatement.getGeneratedKeys();
                if (resultSet.next()) {
                    insertKey = resultSet.getInt(1); //Retrieve the Unique ID for the person
                }
                else
                {
                    throw new SQLException("Failed to add the source reference material in the database!"); /* If the Id wasn't retrieved this means
                    that the insert operation failed*/
                }
                System.out.println("A source reference material has been added to the database with Id - " + insertKey);

                prepStatement.close();
                return true;

            } catch (ClassNotFoundException e) {
                System.out.println("Class not found. Please verify that the appropriate .jar files and classes are set up");
                e.printStackTrace();
            } catch (SQLException e) {
                System.out.println("Error while trying to access the database!");
                e.printStackTrace();
            }
            finally {//Close Connection objects
                if (resultSet != null)
                {resultSet.close();}
                if (con != null)
                {con.close();}
                if (stmt != null)
                {stmt.close();}
            }
        }
        else
            System.out.println("Invalid Source reference material, material was not added to the database!");
            return false;
    }

    public Boolean recordNote( PersonIdentity person, String note ) throws PersonNotFoundException,ClassNotFoundException,SQLException {
        if(note!=null && !note.equalsIgnoreCase("")) //If reference is null or an empty string do not insert!
        {
            Connection con = null;
            Statement stmt = null;
            ResultSet resultSet = null;
            String schema = "benny";//Schema fileLocation, currently hardcoded. Will be modified later
            try {
                int id = person.getId(); //Object to store the id of the personIdentity object passed in the method
                int exists = -1; //Object to store the count of Ids in the table people_reference
                String name= person.getName(); //Object to store the person's fileLocation
                Class.forName("com.mysql.jdbc.Driver");
                con = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306", "benny", "B00899629");
                System.out.println("Connection established with schema " + schema + " successfully");

                stmt = con.createStatement();
                stmt.execute("use " + schema + ";");

                String query = "select count(person_id) from people where person_id=? and person_name=?";
                PreparedStatement prepStatement = con.prepareStatement(query);
                prepStatement.setInt(1, id);
                prepStatement.setString(2, name);
                resultSet = prepStatement.executeQuery();
                if (resultSet.next()) {
                    exists = resultSet.getInt(1); //retrieve the count of people with the given id (Maximum of 1 Minimum 0)
                }

                if (exists <= 0)//Throw an exception if the person does not exist
                {
                    throw new PersonNotFoundException("Person with Id - " + id + " and Name - " + name + " cannot be found");
                }

                String queryInsertNotes = "insert into people_notes values(?,?,?)";
                prepStatement = con.prepareStatement(queryInsertNotes,Statement.RETURN_GENERATED_KEYS);
                prepStatement.setString(1, null);
                prepStatement.setInt(2, id);
                prepStatement.setString(3, note);
                prepStatement.execute();

                int insertKey = 0; //Object to store the Unique Id
                resultSet = prepStatement.getGeneratedKeys();
                if (resultSet.next()) {
                    insertKey = resultSet.getInt(1); //Retrieve the Unique ID for the person
                }
                else
                {
                    throw new SQLException("Failed to add the note in the database!"); /* If the Id wasn't retrieved this means
                    that the insert operation failed*/
                }
                System.out.println("A note has been added to the database with Id - " + insertKey);

                prepStatement.close();
                return true;

            } catch (ClassNotFoundException e) {
                System.out.println("Class not found. Please verify that the appropriate .jar files and classes are set up");
                e.printStackTrace();
            } catch (SQLException e) {
                System.out.println("Error while trying to access the database!");
                e.printStackTrace();
            }
            finally {//Close Connection objects
                if (resultSet != null)
                {resultSet.close();}
                if (con != null)
                {con.close();}
                if (stmt != null)
                {stmt.close();}
            }
        }
        else
            System.out.println("Invalid Note, note was not added to the database!");
        return false;
    }

//    Boolean recordChild( PersonIdentity parent, PersonIdentity child )
//    {
//
//    }
//
//    Boolean recordPartnering( PersonIdentity partner1, PersonIdentity partner2 )
//    {
//
//    }
//
//    Boolean recordDissolution( PersonIdentity partner1, PersonIdentity partner2 )
//    {
//
//    }
//
//    FileIdentifier addMediaFile( String fileLocation )
//    {
//
//    }
//
//    Boolean recordMediaAttributes( FileIdentifier fileIdentifier, Map<String, String> attributes )
//    {
//
//    }
//
//    Boolean peopleInMedia( FileIdentifier fileIdentifier, List<PersonIdentity> people )
//    {
//
//    }
//
//    Boolean tagMedia( FileIdentifier fileIdentifier, String tag )
//    {
//
//    }
//
//    PersonIdentity findPerson( String name )
//    {
//
//    }
//
//    FileIdentifier findMediaFile( String name )
//    {
//
//    }
//
//    String findName( PersonIdentity id )
//    {
//
//    }
//
//    String findMediaFile( FileIdentifier fileId )
//    {
//
//    }
//
//    BiologicalRelation findRelation( PersonIdentity person1, PesonIdentity person2 )
//    {
//
//    }
//
//    Set<PersonIdentity> descendants( PersonIdentity person, Integer generations )
//    {
//
//    }
//
//    Set<PersonIdentity> ancestors( PersonIdentity person, Integer generations )
//    {
//
//    }
//
//    List<String> notesAndReferences( PersonIdentity person )
//    {
//
//    }
//
//    Set<FileIdentifier> findMediaByTag( String tag , String startDate, String endDate)
//    {
//
//    }
//
//    Set<FileIdentifier> findMediaByLocation( String location, String startDate, String endDate)
//    {
//
//    }
//
//    List<FileIdentifier> findIndividualsMedia( Set<PersonIdentity> people, String startDate, String endDate)
//    {
//
//    }
//
//    List<FileIdentifier> findBiologicalFamilyMedia(PersonIdentity person)
//    {
//
//    }




    public static void main(String[] args)
    {
        /*
        Test for valid person and attribute
        Invalid person and valid attribute
        Valid Person and attribute update
        Valid person and invalid attribute
        Invalid person and invalid attribute

         */
        Genealogy familyTree = new Genealogy();
        PersonIdentity person1 = new PersonIdentity(1,"Daniel"); //Valid Person
        PersonIdentity person2 = new PersonIdentity(2,"Subash"); //Invalid Person
        PersonIdentity person3 = new PersonIdentity(3,"Subash"); //valid Person
        Map<String,String> testAttributes = new HashMap<>();
        testAttributes.put("Date of Birth","1996-12-28");
        testAttributes.put("Gender","Male");
        testAttributes.put("Occupation","Cop");

        Map<String,String> testAttributes2 = new HashMap<>();
        testAttributes2.put("Date of Birth","1996-11-27");
        testAttributes2.put("Gender","Male");
        testAttributes2.put("Occupation","CEO");

        Map<String,String> testAttributes3 = new HashMap<>();
        testAttributes3.put("Occupation","Monk");

        Map<String,String> testAttributes4 = new HashMap<>();
        testAttributes4.put("","Monk");
        try {
            boolean check1 = familyTree.recordAttributes(person1,testAttributes);//Test for valid person and attribute
            System.out.println(" True Check: " + check1);
//            boolean check2 = familyTree.recordAttributes(person2,testAttributes2);//Invalid person and valid attribute
//            System.out.println(" Invalid Check: " + check2);
//            boolean check3 = familyTree.recordAttributes(person3,testAttributes2);//Test for valid person 2 and attribute
//            System.out.println(" True Check: " + check3);
//            boolean check4 = familyTree.recordAttributes(person3,testAttributes3);//Valid Person and attribute update
//            System.out.println(" Update Check: " + check4);
//            boolean check5 = familyTree.recordAttributes(person3,testAttributes4);//Valid person and invalid attribute
//            System.out.println(" False Check: " + check5);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

