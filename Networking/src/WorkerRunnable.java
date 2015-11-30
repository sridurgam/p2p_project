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
	    			    		
	    		for(int i=ID; i<ChunkNum; i = i + 3){
	    			File fileChunk = new File("chunk"+i+".pdf");
	    			
	    			dOutStream.writeInt((int)fileChunk.length());
					dOutStream.flush();
	    			
		        	byte [] byteArray = new byte[(int)fileChunk.length()];
		        	
					fInStream = new FileInputStream(fileChunk);
					bInStream = new BufferedInputStream(fInStream);
					int bytesRead = bInStream.read(byteArray, 0, byteArray.length);
					//fOutStream = (FileOutputStream)outStream;
					
					if(bytesRead != -1){
						System.out.println("Bytes read before writing to client "+bytesRead);
						dOutStream.write(byteArray, 0, bytesRead);
						dOutStream.flush();
					}
					System.out.println("Size of chunk " + i + " is " + (int)fileChunk.length() + ", peer is " + ID);
					
					if (bInStream!=null){
						bInStream.close();
					}
					if (fInStream!=null){
						fInStream.close();
					}
				}
	    		dOutStream.writeInt(-100);
	    		dOutStream.flush();
	        } finally {
				if (outStream!=null){
					outStream.close();
				}
				if (inStream != null){
					inStream.close();
				}
				if (serverSocket != null){
					serverSocket.close();
				}
			}
        } catch (Exception e){
			e.printStackTrace();
		}
    }
}
