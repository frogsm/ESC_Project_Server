package com.esc_project.ClientConnection;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.esc_project.Constants;
import com.esc_project.DatabaseConnection.DBController;
import com.esc_project.DatabaseConnection.Product;
import com.esc_project.Parser.JsonHelper;

public class ClientController implements Runnable{
	
	
	/** Ŭ���̾�Ʈ ��Ʈ��ȣ ���� **/
	final int clientPort = 7218;

	private DBController mDBController;
	
	private ServerSocket welcomeSocket;
	private Socket connectionSocket;
	private BufferedReader inFromClient;
	private DataOutputStream outToClient;
	private Thread[] threadArr;
	
	private String clientSentence;
	private String serverSentence;

	
	/** ������ **/
	public ClientController(int serverCnt) {
		// TODO Auto-generated constructor stub
		mDBController = new DBController();
		
		try {
			welcomeSocket = new ServerSocket(clientPort);
			threadArr = new Thread[serverCnt];
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/** i���� �����带 �̿��Ͽ� Ŭ���̾�Ʈ-���� ���! **/
	public void start() {
		for(int i=0 ; i <threadArr.length ; i++) {
			threadArr[i] = new Thread(this);
			threadArr[i].start();
		}
	}
	
	/** Ŭ���̾�Ʈ�� ���� �޼����� �޴´�. **/
	public void getMessageFromClient() {
		
		try {
		connectionSocket = welcomeSocket.accept();
		
		inFromClient = new BufferedReader(
				new InputStreamReader(connectionSocket.getInputStream()));

		outToClient = new DataOutputStream(
				connectionSocket.getOutputStream());

		clientSentence = inFromClient.readLine();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/** Ŭ���̾�Ʈ���� �޼����� ������. **/
	public void sendMessageToClient() {
		try {
			outToClient.writeBytes(serverSentence + '\n');
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** ���������� ���ư��� �κ� **/
	@Override
	public void run() {
		// TODO Auto-generated method stub

		while (true) {
			
			getMessageFromClient();
			System.out.println("Client �޽��� : " + clientSentence);

			JsonHelper jsonhelper = new JsonHelper();
			jsonhelper.parserJsonMessage(clientSentence);
			Object cmdObj = sendCommandToDB(jsonhelper.getType(), jsonhelper.getObject());
			serverSentence = jsonhelper.makeJsonMessage(jsonhelper.getType(), cmdObj);

			sendMessageToClient();
			System.out.println("Sever �޽��� : " + serverSentence);
		}

	}
	
	/** ���ɺ��� �ܺ�DB�� �����Ͽ� ������ JSON�޼����� �����ͺκ��� �޾ƿ��� �κ� **/
	public Object sendCommandToDB(String command, Object data) {
		Object obj = null;
		
		switch(command) {
		case Constants.Uid_Info :
			List<Product> products = new ArrayList<Product>();
			List<String> uid = (List<String>)data;
			
			for(int i=0 ; i <uid.size() ; i++) {
				Product product = mDBController.Uid_Info(uid.get(i).toString());
				products.add(product);
			}
			obj = products;
			break;
		}
		
		return obj;
	}
}