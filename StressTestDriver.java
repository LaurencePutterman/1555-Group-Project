import java.io.*;
import java.sql.*;

public class StressTestDriver
{
	public static void main(String args[])
	{
		//create db connection
		String username = "shb64"; //This is your username in oracle
		String password = "3556313"; //This is your password in oracle
		Connection connection;
		try{
		  //Register the oracle driver.  This needs the oracle files provided
		  //in the oracle.zip file, unzipped into the local directory and 
		  //the class path set to include the local directory
		  DriverManager.registerDriver (new oracle.jdbc.driver.OracleDriver());
		  //This is the location of the database.  This is the database in oracle
		  //provided to the class
		  String url = "jdbc:oracle:thin:@class3.cs.pitt.edu:1521:dbclass"; 
		  
		  connection = DriverManager.getConnection(url, username, password); 
		  //create a connection to DB on class3.cs.pitt.edu
		}
		catch(Exception Ex)  //What to do with any exceptions
		{
		  System.out.println("Error connecting to database.  Machine Error: " +
				Ex.toString());
			Ex.printStackTrace();
		}
	}
}	

