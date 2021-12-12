import java.sql.*;
import java.util.*;
public class Genealogy
{
    private ArrayList<PersonIdentity> people = new ArrayList<>(); // Array list of People
    private ArrayList<FileIdentifier> files = new ArrayList<>(); // Array list of Media Files

    public PersonIdentity addPerson (String name) throws IllegalArgumentException
    {
        PersonIdentity person = new PersonIdentity(name);
        if(name!=null && !name.equalsIgnoreCase("")) //If name is null or an empty string do not insert!
        {
            Connection con=null;
            Statement stmt=null;
            ResultSet resultSet=null;
            String schema="benny";//Schema name, currently hardcoded. Will be modified later
            try
            {
                Class.forName("com.mysql.jdbc.Driver");
                con= DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306","benny","B00899629");
                System.out.println("Connection established with " + schema + " successfully");

                stmt=con.createStatement();
                stmt.execute ("use "+schema+";"); //Select a schema

                String query="insert into people values(?,?)"; /*insert the person's name into the database,
                the Unique Id for the person is automatically generated*/
                PreparedStatement prepStatement = con.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);
                prepStatement.setString(1,null); //Set as null for auto_increment
                prepStatement.setString(2, name); //Set name
                prepStatement.execute();

                int insertKey = 0; //Object to store the Unique Id
                resultSet = prepStatement.getGeneratedKeys();
                if (resultSet.next()) {
                    insertKey = resultSet.getInt(1); //Retrieve the Unique ID for the person
                    person.uniqueId=insertKey;
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
        }
        else
        {
            throw new IllegalArgumentException("Invalid Name, person was not added to the database"); /*If invalid parameter for name is encountered
            do not add person to the database*/
        }
    return person;
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

    Boolean recordChild( PersonIdentity parent, PersonIdentity child ) throws PersonNotFoundException,ClassNotFoundException,SQLException, DuplicateRecordException
    {
        Map<Integer,String> people = new HashMap<>();
        Connection con = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        String schema = "benny";//Schema fileLocation, currently hardcoded. Will be modified later
        try {
            int parentId = parent.getId(); //Object to store the id of the parent object passed in the method
            int childId = child.getId(); //Object to store the id of the parent object passed in the method
            int relationExists = -1; //Object to store the count of a parent-child relation

            if(parentId==childId) //If the Parent and Child Ids are similar do not record the relationship!
            {
                System.out.println("A person can't register themselves as their child!");
                return false;
            }

            String parentName= parent.getName(); //Object to store the person's fileLocation
            String childName= child.getName(); //Object to store the person's fileLocation
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306", "benny", "B00899629");
            System.out.println("Connection established with schema " + schema + " successfully");

            stmt = con.createStatement();
            stmt.execute("use " + schema + ";");

            String query = "select distinct person_id,person_name from people";//Retrieve all the people in the database
            PreparedStatement prepStatement = con.prepareStatement(query);
            resultSet = prepStatement.executeQuery();

            while(resultSet.next())
            {
                people.put(resultSet.getInt(1),resultSet.getString(2)); //Store the Ids and names of people in the database in a Map
            }
            if (!people.containsKey(parentId) || (people.containsKey(parentId) && !Objects.equals(people.get(parentId), parentName)))//Throw an exception if the parent does not exist
            {
                throw new PersonNotFoundException("Parent with Id - " + parentId + " and Name - " + parentName + " cannot be found");
            }
            if (!people.containsKey(childId) || (people.containsKey(childId) && !Objects.equals(people.get(childId), childName)))//Throw an exception if the child does not exist
            {
                throw new PersonNotFoundException("Child with Id - " + childId + " and Name - " + childName + " cannot be found");
            }

            String queryRelationExists = "select count(parent_id) from children_information where parent_id=? and child_id=?";
            prepStatement = con.prepareStatement(queryRelationExists);
            prepStatement.setInt(1, parentId);
            prepStatement.setInt(2, childId);
            resultSet = prepStatement.executeQuery();
            if (resultSet.next())
            {
                relationExists = resultSet.getInt(1); /*Check if the parent-child relationship already exists in the table
                if it does, Display a message that the relationship already exists ELSE Insert the new parent-child relationship*/
            }
            if (relationExists > 0)//Relation already exists in the table, therefore throw an exception stating the child has been recorded
            {
                throw new DuplicateRecordException("A parent-child relationship already exists between Parent -" + parentId + " and Child - " + childId);
//                System.out.println("A parent-child relationship already exists between Parent - " + parentId + " and Child - " + childId);
//                return false;
            }
            else //Relationship is new. Therefore, Insert it into the table
            {
                String queryInsertRelationship = "insert into children_information values(?,?)";
                prepStatement = con.prepareStatement(queryInsertRelationship);
                prepStatement.setInt(1, parentId);
                prepStatement.setInt(2, childId);
                prepStatement.execute();
                System.out.println("A parent-child relationship between person(Parent) - " + parentId + " and person(Child) - " + childId + " has been added to the database");
            }
            prepStatement.close();


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
        return true;
    }

    Boolean recordPartnering( PersonIdentity partner1, PersonIdentity partner2 ) throws PersonNotFoundException,ClassNotFoundException,SQLException, DuplicateRecordException{
        Map<Integer,String> people = new HashMap<>();
        Connection con = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        ResultSet resultSet2 = null;
        String schema = "benny";//Schema fileLocation, currently hardcoded. Will be modified later
        try {
            int partner1Id = partner1.getId(); //Object to store the id of the parent object passed in the method
            int partner2Id = partner2.getId(); //Object to store the id of the parent object passed in the method
            int relationExists = -1; //Object to store the count of a symmetric association relation

            if(partner1Id==partner2Id) //If the partners both are the same person do not record the relationship!
            {
                System.out.println("A person can't be symmetrically associated with themselves! Cannot record this relationship");
                return false;
            }

            String partner1Name= partner1.getName(); //Object to store the person's fileLocation
            String partner2Name= partner2.getName(); //Object to store the person's fileLocation
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306", "benny", "B00899629");
            System.out.println("Connection established with schema " + schema + " successfully");

            stmt = con.createStatement();
            stmt.execute("use " + schema + ";");

            String query = "select distinct person_id,person_name from people";//Retrieve all the people in the database
            PreparedStatement prepStatement = con.prepareStatement(query);

            resultSet = prepStatement.executeQuery();

            while(resultSet.next())//Store the Ids and names of people in the database in a Map to check if both the partners exist in the database before recording a relationship
            {
                people.put(resultSet.getInt(1),resultSet.getString(2));
            }
            if (!people.containsKey(partner1Id) || (people.containsKey(partner1Id) && !Objects.equals(people.get(partner1Id), partner1Name)))//Throw an exception if Partner 1 does not exist
            {
                throw new PersonNotFoundException("Partner with Id - " + partner1Id + " and Name - " + partner1Name + " cannot be found");
            }
            if (!people.containsKey(partner2Id) || (people.containsKey(partner2Id) && !Objects.equals(people.get(partner2Id), partner2Name)))//Throw an exception if Partner 2 does not exist
            {
                throw new PersonNotFoundException("Partner with Id - " + partner2Id + " and Name - " + partner2Name + " cannot be found");
            }

            String queryLatestEvent = "select * from partner_information_history where " +
                    "partner1=? or partner2=? order by record_date desc limit 1;"; //Return the latest record (if any) for partner 1 or partner 2
            prepStatement = con.prepareStatement(queryLatestEvent);
            //Retrieve any row where any partner Id in the Table = Partner 1's ID
            prepStatement.setInt(1, partner1Id);
            prepStatement.setInt(2, partner1Id);
            resultSet = prepStatement.executeQuery();
/*
By default, only one ResultSet object per Statement object can be open at the same time.
Therefore, if the reading of one ResultSet object is interleaved with the reading of another,
each must have been generated by different Statement objects.
 */
            PreparedStatement prepStatement2 = con.prepareStatement(queryLatestEvent);
            //Retrieve any row where any partner Id in the Table = Partner 2's ID
            prepStatement2.setInt(1, partner2Id);
            prepStatement2.setInt(2, partner2Id);
            resultSet2 = prepStatement2.executeQuery();
            int resultSet1Size = resultSet.last() ? resultSet.getRow() : 0;
            resultSet.beforeFirst();
            int resultSet2Size = resultSet2.last() ? resultSet2.getRow() : 0;
            resultSet2.beforeFirst();

            if(resultSet1Size>0 && resultSet2Size>0) //Both people have marriages recorded, need to check if they are married to each other or other people
            {
                resultSet.next();
                resultSet2.next();
                if(resultSet.getString(3).equals("Marriage") && resultSet2.getString(3).equals("Marriage"))
                {
                    if((resultSet.getInt(1)==partner1Id || resultSet.getInt(2)==partner1Id) && //Any 1 column contains Partner 1
                            (resultSet.getInt(1)==partner2Id || resultSet.getInt(2)==partner2Id)) //Any 1 column contains Partner 2
                    {//Attempt to insert a duplicate record. Throw an exception (Ideally we cannot insert duplicate records in a table, will result in an error)
                        resultSet2.beforeFirst();//Reset the cursor for ResultSet
                        resultSet.beforeFirst();//Reset the cursor for ResultSet
                        throw new DuplicateRecordException("A symmetric partnering relation already exists between " +
                                "Partner with Id - " + partner1Id + " and Partner with Id - " + partner2Id);
                    }
                    else
                    {
                        resultSet2.beforeFirst();//Reset the cursor for ResultSet
                        resultSet.beforeFirst();//Reset the cursor for ResultSet
                        System.out.println("Partners with Ids - " + partner1Id + " and " + partner2Id + " are both married to other people. Cannot record this relationship!");
                        return false;
                    }
                }

            }
            else if(resultSet1Size>0 && resultSet2Size<=0) //Partner 1 has an event but Partner 2 does not
            {
                resultSet.next();
                if(resultSet.getString(3).equals("Marriage"))//partner 1 Is currently Married to someone else
                {
                    resultSet.beforeFirst(); //Reset the cursor for ResultSet
                    System.out.println("Partner with Id - " + partner1Id + " is already partnered with someone! Cannot record this relationship!");
                    return false;
                }
            }
            else if(resultSet1Size<=0 && resultSet2Size>0) //Partner 2 has an event but Partner 1 does not
            {
                resultSet2.next();
                if(resultSet2.getString(3).equals("Marriage")) //partner 2 Is currently Married to someone else
                {
                    resultSet2.beforeFirst();
                    System.out.println("Partner with Id - " + partner2Id + " is already partnered with someone! Cannot record this relation!");
                    return false;
                }
            }
            else
            {
                String queryInsertRelationship = "insert into partner_information_history values(?,?,?,?)";
                prepStatement = con.prepareStatement(queryInsertRelationship);
                prepStatement.setInt(1, partner1Id);
                prepStatement.setInt(2, partner2Id);
                prepStatement.setNString(3, "Marriage");
                prepStatement.setTimestamp(4,new java.sql.Timestamp(new java.util.Date().getTime())); //Record the Timestamp when the record was inserted
                prepStatement.execute();
                System.out.println("A Symmetric Partnering relationship between Partner 1 with Id - " + partner1Id + " and Partner 2 with Id - " + partner2Id + " has been added to the database");
                return true;
            }
            prepStatement.close();
            prepStatement2.close();
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
            if (resultSet2 != null)
            {resultSet2.close();}
            if (con != null)
            {con.close();}
            if (stmt != null)
            {stmt.close();}
        }
        return false;

    }
//
    Boolean recordDissolution( PersonIdentity partner1, PersonIdentity partner2 ) throws PersonNotFoundException,ClassNotFoundException,SQLException, DuplicateRecordException, IllegalArgumentException {
        Map<Integer,String> people = new HashMap<>();
        Connection con = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        ResultSet resultSet2 = null;
        String schema = "benny";//Schema fileLocation, currently hardcoded. Will be modified later
        try {
            int partner1Id = partner1.getId(); //Object to store the id of the parent object passed in the method
            int partner2Id = partner2.getId(); //Object to store the id of the parent object passed in the method
            int relationExists = -1; //Object to store the count of a symmetric association relation

            if(partner1Id==partner2Id) //If the partners both are the same person do not record the relationship!
            {
                System.out.println("A person can't be symmetrically dissociated with themselves! Cannot record this relationship");
                return false;
            }

            String partner1Name= partner1.getName(); //Object to store the person's Name
            String partner2Name= partner2.getName(); //Object to store the person's Name
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306", "benny", "B00899629");
            System.out.println("Connection established with schema " + schema + " successfully");

            stmt = con.createStatement();
            stmt.execute("use " + schema + ";");

            String query = "select distinct person_id,person_name from people";//Retrieve all the people in the database
            PreparedStatement prepStatement = con.prepareStatement(query);

            resultSet = prepStatement.executeQuery();

            while(resultSet.next())//Store the Ids and names of people in the database in a Map to check if both the partners exist in the database before recording a relationship
            {
                people.put(resultSet.getInt(1),resultSet.getString(2));
            }
            if (!people.containsKey(partner1Id) || (people.containsKey(partner1Id) && !Objects.equals(people.get(partner1Id), partner1Name)))//Throw an exception if Partner 1 NAME does not exist
            {
                throw new PersonNotFoundException("Partner with Id - " + partner1Id + " and Name - " + partner1Name + " cannot be found");
            }
            if (!people.containsKey(partner2Id) || (people.containsKey(partner2Id) && !Objects.equals(people.get(partner2Id), partner2Name)))//Throw an exception if Partner 2 NAME does not exist
            {
                throw new PersonNotFoundException("Partner with Id - " + partner2Id + " and Name - " + partner2Name + " cannot be found");
            }

            String queryLatestEvent = "select * from partner_information_history where " +
                    "partner1=? or partner2=? order by record_date desc limit 1;"; //Common Query to Return the latest record (if any) for partner 1 or partner 2
            prepStatement = con.prepareStatement(queryLatestEvent);
            //Retrieve any row where any partner Id in the Table = Partner 1's ID
            prepStatement.setInt(1, partner1Id);
            prepStatement.setInt(2, partner1Id);
            resultSet = prepStatement.executeQuery();
/*
By default, only one ResultSet object per Statement object can be open at the same time.
Therefore, if the reading of one ResultSet object is interleaved with the reading of another,
each must have been generated by different Statement objects.
Source: https://docs.oracle.com/javase/7/docs/api/java/sql/Statement.html
 */
            PreparedStatement prepStatement2 = con.prepareStatement(queryLatestEvent);
            //Retrieve any row where any partner Id in the Table = Partner 2's ID
            prepStatement2.setInt(1, partner2Id);
            prepStatement2.setInt(2, partner2Id);
            resultSet2 = prepStatement2.executeQuery();
            int resultSet1Size = resultSet.last() ? resultSet.getRow() : 0;//If there are no events for persorn1 , return 0 else the number of rows whill always be 1)           resultSet.beforeFirst();//Resetting the pointer
            resultSet.beforeFirst();
            int resultSet2Size = resultSet2.last() ? resultSet2.getRow() : 0;//If there are no events for persorn1 , return 0 else the number of rows whill always be 1)
            resultSet2.beforeFirst();//Resetting the pointer

            if(resultSet1Size>0 && resultSet2Size>0) //Both people have marriages recorded, need to check if they are married to each other or other people
            {
                resultSet.next();
                resultSet2.next();
                if((resultSet.getInt(1)==partner1Id || resultSet.getInt(2)==partner1Id) && //Any 1 column contains Partner 1's ID
                        (resultSet.getInt(1)==partner2Id || resultSet.getInt(2)==partner2Id)) //Any 1 column contains Partner 2's ID
                {
                    if(resultSet.getString(3).equals("Marriage") && resultSet2.getString(3).equals("Marriage"))// Check if both the events were recent marriages
                    {
                        //Valid attempt to record a dissolution, since the partners are symmetrically partnered. Therefore INSERT A NEW RECORD IN THIS "HISTORY" TABLE WITH THE THE CURRENT TIME!
    //                        resultSet2.beforeFirst();//Reset the cursor for ResultSet
    //                        resultSet.beforeFirst();//Reset the cursor for ResultSet

                            String queryInsertRelationship = "insert into partner_information_history values(?,?,?,?)";
                            prepStatement = con.prepareStatement(queryInsertRelationship);
                            prepStatement.setInt(1, partner1Id);
                            prepStatement.setInt(2, partner2Id);
                            prepStatement.setNString(3, "Dissolution");
                            prepStatement.setTimestamp(4,new java.sql.Timestamp(new java.util.Date().getTime())); //Record the Timestamp when the record was inserted
                            prepStatement.execute();
                            System.out.println("A Symmetric dissolution between Partner 1 with Id - " + partner1Id + " and Partner 2 with Id - " + partner2Id + " has been added to the database");
                            return true;

                    }
                    /*Else if this is anm attempt to insert a duplicate record of dissolution between the Two Partners, throw an Exception*/
                    else if(resultSet.getString(3).equals("Dissolution") && resultSet2.getString(3).equals("Dissolution"))// Check if both the events were recent marriages
                    {

                            resultSet2.beforeFirst();//Reset the cursor for ResultSet
                            resultSet.beforeFirst();//Reset the cursor for ResultSet
                            throw new DuplicateRecordException("A symmetric dissolution already exists between " +
                                    "Partner with Id - " + partner1Id + " and Partner with Id - " + partner2Id);
                    }
                }
                else //The two persons are not partnered together, but with other people. Return false
                {
                    resultSet2.beforeFirst();//Reset the cursor for ResultSet
                    resultSet.beforeFirst();//Reset the cursor for ResultSet
                    System.out.println("Partners with Ids - " + partner1Id + " and " + partner2Id + " are not Symmetrically Partnered. Therefore, they cannot be dissociated. Cannot record this in the table!");
                    return false;
                }
            }

            else //At least one of the two persons is not partnered with anyone, therefore, they cannot be dissociated. Return false
            {
                resultSet2.beforeFirst();//Reset the cursor for ResultSet
                resultSet.beforeFirst();//Reset the cursor for ResultSet
                System.out.println("Partners with Ids - " + partner1Id + " and " + partner2Id + " are not Symmetrically Partnered. Therefore, they cannot be dissociated. Cannot record this in the table!");
                return false;
            }
            prepStatement.close();
            prepStatement2.close();
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
            if (resultSet2 != null)
            {resultSet2.close();}
            if (con != null)
            {con.close();}
            if (stmt != null)
            {stmt.close();}
        }
        return false;

    }

    FileIdentifier addMediaFile( String fileLocation ) throws ClassNotFoundException,SQLException,DuplicateRecordException //File location is an absolute path to a media file in an archive of visual data
    {
        FileIdentifier file = new FileIdentifier(fileLocation);
        if(fileLocation!=null && !fileLocation.equalsIgnoreCase("")) //If fileLocation is null or an empty string do not insert!
        {
            Connection con=null;
            Statement stmt=null;
            ResultSet fileExists=null;
            ResultSet resultSet=null;
            int exists=-1; //Object to store the count of records in media_archive with path=fileLocation
            int insertKey = 0; //Object to store the Unique Id
            String schema="benny";//Schema fileLocation, currently hardcoded. Will be modified later
            try
            {
                Class.forName("com.mysql.jdbc.Driver");
                con= DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306","benny","B00899629");
                System.out.println("Connection established with " + schema + " successfully");

                stmt=con.createStatement();
                stmt.execute ("use "+schema+";"); //Select a schema

                String queryFileExists="select count(*) from media_archive where media_path=?"; /*Check if the file already exists in the archive*/
                PreparedStatement prepStatement = con.prepareStatement(queryFileExists,Statement.RETURN_GENERATED_KEYS);
                prepStatement.setString(1,null); //Set as null for auto_increment
                prepStatement.setString(2, fileLocation); //Set fileLocation
                fileExists=prepStatement.executeQuery();

                if(fileExists.next())
                {
                    exists = fileExists.getInt(1);
                }
                if(exists>0)
                {
                    throw new DuplicateRecordException("File with path - " + fileLocation + " already exists in the archive! Cannot record this file again!");
                }

                else
                {
                    String queryInsert = "insert into media_archive values(?,?)"; /*insert the fileLocation into the database,
                the Unique Id for the File is automatically generated*/
                    PreparedStatement prepStatement2 = con.prepareStatement(queryInsert, Statement.RETURN_GENERATED_KEYS);
                    prepStatement2.setString(1, null); //Set as null for auto_increment
                    prepStatement2.setString(2, fileLocation); //Set fileLocation
                    prepStatement2.execute();
                    resultSet = prepStatement2.getGeneratedKeys();
                    if (resultSet.next()) {
                        insertKey = resultSet.getInt(1); //Retrieve the Unique ID for the File
                        file.uniqueFileId = insertKey;
                    }
                    else {
                        throw new SQLException("Failed to add the file with path - " + fileLocation + " to the archive!"); /* If the Id wasn't retrieved this means
                    that the insert operation failed*/
                    }
                }
                System.out.println("File with path - " + fileLocation + " has been added to the archive with Id: " + insertKey);

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
        }
        else
        {
            throw new IllegalArgumentException("Invalid filepath, file was not added to the archive"); /*If invalid parameter for fileLocation is encountered
            do not add person to the database*/
        }
        return file;
    }
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
    { //INCLUDE INSERT DATES IN THE DATABASE FOR NOTES AND REFERENCE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        Genealogy familyTree = new Genealogy();
//        FileIdentifier invalidFile1 = new FileIdentifier();
//        FileIdentifier invalidFile1 = new FileIdentifier();
//        FileIdentifier invalidFile1 = new FileIdentifier();
//        FileIdentifier invalidFile1 = new FileIdentifier();
//        FileIdentifier invalidFile1 = new FileIdentifier();


        try {
            String nullString=null;
            FileIdentifier fileCheck = familyTree.addMediaFile(""); //Test for blanks
            System.out.println("Return value: " + "File Id - " + fileCheck.getUniqueFileId() + " Filepath: " + fileCheck.getFileLocation());

//            FileIdentifier fileCheck = familyTree.addMediaFile(nullString); //Test for Nulls
//            System.out.println("Return value: " + "File Id - " + fileCheck.getUniqueFileId() + " Filepath: " + fileCheck.getFileLocation());
//
//            FileIdentifier fileCheck = familyTree.addMediaFile(nullString); //Test for Valid Input 1
//            System.out.println("Return value: " + "File Id - " + fileCheck.getUniqueFileId() + " Filepath: " + fileCheck.getFileLocation());
//
//            FileIdentifier fileCheck = familyTree.addMediaFile(nullString); //Test for Valid Input 2
//            System.out.println("Return value: " + "File Id - " + fileCheck.getUniqueFileId() + " Filepath: " + fileCheck.getFileLocation());
//
//            FileIdentifier fileCheck = familyTree.addMediaFile(nullString); //Test for Duplicate Input
//            System.out.println("Return value: " + "File Id - " + fileCheck.getUniqueFileId() + " Filepath: " + fileCheck.getFileLocation());

//            boolean check1 = familyTree.recordDissolution(person0,person00);//#2 Invalid Partners
//            System.out.println(" Exception Check: " + check1);

//            boolean check2 = familyTree.recordDissolution(person10,person0);//#1 Invalid Partners
//            System.out.println(" Exception Check: " + check2);
//
//            boolean check3 = familyTree.recordDissolution(person4,person8);//Valid partnering relation, can be divorced
//            System.out.println(" True Check: " + check3);
//
//            boolean check3 = familyTree.recordDissolution(person4,person8);//Test for attempt to insert a duplicate relation
//            boolean check3 = familyTree.recordDissolution(person4,person8);
//            System.out.println(" Duplicate Exception Check: " + check3);

            //Test for attempt to record a duplicate relation by changing the order of the partners
//            boolean check3 = familyTree.recordDissolution(person4,person8);
//            boolean check4 = familyTree.recordDissolution(person8,person4);
//            System.out.println(" Exception Check: " + check4);

            //Test for attempt to record a relation between the same person (partner1==partner2)
//            boolean check3 = familyTree.recordDissolution(person1,person1);
//            System.out.println(" False Check: " + check3);
//
//
//
            //Test for any other scenario (any one of person OR both the person are married to someone else
//            boolean check3 = familyTree.recordPartnering(person4,person8);
//            System.out.println(" False Check: " + check3);
//            boolean check5 = familyTree.recordPartnering(person6,person9);
//            System.out.println(" True Check: " + check5);
//            boolean check6 = familyTree.recordDissolution(person4,person9);
//            System.out.println(" False Check: " + check6);
//
//            boolean check3 = familyTree.recordPartnering(person4,person8);
//            System.out.println(" True Check: " + check3);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
}

