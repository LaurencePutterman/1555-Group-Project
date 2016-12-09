import java.io.*;
import java.sql.*;  //import the file containing definitions for the parts
                    //needed by java for database connection and manipulation
import java.util.Scanner;
import java.util.InputMismatchException; //used to check formatting of files
import java.text.ParseException; //used to check date formatting
public class AdministratorInterface
{
  private Connection connection; //used to hold the jdbc connection to the DB
  private String username, password;
  private Scanner keyboard;

  public AdministratorInterface()
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
				eraseDatabaseInterface();
				break;
			case '2':
				loadAirlineInfoInterface();
				break;
			case '3':
				loadFlightInfoInterface();
				break;
			case '4':
				loadPricingInfoInterface();
				break;
			case '5':
				loadPlaneInfoInterface();
				break;
			case '6':
				createManifestInterface();
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
  private void createManifestInterface()
  {
	String flightNumber;
	String date;
	while(true){
		System.out.println("Enter the number of the flight for which to print the manifest, or enter Q to quit:");
		flightNumber = keyboard.nextLine();
		if(flightNumber.toLowerCase().equals("q")){
			return;
		}
		System.out.println("Enter the date of the flight in the format MM/DD/YYYY");
		date = keyboard.nextLine();
			
		int result = AdministratorTasks.createManifest(flightNumber, date, connection);
		if(result <= 0 && result != AdministratorTasks.CONNECTION_FAILED){
			//encountered error or empty result
			if(result == 0){
				//ResultSet has no rows
				System.out.println("The specified flight does not exist or currently has no passengers.  Would you like to try another flight? Y/N");
			}else if(result == AdministratorTasks.FORMAT_ERROR){
				System.out.println("Error: your date may have been incorrectly formatted.  Would you like to try again? Y/N");
			}
			if(keyboard.nextLine().toLowerCase().equals("y")){
			  continue;
			}else{
			  return;
			} 
		}else if(result == AdministratorTasks.CONNECTION_FAILED){
			System.out.println("Error: connection to database failed");
		}
		System.out.println();
			
	}
  }
  
  private void loadFlightInfoInterface()
  {
	String input = null;
	int result = 0;

	while(input == null){
		System.out.println("Please enter the name of the file containing the flight information, or enter \"Q\" to quit");
		input = keyboard.nextLine();
		if(input.equals("Q") || input.equals("q")){
			return;
		}
		try{
			result = AdministratorTasks.loadFlightInfo(new File(input), connection);
		}catch(IOException f){
			System.out.println("Error opening specified file; please try again");
			input = null;
		}
	}
	if(result == AdministratorTasks.CONNECTION_FAILED){
		System.out.println("Error: connection to the database failed.");
	}else if(result == AdministratorTasks.FORMAT_ERROR){
		System.out.println("Error: file is improperly formatted. Could not read all or part of file.");
	}else if(result == AdministratorTasks.SQL_ERROR){
		System.out.println("Unhandled exception when inserting from file into database");
	}else{
		System.out.println("Successfully  inserted " + result + " flight tuples into the database.");
	}
  }
  
  private void loadAirlineInfoInterface()
  {
	String input = null;
	int result = 0;

	while(input == null){
		System.out.println("Please enter the name of the file containing the airline information, or enter \"Q\" to quit");
		input = keyboard.nextLine();
		if(input.equals("Q") || input.equals("q")){
			return;
		}
		try{
			result = AdministratorTasks.loadAirlineInfo(new File(input), connection);
		}catch(IOException f){
			System.out.println("Error opening specified file; please try again");
			input = null;
		}
	}
	if(result == AdministratorTasks.CONNECTION_FAILED){
		System.out.println("Error: connection to the database failed.");
	}else if(result == AdministratorTasks.FORMAT_ERROR){
		System.out.println("Error: file is improperly formatted. Could not read all or part of file.");
	}else if(result == AdministratorTasks.SQL_ERROR){
		System.out.println("Unhandled exception when inserting from file into database.");
	}else{
		System.out.println("Successfully  inserted " + result + " airline tuples into the database.");
	}
  }
  
  private void eraseDatabaseInterface()
  {
	System.out.println("WARNING: You are about to delete all data in the database.  Are you sure you want to proceed? Y/N");
	if(keyboard.nextLine().toLowerCase().equals("y"))
	{
		AdministratorTasks.eraseDatabase(connection);
	}
  }
  
  private void loadPlaneInfoInterface()
  {
	String input = null;
	int result = 0;

	while(input == null){
		System.out.println("Please enter the name of the file containing the plane information, or enter \"Q\" to quit");
		input = keyboard.nextLine();
		if(input.equals("Q") || input.equals("q")){
			return;
		}
		try{
			result = AdministratorTasks.loadPlaneInfo(new File(input), connection);
		}catch(IOException f){
			System.out.println("Error opening specified file; please try again");
			input = null;
		}
	}
	if(result == AdministratorTasks.CONNECTION_FAILED){
		System.out.println("Error: connection to the database failed.");
	}else if(result == AdministratorTasks.FORMAT_ERROR){
		System.out.println("Error: file is improperly formatted. Could not read all or part of file.");
	}else if(result == AdministratorTasks.SQL_ERROR){
		System.out.println("Unhandled exception when inserting from file into database.");
	}else{
		System.out.println("Successfully  inserted " + result + " plane tuples into the database.");
	}
  }
  
  private void loadPricingInfoInterface()
  {
	String input;
	char option = 0;
	long lowPrice = -1;
	long highPrice = -1;
	String departureCity = "";
	String arrivalCity = "";
	int result = 0;
	
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
				input = null;
				while(input == null){
					System.out.println("Please enter the name of the file containing the pricing information, or enter \"Q\" to quit.");
					input = keyboard.nextLine();
					if(input.equals("Q")){
						break;
					}
					try{
						result = AdministratorTasks.loadPricingInfoFromFile(new File(input), connection);
					}catch(IOException e){
						System.out.println("Error opening the specified file. Please try again.");
						input = null;
						continue;
					}
					if(result == AdministratorTasks.CONNECTION_FAILED){
					System.out.println("Error: connection to the database failed.");
					}else if(result == AdministratorTasks.FORMAT_ERROR){
						System.out.println("Error: file is improperly formatted. Could not read all or part of file.");
					}else if(result == AdministratorTasks.SQL_ERROR){
						System.out.println("Unhandled exception when inserting from file into database.");
					}else{
						System.out.println("Successfully  inserted " + result + " plane tuples into the database.");
					}
				}
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
				result = AdministratorTasks.updatePricingInfo(highPrice, lowPrice, departureCity, arrivalCity, connection);
				if(result == AdministratorTasks.SQL_ERROR){
					System.out.println("Error: unable to perform the requested update.");
				}
				break;
			case 'Q': 
				break;
			default:
			System.out.println("Invalid input; please try again");
		}
	}
  }
  
}


