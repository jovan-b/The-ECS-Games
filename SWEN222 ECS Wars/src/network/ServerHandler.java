package network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * The server handler reads the actions of the client
 * then updates to the server to display their action
 * for everyone else in the game.
 *
 * @author Jovan Bogoievski
 *
 */
public class ServerHandler extends Thread{

	private Socket socket;
	private int uid;
	private DataInputStream input;
	private DataOutputStream output;
	private ServerHandler[] serverHandlers;

	public ServerHandler(Socket socket, int uid){
		this.socket = socket;
		this.uid = uid;
	}

	/**
	 * Runs the game taking inputs from the client to send to the server
	 * to update the main game
	 */
	@Override
	synchronized public void run(){
		try{
			//Create the socket input and output to write to the client
			input = new DataInputStream(socket.getInputStream());
			output = new DataOutputStream(socket.getOutputStream());

			boolean running = true;

			//Continue to run the game reading the clients actions
			while(running){
				//Reads what action the player is trying to do
				int action = input.readInt();
				switch(action){
					//If 1 is the action, it means the player is trying to move
					case 1:
						//Read players x and y positions
						int playerX = input.readInt();
						int playerY = input.readInt();
						//Go through each client updating them of the players new position
						for(int i = 0; i<serverHandlers.length; i++){
							if(serverHandlers[i] == this){
								continue; // don't need to broadcast to yourself
							} else{
								Socket socket = serverHandlers[i].getSocket();
								DataOutputStream socketOut = new DataOutputStream(socket.getOutputStream());
								socketOut.writeInt(uid);
								socketOut.writeInt(1);
								socketOut.writeInt(playerX);
								socketOut.writeInt(playerY);
							}
						}
						break;
					//If 2 is the action, it means a mouse clicked on the game
					case 2:
						int x = input.readInt();
						int y = input.readInt();
						System.out.println(x + " : " +y);
						for(int i = 0; i<serverHandlers.length; i++){
							Socket socket = serverHandlers[i].getSocket();
							DataOutputStream socketOut = new DataOutputStream(socket.getOutputStream());
							socketOut.writeInt(uid);
							//5 is the code that a player shoots for the ClientConnection
							socketOut.writeInt(5);
							socketOut.writeInt(x);
							socketOut.writeInt(y);
						}
				}
			}
			socket.close();
		}
		catch(IOException e){
			System.out.println("CLEINT " + uid + "DISCONNECTED");
			//TODO: Remove the player from the game
		}
		finally{
			try{
				//Disconnect player
				socket.close();
			}
			catch(IOException e){
				e.printStackTrace(System.err);
			}
		}
	}

	public Socket getSocket(){
		return socket;
	}

	public void setServerHandlers(ServerHandler[] serverHandlers){
		this.serverHandlers = serverHandlers;
	}
}
