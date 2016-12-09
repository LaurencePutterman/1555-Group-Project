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

    import java.lang.Math;
    import java.util.Date;
    import java.text.SimpleDateFormat;
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
    //username = "shb64"; //This is your username in oracle
    //password = "3556313"; //This is your password in oracle
    username = "mlp81";
    password = "3808669";
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
    	System.out.println("Please select an operation by entering the corresponding number, or enter \"Q\" to quit.\n1: Add customer\n2: Show customer information\n3: Find prices for flights between cities\n4: Find flights for a given route\n5: Find flights for a given route on an airline\n6: Find flights for a given route on a particular date\n7: Find flights for a given route on a particular date with an airline\n8: Add reservation\n9: Get reservation information\n10: Buy ticket");
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
    		case '1':
    			addCustomerHelper();
    			break;
    		case '2':
	    		showCustomerInfoHelper();
	    		break;
    		case '3':
	    		findPriceForFlightsBetweenTwoCitiesHelper();
	    		break;
    		case '4':
	    		findAllRoutesBetweenTwoCitiesHelper();
	    		break;
    		case '5':
	    		findAllRoutesBtwTwoCitiesForAirlineHelper();
	    		break;
    		case '6':
	    		findAllRoutesWithSeatsBtwTwoCitiesOnDayHelper();
	    		break;
    		case '7':
	    		findAllRoutesWithSeatsBtwTwoCitiesOnDayForAirlineHelper();
	    		break;
    		case '8':
	    		makeReservation();
	    		break;
    		case '9':
	    		showReservationInfoGivenNumberHelper();
	    		break;
    		case '0':
	    		buyTicketOnReservationHelper();
	    		break;
    		case 'Q':
    			break;
    		default:
    			System.out.println("Invalid input; please try again.");
    	}

    }

    System.out.println("Exiting program...");
}
private void addCustomerHelper(){
	String first_name;
	String last_name;
	String salutation;
	String street;
	String city;
	String state;
	String phone;
	String email;
	String credit_card_num;
	String expirationDateString;
	boolean result;
	while(true){
		System.out.println("Please enter customer's first name.");
		first_name = keyboard.nextLine();
		System.out.println("Please enter customer's last name.");
		last_name = keyboard.nextLine();
		System.out.println("Please enter customer's salutation.");
		salutation = keyboard.nextLine();
		System.out.println("Please enter customer's street address.");
		street = keyboard.nextLine();
		System.out.println("Please enter customer's city.");
		city = keyboard.nextLine();
		System.out.println("Please enter customer's 2 letter state (e.g, NY).");
		state = keyboard.nextLine();
		System.out.println("Please enter customer's phone number.");
		phone = keyboard.nextLine();
		System.out.println("Please enter customer's email.");
		email = keyboard.nextLine();
		System.out.println("Please enter customer's credit card number.");
		credit_card_num = keyboard.nextLine();
		System.out.println("Please enter customer's credit card expiration date.");
		expirationDateString = keyboard.nextLine();
		result = addCustomer(first_name,last_name,salutation,street,city,state,phone,email,credit_card_num, expirationDateString);
		System.out.println("Add another customer?\nY/N");
		if(keyboard.nextLine().toLowerCase().equals("y")){
			continue;
		}else{
			break;
		}		
	}
	
}
public boolean addCustomer(String first_name, String last_name, String salutation, String street, String city, String state, String phone, String email, String credit_card_num, String expirationDateString)
{
	String query;
	PreparedStatement preparedStatement;
	ResultSet rs;
	Date credit_card_expire = null;
	String cid;


	try{
		//want everything to be one transaction so it can easily be rolled back
		connection.setAutoCommit(false);
	}catch(SQLException e){
		System.out.println("Error: connection to database failed");
		return false;
	}
	try{
		//Check if customer name already exists
		query = "SELECT * FROM customer WHERE first_name = ? and last_name = ?";
		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1,first_name);
		preparedStatement.setString(2,last_name);

		rs = preparedStatement.executeQuery();

		if(rs.next()){
			//results returned
			System.out.println("Sorry, a user with those first and last names already exists.");
			return false;
		}
		
		while(state.length() != 2){
			System.out.println("State entered was more than 3 letters.");
			System.out.println("Please enter customer's 2 letter state (e.g, NY).");
			return false;
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/yy");
		simpleDateFormat.setLenient(false);
		
		boolean dateEntered = false;
		Date currDate = new Date();
		while(!dateEntered){
			try{
				credit_card_expire = simpleDateFormat.parse(expirationDateString);
				if(credit_card_expire.before(currDate)){
					System.out.println("Credit card is expired!");
					// System.out.println("Please enter customer's credit card number.");
					// credit_car_num = keyboard.nextLine();
					// System.out.println("Please enter customer's credit card expiration date.");
					// expirationDateString = keyboard.nextLine();
					// credit_card_expire = simpleDateFormat.parse(expirationDateString);
					return false;
				}
				dateEntered = true;
			} catch(java.text.ParseException pe){
				System.out.println("Enter date of format MM/yy");
				// expirationDateString = keyboard.nextLine();
				return false;
			}
		}
		
		

		//Select max CID, increment by 1
		query = "SELECT max(to_number(cid)) m_cid FROM customer";
		preparedStatement = connection.prepareStatement(query);

		rs = preparedStatement.executeQuery();

		if(!rs.next()){
			cid = "1";
		}
		else if(rs.getString(1) == null){
			cid = "1";
		}
		else{
			String currMax = rs.getString(1);
			cid = String.valueOf((Integer.parseInt(currMax)+1));
		}

		//Insert customer information
		query =  "INSERT INTO "
		+"customer (cid,salutation,first_name,last_name,credit_car_num,credit_card_expire,street,city,state,phone,email) "
		+"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1,cid);
		preparedStatement.setString(2,salutation);
		preparedStatement.setString(3,first_name);
		preparedStatement.setString(4,last_name);
		preparedStatement.setString(5,credit_card_num);
		if(credit_card_expire != null){
			preparedStatement.setDate(6,new java.sql.Date(credit_card_expire.getTime()));
		} else{
			System.out.println("CC expiration is not set!");
			return false;
		}
		
		preparedStatement.setString(7,street);
		preparedStatement.setString(8,city);
		preparedStatement.setString(9,state);
		preparedStatement.setString(10,phone);
		preparedStatement.setString(11,email);

		preparedStatement.executeUpdate();

		System.out.println("Customer succesfully inserted with PittRewards number of "+cid+".");
		connection.commit();
		connection.setAutoCommit(true);
		return true;
	}catch(SQLException e){
		System.out.println("Unhandled SQLException: ");
		System.out.println(e.getMessage());
		try{
			connection.setAutoCommit(true);
		}catch(SQLException se){

		}
		return false;
		
	}
}
private void showCustomerInfoHelper(){
	String first_name;
	String last_name;
	boolean result;
	while(true){
		System.out.println("Please enter customer's first name.");
		first_name = keyboard.nextLine();
		System.out.println("Please enter customer's last name.");
		last_name = keyboard.nextLine();
		result = showCustomerInfo(first_name,last_name);
		System.out.println("\nWould you like to try another?\n Y/N");
		if(keyboard.nextLine().toLowerCase().equals("y")){
			continue;
		}else{
			break;
		}

	}

}  
public boolean showCustomerInfo(String first_name, String last_name)
{
	String query = "SELECT * FROM customer WHERE first_name = ? and last_name = ?";
	PreparedStatement preparedStatement;
	ResultSet rs;

	try{
		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1,first_name);
		preparedStatement.setString(2,last_name);

		rs = preparedStatement.executeQuery();

		if(rs.next()){
			//results returned
			System.out.println("\nName: "+rs.getString(2)+". "+rs.getString(3)+" "+rs.getString(4));
			System.out.println("PittRewards Number: "+rs.getString(1));
			System.out.println("Address: "+rs.getString(7)+" "+rs.getString(8)+" "+rs.getString(9));
			System.out.println("Phone Number: "+rs.getString(10));
			System.out.println("Email: "+rs.getString(11));
			System.out.println("Credit Card Number: "+rs.getString(5));
			System.out.println("Credit Card Expiration: "+rs.getDate(6));
			if(rs.getString(12) != null){
				System.out.println("Frequent Flier Number: "+rs.getString(12));
			}
			return true;
		}
		else{
			System.out.println("Sorry, no user with that name exists");
			return false;
		}
	}catch(SQLException e){
		System.out.println("Unhandled SQLException: ");
		System.out.println(e.getMessage());
		return false;
	}
}
private void findPriceForFlightsBetweenTwoCitiesHelper(){
	String city_a;
	String city_b;
	boolean result;
	while(true){
		System.out.println("Please enter first city");
		city_a = keyboard.nextLine();
		System.out.println("Please enter second city");
		city_b = keyboard.nextLine();
		result = findPriceForFlightsBetweenTwoCities(city_a,city_b);
		System.out.println("\nWould you like to try another?\n Y/N");
		if(keyboard.nextLine().toLowerCase().equals("y")){
			continue;
		}else{
			break;
		}
	}
}
public boolean findPriceForFlightsBetweenTwoCities(String city_a, String city_b)
{
	String query;
	PreparedStatement preparedStatement;
	ResultSet rs1;
	ResultSet rs2;

	try{	
		query = "SELECT high_price,low_price FROM price WHERE departure_city = ? and arrival_city = ?";

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1,city_a);
		preparedStatement.setString(2,city_b);
		rs1 = preparedStatement.executeQuery();

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1,city_b);
		preparedStatement.setString(2,city_a);
		rs2 = preparedStatement.executeQuery();

		if(rs1.next() && rs2.next()){
			System.out.println("\nPrices from "+city_a+" to "+city_b);
			System.out.println("High Price: "+rs1.getInt(1));
			System.out.println("Low Price: "+rs1.getInt(2));

			System.out.println("\nPrices from "+city_b+" to "+city_a);
			System.out.println("High Price: "+rs2.getInt(1));
			System.out.println("Low Price: "+rs2.getInt(2));

			System.out.println("\nRound Trip prices from "+city_a+" to "+city_b);
			System.out.println("High Price: "+(rs1.getInt(1)+rs2.getInt(1)));
			System.out.println("Low Price: "+(rs1.getInt(2)+rs2.getInt(2)));

			return true;
		}
		else{
			System.out.println("Sorry, no price data for this route is available.");
			return false;
		}
	}catch(SQLException e){
		System.out.println("Unhandled SQLException: ");
		System.out.println(e.getMessage());
		return false;
	}
}
private void findAllRoutesBetweenTwoCitiesHelper(){
	String city_a;
	String city_b;
	boolean result;
	while(true){
		System.out.println("Please enter first city");
		city_a = keyboard.nextLine();
		System.out.println("Please enter second city");
		city_b = keyboard.nextLine();
		result = findAllRoutesBetweenTwoCities(city_a,city_b);
		System.out.println("\nWould you like to try another?\n Y/N");
		if(keyboard.nextLine().toLowerCase().equals("y")){
			continue;
		}else{
			break;
		}

	}
}
public boolean findAllRoutesBetweenTwoCities(String city_a, String city_b)
{
	String query;
	PreparedStatement preparedStatement;
	ResultSet rs1;
	ResultSet rs2;
	boolean ret = false;
	try{
		query = "SELECT f.flight_number, f.departure_city, f.arrival_city, f.departure_time, f.arrival_time FROM flight f WHERE f.departure_city = ? and f.arrival_city = ?";

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1,city_a);
		preparedStatement.setString(2,city_b);
		rs1 = preparedStatement.executeQuery();

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1,city_b);
		preparedStatement.setString(2,city_a);
		rs2 = preparedStatement.executeQuery();

		boolean firstStatement = rs1.next();
		boolean secondStatement = rs2.next();
		System.out.println("\n**RESULTS**");
		if(firstStatement || secondStatement){
			System.out.println("\nDirect trips:");
			while(firstStatement && !rs1.isAfterLast()){
				System.out.println("\nFlight Number: "+rs1.getString(1));
				System.out.println("Departure City: "+rs1.getString(2));
				System.out.println("Arrival City: "+rs1.getString(3));
				System.out.println("Departure Time: "+rs1.getString(4));
				System.out.println("Arrival Time: "+rs1.getString(5));		
				
				rs1.next();
			}

			while(secondStatement && !rs2.isAfterLast()){
				System.out.println("\nFlight Number: "+rs2.getString(1));
				System.out.println("Departure City: "+rs2.getString(2));
				System.out.println("Arrival City: "+rs2.getString(3));
				System.out.println("Departure Time: "+rs2.getString(4));
				System.out.println("Arrival Time: "+rs2.getString(5));
				rs2.next();
			}
			ret = true;
		}
		else{
			System.out.println("Sorry, no direct routes are available.");
		}


		query = "SELECT f.flight_number, f.departure_city, f.arrival_city, f.departure_time, f.arrival_time, f.weekly_schedule FROM flight f WHERE f.departure_city = ?";

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1,city_a);
		rs1 = preparedStatement.executeQuery();
		boolean hasConnection = false;
		boolean firstConnection = false;
		while(rs1.next()){
			query = "SELECT f.flight_number, f.departure_city, f.arrival_city, f.departure_time, f.arrival_time, f.weekly_schedule FROM flight f WHERE f.departure_city = ? and f.arrival_city = ?";
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1,rs1.getString(3));
			preparedStatement.setString(2,city_b);
			rs2 = preparedStatement.executeQuery();
			hasConnection = rs2.next();
			if(hasConnection){
				int firstFlightArrival = Integer.parseInt(rs1.getString(5));
				int secondFlightDeparture = Integer.parseInt(rs2.getString(4));
				while(hasConnection  && !rs2.isAfterLast()){
					boolean hasSameDay = false;
					for(int i =0; i< rs1.getString(6).length(); i++){
						if(rs1.getString(6).charAt(i) == rs2.getString(6).charAt(i)){
							hasSameDay = true;
							break;
						}
					}
					if(hasSameDay && Math.abs(secondFlightDeparture - firstFlightArrival) >= 100 ){
						if(!firstConnection){
							firstConnection = true;
							System.out.println("\nConnection trips:");
						}
						System.out.println("\nFlight 1:");
						System.out.println("Flight Number: "+rs1.getString(1));
						System.out.println("Departure City: "+rs1.getString(2));
						System.out.println("Arrival City: "+rs1.getString(3));
						System.out.println("Departure Time: "+rs1.getString(4));
						System.out.println("Arrival Time: "+rs1.getString(5));

						System.out.println("\nFlight 2:");
						System.out.println("Flight Number: "+rs2.getString(1));
						System.out.println("Departure City: "+rs2.getString(2));
						System.out.println("Arrival City: "+rs2.getString(3));
						System.out.println("Departure Time: "+rs2.getString(4));
						System.out.println("Arrival Time: "+rs2.getString(5));
						System.out.println();
						ret = true;

					}
					hasConnection = rs2.next();		
				}
			}
			
		}
		if(!firstConnection){
			System.out.println("Sorry, no connecting trips available");
		}
	}catch(SQLException e){
		System.out.println("Unhandled SQLException: ");
		//System.out.println(e.getMessage());
		e.printStackTrace();
		return false;
	}
	return ret;
}
private void findAllRoutesBtwTwoCitiesForAirlineHelper(){
	String city_a;
	String city_b;
	String airline_name;
	boolean result;
	while(true){
		System.out.println("Please enter first city");
		city_a = keyboard.nextLine();
		System.out.println("Please enter second city");
		city_b = keyboard.nextLine();
		System.out.println("Please enter an airline");
		airline_name = keyboard.nextLine();
		result = findAllRoutesBtwTwoCitiesForAirline(city_a,city_b,airline_name);
		System.out.println("\nWould you like to try another?\n Y/N");
		if(keyboard.nextLine().toLowerCase().equals("y")){
			continue;
		}else{
			break;
		}

	}
}
public boolean findAllRoutesBtwTwoCitiesForAirline(String city_a, String city_b, String airline_name)
{
	String query;
	PreparedStatement preparedStatement;
	ResultSet rs1;
	ResultSet rs2;
	boolean ret = false;
	try{	
		query = "SELECT f.flight_number, f.departure_city, f.arrival_city, f.departure_time, f.arrival_time, a.airline_id FROM flight f INNER JOIN Airline a on f.airline_id = a.airline_id WHERE f.departure_city = ? and f.arrival_city = ? and a.airline_name = ?";

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1,city_a);
		preparedStatement.setString(2,city_b);
		preparedStatement.setString(3,airline_name);
		rs1 = preparedStatement.executeQuery();

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1,city_b);
		preparedStatement.setString(2,city_a);
		preparedStatement.setString(3,airline_name);
		rs2 = preparedStatement.executeQuery();

		boolean firstStatement = rs1.next();
		boolean secondStatement = rs2.next();
		System.out.println("\n**RESULTS**");
		if(firstStatement || secondStatement){
			System.out.println("\nDirect trips:");
			while(firstStatement && !rs1.isAfterLast()){
				System.out.println("\nAirline ID: "+rs1.getString(6));
				System.out.println("Flight Number: "+rs1.getString(1));
				System.out.println("Departure City: "+rs1.getString(2));
				System.out.println("Arrival City: "+rs1.getString(3));
				System.out.println("Departure Time: "+rs1.getString(4));
				System.out.println("Arrival Time: "+rs1.getString(5));		
				
				rs1.next();
			}

			while(secondStatement && !rs2.isAfterLast()){
				System.out.println("\nAirline ID: "+rs2.getString(6));
				System.out.println("Flight Number: "+rs2.getString(1));
				System.out.println("Departure City: "+rs2.getString(2));
				System.out.println("Arrival City: "+rs2.getString(3));
				System.out.println("Departure Time: "+rs2.getString(4));
				System.out.println("Arrival Time: "+rs2.getString(5));
				rs2.next();
			}
			ret = true;
		}
		else{
			System.out.println("Sorry, no direct routes are available.");
		}


		query = "SELECT f.flight_number, f.departure_city, f.arrival_city, f.departure_time, f.arrival_time, f.weekly_schedule, a.airline_id FROM flight f INNER JOIN Airline a on f.airline_id = a.airline_id WHERE f.departure_city = ? and a.airline_name = ?";

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1,city_a);
		preparedStatement.setString(2,airline_name);
		rs1 = preparedStatement.executeQuery();
		boolean hasConnection = false;
		boolean firstConnection = false;
		while(rs1.next()){
			query = "SELECT f.flight_number, f.departure_city, f.arrival_city, f.departure_time, f.arrival_time, f.weekly_schedule, a.airline_id FROM flight f INNER JOIN Airline a on f.airline_id = a.airline_id WHERE f.departure_city = ? and f.arrival_city = ? and a.airline_name = ?";
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1,rs1.getString(3));
			preparedStatement.setString(2,city_b);
			preparedStatement.setString(3,airline_name);
			rs2 = preparedStatement.executeQuery();
			hasConnection = rs2.next();
			if(hasConnection){
				int firstFlightArrival = Integer.parseInt(rs1.getString(5));
				int secondFlightDeparture = Integer.parseInt(rs2.getString(4));
				while(hasConnection  && !rs2.isAfterLast()){
					boolean hasSameDay = false;
					for(int i =0; i< rs1.getString(6).length(); i++){
						if(rs1.getString(6).charAt(i) == rs2.getString(6).charAt(i)){
							hasSameDay = true;
							break;
						}
					}
					if(hasSameDay && (secondFlightDeparture - firstFlightArrival) >= 100 ){
						if(!firstConnection){
							firstConnection = true;
							System.out.println("\nConnection trips:");
						}
						System.out.println("\nFlight 1:");
						System.out.println("\nAirline ID: "+rs1.getString(7));
						System.out.println("Flight Number: "+rs1.getString(1));
						System.out.println("Departure City: "+rs1.getString(2));
						System.out.println("Arrival City: "+rs1.getString(3));
						System.out.println("Departure Time: "+rs1.getString(4));
						System.out.println("Arrival Time: "+rs1.getString(5));

						System.out.println("\nFlight 2:");
						System.out.println("\nAirline ID: "+rs2.getString(7));
						System.out.println("Flight Number: "+rs2.getString(1));
						System.out.println("Departure City: "+rs2.getString(2));
						System.out.println("Arrival City: "+rs2.getString(3));
						System.out.println("Departure Time: "+rs2.getString(4));
						System.out.println("Arrival Time: "+rs2.getString(5));
						System.out.println("------------------------");
						ret = true;
					}
					hasConnection = rs2.next();		
				}
			}
			
		}
		if(!firstConnection){
			System.out.println("Sorry, no connecting trips available");
		}
	
	}catch(SQLException e){
		System.out.println("Unhandled SQLException: ");
		//System.out.println(e.getMessage());
		e.printStackTrace();
		return false;
	}
	return ret;
}
private void findAllRoutesWithSeatsBtwTwoCitiesOnDayHelper(){
	String city_a;
	String city_b;
	Date flight_date = null;
	boolean result;
	SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
	while(true){
			System.out.println("Please enter first city");
			city_a = keyboard.nextLine();
			System.out.println("Please enter second city");
			city_b = keyboard.nextLine();
			System.out.println("Please enter a date (MM/dd/yyyy)");
			boolean haveDate = false;
			while(!haveDate){
				try{
					flight_date = df.parse(keyboard.nextLine());
					haveDate = true;
				} catch(ParseException e){
					System.out.println("Error parsing date.");
				}
			}
			result = findAllRoutesWithSeatsBtwTwoCitiesOnDay(city_a,city_b,flight_date);
			System.out.println("\nWould you like to try another?\n Y/N");
			if(keyboard.nextLine().toLowerCase().equals("y")){
				continue;
			}else{
				break;
			}
	}
}
public boolean findAllRoutesWithSeatsBtwTwoCitiesOnDay(String city_a, String city_b, Date flight_date)
{
	String query;
	PreparedStatement preparedStatement;
	ResultSet rs1;
	ResultSet rs2;
	boolean ret = false;
	try{
			
			//Direct flight queries
			query = "SELECT f.flight_number, f.departure_city, f.arrival_city, f.departure_time, f.arrival_time, f.weekly_schedule FROM flight f inner join (select count(reservation_number) c_res, flight_number, flight_date from reservation_detail group by flight_number,flight_date) rd on f.flight_number = rd.flight_number inner join Plane p on f.plane_type = p.plane_type WHERE f.departure_city = ? and f.arrival_city = ? and rd.flight_date = ? and c_res <> p.plane_capacity";

			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1,city_a);
			preparedStatement.setString(2,city_b);
			if(flight_date != null){
				preparedStatement.setDate(3,new java.sql.Date(flight_date.getTime()));
			} else{
				System.out.println("Flight date is not set!");
				return false;
			}
			rs1 = preparedStatement.executeQuery();

			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1,city_b);
			preparedStatement.setString(2,city_a);
			if(flight_date != null){
				preparedStatement.setDate(3,new java.sql.Date(flight_date.getTime()));
			} else{
				System.out.println("Flight date is not set!");
				return false;
			}
			rs2 = preparedStatement.executeQuery();

			boolean firstStatement = rs1.next();
			boolean secondStatement = rs2.next();
			System.out.println("\n**RESULTS**");
			if(firstStatement || secondStatement){
				System.out.println("\nDirect trips:");
				while(firstStatement && !rs1.isAfterLast()){
					System.out.println("\nFlight Number: "+rs1.getString(1));
					System.out.println("Departure City: "+rs1.getString(2));
					System.out.println("Arrival City: "+rs1.getString(3));
					System.out.println("Departure Time: "+rs1.getString(4));
					System.out.println("Arrival Time: "+rs1.getString(5));		
					
					rs1.next();
				}

				while(secondStatement && !rs2.isAfterLast()){
					System.out.println("\nFlight Number: "+rs2.getString(1));
					System.out.println("Departure City: "+rs2.getString(2));
					System.out.println("Arrival City: "+rs2.getString(3));
					System.out.println("Departure Time: "+rs2.getString(4));
					System.out.println("Arrival Time: "+rs2.getString(5));
					rs2.next();
				}
				ret = true;
				
			}
			else{
				System.out.println("Sorry, no direct routes are available.");
			}


			//Connecting flight queries
			//Assumption: Connecting flights must be on the same date given by the user.
			query = "SELECT f.flight_number, f.departure_city, f.arrival_city, f.departure_time, f.arrival_time, f.weekly_schedule FROM flight f inner join (select count(reservation_number) c_res, flight_number, flight_date from reservation_detail group by flight_number,flight_date) rd on f.flight_number = rd.flight_number inner join Plane p on f.plane_type = p.plane_type WHERE f.departure_city = ? and rd.flight_date = ? and c_res <> p.plane_capacity";

			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1,city_a);
			if(flight_date != null){
				preparedStatement.setDate(2,new java.sql.Date(flight_date.getTime()));
			} else{
				System.out.println("Flight date is not set!");
				return false;
			}
			rs1 = preparedStatement.executeQuery();
			boolean hasConnection = false;
			boolean firstConnection = false;
			while(rs1.next()){
				query = "SELECT f.flight_number, f.departure_city, f.arrival_city, f.departure_time, f.arrival_time, f.weekly_schedule FROM flight f inner join (select count(reservation_number) c_res, flight_number, flight_date from reservation_detail group by flight_number,flight_date) rd on f.flight_number = rd.flight_number inner join Plane p on f.plane_type = p.plane_type WHERE f.departure_city = ? and f.arrival_city = ? and rd.flight_date = ? and c_res <> p.plane_capacity";
				preparedStatement = connection.prepareStatement(query);
				preparedStatement.setString(1,rs1.getString(3));
				preparedStatement.setString(2,city_b);
				if(flight_date != null){
					preparedStatement.setDate(3,new java.sql.Date(flight_date.getTime()));
				} else{
					System.out.println("Flight date is not set!");
					return false;
				}
				rs2 = preparedStatement.executeQuery();
				hasConnection = rs2.next();
				if(hasConnection){
					int firstFlightArrival = Integer.parseInt(rs1.getString(5));
					int secondFlightDeparture = Integer.parseInt(rs2.getString(4));
					while(hasConnection  && !rs2.isAfterLast()){
						boolean hasSameDay = false;
						for(int i =0; i< rs1.getString(6).length(); i++){
							if(rs1.getString(6).charAt(i) == rs2.getString(6).charAt(i)){
								hasSameDay = true;
								break;
							}
						}
						if(hasSameDay && (secondFlightDeparture - firstFlightArrival) >= 100 ){
							if(!firstConnection){
								firstConnection = true;
								System.out.println("\nConnection trips:");
							}
							System.out.println("\nFlight 1:");
							System.out.println("Flight Number: "+rs1.getString(1));
							System.out.println("Departure City: "+rs1.getString(2));
							System.out.println("Arrival City: "+rs1.getString(3));
							System.out.println("Departure Time: "+rs1.getString(4));
							System.out.println("Arrival Time: "+rs1.getString(5));

							System.out.println("\nFlight 2:");
							System.out.println("Flight Number: "+rs2.getString(1));
							System.out.println("Departure City: "+rs2.getString(2));
							System.out.println("Arrival City: "+rs2.getString(3));
							System.out.println("Departure Time: "+rs2.getString(4));
							System.out.println("Arrival Time: "+rs2.getString(5));
							System.out.println();

						}
						hasConnection = rs2.next();		
					}
				}
				ret = true;
				
			}
			if(!firstConnection){
				System.out.println("Sorry, no connecting trips available");
			}
	}catch(SQLException e){
		System.out.println("Unhandled SQLException: ");
		//System.out.println(e.getMessage());
		e.printStackTrace();
		return false;
	}
	return ret;
}
private void findAllRoutesWithSeatsBtwTwoCitiesOnDayForAirlineHelper(){
	String city_a;
	String city_b;
	String airline_name;
	Date flight_date = null;
	boolean result;
	SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
	while(true){
		System.out.println("Please enter first city");
		city_a = keyboard.nextLine();
		System.out.println("Please enter second city");
		city_b = keyboard.nextLine();
		System.out.println("Please enter a date (MM/dd/yyyy)");
		boolean haveDate = false;
		while(!haveDate){
			try{
				flight_date = df.parse(keyboard.nextLine());
				haveDate = true;
			} catch(ParseException e){
				System.out.println("Error parsing date.");
			}
		}
		System.out.println("Please enter an airline");
		airline_name = keyboard.nextLine();

		result = findAllRoutesWithSeatsBtwTwoCitiesOnDayForAirline(city_a,city_b,airline_name,flight_date);

		System.out.println("\nWould you like to try another?\n Y/N");
		if(keyboard.nextLine().toLowerCase().equals("y")){
			continue;
		}else{
			break;
		}

	}
}
public boolean findAllRoutesWithSeatsBtwTwoCitiesOnDayForAirline(String city_a, String city_b, String airline_name, Date flight_date){
	String query;
	PreparedStatement preparedStatement;
	ResultSet rs1;
	ResultSet rs2;
	boolean ret = false;
	try{	
		//Direct flight queries
		query = "SELECT f.flight_number, f.departure_city, f.arrival_city, f.departure_time, f.arrival_time, f.weekly_schedule FROM flight f inner join (select count(reservation_number) c_res, flight_number, flight_date from reservation_detail group by flight_number,flight_date) rd on f.flight_number = rd.flight_number inner join Plane p on f.plane_type = p.plane_type INNER JOIN Airline a on f.airline_id = a.airline_id WHERE f.departure_city = ? and f.arrival_city = ? and rd.flight_date = ? and a.airline_name = ? and c_res <> p.plane_capacity";

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1,city_a);
		preparedStatement.setString(2,city_b);
		if(flight_date != null){
			preparedStatement.setDate(3,new java.sql.Date(flight_date.getTime()));
		} else{
			System.out.println("Flight date is not set!");
			return false;
		}
		preparedStatement.setString(4,airline_name);
		rs1 = preparedStatement.executeQuery();

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1,city_b);
		preparedStatement.setString(2,city_a);
		if(flight_date != null){
			preparedStatement.setDate(3,new java.sql.Date(flight_date.getTime()));
		} else{
			System.out.println("Flight date is not set!");
			return false;
		}
		preparedStatement.setString(4,airline_name);
		rs2 = preparedStatement.executeQuery();

		boolean firstStatement = rs1.next();
		boolean secondStatement = rs2.next();
		System.out.println("\n**RESULTS**");
		if(firstStatement || secondStatement){
			System.out.println("\nDirect trips:");
			while(firstStatement && !rs1.isAfterLast()){
				System.out.println("\nFlight Number: "+rs1.getString(1));
				System.out.println("Departure City: "+rs1.getString(2));
				System.out.println("Arrival City: "+rs1.getString(3));
				System.out.println("Departure Time: "+rs1.getString(4));
				System.out.println("Arrival Time: "+rs1.getString(5));		
				
				rs1.next();
			}

			while(secondStatement && !rs2.isAfterLast()){
				System.out.println("\nFlight Number: "+rs2.getString(1));
				System.out.println("Departure City: "+rs2.getString(2));
				System.out.println("Arrival City: "+rs2.getString(3));
				System.out.println("Departure Time: "+rs2.getString(4));
				System.out.println("Arrival Time: "+rs2.getString(5));
				rs2.next();
			}
			ret = true;
		}
		else{
			System.out.println("Sorry, no direct routes are available.");
		}


		//Connecting flight queries
		//Assumption: Connecting flights must be on the same date given by the user.
		query = "SELECT f.flight_number, f.departure_city, f.arrival_city, f.departure_time, f.arrival_time, f.weekly_schedule FROM flight f inner join (select count(reservation_number) c_res, flight_number, flight_date from reservation_detail group by flight_number,flight_date) rd on f.flight_number = rd.flight_number inner join Plane p on f.plane_type = p.plane_type INNER JOIN Airline a on f.airline_id = a.airline_id WHERE f.departure_city = ? and rd.flight_date = ? and a.airline_name = ? and c_res <> p.plane_capacity";

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1,city_a);
		if(flight_date != null){
			preparedStatement.setDate(2,new java.sql.Date(flight_date.getTime()));
		} else{
			System.out.println("Flight date is not set!");
			return false;
		}
		preparedStatement.setString(3,airline_name);
		rs1 = preparedStatement.executeQuery();
		boolean hasConnection = false;
		boolean firstConnection = false;
		while(rs1.next()){
			query = "SELECT f.flight_number, f.departure_city, f.arrival_city, f.departure_time, f.arrival_time, f.weekly_schedule FROM flight f inner join (select count(reservation_number) c_res, flight_number, flight_date from reservation_detail group by flight_number,flight_date) rd on f.flight_number = rd.flight_number inner join Plane p on f.plane_type = p.plane_type INNER JOIN Airline a on f.airline_id = a.airline_id WHERE f.departure_city = ? and f.arrival_city = ? and rd.flight_date = ? and a.airline_name = ? and c_res <> p.plane_capacity";
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1,rs1.getString(3));
			preparedStatement.setString(2,city_b);
			if(flight_date != null){
				preparedStatement.setDate(3,new java.sql.Date(flight_date.getTime()));
			} else{
				System.out.println("Flight date is not set!");
				return false;
			}
			preparedStatement.setString(4,airline_name);
			rs2 = preparedStatement.executeQuery();
			hasConnection = rs2.next();
			if(hasConnection){
				int firstFlightArrival = Integer.parseInt(rs1.getString(5));
				int secondFlightDeparture = Integer.parseInt(rs2.getString(4));
				while(hasConnection  && !rs2.isAfterLast()){
					boolean hasSameDay = false;
					for(int i =0; i< rs1.getString(6).length(); i++){
						if(rs1.getString(6).charAt(i) == rs2.getString(6).charAt(i)){
							hasSameDay = true;
							break;
						}
					}
					if(hasSameDay && (secondFlightDeparture - firstFlightArrival) >= 100 ){
						if(!firstConnection){
							firstConnection = true;
							System.out.println("\nConnection trips:");
						}
						System.out.println("\nFlight 1:");
						System.out.println("Flight Number: "+rs1.getString(1));
						System.out.println("Departure City: "+rs1.getString(2));
						System.out.println("Arrival City: "+rs1.getString(3));
						System.out.println("Departure Time: "+rs1.getString(4));
						System.out.println("Arrival Time: "+rs1.getString(5));

						System.out.println("\nFlight 2:");
						System.out.println("Flight Number: "+rs2.getString(1));
						System.out.println("Departure City: "+rs2.getString(2));
						System.out.println("Arrival City: "+rs2.getString(3));
						System.out.println("Departure Time: "+rs2.getString(4));
						System.out.println("Arrival Time: "+rs2.getString(5));
						System.out.println();
						ret = true;
					}
					hasConnection = rs2.next();		
				}
			}
			
		}
		if(!firstConnection){
			System.out.println("Sorry, no connecting trips available");
		}
	
	}catch(SQLException e){
		System.out.println("Unhandled SQLException: ");
		//System.out.println(e.getMessage());
		e.printStackTrace();
		return false;
	}
	return ret;
}
private void showReservationInfoGivenNumberHelper(){
	String reservation_number;
	boolean result;
	while(true){
		System.out.println("Please enter your reservation number");
		reservation_number = keyboard.nextLine();

		result = showReservationInfoGivenNumber(reservation_number);

		System.out.println("\nWould you like to try another?\n Y/N");
		if(keyboard.nextLine().toLowerCase().equals("y")){
			continue;
		}else{
			break;
		}
	}
}
public boolean showReservationInfoGivenNumber(String reservation_number)
{
	String query = "SELECT f.flight_number, rd.flight_date, f.departure_city, f.arrival_city, f.departure_time, f.arrival_time, a.airline_name, r.cost, r.ticketed, r.start_city, r.end_city FROM flight f inner join reservation_detail rd on f.flight_number = rd.flight_number inner join reservation r on rd.reservation_number = r.reservation_number inner join airline a on f.airline_id = a.airline_id where rd.reservation_number = ? order by rd.leg ASC";
	PreparedStatement preparedStatement;
	ResultSet rs;
	boolean ret = false;
	try{
		preparedStatement = connection.prepareStatement(query);		
		preparedStatement.setString(1,reservation_number);

		rs = preparedStatement.executeQuery();

		boolean hasResult = rs.next();
		if(hasResult){
			ret = true;
			System.out.println("\n**Reservation Details**");
			System.out.println("Start City: "+rs.getString(10));
			System.out.println("End City: "+rs.getString(11));
			System.out.println("Cost: "+rs.getInt(8));
			System.out.println("Ticketed: "+rs.getString(9));
			int count = 1;
			System.out.println("\n**Flights**");
			do{
				System.out.println("\nFlight "+count+":");
				System.out.println("Airline: "+rs.getString(7));
				System.out.println("Flight Number: "+rs.getString(1));
				System.out.println("Flight Date: "+rs.getDate(2));
				System.out.println("Departure City: "+rs.getString(3));
				System.out.println("Arrival City: "+rs.getString(4));
				System.out.println("Departure Time: "+rs.getString(5));
				System.out.println("Arrival Time: "+rs.getString(6));
				count++;
			}while(rs.next());
		}
		else{
			System.out.println("Reservation not found.");
		}

		

		
	} catch(SQLException e){
		System.out.println("Unhandled SQLException: ");
		//System.out.println(e.getMessage());
		e.printStackTrace();
		return false;
	}
	return ret;
}
private void buyTicketOnReservationHelper(){
	String reservation_number;
	boolean result;
	while(true){
		System.out.println("Please enter your reservation number");
		reservation_number = keyboard.nextLine();
		result = buyTicketOnReservation(reservation_number);

		System.out.println("\nWould you like to try another?\n Y/N");
		if(keyboard.nextLine().toLowerCase().equals("y")){
			continue;
		}else{
			break;
		}
	}
}
public boolean buyTicketOnReservation(String reservation_number)
{
	String query="UPDATE reservation r set r.ticketed = 'Y' where r.reservation_number = ? and r.ticketed = 'N'";
	PreparedStatement preparedStatement;
	int rows;
	

	try{
		preparedStatement = connection.prepareStatement(query);
			
		preparedStatement.setString(1,reservation_number);

		rows = preparedStatement.executeUpdate();
		if(rows > 0){
			System.out.println("\nReservation purchased!");
			return true;
		}
		else{
			System.out.println("Reservation not found or already purchased.");
			return false;
		}

		

		
	} catch(SQLException e){
		System.out.println("Unhandled SQLException: ");
		//System.out.println(e.getMessage());
		e.printStackTrace();
		return false;
	}
}
private void makeReservation()
{
	String startingAirport;
	String cid;
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
		//get the customer's cid
		cid = "";
		System.out.println("Please enter your customer id");
		while(true){
			cid = keyboard.nextLine();
			if(cid.toLowerCase().equals("q")){
				connection.setAutoCommit(true);
				return;
			}
			query = "SELECT * FROM Customer WHERE cid = '" + cid + "'";
			rs = statement.executeQuery(query);
			if(!rs.next()){//no results returned
				System.out.println("Error, customer id not found.  Please try again or enter q to quit.");
			}else{
				break;
			}
		}

		while(true){
			System.out.println("Please enter the departure city for your trip.");
			startingAirport = keyboard.nextLine();
			System.out.println("Please enter the destination city for your trip.");
			destinationAirport = keyboard.nextLine();
			
			//check to see if the database contains pricing data for this trip
			query = "SELECT * FROM Price WHERE departure_city = '" + startingAirport + "' AND arrival_city = '" + destinationAirport + "'";
			rs = statement.executeQuery(query);
			if(!rs.next()){
				//no results returned
				//ASSUMPTION: if a trip is in the db, the trip in the reverse direction is also there
				System.out.println("Sorry, that trip is not in the database.  Would you like to try another? Y/N");
				if(keyboard.nextLine().toLowerCase().equals("y")){
					continue;
				}else{
					connection.rollback();
					connection.setAutoCommit(true);
					return;
				}
			}else{
				break;
			}
		}
		//go ahead and get the next reservation_number to use with the legs and to avoid fk violations
		//issue a query to discover the highest reservation_number in the db
		query = "SELECT * FROM (SELECT reservation_number FROM Reservation ORDER BY to_number(reservation_number) desc) WHERE rownum = 1";
		rs = statement.executeQuery(query);
		//rs should be a single result with just the highest reservation_number as an attribute
		if(rs.next()){
			//add 1 to current highest reservation_num
			reservationNum = String.valueOf(Integer.parseInt(rs.getString(1)) + 1);
		}else{
			//no reservations in db - make this reservation 1
			reservationNum = "1";
		}
		//add the new reservation to db (can be rolled back)
		query = "INSERT INTO Reservation (reservation_number, cid, ticketed, start_city, end_city) VALUES ('" + reservationNum + "', '" + cid + "', 'N', '" + startingAirport + "', '" + destinationAirport + "')";
		statement.execute(query);
		
		System.out.println("Is this a round-trip? Y/N");
		isRoundTrip = keyboard.nextLine().toLowerCase().equals("y");
		String currentFlightNum = "";
		String currentDate;
		String lastArrival = startingAirport;
		String currentArrival;
		int currentLeg = 1;
		String[] flightPrompts = {"Please enter the flight number for your first flight, or enter \"Q\" to quit", "Please enter the flight number for your second flight, or enter \"Q\" to quit", "Please enter the flight number for your first return flight, or enter \"Q\" to quit","Please enter the flight number for your second return flight, or enter \"Q\" to quit"};
		String[] datePrompts = {"Please enter the date for your first flight", "Please enter the date for your second flight", "Please enter the date for your first return flight", "Please enter the date for your second return flight"};
		//track the current status of the trip-in-progress (i.e. first flight, first return flight, etc.)
		int currentStatus = 0;
		//get first leg info
		while(currentStatus < 4){//max 4 flights
			currentFlightNum = getFlightNum(flightPrompts[currentStatus], statement);
			if(currentFlightNum.toLowerCase().equals("q")){
				break;
			}
			if(!checkFlight(lastArrival, currentFlightNum, statement)){
				if(currentStatus == 0){
					System.out.println("Error: your flight must depart from the departure city for your trip");
				}else{
					System.out.println("Error: your flight must depart from the arrival city of the last flight you selected");
				}
				continue;
			}
			//get arrival city for the specified flight
			query = "SELECT arrival_city FROM Flight WHERE flight_number = '" + currentFlightNum + "'";
			rs = statement.executeQuery(query);
			rs.next(); //flight_number is pk - should be only one result
			currentArrival = rs.getString(1);
			
			if(currentStatus == 1){
				//arrival city for this flight must be the ultimate destination supplied earlier
				if(!destinationAirport.equals(currentArrival)){
					System.out.println("Error: the destination for this flight must be the destination city selected earlier");
					continue;
				}
			}else if(currentStatus == 3){
				//end of a round-trip: arrival city must be the starting city
				if(!startingAirport.equals(currentArrival)){
					System.out.println("Error: the destination for this flight must be the city you started from on this round trip");
					continue;
				}
			}
			currentDate = getDate(datePrompts[currentStatus]);
			//add this leg to reservation_detail
			query = "INSERT INTO Reservation_detail VALUES ('" + reservationNum + "', '" + currentFlightNum + "', to_date('" + currentDate + "', 'MM/DD/YYYY'), " + currentLeg + ")";
			statement.execute(query);
			//check to see if the flight is over capacity (in which case leg must be canceled and user informed)
			long planeCapacity;
			long passengerCount;
			query = "SELECT plane_capacity FROM Plane NATURAL JOIN Flight WHERE flight_number = '" + currentFlightNum + "' AND owner_id = airline_id";
			rs = statement.executeQuery(query);
			rs.next();
			planeCapacity = rs.getLong(1);
			//System.out.println("Plane capacity is " + planeCapacity);
			query = "SELECT count(*) FROM reservation_detail WHERE flight_number = '" + currentFlightNum + "' and flight_date = to_date('" + currentDate + "', 'MM/DD/YYYY')";
			rs = statement.executeQuery(query);
			rs.next();
			passengerCount = rs.getLong(1);
			//System.out.println("Flight contains " + passengerCount + " passengers.");
			if(passengerCount > planeCapacity){
				//flight is full
				query = "DELETE FROM reservation_detail WHERE flight_number = '" + currentFlightNum + "' AND flight_date = to_date('" + currentDate + "', 'MM/DD/YYYY')";
				statement.execute(query);
				System.out.println("Could not make reservation: the selected flight is full.");
				continue;
			}
			
			//if we're on the outgoing trip, check to see if we've arrived at the destination
			if(currentArrival.equals(destinationAirport) && currentStatus < 2){
				if(!isRoundTrip){
					//trip is done
					break;
				}else{
					//beginning return trip
					currentStatus = 2;
				}
			}else if(currentArrival.equals(startingAirport) && currentStatus >= 2){
				//we've arrived back to start on a return trip
				break;//we're done
			}else{
				currentStatus++;
			}
			lastArrival = currentArrival;
			currentLeg++;
		}
		if(currentFlightNum.toLowerCase().equals("q")){
			//customer quit mid-dialog
			connection.rollback();
		}else{
			connection.commit();
			System.out.println("Your reservation has been processed and your reservation number is " + reservationNum + ".\n");
		}
		

		//set autocommit back to true before leaving the function
		connection.setAutoCommit(true);
	}catch(SQLException e){
		System.out.println("Unhandled SQLException: ");
		System.out.println(e.getMessage());
		try{
			connection.rollback();
			connection.setAutoCommit(true);
		}catch(SQLException se){

		}
	}
  }

  //returns true if flight is valid
  private boolean checkFlight(String lastArrival, String flight, Statement s)
  {
	ResultSet rs;
	String query = "SELECT departure_city FROM Flight WHERE flight_number = '" + flight + "'";
	try{
		rs = s.executeQuery(query);
		rs.next();
		return rs.getString(1).equals(lastArrival);
	}catch(SQLException e){
		System.out.println("Error querying database or flight number invalid");
		return false;
	}
  }
  
  //prompt the user for a valid date
  private String getDate(String prompt)
  {
	SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
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
  
  //prompt the user for a valid flight number, or q for quit
  private String getFlightNum(String prompt, Statement s) throws SQLException
  {
	String flightNum;
	ResultSet rs;
	while(true){
		System.out.println(prompt);
		flightNum = keyboard.nextLine();
		if(flightNum.toLowerCase().equals("q")){
			return "q";
		}
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

}
