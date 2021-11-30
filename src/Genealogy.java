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
                resultSet=stmt.executeQuery("select * from orders " +
                        "where orderNumber=" + order_number);
                resultSet=stmt.executeQuery("insert into people values(person_id,person_name)" + " values(?,?)");
                while(resultSet.next())
                {
                    System.out.println(resultSet.getString(1));
                }
                con.close();
                stmt.close();
                resultSet.close();

            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }

            return person;
        }
        else
            return null; //Throw an Exception later
    }
}
