import java.io.*;
import java.net.*;
import java.util.*;

public class Client {
	public void createPeers() throws UnknownHostException, IOException{
		Scanner inputScanner = new Scanner(System.in);
		Socket clientSocket = new Socket("127.0.0.1", 8081);		
		Scanner socketScanner = new Scanner(clientSocket.getInputStream());		
		//System.out.println("Enter any number:");
		int number = 22;		
		PrintStream p = new PrintStream(clientSocket.getOutputStream());
		p.println(number);		
		int number1 = socketScanner.nextInt();
		System.out.print(number1+"\n");
		
		inputScanner.close();
		clientSocket.close();
		socketScanner.close();
	}
	public static void main(String args[]) throws UnknownHostException, IOException{
	Client peer1 = new Client();
	peer1.createPeers();
	Client peer2 = new Client();
	peer2.createPeers();
	}
}
