import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
	public final static int FILE_SIZE =7000000;
	protected int upNeighbor;
	protected int downNeighbor;
	
	public void createPeers() throws UnknownHostException, IOException{
		int bytesRead;
		FileOutputStream fOutStream = null;
		BufferedOutputStream bOutStream = null;
		FileInputStream finStream = null;
		Socket socket = null;
		InputStream inStream = null;
		PrintWriter pWriter = null;
		
		try{
			socket = new Socket("127.0.0.1", 8081);
			System.out.println("Connected to server!");
			
			byte [] byteArray = new byte [FILE_SIZE];
						
			pWriter = new PrintWriter(socket.getOutputStream(), true);
			inStream = socket.getInputStream();
			
			pWriter.println("1");
			
			Scanner inScanner = new Scanner(inStream);
			upNeighbor = Integer.parseInt(inScanner.nextLine());
			downNeighbor = Integer.parseInt(inScanner.nextLine());
			
			finStream = (FileInputStream)inStream;
			fOutStream = new FileOutputStream(new File("TransferDocCombined.pdf"));
			bOutStream = new BufferedOutputStream(fOutStream);
			
			do{
				bytesRead = finStream.read(byteArray,0, byteArray.length);
				if (bytesRead > -1)
				{
					bOutStream.write(byteArray, 0, bytesRead);
					bOutStream.flush();
				}
			}while(bytesRead > -1);
			System.out.println("File transfer completed!");
			inScanner.close();
		}finally{
			if (bOutStream != null){
				bOutStream.close();
			}
			if (fOutStream != null){
				fOutStream.close();
			}
			if (socket != null){
				socket.close();
			}
			if (inStream != null){
				inStream.close();
			}
			if (pWriter != null){
				pWriter.close();
			}
		}
	}
	public static void main(String args[]) throws UnknownHostException, IOException{
		Client peer1 = new Client();
		peer1.createPeers();
		
		Client peer2 = new Client();
		peer2.createPeers();
	}
}
