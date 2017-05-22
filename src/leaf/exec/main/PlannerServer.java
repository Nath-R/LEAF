package leaf.exec.main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import leaf.causes.Extraction;
import leaf.context.ContextData;
import leaf.context.LiveSituation;
import leaf.tools.LeafLog;


/**
 * Main process of LEAF.
 * Interact with a planner through a sever.
 * It reacts to various information sent by a planner and send back info if required.
 * For instance, if a task failed, it tags the situation as failure and request user validation.
 * It calls all the function of LEAF.
 * 
 * @author nathan
 *
 */
public class PlannerServer {
	
	
	private static final int PORT = 5601;
	
	private static final int RESPPORT = 5602;
	

	/**
	 * Main that carries the server
	 * @param args nada
	 */
	public static void main(String[] args) {
		
		//Init liveSituation
		LiveSituation ls = new LiveSituation();
		//Start live situation process
		ls.start();
		//TODO
		
		//List of context to validate by user, one at a time
		String failedTaskToCheck = "";
		ArrayList<ContextData> toCheck = new ArrayList<ContextData>();
		
		//Open port        
        DatagramSocket serverSocket = null;
		try {
			serverSocket = new DatagramSocket( PORT );
		} catch (SocketException e1) {
			LeafLog.e("PlannerServer", "Cannot open port !");
			e1.printStackTrace();
		}
		
       

        boolean going = true;
        
        while(going)
        {       	 
        	byte[] receivedData = new byte[1024];
			//Receiving data
			DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
			try {
				serverSocket.receive(receivedPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			InetAddress ip = receivedPacket.getAddress();
			//int port = receivedPacket.getPort();
			
			String dataStr = new String( receivedPacket.getData() );			
			dataStr = dataStr.trim();
           
			LeafLog.i("PlannerServer", "Received message: "+dataStr);
			
			//Interpreting command
			//Command format: command|param1 param2 param3...
			String[] cmdStr = dataStr.split("\\|");
			String cmd = null;
			String[] params = null;
			
			if(cmdStr.length > 1)
			{
				cmd = cmdStr[0];
				params = cmdStr[1].split(" ");
			}
			
			LeafLog.i("PlannerServer", "Received cmd: "+cmd);
			
			
			/** Stopping server **/
			if(dataStr.contains("STOP"))
			{
				going = false;
				break;
			}
			
			/** Error  case **/
			else if( cmd == null || params == null )
			{
				LeafLog.e("PlannerServer", "Invalid Command");
			}
			
			/**Command taskSart: Task is about to start**/
			//-> store current situation
			else if(cmd.contains("taskStart"))
			{
				LeafLog.m("PlannerServer", "Handling command: "+cmd);
				String task = params[0];
				ls.save(task);
				ls.reset();
			}	
			
			/** Command taskend: Task ended **/
			//-> store result
			//-> if failure, launch learning process and store data to validate
			else if(cmd.contains("taskEnd"))
			{
				LeafLog.m("PlannerServer", "Handling command: "+cmd);
				String task = params[0];
				String result = params[1];
				
				//If task was a failure
				if(result.equals("FAILURE"))
				{
					ls.lastTaskFailed();
					failedTaskToCheck = task;
					toCheck = Extraction.extractDataToAsk(task); //Erase previous array
				}
			}	
			
			/** Command reqConf: Request confidence **/
			//-> compute the current confidence for executing the task given in parameter
			//-> format: task method(st1,st2) method(st1,st2)...
			else if(cmd.contains("reqConf"))
			{
				LeafLog.m("PlannerServer", "Handling command: "+cmd);
				//ls.getCurrentTaskRiskStat(task);
				String clientTask = params[0];
				
				HashMap<String, Double> methConf = new HashMap<String, Double>();
				
				//Going through all methods
				for(int i=1; i<params.length; i++)
				{
					String methodName = params[i].split("\\(")[0];
					String subtasksStr = params[i].split("\\(")[1];
					subtasksStr.replaceAll("[()]", "");
					String[] subtasksArr = subtasksStr.split(",");
					ArrayList<String> subtasks = new ArrayList<String>( Arrays.asList(subtasksArr) );
					LeafLog.d("PlannerServer", "Checking confidence for subtasks: "+subtasks);
					
					Double risk = ls.getSubPlanCurrentRisk(subtasks);
					methConf.put(methodName, risk);
				}
				
				//Creating the message
				String message = "metConf|"+clientTask+" ";
				
				for (Entry<String, Double> entry : methConf.entrySet()) 
				{
				    String meth = entry.getKey();
				    Object risk = entry.getValue();
				    message += meth+":"+risk+" ";
				}
				
				//Send message through UDP
				DatagramPacket sendPacket =	new DatagramPacket(message.getBytes(), message.getBytes().length, ip, RESPPORT);
				try {
					serverSocket.send(sendPacket);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			/** Command userValid: user's feedback **/
			//-> get the result of the user to validation of some cd
			else if(cmd.contains("userValid"))
			{
				LeafLog.m("PlannerServer", "Handling command: "+cmd);
				
				String task = params[0];
				HashMap<ContextData, Double> rewards = new HashMap<ContextData, Double>();
				for(int i=1; i<params.length; i++)
				{
					String cdStr = params[i].split(":")[0];
					Double conf = Double.parseDouble( params[i].split(":")[1] );
					String[] cdElemStr = cdStr.replace("(", "").replace(")", "").split(";");
					ContextData cd = new ContextData(cdElemStr[0], cdElemStr[1], cdElemStr[2]);
					rewards.put(cd, conf);
				}
				
				Extraction.getUserFeedback(task, rewards);
			}
			
			/** Command reqDataVal: request for the data that are to query **/
			else if(cmd.contains("reqDataVal"))
			{
				LeafLog.m("PlannerServer", "Handling command: "+cmd);
				
				//Sending all context data to ne asked
				
				String message = "";
				message += failedTaskToCheck+" ";
				System.out.println(toCheck);
				for(ContextData cd: toCheck)
				{
					message += cd.toString().replace(" ", ";")+" ";
				}
				
				//Send message through UDP
				DatagramPacket sendPacket =	new DatagramPacket(message.getBytes(), message.getBytes().length, ip, PORT);
				try {
					serverSocket.send(sendPacket);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
			
			else
			{
				LeafLog.e("PlannerServer", "Unreadable Command");
			}
           
        }
        
        ls.stop();
        serverSocket.close();
	}

}
