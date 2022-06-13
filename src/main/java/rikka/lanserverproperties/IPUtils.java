package rikka.lanserverproperties;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class IPUtils {
	public static String getIPs() {
		String ipv4 = "IPv4:";
		String ipv6 = "IPv6:";

		try {
			Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
			while(e.hasMoreElements())
			{
			    NetworkInterface n = (NetworkInterface) e.nextElement();
			    Enumeration<InetAddress> ee = n.getInetAddresses();
			    while (ee.hasMoreElements())
			    {
			        InetAddress ip = (InetAddress) ee.nextElement();
			        if (ip instanceof Inet4Address) {
			        	if (!ip.isLoopbackAddress()) {
				        	ipv4 += "\n";
					        ipv4 += ip.getHostAddress();
			        	}
			        } else if (ip instanceof Inet6Address) {
			        	if (!ip.isLoopbackAddress()) {
			        		ipv6 += "\n";
			        		ipv6 += ip.getHostAddress();
			        	}
			        }
			    }
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}

		return ipv4 + "\n" + ipv6;
	}
}
