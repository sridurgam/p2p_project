import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
public class Server implements Runnable{
	protected int          serverPort;
    protected ServerSocket serverSocket;
    protected boolean      isStopped    = false;
    protected Thread       runningThread;
    
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
                new WorkerRunnable(clientSocket)
            ).start();
        }
        System.out.println("Server Stopped.") ;
	}
	private synchronized boolean isStopped() 
	{
	        return this.isStopped;
	}
	public synchronized void stop(){
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }
	private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port"+this.serverPort, e);
        }
    }
	public static void main(String args[]) throws IOException{
		Server server = new Server(8081);
		new Thread(server).start();
		try {
		    Thread.sleep(20 * 100);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
		System.out.println("Stopping Server");
		server.stop();

	}
}
