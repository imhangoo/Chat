import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatClient {

	public static void main(String[] args) {
		// create an
		new ChatClientFrame();
	}

}

class ChatClientFrame extends Frame {

	private TextField tfText = new TextField();
	private TextArea taContent = new TextArea();
	private StringBuffer logs = new StringBuffer("");
	private Socket clientSocket = null;
	private PrintWriter pw = null;
	private BufferedReader br = null;
	private Boolean toRead = true;
	private Thread rcThread = null;

	public ChatClientFrame() {
		setTitle("ChatClient");
		// set position and size of this frame
		// equivalent to setBounds(300,300,500,500);
		setLocation(300, 300);
		setSize(500, 500);
		// allow user to manually change its size at runtime
		setResizable(true);
		// bind the "exit" button to exit() command
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				disconnectToServer();
				System.exit(0);
			}
		});
		// add action event (carriage return) to text field
		tfText.addActionListener(new ReturnListener());
		// add two components to frame
		add(tfText, BorderLayout.SOUTH);
		taContent.setEditable(false);
		add(taContent, BorderLayout.NORTH);
		// resize the frame and let it adapt to the size of two inner components
		pack();
		// show the frame in screen
		setVisible(true);
		// connect socket to server
		connectToServer();
		rcThread = new Thread(new ReceiveMessageThread());
		rcThread.start();
	}
	

	public void connectToServer() {
		try {
			// create client socket
			clientSocket = new Socket("localhost", 1803);
			// get outputStream and convert it to printwriter
			pw = new PrintWriter(clientSocket.getOutputStream());
			br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (UnknownHostException e1) {
			System.out.println("server does not exist");
			System.exit(0);
		} catch (IOException e1) {
			System.out.println("server does not exist");
			System.exit(0);
		}
	}

	public void disconnectToServer() {
		try {
			toRead = false;		
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				pw.close();
				br.close();
				clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
	}

	private class ReturnListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			String message = tfText.getText().trim();
			if (message.length() == 0) {
				return;
			}
			taContent.append("Me: "+message + "\n");
			tfText.setText("");
			// write to server
			pw.println(message);
			pw.flush();
			if ("quit".compareTo(message) == 0) {
				disconnectToServer();
				System.exit(0);
			}

		}
	}
	// Used to receive messages from server
	private class ReceiveMessageThread implements Runnable {

		String message = null;

		@Override
		public void run() {
			try {
				while (toRead) {
					message = br.readLine();
					if (message == null) {
						disconnectToServer();
						System.exit(0);
					} else {
						taContent.append(message + "\n");
					}
				}
			} catch (IOException e) {
			}
		}

	}

}
