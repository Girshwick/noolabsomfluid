package org.NooLab.utilities.net.connex;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.Iterator;
import java.util.List;

public class DetectProxySettings {

	public DetectProxySettings() {

	}

	@SuppressWarnings("rawtypes")
	public void detect() {
		String dummyurlstr = "http://www.yahoo.com/" ;
		java.net.URI uri;
		List list ;
		
		try{
			
			uri = new URI(dummyurlstr);
			System.setProperty("java.net.useSystemProxies", "true");
			list = ProxySelector.getDefault().select( uri );

			for (Iterator iter = list.iterator(); iter.hasNext();) {
				
				Proxy proxy = (Proxy) iter.next();
				System.out.println("proxy hostname : " + proxy.type());
				
				InetSocketAddress addr = (InetSocketAddress) proxy.address();
				
				if (addr == null) {
					System.out.println("No Proxy");
				} else {
					System.out.println("proxy hostname : " + addr.getHostName());
					System.out.println("proxy port : " + addr.getPort());
				}
			}
			
		}catch(Exception e){
			
		}
		
	} // detect()
}
