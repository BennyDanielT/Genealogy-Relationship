import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
public class Genealogy
{

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

        if(!keys.isEmpty() && !keys.contains(blanks) && !attributes.containsValue(null) && !values.contains(blanks)) //If attribute key or value is null or an empty string do not insert!
        {
            Connection con = null;
            Statement stmt = null;
            ResultSet resultSet = null;
            String schema = "benny";//Schema fileLocation, currently hardcoded. Will be modified later
            try {
                int id = person.getId(); //Object to store the id of the personIdentity object passed in the method
                int exists = -1; //Object to store the count of Ids in the table people_reference
                String name= person.getName(); //Object to store the person's name
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

                String queryInsertNotes = "insert into people_reference values(?,?,?,?)";
                prepStatement = con.prepareStatement(queryInsertNotes,Statement.RETURN_GENERATED_KEYS);
                prepStatement.setString(1, null);
                prepStatement.setInt(2, id);
                prepStatement.setString(3, reference);
                prepStatement.setTimestamp(4,new java.sql.Timestamp(new java.util.Date().getTime()));
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

                String queryInsertNotes = "insert into people_notes values(?,?,?,?)";
                prepStatement = con.prepareStatement(queryInsertNotes,Statement.RETURN_GENERATED_KEYS);
                prepStatement.setString(1, null);
                prepStatement.setInt(2, id);
                prepStatement.setString(3, note);
                prepStatement.setTimestamp(4,new java.sql.Timestamp(new java.util.Date().getTime()));
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
                List<Integer> parentsToBeRecorded = new ArrayList<>();
                parentsToBeRecorded.add(parentId);

                //RECORDING AN ENTRY FOR THE SPOUSE (IF SPOSUE EXISTS)
                //Check if the Person is married
                String queryLatestEvent = "select * from partner_information_history where " +
                        "partner1=? or partner2=? order by record_date desc limit 1;"; //Return the latest record (if any) for partner 1 or partner 2
                PreparedStatement prepStatement2 = con.prepareStatement(queryLatestEvent);
                //Retrieve any row where any partner Id in the Table = Partner 1's ID
                prepStatement2.setInt(1, parentId);
                prepStatement2.setInt(2, parentId);
                resultSet = prepStatement2.executeQuery();
/*
By default, only one ResultSet object per Statement object can be open at the same time.
Therefore, if the reading of one ResultSet object is interleaved with the reading of another,
each must have been generated by different Statement objects.
Source: https://docs.oracle.com/javase/7/docs/api/java/sql/Statement.html
 */
                int resultSet1Size = resultSet.last() ? resultSet.getRow() : 0;
                resultSet.beforeFirst();

                if(resultSet1Size>0) //Parent has an event recorded. Need to check if it's "Marriage"
                {
                    resultSet.next();
                    //Reset the cursor for ResultSet.
                    if(resultSet.getString(3).equals("Marriage"))//If it's a marriage, retrieve the other Partner's ID
                    {
                        int partner2Id = resultSet.getInt(1)==parentId ? resultSet.getInt(2) : resultSet.getInt(1); //Retrieve the Id which's not equivalent to the current Partner's ID
                        parentsToBeRecorded.add(partner2Id);//Add the Spouse's ID to the list of parents
                        System.out.println("Partner with Id - " + partner2Id + " has been identified as a spouse of Parent with Id - " + parentId + ". Therefore a child will be recorded for both the parents!");
                    }
                    // Else, the parent is currently Unmarried. Therefore, do not record the child for any other Person.
                    resultSet.beforeFirst();//Reset the cursor for ResultSet
                }
                //End of Recording a child for the spouse
                for(int parents : parentsToBeRecorded)
                {
                    String queryInsertRelationship = "insert into children_information values(?,?)";
                    prepStatement = con.prepareStatement(queryInsertRelationship);
                    prepStatement.setInt(1, parents);
                    prepStatement.setInt(2, childId);
                    prepStatement.execute();
                    System.out.println("A parent-child relationship between person(Parent) - " + parents + " and person(Child) - " + childId + " has been added to the database");
                }
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

    Boolean recordPartnering( PersonIdentity partner1, PersonIdentity partner2 ) throws PersonNotFoundException,ClassNotFoundException,SQLException, DuplicateRecordException
    {
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
                PreparedStatement prepStatement = con.prepareStatement(queryFileExists);
                prepStatement.setString(1, fileLocation); //Set fileLocation
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
                        System.out.println("File with path - " + fileLocation + " has been added to the archive with Id: " + insertKey);
                    }
                    else {
                        throw new SQLException("Failed to add the file with path - " + fileLocation + " to the archive!"); /* If the Id wasn't retrieved this means
                    that the insert operation failed*/
                    }
                }


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

    Boolean recordMediaAttributes( FileIdentifier fileIdentifier, Map<String, String> attributes ) throws ClassNotFoundException, SQLException,FileNotFoundException {
        String blanks=""; //Object to store blanks
        String nullString=null;
        String attribute; //Object to store intermediate attribute names
        String attribute_value; //Object to store intermediate attribute values
        Set<String> keys = new HashSet<>(); //Set of keys in the Map

        keys=attributes.keySet();
        System.out.println(keys);
        Collection<String> values = new ArrayList<>(); //Set of Values in the Map
        values=attributes.values();

        if(!keys.isEmpty() && !keys.contains(blanks) && !attributes.containsValue(null) && !values.contains(blanks)) //If attribute key or value is null or an empty string do not insert!
        {
            Connection con = null;
            Statement stmt = null;
            ResultSet resultSet = null;
            String schema = "benny";//Schema fileLocation, currently hardcoded. Will be modified later
            try {
                int id = fileIdentifier.getId(); //Object to store the id of the fileIdentifier object passed in the method
                int exists = -1; //Object to store the count of Ids in the table media_archive
                String name= fileIdentifier.getFileLocation(); //Object to store the file's Location
                Class.forName("com.mysql.jdbc.Driver");
                con = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306", "benny", "B00899629");
                System.out.println("Connection established with schema " + schema + " successfully");

                stmt = con.createStatement();
                stmt.execute("use " + schema + ";");

                String query = "select count(media_id) from media_archive where media_id=? and media_path=?";//Check if file exists
                PreparedStatement prepStatement = con.prepareStatement(query);
                prepStatement.setInt(1, id);
                prepStatement.setString(2, name);
                resultSet = prepStatement.executeQuery();
                if (resultSet.next()) {
                    exists = resultSet.getInt(1); //retrieve the count of files with the given id (Maximum of 1 Minimum 0)
                }

                if (exists <= 0)//Throw an exception if the File does not exist
                {
                    throw new FileNotFoundException("File with Id - " + id + " and Path - " + name + " cannot be found");
                }

                System.out.println("List of Attributes for File with Id - " + id + ":");
                //Display the attributes currently stored for this file in the database
                String queryCurrentAttributes = "select distinct attribute_name as Attribute,attribute_value as Value" +
                        " from media_attributes where media_id=?";
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
                    String queryAttributeExists = "select count(media_id) from media_attributes where media_id=? and attribute_name=?";
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
                        String queryUpdateAttribute = "update media_attributes set attribute_value=? where media_id=? and attribute_name=?";
                        prepStatement = con.prepareStatement(queryUpdateAttribute);
                        prepStatement.setString(1, attribute_value);
                        prepStatement.setInt(2, id);
                        prepStatement.setString(3, attribute);
                        prepStatement.execute();
                        System.out.println("An attribute - " + attribute + " has been updated with value - " + attribute_value);
                    }
                    else //Attribute is new. Therefore, Insert it into the table
                    {
                        String queryInsertAttribute = "insert into media_attributes values(?,?,?)";
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
//
    Boolean peopleInMedia( FileIdentifier fileIdentifier, List<PersonIdentity> people ) throws ClassNotFoundException, SQLException,FileNotFoundException {

        String attribute; //Object to store intermediate attribute names
        String attribute_value; //Object to store intermediate attribute values
        Set<String> keys = new HashSet<>(); //Set of keys in the Map
            Connection con = null;
            Statement stmt = null;
            ResultSet resultSet = null;
            String schema = "benny";//Schema fileLocation, currently hardcoded. Will be modified later
            try {
                int idFile = fileIdentifier.getId(); //Object to store the id of the fileIdentifier object passed in the method
                int existsFile = -1; //Object to store the count of Ids in the table media_archive
                String nameFile= fileIdentifier.getFileLocation(); //Object to store the file's Location

                Map<Integer,String> validPeople = new HashMap<>();
                List<Integer> existsFilePerson = new ArrayList<>();

                Class.forName("com.mysql.jdbc.Driver");
                con = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306", "benny", "B00899629");
                System.out.println("Connection established with schema " + schema + " successfully");

                stmt = con.createStatement();
                stmt.execute("use " + schema + ";");

                String query = "select count(media_id) from media_archive where media_id=? and media_path=?";//Check if file exists
                PreparedStatement prepStatement = con.prepareStatement(query);
                prepStatement.setInt(1, idFile);
                prepStatement.setString(2, nameFile);
                resultSet = prepStatement.executeQuery();
                if (resultSet.next())
                {
                    existsFile = resultSet.getInt(1); //retrieve the count of files with the given id (Maximum of 1 Minimum 0)
                }
                if (existsFile <= 0)//Throw an exception if the File does not exist
                {
                    throw new FileNotFoundException("File with Id - " + idFile + " and Path - " + nameFile + " cannot be found");
                }

                query = "select distinct person_id,person_name from people";//Retrieve all the people in the database
                PreparedStatement prepStatement2 = con.prepareStatement(query);
                resultSet = prepStatement2.executeQuery();
                while(resultSet.next())//Store the Ids and names of people in the database in a Map to check if both the partners exist in the database before recording a relationship
                {
                    validPeople.put(resultSet.getInt(1),resultSet.getString(2));
                }

                query = "select distinct person_id from people_in_media where media_id=?";//Retrieve all existing records for this file and add it to a Map
                PreparedStatement prepStatementDupCheck = con.prepareStatement(query);
                prepStatementDupCheck.setInt(1, idFile);
                resultSet = prepStatementDupCheck.executeQuery();
                while(resultSet.next())//Store the Ids of people, in the database, in a List to check if the instance of File and person exists in the database before recording the instance
                {
                    existsFilePerson.add(resultSet.getInt(1));
                }
/*Add people who exist, to the database.
Else if an instance consists of a person who doesn't exist do not add the person,
let the user know that the person does not exist and ADD THE OTHER people*/
                for(PersonIdentity person : people)
                {
                    if(existsFilePerson.contains(person.getId())) //If the person is already associated with the Media File in the Database, state the same and continue
                    {
                        System.out.println("Person with Id - " + person.getId() + " and Name - " + person.getName() + " is already associated with this file. Therefore, an instance of the person in the media file cannot be recorded again.");
                    }
                    else if (validPeople.containsKey(person.getId()) && Objects.equals(validPeople.get(person.getId()), person.getName()))//Check if Person exists before Insertion
                    {
                        String queryInsertInstance = "insert into people_in_media values(?,?)";
                        PreparedStatement prepStatement3 = con.prepareStatement(queryInsertInstance);
                        prepStatement3.setInt(1, idFile);
                        prepStatement3.setInt(2, person.getId());
                        prepStatement3.execute();
                        System.out.println("An instance of a person with Id - " + person.getId() + " appearing in Media file with Id - " + idFile + " has been recorded in the database");
                        prepStatement3.close();
                        return true;
                    }
                    else
                    {
                        System.out.println("Person with Id - " + person.getId() + " and Name - " + person.getName() + " does not exist. Therefore, an instance of the person in the media file cannot be recorded.");
                    }
                }
                prepStatement.close();
                prepStatement2.close();
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
        return false;
    }
//
    Boolean tagMedia( FileIdentifier fileIdentifier, String tag ) throws ClassNotFoundException, SQLException,FileNotFoundException
    {
        if(tag!=null && !tag.equalsIgnoreCase("")) //If reference is null or an empty string do not insert!
        {
            Connection con = null;
            Statement stmt = null;
            ResultSet resultSet = null;
            String schema = "benny";//Schema fileLocation, currently hardcoded. Will be modified later
            try {
                int idFile = fileIdentifier.getId(); //Object to store the id of the fileIdentifier object passed in the method
                int existsFile = -1; //Object to store the count of Ids in the table media_archive
                String nameFile = fileIdentifier.getFileLocation(); //Object to store the file's Location
                Map<Integer, String> validPeople = new HashMap<>(); //Map to store people who exist in the database
                List<String> existingTags = new ArrayList<>();

                Class.forName("com.mysql.jdbc.Driver");
                con = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306", "benny", "B00899629");
                System.out.println("Connection established with schema " + schema + " successfully");

                stmt = con.createStatement();
                stmt.execute("use " + schema + ";");

                String query = "select count(media_id) from media_archive where media_id=? and media_path=?";//Check if file exists
                PreparedStatement prepStatement = con.prepareStatement(query);
                prepStatement.setInt(1, idFile);
                prepStatement.setString(2, nameFile);
                resultSet = prepStatement.executeQuery();
                if (resultSet.next()) {
                    existsFile = resultSet.getInt(1); //retrieve the count of files with the given id (Maximum of 1 Minimum 0)
                }
                if (existsFile <= 0)//Throw an exception if the File does not exist
                {
                    throw new FileNotFoundException("File with Id - " + idFile + " and Path - " + nameFile + " cannot be found");
                }

                query = "select distinct tag from media_tag where media_id=?";//Retrieve all existing tags for this file and add it to a List
                PreparedStatement prepStatementDupCheck = con.prepareStatement(query);
                prepStatementDupCheck.setInt(1, idFile);
                resultSet = prepStatementDupCheck.executeQuery();
                while(resultSet.next())//Store the existing tags in a List to check if the instance of File and tag already exists in the database before recording the instance
                {
                    existingTags.add(resultSet.getString(1)); //Add the Tag to the List
                }

                if(existingTags.contains(tag))
                {
                    System.out.println("Tag - " + tag + " is already associated with this file. Therefore, an association of the Tag & Media file cannot be recorded again.");
                    return false;
                }

                else
                {
                    String queryInsertNotes = "insert into media_tag values(?,?,?)";
                    prepStatement = con.prepareStatement(queryInsertNotes, Statement.RETURN_GENERATED_KEYS);
                    prepStatement.setString(1, null);
                    prepStatement.setInt(2, idFile);
                    prepStatement.setString(3, tag);
                    prepStatement.execute();

                    int insertKey = 0; //Object to store the Unique Id
                    resultSet = prepStatement.getGeneratedKeys();
                    if (resultSet.next()) {
                        insertKey = resultSet.getInt(1); //Retrieve the Unique ID for the person
                    } else {
                        throw new SQLException("Failed to add the Tag in the database!"); /* If the Id wasn't retrieved this means
                    that the insert operation failed*/
                    }
                    System.out.println("A Tag has been added to the database with Id - " + insertKey);

                    prepStatement.close();
                    return true;
                }

            } catch (ClassNotFoundException e) {
                System.out.println("Class not found. Please verify that the appropriate .jar files and classes are set up");
                e.printStackTrace();
            } catch (SQLException e) {
                System.out.println("Error while trying to access the database!");
                e.printStackTrace();
            } finally {//Close Connection objects
                if (resultSet != null) {
                    resultSet.close();
                }
                if (con != null) {
                    con.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            }
        }
        else
            System.out.println("Invalid Tag, Tag was not added to the database!");
            return false;
    }
//
    Set<PersonIdentity> findPerson( String name ) throws SQLException
    {
        Connection con = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        String schema = "benny";//Schema fileLocation, currently hardcoded. Will be modified later
        if(name!=null && !name.equalsIgnoreCase("")) //If name is null or an empty string do not retrieve the PersonIdentity Object (Don't bother wasting time and memory!)
        {
            Set<PersonIdentity> peopleWithName = new HashSet<>(); //Set of people with the given name
            PersonIdentity person;
            try
            {
                Class.forName("com.mysql.jdbc.Driver");
                con = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306", "benny", "B00899629");
                System.out.println("Connection established with schema " + schema + " successfully");

                stmt = con.createStatement();
                stmt.execute("use " + schema + ";");
                String query = "select distinct person_id,person_name from people where person_name=?";//Retrieve all People with the given name
                PreparedStatement prepStatementDupCheck = con.prepareStatement(query);
                prepStatementDupCheck.setString(1, name);
                resultSet = prepStatementDupCheck.executeQuery();
                while (resultSet.next())
                {
                    person = new PersonIdentity(resultSet.getInt(1),name); //Create a new PersonIdentity Object and insert it into the Set
                    peopleWithName.add(person);
                }
                if(peopleWithName.size()>0)
                    return peopleWithName;
                else
                    System.out.println("No person with the given name exists!");//if no people with the given name exist, return an empty object
            }
            catch (ClassNotFoundException e) {
                System.out.println("Class not found. Please verify that the appropriate .jar files and classes are set up");
                e.printStackTrace();
            } catch (SQLException e) {
                System.out.println("Error while trying to access the database!");
                e.printStackTrace();
            } finally {//Close Connection objects
                if (resultSet != null) {
                    resultSet.close();
                }
                if (con != null) {
                    con.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            }
        }
        else
            System.out.println("Invalid name, person cannot be retrieved!");//If an invalid name is entered return null
            return null;
    }
//
    FileIdentifier findMediaFile( String name ) throws SQLException {
        Connection con = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        String schema = "benny";//Schema name, currently hardcoded. Will be modified later
        if(name!=null && !name.equalsIgnoreCase("")) //If file name is null or an empty string do not retrieve the  Object (Don't bother wasting time and memory!)
        {
            FileIdentifier file;
            try
            {
                Class.forName("com.mysql.jdbc.Driver");
                con = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306", "benny", "B00899629");
                System.out.println("Connection established with schema " + schema + " successfully");

                stmt = con.createStatement();
                stmt.execute("use " + schema + ";");
                String query = "select distinct media_id,media_path from media_archive where media_path=?";//Retrieve File with the given name
                PreparedStatement prepStatementDupCheck = con.prepareStatement(query);
                prepStatementDupCheck.setString(1, name);
                resultSet = prepStatementDupCheck.executeQuery();

                if(resultSet.next())
                {
                    file = new FileIdentifier(resultSet.getInt(1),name);
                    return file;
                }
                else
                    System.out.println("No File with the given path exists!");
            }
            catch (ClassNotFoundException e) {
                System.out.println("Class not found. Please verify that the appropriate .jar files and classes are set up");
                e.printStackTrace();
            } catch (SQLException e) {
                System.out.println("Error while trying to access the database!");
                e.printStackTrace();
            } finally {//Close Connection objects
                if (resultSet != null) {
                    resultSet.close();
                }
                if (con != null) {
                    con.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            }
        }
        else
            System.out.println("Invalid path, File cannot be retrieved!"); //Filename provided is invalid and wouldn't exist since we aren't storing files with blanks names
            return null;

    }
//
    String findName( PersonIdentity id ) throws SQLException {
        Connection con = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        String schema = "benny";//Schema name, currently hardcoded. Will be modified later
        try {
            int exists = -1; //Object to store the count of Ids in the table people_reference
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306", "benny", "B00899629");
            System.out.println("Connection established with schema " + schema + " successfully");

            stmt = con.createStatement();
            stmt.execute("use " + schema + ";");

            String query = "select person_id,person_name from people where person_id=?"; //Retrieve the person's name where the person's Id is identical to the Id of the person passed to the method
            PreparedStatement prepStatement = con.prepareStatement(query);
            prepStatement.setInt(1, id.getId());
            resultSet = prepStatement.executeQuery();
            if (resultSet.next())
            {
                return resultSet.getString(2);

            }
            else//Throw an exception if the person does not exist
            {
                throw new PersonNotFoundException("Person with Id - " + id.getId() + " cannot be found");
            }

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
        return null;
    }

//
    String findMediaFile( FileIdentifier fileId ) throws SQLException {
        Connection con = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        String schema = "benny";//Schema name, currently hardcoded. Will be modified later
        try {
            int exists = -1; //Object to store the count of Ids in the table people_reference
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306", "benny", "B00899629");
            System.out.println("Connection established with schema " + schema + " successfully");

            stmt = con.createStatement();
            stmt.execute("use " + schema + ";");

            String query = "select media_id,media_path from media_archive where media_id=?";//Retrieve Media Path(name) where media_id is identical to the ID of the object passed
            PreparedStatement prepStatement = con.prepareStatement(query);
            prepStatement.setInt(1, fileId.getId());
            resultSet = prepStatement.executeQuery();
            if (resultSet.next())
            {
                return resultSet.getString(2);

            }
            else//Throw an exception if the File does not exist
            {
                throw new FileNotFoundException("File with Id - " + fileId.getId() + " cannot be found");
            }

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
        return null;

    }
//
    BiologicalRelation findRelation( PersonIdentity person1, PersonIdentity person2 ) throws SQLException
    {
        BiologicalRelation relation = new BiologicalRelation();
        int nearestAncestor=-1; //Temp variable to store the id of the nearest ancestor
        int level=Integer.MAX_VALUE; //temporary varial to store lowest level value
        int levelPerson1=Integer.MAX_VALUE;//Level of Person 1 to nearest ancestor
        int levelPerson2=Integer.MAX_VALUE;//Level of Person 2 to nearest ancestor
        int cousinship;
        int removal;
        //Maps to store the nearest ancestor of each person
        Map<Integer,Integer> person1NearestAncestor = new HashMap<>();
        Map<Integer,Integer> person2NearestAncestor = new HashMap<>();
        //Retrieve nearest ancestors
        person1NearestAncestor = nearestAncestor(person1);
        person2NearestAncestor = nearestAncestor(person2);
        //Add each person in the list of ancestors as one person may turn out to be the nearest ancestor of another
        person1NearestAncestor.put(person1.getId(),0);
        person2NearestAncestor.put(person2.getId(),0);

        for(var entryset : person1NearestAncestor.entrySet())
        {//If the ancestor is a common one and the level is lesser than the previously stored level, store the new ancestor and level as the nearest
            if(person2NearestAncestor.containsKey(entryset.getKey()) && (entryset.getValue()<level))
            {
                nearestAncestor = entryset.getKey();
                level = entryset.getValue();
            }
        }
        if(nearestAncestor==-1) //if there's no common ancestor, they're not biologically related
        {
            System.out.println("Not Biologically Related");
            return null;
        }
        levelPerson1 = person1NearestAncestor.get(nearestAncestor);
        levelPerson2 = person2NearestAncestor.get(nearestAncestor);
        if(levelPerson1==Integer.MAX_VALUE && levelPerson2==Integer.MAX_VALUE)
        {
            System.out.println("Not Biologically Related");
            return null;
        }
        //Formula for degree of cousinship and level of removal
        cousinship = (Math.min(levelPerson1,levelPerson2)-1);
        removal = Math.abs(levelPerson1-levelPerson2);
        relation.degreeOfCousinship=cousinship;
        relation.levelOfRemoval=removal;
        return relation;
    }

    Map<Integer,Integer> nearestAncestor( PersonIdentity person) throws SQLException //method to determine ALL ancestors of a person (for use in FindRelation)
    {
        Map<Integer,String> people = new HashMap<>();
        Map<Integer,Integer> nearestAncestor = new HashMap<>();

        Connection con = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        String schema = "benny";
        try {
            int personId = person.getId(); //Object to store the id of the parent object passed in the method
            String personName= person.getName(); //Object to store the person's name
            Map<Integer,PersonIdentity> pcMap = new HashMap<>();

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
            if (!people.containsKey(personId) || (people.containsKey(personId) && !Objects.equals(people.get(personId), personName)))//Throw an exception if the parent does not exist
            {
                throw new PersonNotFoundException("Person with Id - " + personId + " and Name - " + personName + " cannot be found");
            }

            else //Person exists
            {
                //Retrieve parent and children from the database with Parent names
                String queryLatestEvent = "select parent_id,child_id,person_name from children_information c join people p on c.parent_id=p.person_id;";
                PreparedStatement prepStatement2 = con.prepareStatement(queryLatestEvent);
                //Retrieve any row where any partner Id in the Table = Partner 1's ID
                resultSet = prepStatement2.executeQuery();
                int parentId;
                int childId;
                String name;

                while(resultSet.next()) //Form a Map of Parent and Children and Build the Tree
                {
                    parentId=resultSet.getInt(1);
                    childId=resultSet.getInt(2);
                    name=resultSet.getString(3);
                    PersonIdentity newParent = new PersonIdentity(parentId,name);

                    if(pcMap.containsKey(childId)) //If the Map already has a key for this child this means that one of the parents is already Linked!
                    {
                        pcMap.get(childId).parent2=newParent;
                    }
                    else
                    {//link the second parent of the child to the child
                        PersonIdentity newChild = new PersonIdentity(childId);
                        newChild.parent1 = newParent;
                        pcMap.put(childId,newChild);
                    }
                }
                if(!pcMap.containsKey(personId)) //if the person is not a child of anyone
                {
                    System.out.println("No Ancestor recorded for this person!");
                    return nearestAncestor;
                }
                int currentGen=0;//Variable to store Current generation while traversing the tree
                if (pcMap.get(personId).parent1 != null)
                {
                     nearestAncestor = findNearestAncestor(pcMap.get(personId).parent1, pcMap, nearestAncestor, currentGen+1);
                }
                if (pcMap.get(personId).parent2 != null)
                {
                    nearestAncestor = findNearestAncestor(pcMap.get(personId).parent2, pcMap, nearestAncestor, currentGen+1);
                }
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
        return nearestAncestor;
    }

    Map<Integer,Integer> findNearestAncestor(PersonIdentity person, Map<Integer,PersonIdentity> pcMap, Map<Integer,Integer> nearestAncestor,int currGen)
    {
        if(person!=null)
        {
            nearestAncestor.put(person.getId(),currGen);//Add the person to the Map

            if(pcMap.containsKey(person.getId()))
            {//Traverse recursively (both maternal tree and paternal tree)
                nearestAncestor = findNearestAncestor(pcMap.get(person.getId()).parent1, pcMap, nearestAncestor, currGen+1);
                nearestAncestor = findNearestAncestor(pcMap.get(person.getId()).parent2, pcMap, nearestAncestor, currGen+1);
            }
        }
        return nearestAncestor;
    } //Recursive Method to traverse through a Tree of People and store ALL ancestors along with the generation level from the person in question (for use in FindRelation)

    Set<PersonIdentity> descendents( PersonIdentity person, Integer generations ) throws SQLException {
        Map<Integer,String> people = new HashMap<>();
        Set<PersonIdentity> descendants = new HashSet<>();

        Connection con = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        String schema = "benny";
        try {
            int personId = person.getId(); //Object to store the id of the parent object passed in the method
            String personName= person.getName(); //Object to store the person's name
            Map<Integer,PersonIdentity> pcMap = new HashMap<>();

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
            if (!people.containsKey(personId) || (people.containsKey(personId) && !Objects.equals(people.get(personId), personName)))//Throw an exception if the parent does not exist
            {
                throw new PersonNotFoundException("Person with Id - " + personId + " and Name - " + personName + " cannot be found");
            }
            if(generations<=0)
            {
                System.out.println("The value for Generations cannot be lesser than or equal to zero!");
            }
            else //Person exists
            {
                String queryLatestEvent = "select parent_id,child_id,person_name from children_information c join people p on c.child_id=p.person_id;";
                PreparedStatement prepStatement2 = con.prepareStatement(queryLatestEvent);
                //Retrieve any row where any partner Id in the Table = Partner 1's ID
                resultSet = prepStatement2.executeQuery();
                int parentId;
                int childId;
                String name;

                while(resultSet.next()) //Form a Map of Parent and Children and Build the Tree
                {
                    parentId=resultSet.getInt(1);
                    childId=resultSet.getInt(2);
                    name=resultSet.getString(3);
                    //PersonIdentity newParent = new PersonIdentity(parentId,name);

                    PersonIdentity newChild = new PersonIdentity(childId,name);

                    if(pcMap.containsKey(parentId)) //If the Map already has a key for this parent this means that one of the children is already Linked!
                    {
                        pcMap.get(parentId).children.add(newChild);
                    }
                    else
                    {//Add the child to the person's existing list of children
                        PersonIdentity newParent = new PersonIdentity(parentId);
                        newParent.children.add(newChild);
                        pcMap.put(parentId,newParent);
                    }
                }

                if(!pcMap.containsKey(personId)) //if the person is not a parent of anyone
                {
                    System.out.println("No Descendants recorded for this person!");
                    return descendants;
                }

                int currentGen=0; //variable to store current generation value during traversal
                for(PersonIdentity child : pcMap.get(personId).children)
                {
                    if(currentGen<generations)
                    {//Find the descendants for each child of the person
                        descendants = findDescendant(child, pcMap, descendants, currentGen+1,generations);
                    }
                }
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
        return descendants;
    } //method to retrieve the descendants of a person

    Set<PersonIdentity> findDescendant(PersonIdentity person, Map<Integer,PersonIdentity> pcMap, Set<PersonIdentity> descendants,int currGen, int generations)
    {
        if(person!=null)
        {
            descendants.add(person);
            if(pcMap.containsKey(person.getId()) && currGen<generations)
            {
                if(currGen<generations)
                {
                    for (PersonIdentity child : pcMap.get(person.getId()).children) {
                        descendants = findDescendant(child, pcMap, descendants, currGen + 1, generations);
                    }
                }
            }

        }
        return descendants;
    }//Recursive Method to traverse through a Tree of People and store descendants within a certain generation

    Set<PersonIdentity> ancestores( PersonIdentity person, Integer generations ) throws SQLException
    {
        Map<Integer,String> people = new HashMap<>();
        Set<PersonIdentity> ancestors = new HashSet<>();

        Connection con = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        String schema = "benny";
        try {
            int personId = person.getId(); //Object to store the id of the parent object passed in the method
            String personName= person.getName(); //Object to store the person's name
            Map<Integer,PersonIdentity> pcMap = new HashMap<>();

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
            if (!people.containsKey(personId) || (people.containsKey(personId) && !Objects.equals(people.get(personId), personName)))//Throw an exception if the parent does not exist
            {
                throw new PersonNotFoundException("Person with Id - " + personId + " and Name - " + personName + " cannot be found");
            }
            if(generations<=0)
            {
                System.out.println("The value for Generations cannot be lesser than or equal to zero!");
            }
            else //Person exists
            {
                String queryLatestEvent = "select parent_id,child_id,person_name from children_information c join people p on c.parent_id=p.person_id;";
                PreparedStatement prepStatement2 = con.prepareStatement(queryLatestEvent);
                //Retrieve any row where any partner Id in the Table = Partner 1's ID
                resultSet = prepStatement2.executeQuery();
                int parentId;
                int childId;
                String name;

                while(resultSet.next()) //Form a Map of Parent and Children and Build the Tree
                {
                    parentId=resultSet.getInt(1);
                    childId=resultSet.getInt(2);
                    name=resultSet.getString(3);
                    PersonIdentity newParent = new PersonIdentity(parentId,name);

                    if(pcMap.containsKey(childId)) //If the Map already has a key for this child this means that one of the parents is already Linked!
                    {
                        pcMap.get(childId).parent2=newParent;
                    }
                    else
                    {//Add the link the second parent with the child
                        PersonIdentity newChild = new PersonIdentity(childId);
                        newChild.parent1 = newParent;
                        pcMap.put(childId,newChild);
                    }
                }

                if(!pcMap.containsKey(personId)) //if the person is not a child of anyone
                {
                    System.out.println("No Ancestor recorded for this person!");
                    return ancestors;
                }
                int currentGen=0;
                if (pcMap.get(personId).parent1 != null) {
                    if(currentGen<generations)
                    {//Traverse through the ancestors of Parent 1
                        ancestors = findAncestor(pcMap.get(personId).parent1, pcMap, ancestors, currentGen+1,generations);
                    }
                }
                if (pcMap.get(personId).parent2 != null) {

                    if(currentGen<generations)
                    {//Traverse through the ancestors of Parent 2
                        ancestors = findAncestor(pcMap.get(personId).parent2, pcMap, ancestors, currentGen+1,generations);
                    }
                }
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
        return ancestors;
    }//method to retrieve the ancestors of a person

   static Set<PersonIdentity> findAncestor(PersonIdentity person, Map<Integer,PersonIdentity> pcMap, Set<PersonIdentity> ancestors,int currGen,int generations)
    {
        if(person!=null)
        {
            ancestors.add(person); //Add the person to the set
            if(pcMap.containsKey(person.getId()))
            {
                if(currGen<generations)
                {//Traverse Maternal and Paternal tress of the person
                    ancestors = findAncestor(pcMap.get(person.getId()).parent1, pcMap, ancestors, currGen+1, generations);
                    ancestors = findAncestor(pcMap.get(person.getId()).parent2, pcMap, ancestors, currGen+1, generations);
                }
            }

        }
        return ancestors;
    }//Recursive Method to traverse through a Tree of People and store ancestors within a certain generation

    List<String> notesAndReferences( PersonIdentity person ) throws SQLException {
        Connection con = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        List<String>notesAndReferences = new ArrayList<>();
        String schema = "benny";
        try {
            int exists = -1; //Object to store the count of Ids in the table people_reference
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306", "benny", "B00899629");
            System.out.println("Connection established with schema " + schema + " successfully");

            stmt = con.createStatement();
            stmt.execute("use " + schema + ";");

            String query = "select person_id,person_name from people where person_id=?";
            PreparedStatement prepStatement = con.prepareStatement(query);
            prepStatement.setInt(1, person.getId());
            resultSet = prepStatement.executeQuery();
            if (resultSet.next())
            {
                exists = resultSet.getInt(1); //retrieve the count of people with the given id (Maximum of 1 Minimum 0)

            }
            if (exists <= 0)//Throw an exception if the person does not exist
            {
                throw new PersonNotFoundException("Person with Id - " + person.getId() + " and Name - " + person.getName() + " cannot be found");
            }
//Select all notes and references in ascending order of date for the given person
            query = "select notes_id as id,person_id,notes as material,note_date from people_notes where person_id=?" +
                    " UNION ALL" +
                    " select reference_id,person_id,reference_material as material,note_date from people_reference where person_id=? order by note_date; ";
            PreparedStatement prepStatement2 = con.prepareStatement(query);
            prepStatement2.setInt(1, person.getId());
            prepStatement2.setInt(2, person.getId());
            resultSet = prepStatement2.executeQuery();

            while(resultSet.next())
            {
                notesAndReferences.add(resultSet.getString(3)); //Add the Notes and References to the List and Return It
            }
            if(notesAndReferences.size()==0) //If there're no notes or references return an empty object
                System.out.println("No notes and References are available for the person");

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
        return notesAndReferences;

    }

    Set<FileIdentifier> findMediaByTag( String tag , String startDate, String endDate) throws SQLException
    {
        Connection con = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        String query; //Query to be executed for retrieving Files
        Set<FileIdentifier>mediaByTag = new HashSet<>(); //Object to store medias which are associated with the given tag

        String schema = "benny";//Schema fileLocation, currently hardcoded. Will be modified later
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306", "benny", "B00899629");
            System.out.println("Connection established with schema " + schema + " successfully");

            stmt = con.createStatement();
            stmt.execute("use " + schema + ";");

            if(tag==null || tag.equalsIgnoreCase("")) //If tag is null or an empty no file can be retrieved
            {
                System.out.println("Tags with Null or Blank values are not recorded in the system therefore searching for these tags will not return any files!");
            }
            PreparedStatement prepStatement1;

            if(startDate==null || endDate==null) //Since Dates are null return All media files with the given Tag
            {
                query ="select distinct tag.media_id,media_path from media_archive archive" +
                        " join media_tag tag on archive.media_id=tag.media_id where tag=?;";
                prepStatement1 = con.prepareStatement(query);
                prepStatement1.setString(1, tag);
            }
            else //Dates are necessary. Only Media files with given tag and dates in the given range are returned
            {
                //Parse the Start and End dates (currently Strings) to Date format
                LocalDate startDt = LocalDate.parse(startDate);
                LocalDate endDt = LocalDate.parse(endDate);
                query="select distinct tag.media_id,media_path from media_archive archive" +
                        " join media_tag tag on archive.media_id=tag.media_id" +
                        " join media_attributes a on archive.media_id=a.media_id" +
                        " where tag=? and attribute_name=?  and cast(attribute_value as Date) between ? and ?;";
                prepStatement1 = con.prepareStatement(query);
                prepStatement1.setString(1, tag);
                prepStatement1.setString(2, "Date");
                prepStatement1.setDate(3, java.sql.Date.valueOf(startDt));
                prepStatement1.setDate(4,java.sql.Date.valueOf(endDt));
            }
            resultSet = prepStatement1.executeQuery();
            int fileId; //Temporary object to store the Id of each file
            String filePath;//Temporary object to store the path (name) of each file

            while(resultSet.next())//Retrieve the IDs and files and create FileIdentifier objects with them
            {
                fileId=resultSet.getInt(1);
                filePath=resultSet.getString(2);
                FileIdentifier newFile = new FileIdentifier(fileId,filePath);
                mediaByTag.add(newFile); //Add the file to the Set
            }
            if(mediaByTag.size()==0)
                System.out.println("No Media files available for this Tag");

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
        return mediaByTag;
    }

    Set<FileIdentifier> findMediaByLocation( String location, String startDate, String endDate) throws SQLException
    {
        Connection con = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        String query; //Query to be executed for retrieving Files
        Set<FileIdentifier>mediaByLocation = new HashSet<>();

        String schema = "benny";
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306", "benny", "B00899629");
            System.out.println("Connection established with schema " + schema + " successfully");

            stmt = con.createStatement();
            stmt.execute("use " + schema + ";");

            if(location==null || location.equalsIgnoreCase("")) //If Location is null or an empty no file can be retrieved since we are not storing any file attribute which's null or blank
            {
                System.out.println("Locations with Null or Blank values are not recorded in the system therefore searching for these locations will not return any files!");
            }
            PreparedStatement prepStatement1;

            if(startDate==null || endDate==null) //Since Dates are null return All media files with the given Location
            {
                query ="select distinct a.media_id,media_path from media_archive archive " +
                        "join media_attributes a on archive.media_id=a.media_id " +
                        "where attribute_name=? " +
                        "and attribute_value=?;";
                prepStatement1 = con.prepareStatement(query);
                prepStatement1.setString(1, "Location");
                prepStatement1.setString(2, location);
            }
            else //Dates are necessary. Only Media files with given tag and dates in the given range are returned
            {
                //Parse the Start and End dates (currently Strings) to Date format
                LocalDate startDt = LocalDate.parse(startDate);
                LocalDate endDt = LocalDate.parse(endDate);
                query="WITH files_location as\n" +
                        "(select distinct a.media_id,media_path from media_archive archive \n" +
                        "join media_attributes a on archive.media_id=a.media_id\n" +
                        "where attribute_name=?\n" +
                        "and attribute_value=?),\n" +
                        "files_date as\n" +
                        "(select distinct a.media_id,media_path from media_archive archive \n" +
                        "join media_attributes a on archive.media_id=a.media_id\n" +
                        "where attribute_name=?\n" +
                        "and cast(attribute_value as Date) between ? and ?)\n" +
                        "select distinct D.media_id,D.media_path from files_location L join files_date D on L.media_id=D.media_id;";
                prepStatement1 = con.prepareStatement(query);
                prepStatement1.setString(1, "Location");
                prepStatement1.setString(2, location);
                prepStatement1.setString(3, "Date");
                prepStatement1.setDate(4, java.sql.Date.valueOf(startDt));
                prepStatement1.setDate(5,java.sql.Date.valueOf(endDt));
            }
            resultSet = prepStatement1.executeQuery();
            int fileId;
            String filePath;

            while(resultSet.next())
            {
                fileId=resultSet.getInt(1);
                filePath=resultSet.getString(2);
                FileIdentifier newFile = new FileIdentifier(fileId,filePath);
                mediaByLocation.add(newFile); //Add the file to the Set
            }
            if(mediaByLocation.size()==0)
                System.out.println("No Media files available for this Tag");

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
        return mediaByLocation;
    }

    List<FileIdentifier> findIndividualsMedia( Set<PersonIdentity> people, String startDate, String endDate) throws ClassNotFoundException,SQLException {
        Connection con = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        String query; //Query to be executed for retrieving Files
        List<FileIdentifier>mediaByPeople = new ArrayList<>();

        String schema = "benny";
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306", "benny", "B00899629");
            System.out.println("Connection established with schema " + schema + " successfully");

            stmt = con.createStatement();
            stmt.execute("use " + schema + ";");

            PreparedStatement prepStatement1;
//Solution for dynamically creating parameterized queries depending upon the number of people in the Set passed to the method
            char[] parameters = new char[people.size() * 2 - 1];
            for (int i = 0; i < parameters.length; i++)
                parameters[i] = (i % 2 == 0 ? '?' : ','); //Hack to include "?" in Even positions of the array
            String paramterizedInClause = String.valueOf(parameters);

            if(startDate==null || endDate==null) //Since Dates are null return All media files with the given Location
            {
                query ="select distinct p.media_id,media_path from media_archive archive \n" +
                        "join people_in_media p on archive.media_id=p.media_id\n" +
                        "where person_id in (" + paramterizedInClause + ");";
                prepStatement1 = con.prepareStatement(query);
                int index=1;
                for(PersonIdentity person : people)
                {
                    prepStatement1.setInt(index, person.getId());
                    index++;
                }
            }
            else //Dates are necessary. Only Media files with given tag and dates in the given range are returned
            {
                //Parse the Start and End dates (currently Strings) to Date format
                LocalDate startDt = LocalDate.parse(startDate);
                LocalDate endDt = LocalDate.parse(endDate);
                //Retrieve Files with people and Dates & Files with People but without dates and Union them (sort by Date and file names)
                query="WITH files_with_date as\n" +
                        "(select distinct archive.media_id,media_path,person_id from media_archive archive \n" +
                        "join people_in_media p on archive.media_id=p.media_id\n" +
                        "join media_attributes a on archive.media_id=a.media_id\n" +
                        "where attribute_name=\"Date\"\n" +
                        "and cast(attribute_value as Date) between ? and ? \n" +
                        "order by cast(attribute_value as Date) ASC,media_path),\n" +
                        "files_without_date as\n" +
                        "(select distinct archive.media_id,media_path,person_id from media_archive archive \n" +
                        "join people_in_media p on archive.media_id=p.media_id\n" +
                        "where archive.media_id not in (select distinct media_id from media_attributes where attribute_name=\"Date\")\n" +
                        "order by media_path)\n" +
                        "select distinct media_id,media_path,1 as sort_order from files_with_date\n" +
                        "UNION \n" +
                        "select distinct media_id,media_path,2 as sort_order from files_without_date where person_id in (" + paramterizedInClause + ") order by sort_order,media_path;";
                prepStatement1 = con.prepareStatement(query);
                prepStatement1.setDate(1, java.sql.Date.valueOf(startDt));
                prepStatement1.setDate(2,java.sql.Date.valueOf(endDt));
                int index=3;
                for(PersonIdentity person : people)
                {
                    prepStatement1.setInt(index, person.getId());
                    index++;
                }
            }
            resultSet = prepStatement1.executeQuery();
            int fileId;
            String filePath;

            while(resultSet.next())//Retrieve Fild Ids and Path and store them in FileIdentifier objects
            {
                fileId=resultSet.getInt(1);
                filePath=resultSet.getString(2);
                FileIdentifier newFile = new FileIdentifier(fileId,filePath);
                mediaByPeople.add(newFile); //Add the file to the Set
            }
            if(mediaByPeople.size()==0)
                System.out.println("No Media files available for this Set of people");

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
        return mediaByPeople;

    }

    List<FileIdentifier> findBiologicalFamilyMedia(PersonIdentity person) throws SQLException {
        Connection con = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        String query; //Query to be executed for retrieving Files
        List<FileIdentifier>mediaByPeople = new ArrayList<>();
        List<Integer>childrenId = new ArrayList<>();

        String schema = "benny";//Schema name, currently hardcoded. Will be modified later
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306", "benny", "B00899629");
            System.out.println("Connection established with schema " + schema + " successfully");

            stmt = con.createStatement();
            stmt.execute("use " + schema + ";");

            String queryRetrieveChildren = "select child_id from children_information where parent_id=?;";//Find the children of this person
            PreparedStatement prepStatement = con.prepareStatement(queryRetrieveChildren);
            prepStatement.setInt(1, person.getId());
            resultSet = prepStatement.executeQuery();

            while(resultSet.next())
            {
                childrenId.add(resultSet.getInt(1));
            }
//Solution for parameterized queires depending on the number of children the person has
            PreparedStatement prepStatement1;
            char[] parameters = new char[childrenId.size() * 2 - 1];
            for (int i = 0; i < parameters.length; i++)
                parameters[i] = (i % 2 == 0 ? '?' : ','); //Hack to include "?" in Even positions of the array
            String paramterizedInClause = String.valueOf(parameters);

            query="WITH files_with_date as\n" +
                    "(select distinct archive.media_id,media_path,person_id from media_archive archive \n" +
                    "join people_in_media p on archive.media_id=p.media_id\n" +
                    "join media_attributes a on archive.media_id=a.media_id\n" +
                    "where attribute_name=\"Date\"\n" +
                    "order by cast(attribute_value as Date) ASC,media_path),\n" +
                    "files_without_date as\n" +
                    "(select distinct archive.media_id,media_path,person_id from media_archive archive \n" +
                    "join people_in_media p on archive.media_id=p.media_id\n" +
                    "where archive.media_id not in (select distinct media_id from media_attributes where attribute_name=\"Date\")\n" +
                    "order by media_path)\n" +
                    "select distinct media_id,media_path,1 as sort_order from files_with_date\n" +
                    "UNION \n" +
                    "select distinct media_id,media_path,2 as sort_order from files_without_date where person_id in (" + paramterizedInClause + ") order by sort_order,media_path;";
            prepStatement1 = con.prepareStatement(query);
            int index=1;
            for(int i : childrenId)
            {
                prepStatement1.setInt(index, i);
                index++;
            }

            resultSet = prepStatement1.executeQuery();
            int fileId;
            String filePath;

            while(resultSet.next())
            {
                fileId=resultSet.getInt(1);
                filePath=resultSet.getString(2);
                FileIdentifier newFile = new FileIdentifier(fileId,filePath);
                mediaByPeople.add(newFile); //Add the file to the Set
            }
            if(mediaByPeople.size()==0)
                System.out.println("No Media files available for this person's children");

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
        return mediaByPeople;
    }

//    //Sample Main Method to Test Methods
//    public static void main(String[] args)
//    {
//        Genealogy familyTree = new Genealogy();
//        PersonIdentity X = new PersonIdentity(1,"X");
//        PersonIdentity ADM = new PersonIdentity(2,"ADM");
//        PersonIdentity XX = new PersonIdentity(3,"XX");
//        PersonIdentity AO = new PersonIdentity(4,"AO");
//        PersonIdentity XXX = new PersonIdentity(5,"XXX");
//        PersonIdentity BDT = new PersonIdentity(6,"BDT");
//        PersonIdentity AOY = new PersonIdentity(7,"AOY");
//        PersonIdentity GRVM = new PersonIdentity(8,"GRVM");
//        PersonIdentity XXXX = new PersonIdentity(9,"XXXX");
//        PersonIdentity XXXY = new PersonIdentity(10,"XXXY");
//        PersonIdentity AOYX = new PersonIdentity(11,"AOYX");
//        PersonIdentity AOYY = new PersonIdentity(12,"AOYY");
//        PersonIdentity XXXYX = new PersonIdentity(13,"XXXYX");
//        PersonIdentity XXXYY = new PersonIdentity(14,"XXXYY");
//        PersonIdentity AOYXX = new PersonIdentity(15,"AOYXX");
//        PersonIdentity AOYXY = new PersonIdentity(16,"AOYXY");
//        PersonIdentity AOYYX = new PersonIdentity(17,"AOYYX");
//        PersonIdentity AOYYY = new PersonIdentity(18,"AOYYY");
//
//        try {
//            BiologicalRelation output = new BiologicalRelation();
//            BiologicalRelation output2 = new BiologicalRelation();
//            BiologicalRelation output3 = new BiologicalRelation();
//
//
//            System.out.println("------------------------");
//            output2 = familyTree.findRelation(AO,GRVM);
//            System.out.println("Cousinship: " + output2.getCousinship() + " Removal: " + output2.getRemoval());
//            System.out.println("------------------------");
//            output3 = familyTree.findRelation(ADM,XXXYY);
//            System.out.println("Cousinship: " + output3.getCousinship() + " Removal: " + output3.getRemoval());
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
}

