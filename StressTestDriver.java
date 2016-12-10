import java.io.*;
import java.sql.*;
import java.util.Scanner;

public class StressTestDriver
{
	public static void main(String args[])
	{
		//create db connection
		String username = "shb64"; //This is your username in oracle
		String password = "3556313"; //This is your password in oracle
		String query;
		Connection connection = null;
		Statement statement = null;
		ResultSet rs;
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
		  statement = connection.createStatement();
		}
		catch(Exception Ex)  //What to do with any exceptions
		{
		  System.out.println("Error connecting to database.  Machine Error: " +
				Ex.toString());
			Ex.printStackTrace();
			System.exit(-1);
			System.exit(-1);
		}
		
		//test Admin Task 2
		System.out.println("Administrator Task 2: Load airline information");
		query = "SELECT count(*) FROM Airline";
		try{
			for(int counter = 0; counter < 10; counter++){
				String filename = "airline_information-" + counter + ".csv";
				//insert the contents of the file
				int result = AdministratorTasks.loadAirlineInfo(new File(filename), connection);
				//see how many records the table now contains
				rs = statement.executeQuery(query);
				rs.next();
				long recordCount = rs.getLong(1);
				System.out.println("Loaded " + result + " airline records. The table now contains " + recordCount + " records.");
			}
		}catch(Exception e){
			System.out.println("An unhandled exception occurred while inserting airline records: " + e.getMessage());
		}
		
		//test Admin Task 5 (we have to do these out of order since the other task's insertions depend on plane tuples
		System.out.println("Administrator Task 5: Load plane information");
		query = "SELECT count(*) FROM Plane";
		try{
			for(int counter = 0; counter < 10; counter++){
				String filename = "plane_information-" + counter + ".csv";
				//insert the contents of the file
				int result = AdministratorTasks.loadPlaneInfo(new File(filename), connection);
				//see how many records the table now contains
				rs = statement.executeQuery(query);
				rs.next();
				long recordCount = rs.getLong(1);
				System.out.println("Loaded " + result + " plane records. The table now contains " + recordCount + " records.");
			}
		}catch(Exception e){
			System.out.println("An unhandled exception occurred while inserting plane records: " + e.getMessage());
		}
		
		//test Admin task 3
		System.out.println("Administrator Task 3: Load schedule information");
		query = "SELECT count(*) FROM Flight";
		try{
			for(int counter = 0; counter < 10; counter++){
				String filename = "schedule_information-" + counter + ".csv";
				//insert the contents of the file
				int result = AdministratorTasks.loadFlightInfo(new File(filename), connection);
				//see how many records the table now contains
				rs = statement.executeQuery(query);
				rs.next();
				long recordCount = rs.getLong(1);
				System.out.println("Loaded " + result + " flight records. The table now contains " + recordCount + " records.");
			}
		}catch(Exception e){
			System.out.println("An unhandled exception occurred while inserting flight records: " + e.getMessage());
		}
		
		//test Admin task 4 part 1 (load from file)
		System.out.println("Administrator Task 4: Load price information from file");
		query = "SELECT count(*) FROM Price";
		try{
			for(int counter = 0; counter < 10; counter++){
				String filename = "pricing_information-" + counter + ".csv";
				//insert the contents of the file
				int result = AdministratorTasks.loadPricingInfoFromFile(new File(filename), connection);
				//see how many records the table now contains
				rs = statement.executeQuery(query);
				rs.next();
				long recordCount = rs.getLong(1);
				System.out.println("Loaded " + result + " price records. The table now contains " + recordCount + " records.");
			}
		}catch(Exception e){
			System.out.println("An unhandled exception occurred while inserting price records: " + e.getMessage());
		}
		
		//test Admin task 4 part 2 (update prices)
		System.out.println("Administrator Task 4: Update prices for existing records");
		query = "SELECT * FROM Price";
		try{
			rs = statement.executeQuery(query);
			for(int counter = 0; counter < 10 && rs.next(); counter++){
				String departureCity = rs.getString(1);
				String arrivalCity = rs.getString(2);
				System.out.println("Setting high price of flight from " + departureCity + " to " + arrivalCity + " to " + 500 + " and low price to " + 400);
				AdministratorTasks.updatePricingInfo(500, 400, departureCity, arrivalCity, connection);
				query = "SELECT high_price, low_price FROM Price WHERE departure_city = " + departureCity + " AND arrival_city = " + arrivalCity;
				ResultSet priceRS = statement.executeQuery(query);
				priceRS.next();
				System.out.println("The new high price of the flight from " + departureCity + " to " + arrivalCity + " is " + priceRS.getLong(1) + " and the new low price is " + priceRS.getLong(2));
			}
		}catch(Exception e){
			System.out.println("An unhandled exception occurred while updating prices: " + e.getMessage());
		}
		
		//test administrator task 6 by creating the manifests for the first 10 flights people have reservations for in reservation_detail
		System.out.println("Administrator Task 6: print flight manifest");
		try{
			query = "SELECT * FROM Reservation_detail";
			rs = statement.executeQuery(query);
			for(int counter = 0; counter < 10 && rs.next(); counter++){
				String flightNum = rs.getString(2);
				java.sql.Date flightDate = rs.getDate(3);
				//turn the date into a string that can be accepted by createManifest
				Scanner dateScanner = new Scanner(flightDate.toString()).useDelimiter("-");
				String year = dateScanner.next();
				String month = dateScanner.next();
				String day = dateScanner.next();
				String flightDateString = month + "/" + day + "/" + year;
				System.out.println("Manifest for flight " + flightNum + " on " + flightDateString + ":");
				AdministratorTasks.createManifest(flightNum, flightDateString, connection);
			}
		}catch(Exception e){
			System.out.println("Unhandled exception while printing flight manifests " + e.getMessage());
		}
		
		
		//test the deletion function (can only do this once!)
		System.out.println("Administrator Task 1: Delete all contents of the database");
		AdministratorTasks.eraseDatabase(connection);
		try{
			query = "SELECT Count(*) FROM Airline";
			rs = statement.executeQuery(query);
			rs.next();
			//all other tables depend directly or indirectly on the airline table, so if it's erased, so is the rest of the db
			System.out.println("The airline table contains " + rs.getLong(1) + " entries");
		}catch(Exception e){
			System.out.println("Unhandled exception while erasing the contents of the database " + e.getMessage());
		}
	}
}	

