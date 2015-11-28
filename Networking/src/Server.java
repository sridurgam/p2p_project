import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server implements Runnable{
	protected int serverPort;
    protected ServerSocket serverSocket;
    protected boolean isStopped = false;
    protected Thread runningThread;
    protected String fileName;
    protected int[][] Neighbors;
    protected int chunkNum;
    
	public Server(int port)	{
		this.serverPort = port;
	}
	
	public void populateNeighbors() throws FileNotFoundException{
		File configFile = new File("config.txt");
		BufferedReader configReader = new BufferedReader(new FileReader(configFile));
		
		Neighbors = new int[5][3];
		try{
			for (int i = 0; i < 5; i++){
				String[] NextLine = configReader.readLine().split(" ");
				int id1 = Integer.parseInt(NextLine[0]);
				int id2 = Integer.parseInt(NextLine[1]);
				int id3 = Integer.parseInt(NextLine[2]);
				Neighbors[id1-8091][0] = id2;
				Neighbors[id1-8091][1] = id3;
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				configReader.close();
			} catch(Exception e) {
				e.printStackTrace();
			} 
		}
	}
	
	public void run(){
		synchronized(this){
            this.runningThread = Thread.currentThread();
        }
		
        openServerSocket();
        chunkInputFile();
        
        try {
			populateNeighbors();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
        
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
                new WorkerRunnable(clientSocket, chunkNum, Neighbors)
            ).start();
        }
        System.out.println("Server Stopped.") ;
	}
	
	private void chunkInputFile(){
		File file = new File(System.getProperty("user.dir") + "/src/"+this.fileName);
		chunkNum = 0;
		int chunkSize = 100000;
		byte[] buffer = new byte[chunkSize];
		try{
			BufferedInputStream bf = new BufferedInputStream(new FileInputStream(file));
			int temp =0;
			while((temp = bf.read(buffer)) >=0)
					{
					    System.out.println(buffer[0]);
					    System.out.println(buffer[temp-7]);
						File newFile = new File(System.getProperty("user.dir"), "chunk" + String.format("%d", chunkNum++)+".pdf");
						FileOutputStream fout = new FileOutputStream(newFile);
						fout.write(buffer,0, temp);
						fout.close();
					}
			bf.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			
		}
	}
	
	private synchronized boolean isStopped(){
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
	
	private void openServerSocket(){
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open server port"+this.serverPort, e);
        }
    }
	
	public static void main(String args[]) throws IOException{
		System.out.println("Provide the name of the file to be transferred: ");
		Scanner inputScanner = new Scanner(System.in);
		String fileName = inputScanner.nextLine();
		
		Server server = new Server(8081);
		server.fileName = fileName;
		
		new Thread(server).start();
		try {
		    Thread.sleep(20 * 100000);
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
