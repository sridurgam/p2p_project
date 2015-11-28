import java.io.*;
import java.net.*;

public class Client2 implements Runnable{
	public final static int FILE_SIZE = 7000000;
	protected int upNeighbor;
	protected int downNeighbor;
	protected boolean isStopped = false;
	protected Thread runningThread;
	protected ServerSocket serverSocket;
	protected int serverPort;
	protected Socket downloadNeighbourSocket = null;
	protected Socket uploadNeighbourSocket = null;
	protected static int peerID = 8092;
	protected static int numChunks = 0;
	protected static int requestChunks[];
	protected static int downloadedChunks = 0;
	
	public Client2(int serverPort){
		this.serverPort = serverPort;
	}
	
	public void run(){		
		synchronized(this){
            this.runningThread = Thread.currentThread();
        }
		
		try{
			this.getServerChunks();
	        openPeerServerSocket();  
	        
	        while(!isStopped()){
	            /*try 
	            {
	            	System.out.println("Starting server thread for client" + this.serverPort % 8090);
	                uploadNeighbourSocket = this.serverSocket.accept();
	            } catch (IOException e) {
	                if(isStopped()) {
	                    System.out.println("Client2 Server Stopped.") ;
	                    return;
	                }
	                throw new RuntimeException(
	                    "Error accepting uploadNeighbour request connection", e);
	            }

	            new Thread(
	                new UploadNeighbourRunnable(uploadNeighbourSocket,this.serverPort)
	            ).start();*/
	            
	            //Connect to download neighbor and query the chunks not yet present
	            System.out.println("Here in peer2");
	            downloadNeighbourSocket = new Socket("127.0.0.1", downNeighbor);

	            OutputStream downOut = downloadNeighbourSocket.getOutputStream();
	            DataOutputStream downDataOut = new DataOutputStream(downOut);
	            
	            InputStream downIn = downloadNeighbourSocket.getInputStream();
	            DataInputStream downDataIn = new DataInputStream(downIn);
	            
	            int request;

	            request = numChunks - downloadedChunks;
	            System.out.println(request);
	            downDataOut.writeInt(request);

	            for(int i = 0; i < numChunks; i++) {
	            	System.out.println("Request of " + i + " is " + requestChunks[i]);
	            	if(requestChunks[i] != -1){
	            		downDataOut.writeInt(i);
	            	}
	            }

	            int tempFileNum = downDataIn.readInt();
	            int dBytesRead = 0;
	            byte[] dByteArray = new byte[FILE_SIZE];
	            FileInputStream downFIn = (FileInputStream)downIn;
	            FileOutputStream downFOut;
				BufferedOutputStream downBOut;
	            while(tempFileNum >= 0) {
	    			int chunkSize = downDataIn.readInt();
	    			System.out.println("chunksize = " + chunkSize);
	    			
	    			while(chunkSize > 0 && tempFileNum >= 0){
	    				downFOut = new FileOutputStream(new File(System.getProperty("user.dir") + "/" + peerID + "/" + tempFileNum + ".pdf"));
	    				downBOut = new BufferedOutputStream(downFOut);
	    				
	    				requestChunks[tempFileNum] = -1;
	    				
	    				int currentRead = 0;
	    				do{
	    					dBytesRead = downFIn.read(dByteArray, 0, dByteArray.length);
	    					if (dBytesRead > -1){
	    						downBOut.write(dByteArray, 0, dBytesRead);
	    						currentRead += dBytesRead;
	    						downBOut.flush();
	    					}
	    				}while(currentRead < chunkSize);
	    				System.out.println("Read " + currentRead + " bytes from Download Neighbor");
	    				downloadedChunks++;
	    				System.out.println("Total files downloaded so far: " + downloadedChunks);
	    				tempFileNum = downDataIn.readInt();
	    			}
	    			System.out.println("File transfer completed!");
	            }
	            if(downloadedChunks == numChunks){
	            	this.stop();
	            }
	        }
	        if(downloadNeighbourSocket != null){
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
		OutputStream outStream = null;
		DataInputStream dInStream = null;
		DataOutputStream dOutStream = null;
		
		try{
			socket = new Socket("127.0.0.1", 8081);
			System.out.println("Connected to server!");
			
			byte [] byteArray = new byte [FILE_SIZE];
						
			inStream = socket.getInputStream();
			outStream = socket.getOutputStream();
			
			dInStream = new DataInputStream(inStream);
			dOutStream = new DataOutputStream(outStream);
			
			dOutStream.writeInt(peerID);
			
			numChunks = dInStream.readInt();
			upNeighbor = dInStream.readInt();
			downNeighbor = dInStream.readInt();
			
			requestChunks = new int[numChunks];
			for (int i = 0; i < numChunks; i++){
				requestChunks[i] = i;
			}
			
			finStream = (FileInputStream)inStream;

			int tempFileNum = peerID - 8091;
			int chunkSize = 0;

			chunkSize = dInStream.readInt();
			System.out.println("chunksize = " + chunkSize);
			
			while(chunkSize > 0){
				fOutStream = new FileOutputStream(new File(System.getProperty("user.dir") + "/" + peerID + "/" + tempFileNum + ".pdf"));
				bOutStream = new BufferedOutputStream(fOutStream);
				
				requestChunks[tempFileNum] = -1;
				
				int currentRead = 0;
				do{
					bytesRead = finStream.read(byteArray, 0, byteArray.length);
					if (bytesRead > -1){
						bOutStream.write(byteArray, 0, bytesRead);
						currentRead += bytesRead;
						bOutStream.flush();
					}
				}while(currentRead < chunkSize);
				System.out.println("Read " + currentRead + " bytes from Server");
				tempFileNum += 2;
				downloadedChunks++;
				chunkSize = dInStream.readInt();
			}
			System.out.println("File transfer completed!");
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
	private synchronized boolean isStopped(){
	        return this.isStopped;
	}
	public synchronized void stop(){
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing peer "+this.serverPort, e);
        }
    }
	
	public static void main(String args[]) throws UnknownHostException, IOException{
		Client2 peer = new Client2(peerID);
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
