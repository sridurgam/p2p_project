import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class WorkerRunnable implements Runnable{

    protected Socket serverSocket = null;
    protected FileInputStream fInStream  = null;
	protected BufferedInputStream bInStream = null;
	protected FileOutputStream fOutStream = null;
	protected File file = null;
	protected InputStream inStream= null;
	protected int[][] Neighbors= new int[5][3];
	protected int ChunkNum;
	protected DataOutputStream dOutStream = null;
	protected DataInputStream dInStream = null;
	protected OutputStream outStream = null;
	
	public WorkerRunnable(Socket serverSocket, int chunkNum, int[][] Neighbors) {
        this.serverSocket = serverSocket;
        this.Neighbors = Neighbors;
        this.ChunkNum = chunkNum;
    }

    public void run() {
    	try{
	    	try {
	    		outStream = serverSocket.getOutputStream();
	    		inStream = serverSocket.getInputStream();
	    		dOutStream = new DataOutputStream(outStream);
	    		dInStream = new DataInputStream(inStream);
	    		
	    		int ID = dInStream.readInt() - 8091;
	    		
	    		dOutStream.writeInt(ChunkNum);
	    		
	    		dOutStream.writeInt(Neighbors[ID][0]);
	    		dOutStream.writeInt(Neighbors[ID][1]);
	    			    		
	    		for(int i=ID; i<ChunkNum; i = i + 2){
	    			File fileChunk = new File("chunk"+i+".pdf");
	    			System.out.println(fileChunk.length());
	    			dOutStream.writeInt((int)fileChunk.length());
	    			
		        	byte [] byteArray = new byte[(int)fileChunk.length()];
		        	System.out.println("Chunk "+i+" "+fileChunk.length());
		        	
					fInStream = new FileInputStream(fileChunk);
					bInStream = new BufferedInputStream(fInStream);
					int bytesRead = bInStream.read(byteArray, 0, byteArray.length);
					fOutStream = (FileOutputStream)outStream;
					System.out.println("Sending chunk "+i);
					
					if(bytesRead != -1){
						fOutStream.write(byteArray, 0, bytesRead);
						fOutStream.flush();
					}
					
					System.out.println("Chunk transfer completed for "+"Chunk "+i);
					System.out.println("Transferred " + fileChunk.length() + " bytes for chunk "+i);
	    		}
	    		System.out.println("Exited Loop");
	    		dOutStream.writeInt(0);
	        } finally {
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
			}
        } catch (Exception e){
			e.printStackTrace();
		}
    }
}
