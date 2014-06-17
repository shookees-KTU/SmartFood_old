package com.shookees.smartfood;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MainServer 
	extends Thread
{
	ServerSocket serverSocket;
	Socket socket;
	BufferedWriter socket_out;
	BufferedReader socket_in;
	
	public void run()
	{
		//nothing to add yet
	}
	
	/**
	 * Creates a SSL server and listens on given port
	 * @param port socket port number
	 * @author shookees
	 * @category server
	 */
	public MainServer(int port)
	{
		this.changePort(port);
	}
	
	/**
	 * Returns input stream of a selected socket
	 * @return input stream or on ioexception
	 * @author shookees
	 * @category server
	 */
	public void writeBuffer(String content)
	{
		try 
		{
			socket_out.write(content);
		} catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Reads a line from input socket stream
	 * @return line string
	 * @author shookees
	 * @category server
	 */
	public String readBufferLine()
	{
		try
		{
			return socket_in.readLine();
		} catch (IOException e)
		{
			e.printStackTrace();
			return "";
		}
	}
	
	/**
	 * Changes port, restarts the socket
	 * @param port port number
	 * @author shookees
	 * @category server
	 */
	public void changePort(int port)
	{
		try 
		{
			serverSocket.close();
			serverSocket = new ServerSocket(port);
			socket = serverSocket.accept();
			
			socket_out = new BufferedWriter(
							new OutputStreamWriter(socket.getOutputStream()));
			
			socket_in = new BufferedReader(
							new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}
