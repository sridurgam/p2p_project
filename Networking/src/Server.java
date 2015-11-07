import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server implements Runnable{
	protected int          serverPort;
    protected ServerSocket serverSocket;
    protected boolean      isStopped    = false;
    protected Thread       runningThread;
    protected String       fileName;
    
	public Server(int port)
	{
		this.serverPort = port;
	}
	public void run()
	{
		synchronized(this){
            this.runningThread = Thread.currentThread();
        }
		
        openServerSocket();
        chunkInputFile();
        
        while(!isStopped()){
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();
            } catch (IOException e) {
                if(isStopped()) {
                    System.out.println("Server Stopped.") ;
                    return;
                }
                throw new RuntimeException(
                    "Error accepting client connection", e);
            }
            new Thread(
                new WorkerRunnable(clientSocket, 1)
            ).start();
        }
        System.out.println("Server Stopped.") ;
	}
	
	private void chunkInputFile(){
		
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
            throw new RuntimeException("Error closing server", e);
        }
    }
	
	private void openServerSocket()
	{
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port"+this.serverPort, e);
        }
    }
	
	public static void main(String args[]) throws IOException
	{
		System.out.println("Provide the name of the file to be transferred: ");
		Scanner inputScanner = new Scanner(System.in);
		String fileName = inputScanner.nextLine();
		
		Server server = new Server(8081);
		server.fileName = fileName;
		
		new Thread(server).start();
		try {
		    Thread.sleep(20 * 10000);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
		
		if (inputScanner != null){
			inputScanner.close();
		}
		
		System.out.println("Stopping Server");
		server.stop();
	}
}
