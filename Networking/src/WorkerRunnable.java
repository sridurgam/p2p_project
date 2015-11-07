import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;

public class WorkerRunnable implements Runnable{

    protected Socket serverSocket = null;
    protected FileInputStream fInStream  = null;
	protected BufferedInputStream bInStream = null;
	protected FileOutputStream outStream = null;
	protected File file = null;

	public WorkerRunnable(Socket serverSocket, int chunkNum) {
        this.serverSocket = serverSocket;
       // this.file = new File(System.getProperty("user.dir") + "/src/TransferDoc.txt");
       // System.out.println(this.file);
    }

    public void run() {
    	try{
	    	try {	
	    		for(int i=0;i<3;i++)
	    		{
	    			File fileChunk = new File("chunk"+i+".txt");
		        	byte [] byteArray = new byte[(int)fileChunk.length()];
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
			}
        } catch (Exception e){
			e.printStackTrace();
		}
    }
}
