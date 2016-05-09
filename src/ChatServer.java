import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {
	private List<ChatThread> clientList = new ArrayList<ChatThread>();
	public static void main(String[] args) {
		new ChatServer().start();
		
	}
	 public void start(){
		 int portNumber = 1803;
			// create welcome socket
			ServerSocket welcomeSocket = null;
			try {
				welcomeSocket = new ServerSocket(portNumber);
			} catch (BindException e){
				System.out.println("Port "+portNumber+" has been occupied...");
				System.exit(0);
			}catch (IOException e) {
				e.printStackTrace();
			}
			// get client socket
			Socket clientSocket = null;
			
			while (true) {
				try {
					clientSocket = welcomeSocket.accept();
					ChatThread ct = new ChatThread(clientSocket);
					clientList.add(ct);
					new Thread(ct).start();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
	 }
	 
	 class ChatThread implements Runnable{
		private Boolean socketAlive = true;
		private Socket clientSocket = null;
		private PrintWriter pw = null;
		
		public ChatThread(Socket clientSocket){
			this.clientSocket = clientSocket;
			try {
				pw = new PrintWriter(this.clientSocket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void send(String message){
			
			
				pw.println(message);
				pw.flush();
				
			
		}
		@Override
		public void run() {
			// get InputStream
			InputStream is = null;
			try {
				is = clientSocket.getInputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			// read from client
			String receivedMessage = null;
			try{
				while (socketAlive) {
					// read from client
					receivedMessage = br.readLine();
					if(receivedMessage==null || "quit".compareTo(receivedMessage)==0){
						System.out.println("a client exit");
						socketAlive = false;
						
					}else{
						// print to all clients
						int portNumber = clientSocket.getPort();
						for(int i = 0;i < clientList.size();i++){
							ChatThread ct = clientList.get(i);
							if(this==ct){
								continue;
							}
							
							ct.send(portNumber+ ": "+receivedMessage);
						}
					}
				}
			}catch(IOException e){
				socketAlive = false;
			}finally{
				//remove the client Thread from clientList
				clientList.remove(this);
				// close BufferdReader and clientSocket
				try {
					if(br!=null){
						br.close();
					}
					if(pw!=null){
						pw.close();
					}
					if(clientSocket!=null){
						clientSocket.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}


		public Socket getClientSocket() {
			return clientSocket;
		}


		
	}
}

