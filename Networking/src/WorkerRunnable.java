import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class WorkerRunnable implements Runnable{

    protected Socket serverSocket = null;
    protected FileInputStream fInStream  = null;
	protected BufferedInputStream bInStream = null;
	protected FileOutputStream outStream = null;
	protected File file = null;

	public WorkerRunnable(Socket serverSocket, int chunkNum) {
        this.serverSocket = serverSocket;
        this.file = new File(System.getProperty("user.dir") + "\\src\\TransferDoc.pdf");
        System.out.println(this.file);
    }

    public void run() {
        try{
	    	try {
	        	byte [] byteArray = new byte[(int)this.file.length()];
				fInStream = new FileInputStream(this.file);
				bInStream = new BufferedInputStream(fInStream);
				bInStream.read(byteArray, 0, byteArray.length);
				
				outStream = (FileOutputStream)serverSocket.getOutputStream();
				System.out.println("Sending file...");
				outStream.write(byteArray, 0, byteArray.length);
				outStream.flush();
				
				System.out.println("File transfer complete");
				System.out.println("Transferred " + this.file.length() + " bytes");
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
