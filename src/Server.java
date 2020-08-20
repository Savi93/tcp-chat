import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

public class Server 
{	
	//Die erste ArrayList enthält eine Liste von die Sockets die Aktiv sind;
	//die zweite ArrayList enthält eine Liste von die benutzte Namen in das Chat (wir können uns mit Namen die nicht registriert sind reigistrieren).
	private static ArrayList<Socket> activesockets = new ArrayList<Socket>();
	private static ArrayList<String> clientsusername = new ArrayList<String>();
	
	public static void main(String[] args) throws IOException
	{
		ServerSocket serversocket = new ServerSocket(10101);
		
		while(true)
		{
			//Diesen Main Thread wartet auf neue Verbindungen und startet für jede
			//neue Verbindung ein neues Thread.
			Socket clientsocket = serversocket.accept();
			activesockets.add(clientsocket);

			ServerThread thread = new ServerThread(clientsocket);
			thread.start();
		}
	}
	
	public static ArrayList<Socket> getActiveSockets()
	{
		return activesockets;
	}
	
	public static ArrayList<String> getClientsUsername()
	{
		return clientsusername;
	}
	
	//Diese Methode kontrolliert wenn eine Name ins Chat schon benutzt ist.
	public static boolean isExistingUsername(String username)
	{
		boolean existing = false;
		
		for(String clientusername : clientsusername)
			if(username.equals(clientusername))
				existing = true;
		
		return existing;
	}
}

//Diese Klasse enthält den Thread der die Kommunikation mit ein neuen Client ermöglicht.
class ServerThread extends Thread 
{
	private Date date;
	private Socket clientsocket;
	private String text = "";
	private String username;
	private DataInputStream in;
	private DataOutputStream out;
	
	public ServerThread(Socket clientsocket)
	{
		this.clientsocket = clientsocket;
	}
	
	@Override
	public void run() 
	{	
		try
		{	
			in = new DataInputStream(clientsocket.getInputStream());
			out = new DataOutputStream(clientsocket.getOutputStream());
			
			//Hier, in ein while loop, kontrolliert mann den username der den Client benutzen will
			//wenn es schon benutzt ist, wird ein "Existing username!" geschickt und denn loop geht weiter,
			//sonst wird ein "OK!" geschikt, und denn loop endet.
			do
			{
				username = in.readUTF();
				
				if(Server.isExistingUsername(username))
					out.writeUTF("Existing username!");
				else
					out.writeUTF("OK!");
			}
			while(Server.isExistingUsername(username));
			
			//Hier wird die Methode addClient() abgerufen.
			addClient();
			
			//Hier, liest mann die Nachrichten die aus den Client kommen und schickt denen per Broadcast zu alle Clients
			//dieses loop geht weiter bis den Client seine Verbindung nicht schließt.
			while(!(clientsocket.isClosed()))
			{
				text = in.readUTF();
				System.out.println(getCurrentDate() + " VON " + username + ": " + text);
				broadcastMessage("VON " + username + ": " + text);
			}
			
			removeClient();
		}
		
		catch(IOException e)
		{
			removeClient();
		}
	}
	
	//Diese Methode schickt eine spezifische Nachricht zum alle verbundene Clients.
	public void broadcastMessage(String message) throws IOException
	{
		for(Socket socket : Server.getActiveSockets())
		{
			out = new DataOutputStream(socket.getOutputStream());
			out.writeUTF(getCurrentDate() + " " + message);
		}
	}
	
	//Diese Methode schließt das aktuelle Socket, löscht den Socket aus activesockets ArrayList, löscht die Name aus clientsusername ArrayList
	//und schickt per Broadcast eine Nachricht die sagt das den spezifischen Client die Verbindung geschlossen hat.
	public void removeClient()
	{	
		try 
		{
			clientsocket.close();
			Server.getActiveSockets().remove(clientsocket);	
			Server.getClientsUsername().remove(username);
			broadcastMessage("USER " + username + " hat sich abgemeldet..");
			System.out.println(getCurrentDate() + " USER " + username + " hat sich abgemeldet..");
		} 
		
		catch (IOException e) {}
	}
	
	//Mit diese Methode wird das neu benutzte username in die Liste gefügen, und wird
	//per Broadcast die Nachricht das sich User mit Name XXX sich verbunden hat.
	public void addClient() throws IOException
	{
		Server.getClientsUsername().add(username);
		
		broadcastMessage("USER " + username + " hat sich verbunden..");
		System.out.println(getCurrentDate() + " USER " + username + " hat sich verbunden..");
	}
	
	//Diese Methode gibt zurück die aktuelle Datum/Uhr in String Format.
	public String getCurrentDate()
	{
		date = new Date();
		return date.getDate() + "/" + ((int)date.getMonth() + 1) + "/" + ((int)date.getYear() + 1900) + " " + date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds();
	}
}

