import java.util.Scanner;

public class ProjectDriver
{
	public static void main(String args[])
	{
		Scanner keyboard = new Scanner(System.in);
		char option = '0';
		while(option != 'Q'){
			System.out.println("Select an option:\n1: Customer Interface\n2: Admin Interface\nQ: Quit");
			String choice = keyboard.nextLine();
			option = choice.toUpperCase().charAt(0);
			switch(option)
			{
				case '1':
					CustomerTasks customerInterface = new CustomerTasks();
					break;
				case '2':
					AdministratorTasks adminInterface = new AdministratorTasks();
					break;
				case 'Q':
					break;
				default:
					System.out.println("Invalid input.");
			}
		}
		
		
	}
}