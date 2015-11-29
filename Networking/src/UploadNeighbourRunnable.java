import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class UploadNeighbourRunnable implements Runnable{

	protected ServerSocket sSocket = null;
    protected Socket serverSocket = null;
    protected FileInputStream fInStream  = null;
	protected BufferedInputStream bInStream = null;
	protected FileOutputStream fOutStream = null;
	protected File file = null;
	protected InputStream inStream= null;
	protected OutputStream outStream = null;
	protected DataInputStream dInStream= null;
	protected DataOutputStream dOutStream = null;
	protected int peerId;
	protected int chunksToTransfer;

	public UploadNeighbourRunnable(int peer) {
		this.peerId = peer;
    }

	private void openPeerServerSocket()
	{
		System.out.println("In peer server socket!!");
        try {
            this.sSocket = new ServerSocket(this.peerId);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open server port for peer1"+this.peerId, e);
        }
    }
	
	public int transferChunks(int length_array,int[] request_chunks) throws FileNotFoundException,IOException{
		int transferred_chunks = 0;
		for(int i=0;i<length_array;i++){
//			System.out.println(System.getProperty("user.dir")+"/"+ peerId+ "/" + request_chunks[i]+".pdf");
			File fileChunk = new File(System.getProperty("user.dir") + "/" + peerId + "/" + request_chunks[i]+".pdf");
			if(fileChunk.exists() && !fileChunk.isDirectory()){ 
				System.out.println("Sending chunks to upload neighbour");
				byte [] byteArray = new byte[(int)fileChunk.length()];
				
				dOutStream.writeInt(request_chunks[i]);
				dOutStream.writeInt((int)fileChunk.length());
				
	        	System.out.println("Chunk "+i+" "+fileChunk.length());
				fInStream = new FileInputStream(fileChunk);
				bInStream = new BufferedInputStream(fInStream);
				bInStream.read(byteArray, 0, byteArray.length);
				fOutStream = (FileOutputStream)outStream;
				
//				System.out.println("Sending chunk "+i+" for peer"+this.peerId );
				fOutStream.write(byteArray, 0, byteArray.length);
				fOutStream.flush();
				
//				System.out.println("Chunk transfer completed for "+"Chunk "+i+" for peer "+this.peerId);
				transferred_chunks++;
				System.out.println("Transferred " + fileChunk.length() + " bytes for chunk "+request_chunks[i]+" for "+this.peerId);	
			}
		}
		dOutStream.writeInt(-1);
		return transferred_chunks;
	}
    public void run(){
    	int transferred_chunks;
    	try{
    		openPeerServerSocket();
    		serverSocket = sSocket.accept();
    		
    	} catch (Exception e){
    		e.printStackTrace();
    	}
    	try{
	    	try{
	    		inStream = serverSocket.getInputStream();
	    		outStream = serverSocket.getOutputStream();
	    		
	    		//read array containing chunks requested
	    		dInStream = new DataInputStream(inStream);
	    		dOutStream = new DataOutputStream(outStream);
	    		this.chunksToTransfer = dInStream.readInt();
	    		
	    		System.out.println("Array Length "+this.chunksToTransfer + " peer:" + peerId);
	        	while(true)
	        	{
		    		int [] request_chunks = new int[this.chunksToTransfer];
		    		for(int i = 0; i < this.chunksToTransfer; i++) 
		    		{
		    		      request_chunks[i] = dInStream.readInt();	 
		    		    //  System.out.println("Request chunk "+request_chunks[i]);
		    		}
		    		transferred_chunks = this.transferChunks(this.chunksToTransfer,request_chunks);
		    		this.chunksToTransfer = this.chunksToTransfer - transferred_chunks;
		    		if(this.chunksToTransfer == 0)
		    		{
		    			break;
		    		}
	        	}
	        } 
	    	catch(Exception e)
	    	{
	    		e.printStackTrace();
	    	}
	    	finally {
	    		if (dInStream!=null){
					dInStream.close();
				}
	    		if (dOutStream!=null){
					dOutStream.close();
				}
				if (bInStream!=null){
					bInStream.close();
				}
				if (fInStream!=null){
					fInStream.close();
				}
				if (fOutStream!=null){
					fOutStream.close();
				}
				if (serverSocket != null){
					serverSocket.close();
				}
				if (inStream != null){
					inStream.close();
				}
			}
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
     } 
		
 }

