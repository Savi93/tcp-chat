import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client
{
	private static String username = "";
	private static Socket clientsocket;
	private static boolean interruptedThread = false;

	//Den Main Thread wird benutzt um sich mit den Server mit einen nicht benutzte Username zu verbinden,
	//zum den Thread für schreiben zu generieren und zum UTF Nachrichten den ganzen Zeit zu lesen.
	//wenn den Status von interruptedThread zu TRUE gestellt wird, wird sich auch die Main Methode stoppen.
	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException
	{	
		Scanner input = new Scanner(System.in);

		try
		{
			clientsocket = new Socket("127.0.0.1", 10101);

			DataInputStream in = new DataInputStream(clientsocket.getInputStream());
			DataOutputStream out = new DataOutputStream(Client.getClientsocket().getOutputStream());
			
			ClientThreadWrite thread = new ClientThreadWrite();
			
			String answer = "";
			
			//Hier wartet mann auf eine nicht benutzte username.
			while(answer.equals("Existing username!") || answer.equals(""))
			{
				if(!(answer.equals("")))
					System.out.println("Name schon benutzt!");
				
				System.out.println("Welche Name moechten Sie benutzen?");
				username = input.nextLine();
				
				if(username.equals(""))
					out.writeUTF("Unknown");
				else
					out.writeUTF(username);
				
				answer = in.readUTF();
			}
			
			//Hier wird denn WriteThread gestarten.
			thread.start();	
			
			//Hier ließt mann UTF Nachrichten die aus den Server ankommern.
			while(!(interruptedThread))
				System.out.println("\n" + in.readUTF());	
				
			clientsocket.close();
			input.close();
		}
		
		catch(IOException e)
		{
			System.out.println("Probleme mit die Verbindung..");
		}
	}

	public static String getUsername() 
	{
		return username;
	}

	public static void setUsername(String username) 
	{
		Client.username = username;
	}

	public static Socket getClientsocket() 
	{
		return Client.clientsocket;
	}

	public static void setClientsocket(Socket clientsocket) 
	{
		Client.clientsocket = clientsocket;
	}
	
	public static void setInterruptedThread(boolean status) 
	{
		interruptedThread = status;
	}
}

//Diese Klasse enthält den Thread die den Client input liest und Nachrichten zum Server schickt;
//Wenn den User /quit tippt (oder wenn die CMD Fenster geschlossen wird) wird den Status interruptedThread zu TRUE gestellt,
//und den ThreadThread und Main Thread gestoppt.
class ClientThreadWrite extends Thread
{
	private Scanner input = new Scanner(System.in);
	private String text = "";
	
	@Override
	public void run() 
	{	
		
		DataOutputStream out = null;
		
		try 
		{
			out = new DataOutputStream(Client.getClientsocket().getOutputStream());
			
			while(!(text.equals("/quit")))
			{
				text = input.nextLine();
				out.writeUTF(text);
			}
			
			input.close();
			interruptThread();
		} 
		
		catch (IOException e) 
		{
			System.out.println("Probleme mit die Verbindung..");
			
			input.close();
			interruptThread();
		}
	}
	
	public void interruptThread()
	{
		Client.setInterruptedThread(true);
		this.interrupt();
	}
}
