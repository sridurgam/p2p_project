import java.io.*;
import java.net.*;

public class Client {
	public final static int FILE_SIZE = 9322386;
	
	public void createPeers() throws UnknownHostException, IOException{
		int bytesRead;
		int current = 0;
		
		FileOutputStream fOutStream = null;
		BufferedOutputStream bOutStream = null;
		FileInputStream inStream = null;
		Socket socket = null;
		
		try{
			socket = new Socket("127.0.0.1", 8081);
			System.out.println("Connected to server!");
			
			byte [] byteArray = new byte [FILE_SIZE];
			inStream = (FileInputStream)socket.getInputStream();
			
			fOutStream = new FileOutputStream("TransferDoc.pdf");
			bOutStream = new BufferedOutputStream(fOutStream);
			bytesRead = inStream.read(byteArray, 0, byteArray.length);
			current = bytesRead;
			
			do{
				bytesRead = inStream.read(byteArray, 0, byteArray.length);
				if (bytesRead >= 0){
					current += bytesRead;
				}
			}while(bytesRead > -1);
			bOutStream.write(byteArray, 0, current);
			bOutStream.flush();
			
			System.out.println("File transfer complete!");
			
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
		}
	}
	public static void main(String args[]) throws UnknownHostException, IOException{
		Client peer1 = new Client();
		peer1.createPeers();
		
		Client peer2 = new Client();
		peer2.createPeers();
	}
}
