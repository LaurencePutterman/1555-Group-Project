import java.io.*;
import java.sql.*;  //import the file containing definitions for the parts
                    //needed by java for database connection and manipulation
import java.util.Scanner;
import java.util.InputMismatchException; //used to check formatting of files
import java.text.ParseException; //used to check date formatting
public class AdministratorTasks
{
  private Connection connection; //used to hold the jdbc connection to the DB
  private String username, password;
  private Scanner keyboard;

  public AdministratorTasks()
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
	System.out.println("Welcome, administrator");
	char option = 0;
	String input = "";
	keyboard = new Scanner(System.in);
	while(option != 'Q'){
		System.out.println("Please select an operation by entering the corresponding number, or enter \"Q\" to quit.\n1: Delete all data from the database\n2: Load airline information\n3: Load flight schedule information\n4: Load pricing information\n5: Load plane information\n6: Generate passenger manifest");
		input = keyboard.nextLine();
		if(input.length() == 1){
			option = input.toUpperCase().charAt(0);
		}else{
			option = 0;
		}
		switch(option)
		{
			case '1':
				eraseDatabase();
				break;
			case '2':
				loadAirlineInfo();
				break;
			case '3':
				loadFlightInfo();
				break;
			case '4':
				loadPricingInfo();
				break;
			case '5':
				loadPlaneInfo();
				break;
			case '6':
				createManifest();
				break;
			case 'Q':
				break;
			default:
				System.out.println("Invalid input; please try again.");
		}
		
	}
	
	System.out.println("Exiting program...");
  }
  
  //ask the user for a flight number and date, and print the info of all passengers with TICKETED reservations on that flight
  private void createManifest()
  {
	String flightNumber;
	String date;
	Statement statement = null;
	ResultSet passengers;
	String query;
	try{
		statement = connection.createStatement();
	}catch(SQLException e){
		System.out.println("Error: connection to database failed");
		return;
	}
	while(true){
		System.out.println("Enter the number of the flight for which to print the manifest, or enter Q to quit:");
		flightNumber = keyboard.nextLine();
		if(flightNumber.toLowerCase().equals("q")){
			return;
		}
		System.out.println("Enter the date of the flight in the format MM/DD/YYYY");
		date = keyboard.nextLine();
		try{
			query = "SELECT salutation, first_name, last_name FROM (Reservation_detail NATURAL JOIN Reservation) JOIN Customer ON Customer.CID=Reservation.CID WHERE flight_number = '" + flightNumber + "' AND flight_date = to_date('" + date + "', 'MM/DD/YYYY')";
			//query = "SELECT salutation, first_name, last_name FROM (Reservation_detail NATURAL JOIN Reservation) JOIN Customer ON Customer.CID=Reservation.CID WHERE flight_number = '69' AND flight_date = to_date('04-11-2007', 'MM-DD-YYYY')";
			passengers = statement.executeQuery(query);
			int counter = 0;
			while(passengers.next()){
				System.out.println(passengers.getString(1) + " " + passengers.getString(2) + " " + passengers.getString(3));
				counter++;
			}
			if(counter == 0){
				//ResultSet has no rows
				System.out.println("The specified flight does not exist or currently has no passengers.  Would you like to try another flight? Y/N");
				if(keyboard.nextLine().toLowerCase().equals("y")){
				  continue;
				}else{
				  return;
				} 
			}
			System.out.println();
			return;
		}catch(SQLException f){
			System.out.println("Error, your data may have been incorrectly formatted.  Would you like to try again? Y/N");
			System.out.println(f.getMessage());
			if(!keyboard.nextLine().toLowerCase().equals("y")){
				return;
			}
		}
	}
  }
  
  private void loadFlightInfo()
  {
	String input = "";
	String query = "INSERT INTO Flight values(?,?,?,?,?,?,?,?)";
	PreparedStatement insertStatement = null;
	File flightInfoFile;
	Scanner flightInfo = null;
	boolean badParse = false;
	try{
		insertStatement = connection.prepareStatement(query);
		connection.setAutoCommit(false); //this will allow us to roll back the deletion if something goes wrong
	}catch(SQLException e){
		System.out.println("Error: connection to database failed");
		return;
	}
	while(flightInfo == null){
		System.out.println("Please enter the name of the file containing the flight information, or enter \"Q\" to quit");
		input = keyboard.nextLine();
		if(input.equals("Q") || input.equals("q")){
			return;
		}
		try{
			flightInfoFile = new File(input);
			flightInfo = new Scanner(flightInfoFile);
		}catch(IOException f){
			System.out.println("Error opening specified file; please try again");
			flightInfo = null;
		}
	}
	while(flightInfo.hasNext()){
		Scanner currentTuple = null;
		try{
			String currentToken;
			currentTuple = new Scanner(flightInfo.nextLine()).useDelimiter("\\s*,\\s*");
			
			insertStatement.setString(1, currentTuple.next());
			insertStatement.setString(2, currentTuple.next());
			insertStatement.setString(3, currentTuple.next());
			insertStatement.setString(4, currentTuple.next());
			insertStatement.setString(5, currentTuple.next());
			insertStatement.setString(6, currentTuple.next());
			insertStatement.setString(7, currentTuple.next());
			insertStatement.setString(8, currentTuple.next());
			
			insertStatement.execute();
			//System.out.println("Successfully executed insert");
		}catch(InputMismatchException ime){
			System.out.println("Error: file is improperly formatted. Could not read all or part of file.");
			try{
				connection.rollback();
			}catch(SQLException h) {}
			badParse = true;
			break;
		}catch(SQLException sqle){
			System.out.println("Unhandled exception when inserting from file into database");
			System.out.println(sqle.getMessage());
			try{
				connection.rollback();
			}catch(SQLException h) {}
			badParse = true;
			break;
		}
	}
	try{
		if(!badParse){
			connection.commit();
		}
		connection.setAutoCommit(true);
	}catch(SQLException g){}
  }
  
  private void loadAirlineInfo()
  {
	String input = "";
	String query = "INSERT INTO Airline values(?,?,?,?)";
	PreparedStatement insertStatement = null;
	File airlineInfoFile;
	Scanner airlineInfo = null;
	boolean badParse = false;
	try{
		insertStatement = connection.prepareStatement(query);
		connection.setAutoCommit(false); //this will allow us to roll back the deletion if something goes wrong
	}catch(SQLException e){
		System.out.println("Error: connection to database failed");
		return;
	}
	while(airlineInfo == null){
		System.out.println("Please enter the name of the file containing the airline information, or enter \"Q\" to quit");
		input = keyboard.nextLine();
		if(input.equals("Q") || input.equals("q")){
			return;
		}
		try{
			airlineInfoFile = new File(input);
			airlineInfo = new Scanner(airlineInfoFile);
		}catch(IOException f){
			System.out.println("Error opening specified file; please try again");
			airlineInfo = null;
		}
	}
	while(airlineInfo.hasNext()){
		Scanner currentTuple = null;
		try{
			currentTuple = new Scanner(airlineInfo.nextLine()).useDelimiter("\\s*,\\s*");
			insertStatement.setString(1, currentTuple.next());
			insertStatement.setString(2, currentTuple.next());
			insertStatement.setString(3, currentTuple.next());
			insertStatement.setLong(4, currentTuple.nextLong());
			
			insertStatement.execute();
			//System.out.println("Successfully executed insert");
		}catch(InputMismatchException ime){
			System.out.println("Error: file is improperly formatted. Could not read all or part of file.");
			try{
				connection.rollback();
			}catch(SQLException h) {}
			badParse = true;
			break;
		}catch(SQLException sqle){
			System.out.println("Unhandled exception when inserting from file into database");
			System.out.println(sqle.getMessage());
			try{
				connection.rollback();
			}catch(SQLException h) {}
			badParse = true;
			break;
		}
	}
	try{
		if(!badParse){
			connection.commit();
		}
		connection.setAutoCommit(true);
	}catch(SQLException g){}
  }
  
  private void eraseDatabase()
  {
	System.out.println("WARNING: You are about to delete all data in the database.  Are you sure you want to proceed? Y/N");
	if(keyboard.nextLine().toLowerCase().equals("y"))
	{
		try{
			connection.setAutoCommit(false); //this will allow us to roll back the deletion if something goes wrong
			Statement statement = connection.createStatement();
			//delete data from all tables
			statement.execute("SET CONSTRAINTS ALL DEFERRED");
			statement.execute("DELETE FROM Reservation_detail");
			statement.execute("DELETE FROM Reservation");
			statement.execute("DELETE FROM Customer");
			statement.execute("DELETE FROM Price");
			statement.execute("DELETE FROM Flight");
			statement.execute("DELETE FROM Plane");
			statement.execute("DELETE FROM Airline");
			
			connection.commit();
			
		}catch(SQLException e){
			System.out.println("Unhandled SQL exception while deleting data from database: ");
			System.out.println(e.getMessage());
			try{
				connection.rollback();
			}catch(SQLException g){}
		}
	}
	try{
		connection.setAutoCommit(true);
	}catch(SQLException f){
		System.out.println("Failed to set AutoCommit back to true - future operations may not work");
	}
  }
  
  private void loadPlaneInfo()
  {
	String input = "";
	String query = "INSERT INTO Plane values(?,?,?,?,?,?)";
	PreparedStatement insertStatement = null;
	java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("MM/dd/yyyy");
    java.sql.Date lastService = null;
	File planeInfoFile;
	Scanner planeInfo = null;
	try{
		insertStatement = connection.prepareStatement(query);
	}catch(SQLException e){
		System.out.println("Error: connection to database failed");
		return;
	}
	while(planeInfo == null){
		System.out.println("Please enter the name of the file containing the plane information, or enter \"Q\" to quit");
		input = keyboard.nextLine();
		if(input.equals("Q") || input.equals("q")){
			return;
		}
		try{
			planeInfoFile = new File(input);
			planeInfo = new Scanner(planeInfoFile);
		}catch(IOException f){
			System.out.println("Error opening specified file; please try again");
			planeInfo = null;
		}
	}
	while(planeInfo.hasNext()){
		Scanner currentTuple = null;
		try{
			currentTuple = new Scanner(planeInfo.nextLine()).useDelimiter("\\s*,\\s*");
			insertStatement.setString(1, currentTuple.next());
			insertStatement.setString(2, currentTuple.next());
			insertStatement.setLong(3, currentTuple.nextLong());
			input = currentTuple.next();
			lastService = new java.sql.Date (df.parse(input).getTime());
			if(lastService == null){
				//parse failed
				throw new InputMismatchException();
			}
			insertStatement.setDate(4, lastService);
			insertStatement.setLong(5, currentTuple.nextLong());
			insertStatement.setString(6, currentTuple.next());
			
			insertStatement.execute();
			//System.out.println("Successfully executed insert");
		}catch(InputMismatchException ime){
			System.out.println("Error: file is improperly formatted. Could not read all or part of file.");
			break;
		}catch(ParseException p){
			System.out.println("Error: file is improperly formatted. Could not read all or part of file.");
			break;
		}catch(SQLException sqle){
			System.out.println("Unhandled exception when inserting from file into database");
			System.out.println(sqle.getMessage());
			break;
		}
	}
  }
  
  private void loadPricingInfo()
  {
	String input = "";
	char option = 0;
	String query = "INSERT INTO Price values(?,?,?,?,?)";
	String updateQuery = "UPDATE Price SET high_price = ?, low_Price = ? WHERE departure_city = ? AND arrival_city = ?";
	PreparedStatement insertStatement = null;
	PreparedStatement updateStatement = null;
	long lowPrice = -1;
	long highPrice = -1;
	String departureCity = "";
	String arrivalCity = "";
	try{
		insertStatement = connection.prepareStatement(query);
		updateStatement = connection.prepareStatement(updateQuery);
	}catch(SQLException e){
		System.out.println("Error: connection to database failed");
		return;
	}
	
	while(option != 'Q'){
		System.out.println("Enter \"L\" to load pricing information from a file, or enter \"C\" to update the information for an existing flight. Enter \"Q\" to quit.");
		input = keyboard.nextLine();
		if(input.length() == 1){
			option = input.toUpperCase().charAt(0);
		}else{
			option = 0;
		}
		switch(option)
		{
			case 'L':
				File pricingInfoFile;
				Scanner pricingInfo = null;
				while(pricingInfo == null){
					System.out.println("Please enter the name of the file containing the pricing information, or enter \"Q\" to quit.");
					input = keyboard.nextLine();
					if(input.equals("Q")){
						break;
					}
					try{
						pricingInfoFile = new File(input);
						pricingInfo = new Scanner(pricingInfoFile);
					}catch(IOException e){
						System.out.println("Error opening the specified file. Please try again.");
						pricingInfo = null;
						continue;
					}
					while(pricingInfo.hasNext()){
						Scanner currentTuple = null;
						try{
							currentTuple = new Scanner(pricingInfo.nextLine()).useDelimiter("\\s*,\\s*");
							departureCity = currentTuple.next();
							insertStatement.setString(1, departureCity);
							arrivalCity = currentTuple.next();
							insertStatement.setString(2, arrivalCity);
							insertStatement.setString(3, currentTuple.next());
							highPrice = currentTuple.nextLong();
							insertStatement.setLong(4, highPrice);
							lowPrice = currentTuple.nextLong();
							insertStatement.setLong(5, lowPrice);
							
							insertStatement.execute();
							//System.out.println("Successfully executed insert");
						}catch(InputMismatchException ime){
							System.out.println("Error: file is improperly formatted. Could not read all or part of file.");
							break;
						}catch(SQLException sqle){
							//if this is a primary key violation, we want to update the prices rather than inserting
							if(sqle.getSQLState().startsWith("23")){
								//System.err.println("Attempting update");
								try{
									updateStatement.setLong(1, highPrice);
									updateStatement.setLong(2, lowPrice);
									updateStatement.setString(3, departureCity);
									updateStatement.setString(4, arrivalCity);
									updateStatement.executeUpdate();
								}catch(Exception e){
									System.out.println("Unhandled exception when inserting from file into database:");
									System.out.println(e.getMessage());
									break;
								}
							}else{
								System.out.println("Unhandled exception when inserting from file into database:");
								System.out.println(sqle.getMessage());
								break;
							}
						}
					}
				}
				pricingInfo.close();
				break;
			case 'C':
				System.out.println("Enter the departure city of the route to be updated:");
				departureCity = keyboard.nextLine();
				System.out.println("Enter the arrival city of the route to be updated:");
				arrivalCity = keyboard.nextLine();
				while(true){
					try{
						System.out.println("Enter the new high price:");
						highPrice = Long.parseLong(keyboard.nextLine());
						System.out.println("Enter the new low price:");
						lowPrice = Long.parseLong(keyboard.nextLine());
						if(lowPrice < 0 || highPrice < 0 || lowPrice > highPrice){
							System.out.println("Error, please make sure that prices are >= 0 and that the high price is >= the low price");
						}else{
							break;
						}
					}catch(NumberFormatException nf){
						System.out.println("Error, please be sure to enter a number when prompted");
					}
				}
				try{
					updateStatement.setLong(1, highPrice);
					updateStatement.setLong(2, lowPrice);
					updateStatement.setString(3, departureCity);
					updateStatement.setString(4, arrivalCity);
					updateStatement.executeUpdate();
				}catch(SQLException se){
					System.out.println("Error: unable to perform update.");
					System.out.println(se.getMessage());
				}
				break;
			case 'Q': 
				break;
			default:
			System.out.println("Invalid input; please try again");
		}
	}
  }
  
  
	

  public static void main(String args[])
  {
    AdministratorTasks adminInterface = new AdministratorTasks();
  }
}
