package org.NooLab.utilities.net.pages;


import java.io.*;
import java.net.*;
import java.util.*;

// C:\Programs\dev\eclipse\xlibs\textworx\solrClient\solrj-lib
// http://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/constant-values.html


import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang3.*;

import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.logging.LogControl;
import org.NooLab.utilities.strings.StringsUtil;


/**
 * 
 * provides a non-blocking retrieval of pages from the web;
 * 
 * offers access through standard http client and http-get as well.
 * 
 * for repeated requests, it buffers the retrieved pages for a specifiable period of time,
 * before reloading will be enforced (that age is specified for the host, not for the page) 
 * 
 * http://www.java2s.com/Code/Java/Apache-Common/UsingHttpClientInsideThread.htm
 * http://www.java2s.com/Code/Java/Apache-Common/ExecuteHttpmethodpostget.htm
 *
 */
public class WebRetriever {

	// constants ......................

	String userAgent = "Mozilla/5.0 (Windows NT 6.1; rv:6.0a2) Gecko/20110613 Firefox/6.0a2" ;

	String delayedAccessProps = "~nooretr.prop" ;
	
	// main variables / properties ....

	String htmlStr = "";
	String urlStr = "" ;
	String accessmode = "httpc" ;
	
	/** if the returned html contains this string, the return of the retriever will be an empty string */
	Vector<String> emptyPagePatterns = new Vector<String>();
	
	boolean saveRawPagestoTmp = false ;
	boolean useSystemTmp = true;
	boolean enforcedRemoteRetrieval = false;
	
	String tempPathBase=""; 
	
	/** millis delay between 2 calls to the same host;
	 *  will be varied randomly
	 */
	int politenessDelay = 800 ; 
	double expiryAge = 100.0;
	
	int timeout = 8000;
	
	int printLevel = 2;
	
	// volatile variables .............

	String host ="";
	String page ="";
	int port = 80;
	String tmpfilename="";
	
	// helper objects .................

	DFutils fileutil = new DFutils();
	StringsUtil strgutil = new StringsUtil() ;
	
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
	public WebRetriever(){
		LogControl.Level=2;
	}
	
	public WebRetriever( int printlevel ){
		LogControl.Level=printlevel;
	}
	public WebRetriever( String host, String page){
		_init( host, page, 4 ) ;
	}
	public WebRetriever( String host, String page, int printlevel ){
		_init( host, page,printlevel);
	}
	private void _init(String host, String page, int printlevel ){
		LogControl.Level=printlevel;
		
		this.urlStr = host + page;
		this.host = host;
		this.page = page;
	}
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	
	// setTempPathBase(tablesPath) ;
	/**
	 * 
	 * should NOT be a file, just HTML !
	 * @param urlstr
	 */
	@SuppressWarnings("static-access")
	protected int getPageSafely( String host, String page, int methodID ){
		
		int resultState = -1;
		boolean waiting=true;
		HttpClient client;
		HostConfiguration hostcfg ;
		MethodThread retrieval ;

		String str="",urlstr="", request ="";
		URI uri;
		java.net.URI juri ;
		int z=0;
	 
		// from here onwards we will only meet access of remote sources, 
		// accessing buffer files is upstream !!
		// so we check for the politeness delay (and preventing to get blacklisted...)
		checkAndWaitforPoliteness( host );
		
		if (page.trim().startsWith("/")==false){
			page = "/"+page;
		}
		
		// System.out.println("\naccessing page -> "+ page +"\n");
		urlStr = (host + ":"+port+page).trim();
		urlstr = urlStr ;
		
		if ((urlstr!=null) && (urlstr.length()>4)){
			urlStr = urlstr;
		}
		if (urlStr.toLowerCase().startsWith("http://")==false){urlStr= "http://"+urlStr;}
	    client = new HttpClient();
	    client.getParams().setParameter("http.useragent", userAgent);
	    client.getParams().setParameter("http.protocol.single-cookie-header", true);
	    
	    client.getParams().setCookiePolicy("compatibility");
	    
	    
	    // CookiePolicy.ACCEPT_ALL
	    hostcfg = new HostConfiguration(); 
	    
	    try {
	    	
	    	if (port!=80){
	    		uri = new URI( host+":"+port,true) ; //
	    	}else{
	    		uri = new URI( host,true) ; //
	    	}
	    	
	    	hostcfg.setHost( uri ); //

	    	str = hostcfg.getHost()+":"+port ; // without "http://"   starting with www.
	    	if (page.startsWith("/")==false){
	    		page = "/"+page ;
	    	}
	    	/* activate that with silent exception
	    	juri = new java.net.URI( "http",  str, page, null);
	    	
	    	if (methodID<=1){
	    		request = juri.toASCIIString(); // should : häufig -> h%E4ufig
	    	                                	//    yet          -> h%C3%A4ufig
	    	}else{
	    		request = juri.toString();
	    	}
			*/
	    	request = page;
	    	
	    	if (methodID<=2){
	    		request = request.replaceAll("%25", "%");
	    		request = request.replaceAll("%3F", "?");
	    		request = request.replaceAll("ß", "%DF");
				request = request.replaceAll("ä", "%E4");
				request = request.replaceAll("ö", "%F6");
				request = request.replaceAll("ü", "%FC");
				request = request.replaceAll("Ä", "%C4");
				request = request.replaceAll("Ö", "%D6");
				request = request.replaceAll("Ü", "%DC");

				request = request.replaceAll("%C3%A4", "%E4");
				request = request.replaceAll("ö", "%FC");
				request = request.replaceAll("ü", "%F6");
				request = request.replaceAll("Ä", "%C4");
				request = request.replaceAll("Ö", "%D6");
				request = request.replaceAll("Ü", "%DC"); 
			
	    	}
	    	
	    	str = hostcfg.getHostURL() ; // with ... starting with http://...
	    	request = request.replace(str,"") ;
	    	request = page ;
	    	// uri = new URI( "",  "", page, null);
	    	urlstr = uri.getURI() ;
	    	urlstr = uri.toString() ;
	    		//or String request = uri.toString();
	    	
	    	 
			retrieval = new MethodThread( client, hostcfg, request );
			retrieval.setPrintLevel(printLevel) ;
			
			if (methodID<=1){
				retrieval.setAccessmode("httpc" ) ; 
			}
			if (methodID==2){
				retrieval.setAccessmode("httpc-get" ) ; 
			}
			if (methodID==3){
				retrieval.setAccessmode("socket" ) ;  
			}
			if (methodID==4){
				retrieval.setAccessmode("httpc-put" ) ; 
			}
	     
	    	
			str="";
			
			// we start the retrieval process as a separate thread
			boolean started = retrieval.startRetrieval();
			
			z=0;
			if ((started) && (waiting==false)){
				while (waiting==false) {
					
					waiting = (retrieval.isRunning()==false);
					Thread.currentThread().sleep(2);
				}
			}
			// the server itself should react very fast, for a check, sth <3 sec
			while ( (started) && (waiting || (retrieval.getState()<7)) && (z< timeout/100)){ // 100 = 10 seconds 
				
				waiting = (retrieval.isRunning()==true);
				Thread.currentThread().sleep(100);
				z++ ;
				// System.out.println("waiting period : "+z+"  waiting state : "+waiting+"  r-state = "+ retrieval.getState()) ;
			} // waiting -> 
			
			if ( ((waiting==true) || (retrieval.getState()<7)) && (z>=198)){
				resultState = 3;
			}else{
				if ((started) && (waiting==false)){
					resultState = 0;
				}else{
					resultState = -3;
				}
			}
			// this.lastResponseCode; ...
			
			z = retrieval.lastResponseCode ;
			if ((resultState!=0) || 
				( (retrieval.lastResponseCode!=200) && (retrieval.lastResponseCode!=302)  && (retrieval.lastResponseCode== -999))){
				htmlStr = "HTTP_RESPONSE : " + retrieval.lastResponseCode;
			}else{
				// System.out.println("waiting over, retrieving results... " );
				htmlStr = retrieval.getHtmlStr();
				retrieval.lastResponseCode=200;
			}
			z=z+0;
			
		} catch (URIException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		 
		
		return resultState;
	}
	
	
	protected void getPageSource( String urlstr ){
		// "http://www.google.com"
		
		HttpURLConnection urlConnection ;
		URL pageURL ;
		String sourcetext,key, header, response ;
		int c, count, respCode ;
		InputStream istream;
		Reader reader ;
		 
		
		try {
	          pageURL = new URL( urlstr );
	          urlConnection = (HttpURLConnection) pageURL.openConnection();
	          respCode = urlConnection.getResponseCode();
	          response = urlConnection.getResponseMessage();
	          sourcetext = "HTTP/1.x " + respCode + " " + response +"\n" ;
	          
	          count = 1;
	          
	          while (true) {
	            header = urlConnection.getHeaderField(count);
	            key = urlConnection.getHeaderFieldKey(count);
	            
	            if (header == null || key == null) {
	              break;
	            }
	            sourcetext = sourcetext + urlConnection.getHeaderFieldKey(count) + ": " + header + "\n";
	            count++;
	          }
	          
	          istream = new BufferedInputStream(urlConnection.getInputStream());
	          reader = new InputStreamReader(istream);
	          
	          
	          while ((c = reader.read()) != -1) {
	        	  sourcetext = sourcetext+ String.valueOf((char) c);
	          }
	          
	        } catch (Exception ee) {
	        }
		
	}
	
	
	public String getHtml(int method){
		return getHtml("",method);
	}
	public String getHtml(){
		return getHtml("",1);
	}
	
	public String provideTmpFilename(){
		String str,tmpfilename="";
		int p;
		
		if (useSystemTmp){
			tmpfilename = fileutil.getTempDir("~noo.nlp");
		}else{
			tmpfilename = fileutil.getTempDir( tempPathBase, "~noo.nlp");
		}
		
		str = page.replace("cgi-bin/", "");
		p = str.indexOf("?");
		if (p>0){
			str = str.substring(p+1,str.length()) ;
		}
		
		p = str.lastIndexOf("=");
		if (p>0){
			str = str.substring(p+1,str.length()) ;
		}
		str = str.replace("&", "");
		str = str.replace("/", "");
		str = str.replace("=", "");
		str = str.replace(":", "");
		str = str.replace(";", "");
		str = str.replace("!", "");
		str = str.replace("..", ".");
		
		if (str.indexOf("htm")<0){
			str = str + ".html";
			str = str.replace("..", ".");
		}
		
		if (str.charAt(0)=='.'){
			tmpfilename = "" ;
		}else{
			tmpfilename = fileutil.createPath(tmpfilename, str);
		}
		return tmpfilename;
	}
	
	public String getHtml( String mandatoryContentStr, int methodid){
		int p1,p2,p3,r=-1; 
		String str ;
		boolean fileislocal=false,hb;
		
		try {

			htmlStr = "";
			tmpfilename="";
			
			if (saveRawPagestoTmp){
				tmpfilename = provideTmpFilename();
			}else{
				tmpfilename ="" ;
				
			}

			
			
			if ((fileutil.fileexists(tmpfilename)) && (enforcedRemoteRetrieval==false) ) {
				
				hb = checkForPageExpiry( tmpfilename );
				
				if (hb){
					htmlStr = fileutil.readFile2String(tmpfilename);
					fileislocal = true;
					r = 0;
					if ((htmlStr == null) || (htmlStr.length() < 3)) {
						r = -3;
						fileutil.deleteFile(tmpfilename);
						fileislocal = false;
					}
				} // true == page NOT expired ?
			} 
			if (r<0){
				
				r = getPageSafely(host, page,methodid);
			}

			if (htmlStr == null) {
				htmlStr = "";
			} else {
				// for cgi requests (get/put)) we usually have characters in the page address, which are
				// forbidden for the file system 
				// so we restrict it to standard read
				if ( (saveRawPagestoTmp) && (methodid<=2) && (fileislocal==false) && (htmlStr.length()>10)){
					fileutil.writeFileSimple(tmpfilename, htmlStr); 
				}
			}

			// NO content specific stuff here !! 
			// p1 = htmlStr.indexOf("ctl00_ch_divSimple");
			// if (p1<0){ htmlStr = ""; }
			
			// simple check whether it is indeed valid HTML
			str = htmlStr.toLowerCase();
			p1 = str.indexOf("<html");
			p2 = str.indexOf("<body");
			p3 = -1; 
			p3 = str.indexOf("http_response"); // do this only if requested
			if ((p3<0) &&((p1 < 0) || (p2 < 0))) {
				htmlStr = "";
			}
			if ( r>0 ){
				htmlStr = "[]";
			}
			
			checkForEmptyPagePatterns();
			if (htmlStr.length()==0){
				if (fileutil.fileexists(tmpfilename)){
					fileutil.deleteFile(tmpfilename) ;
				}
			}
			
			
		} catch (IOException e) {

			e.printStackTrace();
		}

		return htmlStr;
	}


	public long getDateofUrlConnection( String urlstr ){
		
		HttpURLConnection httpCon;
		URL url ;
		long date = -1;
		
		try {
			
			url = new URL( urlstr );
			httpCon = (HttpURLConnection) url.openConnection();

			date = httpCon.getDate();

			if (date == 0)
				System.out.println("No date information.");
			else
				System.out.println("Date: " + new Date(date));

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return date;	
	}
	
	
	public String getContentTypeofPage( String urlstr ) {
		String typestr = "";
		URL url;
		HttpURLConnection httpCon;

		
		if ((urlstr == null) || (urlstr.length() < 4)) {
			urlstr = urlStr;
		}
		
		try {
		
			url = new URL(urlstr);
			httpCon = (HttpURLConnection) url.openConnection();

			System.out.println("Content-Type: " + httpCon.getContentType());

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return typestr;
	}

	 
	  public void getContenLengthofPage( String urlstr ) {
		  // like "http://www.google.com";

		try {

			URL url = new URL(urlstr);
			HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();

			int len = httpCon.getContentLength();
			if (len == -1)
				System.out.println("Content length unavailable.");
			else
				System.out.println("Content-Length: " + len);
			
		} catch (Exception e) {

		}

	}

	public String getprimaryResponseMsg(String urlstr) {
		// like: "http://www.google.com"
		URL url;
		String msg = "" ;
		HttpURLConnection httpCon;
		
		try {
			url = new URL(urlstr);
			httpCon = (HttpURLConnection) url.openConnection();

			msg = "Response Message is " + httpCon.getResponseMessage();

			System.out.println(msg);

		} catch (Exception e) {
		}
		
		return msg;
	}


	public String savePagetoFile(){
		return savePagetoFile( urlStr, "") ;
	}

	public String savePagetoFile( String urlstr ){
		return savePagetoFile(urlstr, "") ;
	}

	public String savePagetoFile( String urlstr, String fileName ){
		String filename = "" ;
		String contentStr = "";
		
		contentStr = getHtml() ;
		fileutil.writeFileSimple(filename, contentStr) ;
		
		return filename ;
	}
	
	
	public boolean checkForPageExpiry( String pathToBufferFile ){
		boolean rb=true;
		
		
		
		
		return rb;
	}
	
	public String htmlCorrection( String htmlstr){
		
		
		if (htmlstr==null){
			htmlstr="";
		}
		
		htmlstr = strgutil.replaceAll( htmlstr, "UL>", "ul>");
		htmlstr = strgutil.replaceAll( htmlstr, "LI>", "li>");
		htmlstr = strgutil.replaceAll( htmlstr, "</A>", "</a>");
		htmlstr = strgutil.replaceAll( htmlstr, "TABLE>", "table>");
		htmlstr = strgutil.replaceAll( htmlstr, "<TABLE ", "<table ");
		htmlstr = strgutil.replaceAll( htmlstr, "TD>", "td>");
		htmlstr = strgutil.replaceAll( htmlstr, "TR>", "tr>");
		htmlstr = strgutil.replaceAll( htmlstr, "TT>", "tt>");
		htmlstr = strgutil.replaceAll( htmlstr, "P>", "p>");
		htmlstr = strgutil.replaceAll( htmlstr, "<BR", "<br");
		htmlstr = strgutil.replaceAll( htmlstr, "<br>", "<br/>");
		htmlstr = strgutil.replaceAll( htmlstr, "DIV>", "div>");
		htmlstr = strgutil.replaceAll( htmlstr, "<DIV ", "div ");
		htmlstr = strgutil.replaceAll( htmlstr, "<A HREF=", "<a href=");
		htmlstr = strgutil.replaceAll( htmlstr, "<INPUT ", "<input ");
		
		htmlstr = strgutil.replaceAll( htmlstr, " TYPE=\"", " type=\"");
		htmlstr = strgutil.replaceAll( htmlstr, " NAME=\"", " name=\"");
		htmlstr = strgutil.replaceAll( htmlstr, " VALUE=", " value=");

		
		
		
		htmlstr = strgutil.replaceAll( htmlstr, "U>", "u>");
		htmlstr = strgutil.replaceAll( htmlstr, "I>", "i>");
		htmlstr = strgutil.replaceAll( htmlstr, "P>", "p>");
		htmlstr = strgutil.replaceAll( htmlstr, "<P ", "<p ");
		htmlstr = strgutil.replaceAll( htmlstr, "<P>", "<p>");
		htmlstr = strgutil.replaceAll( htmlstr, "B>", "b>");
		
		htmlstr = strgutil.replaceAll( htmlstr, "STYLE=\"", "style=\"");
		htmlstr = strgutil.replaceAll( htmlstr, "BORDER", "border");
		htmlstr = strgutil.replaceAll( htmlstr, "CELLSPACING", "cellspacing");
		htmlstr = strgutil.replaceAll( htmlstr, "CELLPADDING", "cellpadding");
		htmlstr = strgutil.replaceAll( htmlstr, "WIDTH", "width");
		htmlstr = strgutil.replaceAll( htmlstr, "HEIGHT", "height");
		
		return htmlstr;
	}
	/**
	 * 
	 * called from "getPageSafely()" which is shared by any kind of access
	 * 
	 * @param host
	 */
	@SuppressWarnings("static-access")
	private void checkAndWaitforPoliteness(String host){
	
		String tmpfilename,tmpdir,lockfilename,str;
		int actualdelay,z;
		long lastPoint,timediff=0, stopwatchStart;
		Properties delayProperties = new Properties() ;
		FileInputStream fileIn;
		FileOutputStream propsOut;
		
		if (politenessDelay<20){
			return;
		}
		
		
		stopwatchStart = System.currentTimeMillis();
		
		tmpdir = fileutil.getTempDir("~noo.nlp");
		tmpfilename = fileutil.createPath( tmpdir,delayedAccessProps);
		
		// since there could be several retrieval processes, we have to use a lock-file
		// for controlling the access to the properties file
		
		lockfilename  = fileutil.createPath( tmpdir,"~props.lck");
		
		try{
			// wait at most 10 seconds...
			z=0;
			while (fileutil.fileexists(lockfilename) && (z < 1000000000)) {
				Thread.currentThread().sleep(10);
				z++;
				System.out.print(".");
				if ((z % 100 ==0) || (z<=1)){
					System.out.println();
				}
			}
		}catch(Exception e){
			
		}
		
		fileutil.writeFileSimple(lockfilename, "");
		
		
		
		try {

			if (fileutil.fileexists(tmpfilename) == false) {
				actualdelay = 0;
				
			} else {

				fileIn = new FileInputStream(tmpfilename);

				delayProperties.load(fileIn);

				if (delayProperties.containsKey(host)==false){
					actualdelay = 0;
					 
				}else{
					
					str = delayProperties.getProperty( host, "0") ;
					
					lastPoint = Long.parseLong(str) ;
					timediff = System.currentTimeMillis() - lastPoint;
					if (timediff<99999999){ // max of 27 hours
						actualdelay = politenessDelay - (int)timediff  ;
						if (actualdelay<0){
							actualdelay=0;
						}
					}else{
						actualdelay = politenessDelay + 100 ;
					}
				}
			} // props file exists
			
			
			delayProperties.setProperty(host, ""+System.currentTimeMillis());
			
			propsOut = new FileOutputStream( tmpfilename );
			delayProperties.store( propsOut, null);
			
			// remove any of the entries older than 5 days...
			// should use its own thread for this!!! -> a properties file wrapper ?
			removeOldHostEntries();
			
			if (fileutil.fileexists(lockfilename)==true){
				fileutil.deleteFile(lockfilename) ;
			}
			 
 			
			while ( System.currentTimeMillis() - stopwatchStart < actualdelay){
				Thread.currentThread().sleep( 2 );
			}
			
			 
		} catch (Exception e) {
 
		}finally{
			// be sure to delete the lock-file even in case of a problem
			if (fileutil.fileexists(lockfilename)==true){
				fileutil.deleteFile(lockfilename) ;
			}
		}
		 
	}
	
	private void removeOldHostEntries(){
		
		String tmpfilename ;
		 
		Properties delayProperties = new Properties() ;
		FileInputStream fileIn;
		
		
		if (politenessDelay<20){
			return;
		}
		
		tmpfilename = fileutil.getTempDir("~noo.nlp");
		tmpfilename = fileutil.createPath( tmpfilename,delayedAccessProps);
		
		try {

			if (fileutil.fileexists(tmpfilename) == false) {
				return;
				
			} else {

				fileIn = new FileInputStream(tmpfilename);
				delayProperties.load(fileIn);
			}

			
			
			
		}catch(Exception e){
			
		}
	}

	private void checkForEmptyPagePatterns(){
		int p;
		for (int i=0;i<emptyPagePatterns.size();i++){
		
			p = htmlStr.indexOf( emptyPagePatterns.get(i)) ;
			if (p>=0){
				htmlStr = "" ;
				break;
			}
		}
	}
	
	// ------------------------------------------------------------------------
	
	public void setSaveRawPagestoTmp( boolean flag){
		saveRawPagestoTmp = flag ;
	}
	
	public String getUrlStr() {
		return urlStr;
	}

	public void setUrlStr(String host, int port, String page) {
		this.urlStr = host+":"+port + page;
		this.host = host;
		this.page = page ;
		this.port = port;
	}
	public void setUrlStr(String host, String page) {
		this.urlStr = host+page;
		this.host = host;
		this.page = page ;
		this.port = 80;
	}

	public String getHtmlStr() {
		return htmlStr;
	}
	public Vector<String> getEmptyPagePattern() {
		return emptyPagePatterns;
	}

	public String getEmptyPagePatternAsStr() {
		String str;
		str = strgutil.collapse( emptyPagePatterns,"; ");
		str = strgutil.trimm(str, ";");
		return str ;
	}
	
	public void setEmptyPagePattern(String emptypagepattern) {
		emptyPagePatterns.clear();
		emptyPagePatterns.add( emptypagepattern );
	}
	
	public void addEmptyPagePattern(String emptypagepattern) {
		emptyPagePatterns.add( emptypagepattern);
	}

	public String getTempPathBase() {
		return tempPathBase;
	}

	public void setTempPathBase(String tempPathBase) {
		this.tempPathBase = tempPathBase;
	}

	
	
	public String getTmpfilename() {
		return tmpfilename;
	}

	public boolean isUseSystemTmp() {
		return useSystemTmp;
	}

	public void setUseSystemTmp(boolean useSystemTmp) {
		this.useSystemTmp = useSystemTmp;
	}

	public boolean isEnforcedRemoteRetrieval() {
		return enforcedRemoteRetrieval;
	}

	public void setEnforcedRemoteRetrieval(boolean enforcedRemoteRetrieval) {
		this.enforcedRemoteRetrieval = enforcedRemoteRetrieval;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getPolitenessDelay() {
		return politenessDelay;
	}

	public void setPolitenessDelay(int politenessDelay) {
		this.politenessDelay = politenessDelay;
	}

	public double getExpiryAge() {
		return expiryAge;
	}

	public void setExpiryAge(double expiryAge) {
		this.expiryAge = expiryAge;
	}

	public String getAccessmode() {
		return accessmode;
	}

	public void setAccessmode(String accessmode) {
		this.accessmode = accessmode;
	}
	 
	/**
	 * that's really important, since websites check this... in order to identify a "human user" == standard browser
	 * 
	 */
	public void setUserAgent( String useragentStr){
		userAgent = useragentStr ;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setPrintLevel(int printLevel) {
		this.printLevel = printLevel;
	}
	
	
	
} // class WebRetriever



class MethodThread implements Runnable {

	
	
	// main variables / properties ....

	private boolean isActive = false;
	private String htmlStr = "";
	private int state = -1;

	private String accessmode = "httpc" ;
	private InputStream datastream;
	private boolean immediateWriting = false;
	
	private String proxydomain ;
	private String userID ;
	private String userPassWord ;
	
	int lastResponseCode = -999; 
		
	int printLevel = 2;
	// ..........................................
	
	// volatile variables .............

	private HttpClient client;
	private HostConfiguration hostCfg;

	private GetMethod method;
	private String pageAddr, tempDir;
	
	
	
	// helper objects .................

	Thread reThrd ;

	DFutils fileutil = new DFutils();
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
	public MethodThread( HttpClient client, HostConfiguration hostcfg, String resource ) {
		
		this.client = client;
		this.hostCfg = hostcfg;
		this.method = new GetMethod(resource);
		
		pageAddr = hostCfg.getHostURL() + resource ;
		
		reThrd = new Thread(this) ;
		
		tempDir = System.getProperty("java.io.tmpdir") ;
	}
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	
	public boolean startRetrieval(){
		boolean rb=false;
		
		// this too should run IN the thread...
		// if (pageExists(pageAddr)==true)
		{ 
			reThrd.start() ;
			rb = true;
		}
		
		return rb;
	}
	
	public void run() {
		isActive = true;

		if (state<0){
			if (accessmode.contentEquals( "httpc")){
				executeHttpClient();
			}
			if (accessmode.contentEquals( "httpc-get")){
				executeGet();
			}
			if (accessmode.contentEquals( "httpc-put")){
				// executeGe();
			}
			if (accessmode.contentEquals( "socket")){
				 
				executeSocketAccess( pageAddr );
			}
		}

		isActive = false;
	}

	
	private boolean pageExists( String urlStr){
		
		HttpURLConnection con ;
		boolean rb=false;
		
		try{
			
			// urlStr=urlStr+"";
			HttpURLConnection.setFollowRedirects(false);

			 con = (HttpURLConnection) new URL( urlStr ).openConnection();
			 con.setRequestMethod("HEAD");
			 
			 if (printLevel>=4){
				 System.out.println(con.getResponseCode()+" : "+urlStr );
				 System.out.println(con.getResponseCode() == HttpURLConnection.HTTP_OK);
			 }
			 int rc = con.getResponseCode();
			 
			 rb = (rc >=HttpURLConnection.HTTP_OK) && 
			 	  (con.getResponseCode() <400);
	
			 lastResponseCode = con.getResponseCode() ;
				 
		}catch(SocketException e){
			rb=false; 
			// actually, the address may not existing, but we do not want to see the result as exception...
			if (LogControl.Level >= 4){
				System.out.println("SocketException on checking pageExists() for url : "+urlStr+".");
			};
			// System.out.println("SocketException on checking pageExists(). ");
			
		}catch(Exception e){
			rb=false ;
			e.printStackTrace();
		}		 
		 return rb;
	}
	
	private void executeGet( ){
		executeGet( "", "") ;
	}
	
	/**
	 * username & password could also be set explicitly using setters
	 * 
	 * @param username
	 * @param password
	 */
	private void executeGet( String username, String password){
		// 
		Credentials creds ;
		String url;
        HttpMethod method = null ;
        HttpClient client ;
        String responseBody ;
		
		creds = null;
        if (username.length() >= 3) {
            creds = new UsernamePasswordCredentials( username, password);
        }
        
        
        //create a singular HttpClient object
        client = new HttpClient();

        //establish a connection within 5 seconds
        client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);

        //set the default credentials
        if (creds != null) {
            client.getState().setCredentials(AuthScope.ANY, creds);
        }

        
        
        try{
        	url = pageAddr;
            method = null;

             //create a method object
                 method = new GetMethod(url);
                 
                 method.setFollowRedirects(true);
                 method.getParams().setCookiePolicy("compatibility");
                
         	    
         	    
        } catch (Exception murle) {
             //    System.out.println("<url> argument '" + url
             //            + "' is not a valid URL");
             //    System.exit(-2);
        	murle.printStackTrace() ;
        }
        
        htmlStr = "";

        //execute the method
        responseBody = null;
        try{
            client.executeMethod(method);
            responseBody = method.getResponseBodyAsString();
            
            state = 6;
            htmlStr = responseBody;
            if (htmlStr.length()>0){
            	htmlStr = org.apache.commons.lang3.StringEscapeUtils.unescapeHtml3(htmlStr) ;
            	// htmlStr = org.apache.commons.lang.StringEscapeUtils.unescapeHtml(htmlStr); 
            }
            System.out.println(htmlStr+"\nLength of html="+htmlStr.length());
            
            
            fileutil.writeFileSimple("C:/temp/~webretrieverDownload.html", htmlStr);
            /*
            
            
        //write out the request headers
        System.out.println("Request Path: " + method.getPath());
        System.out.println("Request Query: " + method.getQueryString());
        Header[] requestHeaders = method.getRequestHeaders();
        for (i=0; i<requestHeaders.length; i++){
            System.out.print(requestHeaders[i]);
        }

        //write out the response headers
        System.out.println("Status Line: " + method.getStatusLine());
        Header[] responseHeaders = method.getResponseHeaders();
        for (i=0; i<responseHeaders.length; i++){
            System.out.print(responseHeaders[i]);
        }

            */
        } catch (HttpException he) {
            System.err.println("Http error connecting to '" + pageAddr + "'");
            System.err.println(he.getMessage());
            System.exit(-4);
        } catch (IOException ioe){
            System.err.println("Unable to connect to '" + pageAddr + "'");
            System.exit(-3);
        }
        finally{
        	state = 7;
        	try{
        		method.releaseConnection();
        	}catch(Exception e){
        	}
        }
        
        
        state = 8;
        
        
	} // get method
	
	@SuppressWarnings("static-access")
	private void executeHttpClient() {
		
		String str = "";
		int  n;
		
		// Reader reader;
		BufferedReader brwebsite = null;

		// System.err.println("Connecting to: " + host);
		try {
			
			state = -999;
			
			if (pageExists(pageAddr)==false){
				
				state = 8; 
				return;
			}
			
			state = 1;
			client.executeMethod(hostCfg, method);
			datastream = method.getResponseBodyAsStream();
			state = 2;
			if (datastream != null) {

				 
				if (datastream != null) {

					// !!! The InputStreamReader also has alternative
					// constructors that allows
					// to specify the character set (ISO-Latin1, UTF-8, UTF-16
					// etc.)

					// reader = new InputStreamReader(datastream, "UTF-8");
					brwebsite = new BufferedReader(new InputStreamReader( datastream, "UTF-8"));

					String line = null;
					n = 0;
					while ((line = brwebsite.readLine()) != null) {
						// Process the data, here we just concatenate lines (we expect html)
						str = str + line;

						n++;
						// System.out.println("line " + n);
						state = 5;
					}

					// treating binary data, ie charwise
					/*
					 * int data = reader.read();
					 * 
					 * while (data != -1) { ch = (char) data; data =
					 * reader.read();
					 * 
					 * str = str + Character.toString(ch) ; }
					 * 
					 * reader.close();
					 */
				} // istream available ?
				state = 6;
				htmlStr = str;
			} // stream from http source available ?

			lastResponseCode = 200;
		} catch (Exception e) {
			System.err.println(e);
		} finally {
			method.releaseConnection();
			try {
				if (brwebsite != null) {
					brwebsite.close();
				}
			} catch (IOException e) {
			}
		}

		state = 7;
		try {
			Thread.currentThread().sleep(100);
		} catch (Exception e) {
		}
		state = 8;
		
		try{
			reThrd.interrupt();
		} catch (Exception e) {
		}
	}

	private void executeSocketAccess( String pageaddr){
		// pageAddr = "http://www.google.com/index.htm";
		
		String fileNameIn,fileNameOut, websiteAddress, contentStr="", input ;
		boolean more ;
		URL url;
		Socket clientSocket ; 
		InputStreamReader iread;

		BufferedReader inFromServer ;
		OutputStreamWriter outWriter ;
		OutputStream outStream ;
		BufferedWriter writer; 
		FileWriter fileWriter;
		
		
		
		try {
			url = new URL(pageaddr);

			websiteAddress = url.getHost();

			fileNameIn = url.getFile();
			clientSocket = new Socket(websiteAddress, 80);

			iread = new InputStreamReader(clientSocket.getInputStream());
			inFromServer = new BufferedReader(iread);
			
			outStream = clientSocket.getOutputStream() ;
			outWriter = new OutputStreamWriter(outStream);
			
			outWriter.write("GET " + fileNameIn + " HTTP/1.0\r\n\n");
			outWriter.flush();
			
			fileNameOut = fileutil.createPath( tempDir, fileNameIn) ;
			fileWriter = new FileWriter(fileNameOut) ;
			writer = new BufferedWriter(fileWriter);
			
			more = true;
			
			
			while (more) {
				input = inFromServer.readLine();
				if (input == null)
					more = false;
				else {
					if (immediateWriting==true){ 
						writer.write(input);
					}
					contentStr = contentStr + input;
				}
			}
			
			writer.close();
			clientSocket.close();

			lastResponseCode = 200;
			
		} catch (Exception e) {

			e.printStackTrace();
		}

	}
	
	
	  public void identifyToProxy(){

		String encodedUserPwd, userPwd;

		URL url;
		Properties systemSettings;
		HttpURLConnection con;
		  
		  /*
		  		sun.misc.BASE64Encoder encoder ;
		  		error message :
		  		Access restriction: The type BASE64Encoder is not accessible due to restriction 
		  		on required library C:\Programs\Java\jre6\lib\rt.jar
		  		
		  		so we need a different BASE64Encoder
		  */
 
		try {
			systemSettings = System.getProperties();
			systemSettings.put("proxySet", "true");
			systemSettings.put("http.proxyHost", "proxy.mycompany.local");
			systemSettings.put("http.proxyPort", "80");

			url = new URL("http://www.java.com");
			con = (HttpURLConnection) url.openConnection();

			 
			
			
			userPwd = proxydomain + "\\"+userID+":"+userPassWord ;
			encodedUserPwd = new String(Base64.encodeBase64(userPwd.getBytes()));

			con.setRequestProperty("Proxy-Authorization", "Basic " + encodedUserPwd);
			con.setRequestMethod("HEAD");
			
			System.out.println(con.getResponseCode() + " : " + con.getResponseMessage());
			System.out.println(con.getResponseCode() == HttpURLConnection.HTTP_OK);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(false);
		}
	}

	  public void contentByProxy(String urlstr ){
		  // like:  "http://www.google.com"

		byte[] bytes;
		URL url;
		DataInputStream dinstream;
		HttpURLConnection con;
		String userPwd, encodedUserPwd, contentstr="";
		Properties systemSettings;
		
		try {
			bytes = new byte[1];
			systemSettings = System.getProperties();
			systemSettings.put("http.proxyHost", "proxy.mydomain.local");
			systemSettings.put("http.proxyPort", "80");

			url = new URL(urlstr);

			con = (HttpURLConnection) url.openConnection();

			userPwd = proxydomain + "\\"+userID+":"+userPassWord ;
			encodedUserPwd = new String(Base64.encodeBase64(userPwd.getBytes()));

			con.setRequestProperty("Proxy-Authorization", "Basic " + encodedUserPwd);
			dinstream = new DataInputStream(con.getInputStream());

			while (-1 != dinstream.read(bytes, 0, 1)) {
				// System.out.print(new String(bytes));
				// handling the stream received by the connection
				contentstr = contentstr + (new String(bytes));
				
			} // while -> reading possible
			
			
		} catch (MalformedURLException e) {

			e.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}

	  }
	  
	  
	
	// ------------------------------------------------------------------------
	
	public InputStream getDatastream() {
		return datastream;
	}

	public boolean isRunning() {
		return isActive;
	}

	public String getHtmlStr() {
		return htmlStr;
	}

	public int getState() {
		return state;
	}

	public String getAccessmode() {
		return accessmode;
	}

	public void setAccessmode(String accessmode) {
		this.accessmode = accessmode;
	}


	public boolean isImmediateWriting() {
		return immediateWriting;
	}


	public void setImmediateWriting(boolean immediateWriting) {
		this.immediateWriting = immediateWriting;
	}


	public String getProxydomain() {
		return proxydomain;
	}


	public void setProxydomain(String proxydomain) {
		this.proxydomain = proxydomain;
	}


	public int getLastResponseCode() {
		return lastResponseCode;
	}


	public void setLastResponseCode(int lastResponseCode) {
		this.lastResponseCode = lastResponseCode;
	}


	public String getUserID() {
		return userID;
	}


	public void setUserID(String userID) {
		this.userID = userID;
	}


	public int getPrintLevel() {
		return printLevel;
	}


	public void setPrintLevel(int printLevel) {
		this.printLevel = printLevel;
	}


	public String getUserpassword() {
		return userPassWord;
	}


	public void setUserpassword(String userpassword) {
		userPassWord = userpassword;
	}

}
