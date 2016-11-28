/*
    Here is an example of connecting to a database using jdbc

    The table we will use in the example is
    Table Test(
       name     varchar(30),
       ssn      number(10),
       bday     date
    );
    
    For demostratration purpose, insert two records into this table:
    ( 'Mike', 123456789, '09/Nov/03' )
    ( 'Amy', 987654321, '10/Nov/03' )

    Written by: Jonathan Beaver, modified by Thao Pham
    Purpose: Demo JDBC for CS1555 Class

    IMPORTANT (otherwise, your code may not compile)	
    Same as using sqlplus, you need to set oracle environment variables by 
    sourcing bash.env or tcsh.env
*/

import java.io.*;
import java.sql.*;  //import the file containing definitions for the parts
                    //needed by java for database connection and manipulation
import java.util.Scanner;
import java.text.ParseException; //used to check date formatting

public class CustomerTasks
{
  private Connection connection; //used to hold the jdbc connection to the DB
  private String username, password;
  private Scanner keyboard;

  public CustomerTasks()
  {
    /*Making a connection to a DB causes certian exceptions.  In order to handle
    these, you either put the DB stuff in a try block or have your function
    throw the Execptions and handle them later.  For this demo I will use the
    try blocks*/
    username = "shb64"; //This is your username in oracle
    password = "3556313"; //This is your password in oracle
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
	System.out.println("Welcome, customer");
	char option = 0;
	String input = "";
	keyboard = new Scanner(System.in);
	while(option != 'Q'){
		System.out.println("Please select an operation by entering the corresponding number, or enter \"Q\" to quit.\n8: Add reservation\n9: Get reservation information\n10: Buy ticket");
		input = keyboard.nextLine();
		if(input.length() == 1){
			option = input.toUpperCase().charAt(0);
		}else if (input.equals("10")){
			option = '0';
		}else{
			option = 0;
		}
		switch(option)
		{
			case '8':
				makeReservation();
				break;
			case '9':
				break;
			case '0':
				break;
			case 'Q':
				break;
			default:
				System.out.println("Invalid input; please try again.");
		}
		
	}
	
	System.out.println("Exiting program...");
  }
  

  private void makeReservation()
  {
	String startingAirport;
	String destinationAirport;
	boolean isRoundTrip;
	String reservationNum;
	String query;
	Statement statement;
	ResultSet rs;
	try{
		statement = connection.createStatement();
		//want everything to be one transaction so it can easily be rolled back
		connection.setAutoCommit(false);
	}catch(SQLException e){
		System.out.println("Error: connection to database failed");
		return;
	}
	try{
		//go ahead and get the next reservation_number to use with the legs and to avoid fk violations
		//issue a query to discover the highest reservation_number in the db
		query = "SELECT * FROM (SELECT reservation_number FROM Reservation ORDER BY to_number(reservation_number) desc) WHERE rownum = 1";
		rs = statement.executeQuery(query);
		//rs should be a single result with just the highest reservation_number as an attribute
		rs.next();
		//add 1 to current highest reservation_num
		reservationNum = String.valueOf(Integer.parseInt(rs.getString(1)) + 1);
		//add the new reservation to db (can be rolled back)
		query = "INSERT INTO Reservation (reservation_number) VALUES ('" + reservationNum + "')";
		statement.execute(query);
		while(true){
			System.out.println("Please enter the departure city for your trip.");
			startingAirport = keyboard.nextLine();
			System.out.println("Please enter the destination city for your trip.");
			destinationAirport = keyboard.nextLine();
			//check to see if the database contains pricing data for this trip
			query = "SELECT * FROM Price WHERE departure_city = '" + startingAirport + "' AND arrival_city = '" + destinationAirport+ "'";
			rs = statement.executeQuery(query);
			if(!rs.next()){
				//no results returned
				//ASSUMPTION: if a trip is in the db, the trip in the reverse direction is also there
				System.out.println("Sorry, that trip is not in the database.  Would you like to try another? Y/N");
				if(keyboard.nextLine().toLowerCase().equals("y")){
					continue;
				}else{
					break;
				}
			}	
			System.out.println("Is this a round-trip? Y/N");
			isRoundTrip = keyboard.nextLine().toLowerCase().equals("y");
			String currentFlightNum;
			String currentDate;
			int currentLeg = 1;
			//get first leg info
			currentFlightNum = getFlightNum("Please enter the flight number for your first flight", statement);
			currentDate = getDate("Please enter the date for your first flight");
			//add to reservation_detail
		}	
		

		//set autocommit back to true before leaving the function
		connection.setAutoCommit(true);
	}catch(SQLException e){
		System.out.println("Unhandled SQLException: ");
		System.out.println(e.getMessage());
		connection.setAutoCommit(true);
	}
  }
  
  
  private String getDate(String prompt)
  {
	java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("MM/dd/yyyy");
	String dateStr;
	while(true){
		System.out.println(prompt + " in the format MM/DD/YYYY");
		dateStr = keyboard.nextLine();
		try{
			if(df.parse(dateStr) == null){
				System.out.println("Error: could not parse date.  Please enter it again.");
			}else{
				break;
			}
		}catch(ParseException p){
			System.out.println("Error: could not parse date.  Please enter it again.");
		}
	}
	return dateStr;
  }
  
  private String getFlightNum(String prompt, Statement s) throws SQLException
  {
	String flightNum;
	ResultSet rs;
	while(true){
		System.out.println(prompt);
		flightNum = keyboard.nextLine();
		String query = "SELECT * FROM Flight WHERE flight_number = '" + flightNum + "'";
		rs = s.executeQuery(query);
		if(!rs.next()){
			System.out.println("Error, invalid flight number.  Please try again.");
			continue;
		}else{
			break;
		}
	}
	return flightNum;
	  
  }
  
	

  public static void main(String args[])
  {
    CustomerTasks adminInterface = new CustomerTasks();
  }
}
