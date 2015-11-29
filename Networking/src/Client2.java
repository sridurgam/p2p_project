import java.io.*;
import java.net.*;

public class Client2 implements Runnable{
	public final static int FILE_SIZE = 100000;
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
	
	public void downloadNeighbourConnect() throws InterruptedException{
		while(true){
			try{
				System.out.println("Attempting connection to download Neighbor");
				downloadNeighbourSocket = new Socket("127.0.0.1", downNeighbor);
				System.out.println("Connected to downloadNeighbor" + downNeighbor);
				break;
			} catch (Exception e){
				Thread.sleep(3000);
			}
		}
	}
	
	public void run(){		
		synchronized(this){
            this.runningThread = Thread.currentThread();
        }
		
		try{
			this.getServerChunks();
	        
	        new Thread(
	        		new UploadNeighbourRunnable(this.serverPort)
	        ).start();
	        
	        downloadNeighbourConnect();
	        
            OutputStream downOut = downloadNeighbourSocket.getOutputStream();
            DataOutputStream downDataOut = new DataOutputStream(downOut);
            
            InputStream downIn = downloadNeighbourSocket.getInputStream();
            DataInputStream downDataIn = new DataInputStream(downIn);
	        
            int request;
            request = numChunks - downloadedChunks;
            System.out.println(request);
            downDataOut.writeInt(request);

	        while(!isStopped()){
	            for(int i = 0; i < numChunks; i++) {
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
	    			
	    			downloadedChunks++;
	    			
	    			try
	    			{
	    				tempFileNum = downDataIn.readInt();
	    			}
	    			catch(Exception e){
	    				tempFileNum = -1;
	    			}
	            }
	            if(downloadedChunks == numChunks){
	            	this.stop();
	            }
	        }
	        if(downloadNeighbourSocket != null){
				downloadNeighbourSocket.close();
			}
        }
		catch(Exception e){
			e.printStackTrace();
		}
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
			
			while(chunkSize > 0){
				fOutStream = new FileOutputStream(new File(System.getProperty("user.dir") + "/" + peerID + "/" + tempFileNum + ".pdf"));
				bOutStream = new BufferedOutputStream(fOutStream);
								
				int currentRead = 0;
				
				System.out.println("chunkSize for " + tempFileNum + " is: " + chunkSize);
				
				do{
					bytesRead = dInStream.read(byteArray, 0, byteArray.length);
					if (bytesRead > -1){
						bOutStream.write(byteArray, 0, bytesRead);
						currentRead += bytesRead;
						bOutStream.flush();
					}
				}while(currentRead < chunkSize);
				
				requestChunks[tempFileNum] = -1;
				tempFileNum += 5;
				downloadedChunks++;
				try
				{
				chunkSize = dInStream.readInt();
				}
				catch(Exception e){
					chunkSize=-1;
					System.out.println("chunkSizeRead : " + chunkSize);
				}
				System.out.println("Downloaded chunk" + tempFileNum + " from Server");
				System.out.println("Downloaded " + downloadedChunks + "/" + numChunks + " chunks");
			}
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
	
	private synchronized boolean isStopped(){
	        return this.isStopped;
	}
	
	public synchronized void stop(){
        this.isStopped = true;
        try{
        	mergeFiles(numChunks);
        } catch(Exception e){
        	e.printStackTrace();
        }
    }
	
	public void mergeFiles(int numChunks) throws IOException{
		FileInputStream inReaderStream = null;
		FileOutputStream outReaderStream = null;
		BufferedInputStream bInReaderStream = null;
		int bytesRead = 0;
		byte[] byteArray = new byte[FILE_SIZE];
		
		try {
			outReaderStream = new FileOutputStream(new File(System.getProperty("user.dir") + "/" + peerID + "/" + "/CombinedFile.mp3"));
			
			for(int i = 0; i < numChunks; i++){
				inReaderStream = new FileInputStream(new File(System.getProperty("user.dir") + "/" + peerID + "/" + i + ".pdf"));
				bInReaderStream = new BufferedInputStream(inReaderStream);
				bytesRead = bInReaderStream.read(byteArray, 0, byteArray.length);
				
				outReaderStream.write(byteArray, 0, bytesRead);
				outReaderStream.flush();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally{
			if(inReaderStream != null){
				inReaderStream.close();
			}
			if(outReaderStream != null){
				outReaderStream.close();
			}
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
		
		System.out.println("Stopping Peer2");
		peer.stop();
	}
}
