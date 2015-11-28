import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.util.Scanner;
import java.io.PrintWriter;

public class WorkerRunnable implements Runnable{

    protected Socket serverSocket = null;
    protected FileInputStream fInStream  = null;
	protected BufferedInputStream bInStream = null;
	protected FileOutputStream outStream = null;
	protected File file = null;
	protected PrintWriter pWriter= null;
	protected InputStream inStream= null;
	protected int[][] Neighbors= new int[5][3];

	public WorkerRunnable(Socket serverSocket, int chunkNum, int[][] Neighbors) {
        this.serverSocket = serverSocket;
        this.Neighbors = Neighbors;
    }

    public void run() {
    	try{
	    	try {
	    		pWriter = new PrintWriter(serverSocket.getOutputStream(), true);
	    		inStream = serverSocket.getInputStream();
	    		
	    		Scanner inScanner = new Scanner(inStream);
	    		int ID = Integer.parseInt(inScanner.nextLine());
	    		
	    		pWriter.println(Neighbors[ID-8090][0]);
	    		pWriter.println(Neighbors[ID-8090][1]);
	    			    		
	    		for(int i=0;i<2;i++)
	    		{
	    			File fileChunk = new File("chunk"+i+".pdf");
		        	byte [] byteArray = new byte[(int)fileChunk.length()];
		        	System.out.println("Chunk "+i+" "+fileChunk.length());
					fInStream = new FileInputStream(fileChunk);
					bInStream = new BufferedInputStream(fInStream);
					bInStream.read(byteArray, 0, byteArray.length);
					outStream = (FileOutputStream)serverSocket.getOutputStream();
					System.out.println("Sending chunk "+i);
					outStream.write(byteArray, 0, byteArray.length);
					outStream.flush();
					System.out.println("Chunk transfer completed for "+"Chunk "+i);
					System.out.println("Transferred " + fileChunk.length() + " bytes for chunk "+i);
	    		}
	    		inScanner.close();
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
				if (pWriter != null){
					pWriter.close();
				}
			}
        } catch (Exception e){
			e.printStackTrace();
		}
    }
}
