import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class WorkerRunnable implements Runnable{

    protected Socket serverSocket = null;

    public WorkerRunnable(Socket serveSocket) {
        this.serverSocket = serveSocket;
    }

    public void run() {
        try {
            Scanner socketScanner = new Scanner(serverSocket.getInputStream());
    		int number = socketScanner.nextInt();
    		number *= 2;
    		PrintStream printStream = new PrintStream(serverSocket.getOutputStream());
    		printStream.println(number);
    		socketScanner.close();
    		serverSocket.close();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }
}