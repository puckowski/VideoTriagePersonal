package com.keypointforensics.videotriage.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

public class NetworkUtils {

	public static String getMacAddress() {		
	    try {
	    	InetAddress localHostIp = InetAddress.getLocalHost();
	        NetworkInterface networkInterface = NetworkInterface.getByInetAddress(localHostIp);
	        byte[] macAddress = networkInterface.getHardwareAddress();

	        StringBuilder macAddressBuilder = new StringBuilder();
	        
	        for (int i = 0; i < macAddress.length; i++) {
	            macAddressBuilder.append(String.format("%02X%s", macAddress[i], (i < macAddress.length - 1) ? "-" : ""));        
	        }

	        return macAddressBuilder.toString();
	    } catch (UnknownHostException unknownHostException) {
	    	//unknownHostException.printStackTrace();
	    } catch (SocketException socketException){
	    	//socketException.printStackTrace();
	    }
	    
	    return null;
	}
	
}
