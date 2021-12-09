import java.io.*;
import java.sql.*;
import java.util.*;
public class Genealogy
{
    private ArrayList<PersonIdentity> people = new ArrayList<>(); // Array list of People
    private ArrayList<FileIdentifier> files = new ArrayList<>(); // Array list of Media Files

    public PersonIdentity addPerson(String name)
    {
        if(name!=null && !name.equalsIgnoreCase("")) {
            PersonIdentity person = new PersonIdentity(name);

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
                stmt.execute ("use "+schema+";");

                String query="insert into people(person_id,person_name)" + " values(?,?)";
                PreparedStatement prepStatement = con.prepareStatement(query);
                prepStatement.setString(1,null);
                prepStatement.setString(2, name);
                prepStatement.execute();

                int insertKey = 0;

                resultSet = prepStatement.getGeneratedKeys();

                if (resultSet.next()) {
                    insertKey = resultSet.getInt(1);
                }
                else
                {

                    throw new SQLException("Failed to add the person - " + name + " to the database!");
                }

                System.out.println("Person - " + name " has been added to the database with Id: " + insertKey);


//                con.close();
//                stmt.close();
//                prepStatement.close();

            }
            finally
            {

                if (resultSet != null && stmt != null)
                {
                    try {
                        resultSet.close();
                        stmt.close();
                        con.close();
                        prepStatement.close();
                    } catch (ClassNotFoundException | SQLException e)
                    {
                        // ignore
                        e.printStackTrace();
                    }
                }
//            catch (ClassNotFoundException | SQLException e) {
////                System.out.println("Exception here!");
//                e.printStackTrace();
            }
            return person;
        }
        else
            return null; //Throw an Exception later
    }

    Boolean recordAttributes (PersonIdentity person, Map<String, String> attributes )
    {

    }
}
