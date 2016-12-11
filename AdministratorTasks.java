import java.io.*;
import java.sql.*;  //import the file containing definitions for the parts
                    //needed by java for database connection and manipulation
import java.util.Scanner;
import java.util.InputMismatchException; //used to check formatting of files
import java.text.ParseException; //used to check date formatting

public class AdministratorTasks
{
  public static final int CONNECTION_FAILED = -2;
  public static final int FORMAT_ERROR = -1;
  public static final int FILE_ERROR = -3;
  public static final int SQL_ERROR = -4;
  
  //ask the user for a flight number and date, and print the info of all passengers with TICKETED reservations on that flight
  //returns the number of passengers on the flight, 0 if the flight does not exist, or -1 if an error was encountered
  public static int createManifest(String flightNumber, String date, Connection connection)
  {
	Statement statement = null;
	ResultSet passengers;
	String query;
	int passengerCount = 0;
	try{
		statement = connection.createStatement();
	}catch(SQLException e){
		System.out.println("Error: connection to database failed");
		return CONNECTION_FAILED;
	}
	try{
		query = "SELECT salutation, first_name, last_name FROM (Reservation_detail NATURAL JOIN Reservation) JOIN Customer ON Customer.CID=Reservation.CID WHERE flight_number = '" + flightNumber + "' AND flight_date = to_date('" + date + "', 'MM/DD/YYYY')";
		//query = "SELECT salutation, first_name, last_name FROM (Reservation_detail NATURAL JOIN Reservation) JOIN Customer ON Customer.CID=Reservation.CID WHERE flight_number = '69' AND flight_date = to_date('04-11-2007', 'MM-DD-YYYY')";
		passengers = statement.executeQuery(query);
		while(passengers.next()){
			System.out.println(passengers.getString(1) + " " + passengers.getString(2) + " " + passengers.getString(3));
			passengerCount++;
		}
		if(passengerCount > 0){
			System.out.println();
		}
		return passengerCount;
	}catch(SQLException f){
		System.out.println("Error, your date may have been incorrectly formatted.");
		System.out.println(f.getMessage());
		return FORMAT_ERROR;
	}
  }
  
  //throws an IOException if file could not be opened, and returns false if reading from file or connection to db failed
  public static int loadFlightInfo(File flightInfoFile, Connection connection) throws IOException
  {
	String query = "INSERT INTO Flight values(?,?,?,?,?,?,?,?)";
	PreparedStatement insertStatement = null;
	Scanner flightInfo = null;
	int retval = 0;
	//create Scanner for file (may throw IOException)
	flightInfo = new Scanner(flightInfoFile);
	//create PreparedStatement
	try{
		insertStatement = connection.prepareStatement(query);
		connection.setAutoCommit(false); //this will allow us to roll back the deletion if something goes wrong
	}catch(SQLException e){
		return CONNECTION_FAILED;
	}
	//read from file and insert into db
	while(flightInfo.hasNext()){
		Scanner currentTuple = null;
		//String flightNumTemp = null;
		try{
			String currentToken;
			currentTuple = new Scanner(flightInfo.nextLine()).useDelimiter("\\s*,\\s*");
			
			//flightNumTemp = currentTuple.next();
			//insertStatement.setString(1, flightNumTemp);
			insertStatement.setString(1, currentTuple.next());
			insertStatement.setString(2, currentTuple.next());
			insertStatement.setString(3, currentTuple.next());
			insertStatement.setString(4, currentTuple.next());
			insertStatement.setString(5, currentTuple.next());
			insertStatement.setString(6, currentTuple.next());
			insertStatement.setString(7, currentTuple.next());
			insertStatement.setString(8, currentTuple.next());
			
			insertStatement.execute();
			retval++;
			//System.out.println("Successfully executed insert");
		}catch(InputMismatchException ime){
			retval = FORMAT_ERROR;
			break;
		}catch(SQLException sqle){
			System.out.println("SQL Error: " + sqle.getMessage());
			//System.out.println("Error occurred on record with flight number " + flightNumTemp);
			retval = SQL_ERROR;
			break;
		}
	}
	//if retval is negative, we encountered an exception reading from the file, so we should roll back the transaction. else, commit it
	try{
		if(retval >= 0){
			connection.commit();
		}else{
			connection.rollback();
		}
		connection.setAutoCommit(true);
	}catch(SQLException g){}
	return retval;
  }
  
  public static int loadAirlineInfo(File airlineInfoFile, Connection connection) throws IOException
  {
	String query = "INSERT INTO Airline values(?,?,?,?)";
	PreparedStatement insertStatement = null;
	Scanner airlineInfo = null;
	int retval = 0;
	//create Scanner (may throw IOException)
	airlineInfo = new Scanner(airlineInfoFile);
	//create PreparedStatement and set AutoCommit to false
	try{
		insertStatement = connection.prepareStatement(query);
		connection.setAutoCommit(false); //this will allow us to roll back the deletion if something goes wrong
	}catch(SQLException e){
		return CONNECTION_FAILED;
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
			retval++;
			//System.out.println("Successfully executed insert");
		}catch(InputMismatchException ime){
			retval = FORMAT_ERROR;
			break;
		}catch(SQLException sqle){
			System.out.println("SQL Error: " + sqle.getMessage());
			retval = SQL_ERROR;
			break;
		}
	}
	try{
		if(retval >= 0){
			connection.commit();
		}else{
			connection.rollback();
			connection.setAutoCommit(true);
		}
	}catch(SQLException g){}
	return retval;
  }
  
  public static int eraseDatabase(Connection connection)
  {
	int retval = 0;
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
		System.out.println(e.getMessage());
		retval = SQL_ERROR;
		try{
			connection.rollback();
		}catch(SQLException g){}
	}
	try{
		connection.setAutoCommit(true);
	}catch(SQLException f){}
	return retval;
  }

  public static int loadPlaneInfo(File planeInfoFile, Connection connection) throws IOException
  {
	String query = "INSERT INTO Plane values(?,?,?,?,?,?)";
	PreparedStatement insertStatement = null;
	Scanner planeInfo = null;
	int retval = 0;
	java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("MM/dd/yyyy");
    java.sql.Date lastService = null;
	//create Scanner (may throw IOException)
	planeInfo = new Scanner(planeInfoFile);
	//create PreparedStatement and set AutoCommit to false
	try{
		insertStatement = connection.prepareStatement(query);
		connection.setAutoCommit(false); //this will allow us to roll back the deletion if something goes wrong
	}catch(SQLException e){
		return CONNECTION_FAILED;
	}

	
	while(planeInfo.hasNext()){
		Scanner currentTuple = null;
		try{
			String date;
			currentTuple = new Scanner(planeInfo.nextLine()).useDelimiter("\\s*,\\s*");
			insertStatement.setString(1, currentTuple.next());
			insertStatement.setString(2, currentTuple.next());
			insertStatement.setLong(3, currentTuple.nextLong());
			date = currentTuple.next();
			lastService = new java.sql.Date (df.parse(date).getTime());
			if(lastService == null){
				//parse failed
				throw new InputMismatchException();
			}
			insertStatement.setDate(4, lastService);
			insertStatement.setLong(5, currentTuple.nextLong());
			insertStatement.setString(6, currentTuple.next());
			
			insertStatement.execute();
			retval++;
			//System.out.println("Successfully executed insert");
		}catch(InputMismatchException ime){
			retval = FORMAT_ERROR;
			break;
		}catch(ParseException p){
			retval = FORMAT_ERROR;
			break;
		}catch(SQLException sqle){
			System.out.println("SQL Error: " + sqle.getMessage());
			retval = SQL_ERROR;
			break;
		}
	}
	try{
		if(retval >= 0){
			connection.commit();
		}else{
			connection.rollback();
			connection.setAutoCommit(true);
		}
	}catch(SQLException g){}
	return retval;
  }
  
    //throws IOException if opening the file fails
  public static int loadPricingInfoFromFile(File pricingInfoFile, Connection connection) throws IOException
  {
	String query = "INSERT INTO Price values(?,?,?,?,?)";
	String updateQuery = "UPDATE Price SET high_price = ?, low_Price = ? WHERE departure_city = ? AND arrival_city = ?";
	PreparedStatement insertStatement = null;
	PreparedStatement updateStatement = null;
	int retval = 0;
	long lowPrice = -1;
	long highPrice = -1;
	String departureCity = "";
	String arrivalCity = "";
	
	try{
		insertStatement = connection.prepareStatement(query);
		updateStatement = connection.prepareStatement(updateQuery);
	}catch(SQLException e){
		return CONNECTION_FAILED;
	}
	
	//may throw IOException
	Scanner pricingInfo = new Scanner(pricingInfoFile);
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
			retval++;
			//System.out.println("Successfully executed insert");
		}catch(InputMismatchException ime){
			retval = FORMAT_ERROR;
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
					retval = SQL_ERROR;
					System.out.println("SQL Error: " + e.getMessage());
					break;
				}
			}else{
				retval = SQL_ERROR;
				System.out.println("SQL Error: " + sqle.getMessage());
				break;
			}
		}
	}
	return retval;
  }
  
  public static int updatePricingInfo(long highPrice, long lowPrice, String departureCity, String arrivalCity, Connection connection)
  {
	String updateQuery = "UPDATE Price SET high_price = ?, low_Price = ? WHERE departure_city = ? AND arrival_city = ?";
	PreparedStatement updateStatement = null;
	int retval = 0;
	try{
		updateStatement = connection.prepareStatement(updateQuery);
	}catch(SQLException e){
		return CONNECTION_FAILED;
	}
	try{
		updateStatement.setLong(1, highPrice);
		updateStatement.setLong(2, lowPrice);
		updateStatement.setString(3, departureCity);
		updateStatement.setString(4, arrivalCity);
		updateStatement.executeUpdate();
	}catch(SQLException se){
		retval = SQL_ERROR;
		System.out.println(se.getMessage());
	}
	return retval;
  }
}
