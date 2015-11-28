import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client implements Runnable{
	public final static int FILE_SIZE =7000000;
	protected int upNeighbor;
	protected int downNeighbor;
	protected boolean isStopped    = false;
	protected Thread runningThread;
	protected ServerSocket serverSocket;
	protected int serverPort;
	
	public Client(int serverPort){
		this.serverPort = serverPort;
	}
	public void run()
	{
		Socket downloadNeighbourSocket = null;
		Socket uploadNeighbourSocket = null;
		synchronized(this){
            this.runningThread = Thread.currentThread();
        }
		try{
		this.getServerChunks();
        openPeerServerSocket();   
        while(!isStopped()){
            try 
            {
            	System.out.println("In peer server socket!!");
                uploadNeighbourSocket = this.serverSocket.accept();
            } catch (IOException e) {
                if(isStopped()) {
                    System.out.println("Peer1 Server Stopped.") ;
                    return;
                }
                throw new RuntimeException(
                    "Error accepting uploadNeighbour request connection", e);
            }
            System.out.println("HERE");
            new Thread(
                new UploadNeighbourRunnable(uploadNeighbourSocket,this.serverPort)/*total_chunks - num_chunks_from_server*/
            ).start();
            //Connect to download neighbour and query the chunks not yet present
             downloadNeighbourSocket = new Socket("127.0.0.1",8092);
            
        }
        if(downloadNeighbourSocket != null)
		{
			downloadNeighbourSocket.close();
		}   
        }
		catch(Exception e)
		{
			e.printStackTrace();
		}
        System.out.println("Server Stopped.") ;
	}
	
	public void getServerChunks() throws UnknownHostException, IOException{
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
			
			pWriter.println("8091");
			
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
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally{
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
	private void openPeerServerSocket()
	{
		System.out.println("In peer server socket!!");
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open server port for peer1"+this.serverPort, e);
        }
    }
	private synchronized boolean isStopped()
	{
	        return this.isStopped;
	}
	public synchronized void stop()
	{
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing peer "+this.serverPort, e);
        }
    }
	
	public static void main(String args[]) throws UnknownHostException, IOException{
		Client peer = new Client(8091);
		//peer1.createPeers();
		new Thread(peer).start();
		try {
		    Thread.sleep(20 * 10000);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
		
		System.out.println("Stopping Peer1");
		peer.stop();
	}
}
