package Database;//Create a program that will ask for an order number from the user and will show the
//        order information on the screen as an invoice, with a well-formatted structure. The
//        invoice should include
//        a. The order date and order number
//        b. The customer name and address
//        c. The name of the sales representative
//        d. Order lines with product codes and product names, aligned appropriately
//        e. The total cost of the order

import java.sql.*;
import java.util.Scanner;

public class Database_Lab_6
{
    public static void main(String args[])
    {

        Connection con=null;
        Statement stmt=null;
        ResultSet resultSet=null;

        Scanner scan=new Scanner(System.in);
        System.out.println("Enter an order number");
        int order_number=scan.nextInt();
        try
        {
//            ?serverTimezone=UTC&useSSL=false
            Class.forName("com.mysql.jdbc.Driver");
            con=DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306?serverTimezone=UTC&useSSL=false","benny","B00899629");
            System.out.println("Connection established successfully");
            stmt=con.createStatement();
            stmt.execute ("use csci3901;");
            resultSet=stmt.executeQuery("select * from orders " +
                    "where orderNumber=" + order_number);
            while(resultSet.next())
            {
                resultSet.getString(1);

                //System.out.println(resultSet.getString(1));
            }
            con.close();
            stmt.close();
            resultSet.close();

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
}

//    customers
//            employees
//    offices
//            orderdetails
//    orders
//            payments
//    productlines
//            products

//or.orderDate,or.orderNumber,customerName,addressLine1," +
//        "firstName,orderLineNumber,od.productCode,productName,(priceEach*quantityOrdered)" +
//        " from orders or join customer c on or.customerNumber=c.customerNumber" +
//        "join employees e on c.salesRepEmployeeNumber=e.employeeNumber" +
//        "join orderdetails od on or.orderNumber=od.orderNumber" +
//        "join products p on od.productCode=p.productCode