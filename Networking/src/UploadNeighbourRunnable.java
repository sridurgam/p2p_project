import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.io.PrintWriter;

public class UploadNeighbourRunnable implements Runnable{

    protected Socket serverSocket = null;
    protected FileInputStream fInStream  = null;
	protected BufferedInputStream bInStream = null;
	protected FileOutputStream outStream = null;
	protected File file = null;
	protected PrintWriter pWriter= null;
	protected InputStream inStream= null;
	protected int peerId;
	protected int chunksToTransfer;

	public UploadNeighbourRunnable(Socket serverSocket, int peer) {
		this.peerId = peer;
        this.serverSocket = serverSocket;
    }

	public void transferChunks(int length_array,int[] request_chunks) throws FileNotFoundException,IOException
	{
		for(int i=0;i<length_array;i++)
		{
			System.out.println(System.getProperty("user.dir") + "/src/peer"+this.peerId+"/"+"chunk"+request_chunks[i]);
			File fileChunk = new File(System.getProperty("user.dir") + "/src/peer"+this.peerId+"/"+"chunk"+request_chunks[i]+".pdf");
			if(fileChunk.exists() && !fileChunk.isDirectory()) 
			{ 
				byte [] byteArray = new byte[(int)fileChunk.length()];
	        	System.out.println("Chunk "+i+" "+fileChunk.length());
				fInStream = new FileInputStream(fileChunk);
				bInStream = new BufferedInputStream(fInStream);
				bInStream.read(byteArray, 0, byteArray.length);
				outStream = (FileOutputStream)serverSocket.getOutputStream();
				System.out.println("Sending chunk "+i+" for peer"+this.peerId );
				outStream.write(byteArray, 0, byteArray.length);
				outStream.flush();
				System.out.println("Chunk transfer completed for "+"Chunk "+i+" for peer "+this.peerId);
				System.out.println("Transferred " + fileChunk.length() + " bytes for chunk "+i+" for "+this.peerId);	
			}
		}
	}
    public void run(){
    	try{
	    	try 
	    	{
	    		System.out.println("In run for UploadNeighbourRunnable");
	    		inStream = serverSocket.getInputStream();
	    		//read array containing chunks requested
	    		DataInputStream in = new DataInputStream(inStream);
	    		int length_array = in.readInt();
	    		System.out.println(length_array);
	    		int [] request_chunks = new int[length_array];
	    		for(int i = 0; i < length_array; i++) 
	    		{
	    		      request_chunks[i] = in.readInt();	 
	    		      System.out.println(request_chunks[i]);
	    		}
	    		this.transferChunks(length_array,request_chunks);
	    		in.close();
	        } 
	    	catch(Exception e)
	    	{
	    		e.printStackTrace();
	    	}
	    	finally {			
				if (bInStream!=null){
					bInStream.close();
				}
				if (fInStream!=null){
					fInStream.close();
				}
				if (outStream!=null){
					outStream.close();
				}
				if (serverSocket != null){
					serverSocket.close();
				}
				if (inStream != null){
					inStream.close();
				}
				if (pWriter != null){
					pWriter.close();
				}
			}
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
     } 
		
 }

