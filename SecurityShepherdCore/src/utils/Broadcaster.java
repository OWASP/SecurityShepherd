package utils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.apache.log4j.Logger;

/**
 * Broadcaster used by Insufficient Network Layer Protection level to broad cast result key in plain text
 * <br/><br/>
 * This file is part of the Security Shepherd Project.
 * 
 * The Security Shepherd project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.<br/>
 * 
 * The Security Shepherd project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.<br/>
 * 
 * You should have received a copy of the GNU General Public License
 * along with the Security Shepherd project.  If not, see <http://www.gnu.org/licenses/>. 
 * @author Mark Denihan
 *
 */
public class Broadcaster 
{
	/**
	 * Method identifies all broadcast addresses the application servers host has available and broadcasts a packet containing the result key for the insufficent network layer project lesson.
	 * The environment of the application server needs to be set to prefer IP version four addresses if the host's network is still using IP version four. If this is not done, this method will not identify the correct broadcast addresses available.
	 */
	private static org.apache.log4j.Logger log = Logger.getLogger(Broadcaster.class);
	public static boolean sendBroadcast()
	{
		boolean result = false;
		try
		{
			log.debug("Prepairing Packet");
			String broadcastString = new String("Result Key for the lesson is 15e83da388267da584954d4fe5a127be3dff117eaee7a97fcda40e61f3c2868b");
			DatagramPacket newPacket = new DatagramPacket(broadcastString.getBytes(), 93);
			
			//Get Broadcast Address for this network
			InetAddress broadcast = null;
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) 
			{
				NetworkInterface networkInterface = interfaces.nextElement();
				if (networkInterface.isLoopback())
					continue; // Don't want to broadcast to the loopback interface
				for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) 
				{
					log.debug("Looking at: " + interfaceAddress.getAddress());
					if (interfaceAddress.getBroadcast() != null)
					{
						broadcast = interfaceAddress.getBroadcast();
						if(!broadcast.equals(InetAddress.getByName("255.255.255.255")))
						{
							log.debug("Found broadcast address: " + broadcast.getCanonicalHostName());
							newPacket.setAddress(broadcast);
							log.debug("Parparing Broadcast");
							DatagramSocket broadcastSocket = new DatagramSocket();
							log.debug("Setting Broadcase to TRUE");
							broadcastSocket.setBroadcast(true);
							log.debug("Sending Packet");
							broadcastSocket.send(newPacket);
							log.debug("Packet Sent, closing socket");
							broadcastSocket.close();
							log.debug("Socket Closed");
							result = true; //Atleast one packet was broadcasted on a broadcast address
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			log.fatal("Brodcaster Error: " + e.toString());
		}
		return result;
	}
}
