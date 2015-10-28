package r01f.httpclient.jsse.handler;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import sun.misc.RegexpPool;
import sun.net.NetworkClient;
import sun.net.www.http.HttpClient;
import sun.security.action.GetPropertyAction;
import sun.security.util.HostnameChecker;
//import sun.net.NetworkClient;

final class HttpsClient extends HttpClient implements
		HandshakeCompletedListener {

	private static final int defaultPort = 443;
	private HostnameVerifier hostNameVerifier;
	private SSLSocketFactory sslSocketFactory;
	private String proxyHostUsed;
	private int proxyPortUsed;
	private SSLSession sslSession;

	static HttpClient getHTTPSClient(SSLSocketFactory sslSocketFactory, URL url, HostnameVerifier hostNameVerifier) throws IOException {
		return getHTTPSClient(sslSocketFactory, url, hostNameVerifier, true);
	}

	static HttpClient getHTTPSClient(SSLSocketFactory sslSocketFactory, URL url, HostnameVerifier hostNameVerifier, boolean useCache) throws IOException {
		return getHTTPSClient(sslSocketFactory, url, hostNameVerifier, (String) null, -1, useCache);
	}

	static HttpClient getHTTPSClient(SSLSocketFactory sslSocketFactory, URL url, HostnameVerifier hostNameVerifier, String proxyHost, int proxyPort) throws IOException {
		return getHTTPSClient(sslSocketFactory, url, hostNameVerifier, proxyHost, proxyPort, true);
	}

	static HttpClient getHTTPSClient(SSLSocketFactory sslSocketFactory, URL url, HostnameVerifier hostNameVerifier, String proxyHost, int proxyPort, boolean useCache) throws IOException {
		HttpsClient httpsClient = null;
		if (useCache) {
			httpsClient = (HttpsClient) HttpClient.kac.get(url, sslSocketFactory);
			if (httpsClient != null) {
				httpsClient.cachedHttpClient = true;
			}
		}
		if (httpsClient == null) {
			httpsClient = new HttpsClient(sslSocketFactory, url, proxyHost, proxyPort);
		} else {
			SecurityManager securitymanager = System.getSecurityManager();
			if (securitymanager != null) {
				securitymanager.checkConnect(url.getHost(), url.getPort());
			}
			httpsClient.url = url;
		}
		httpsClient.setHostNameVerifier(hostNameVerifier);
		return httpsClient;
	}

	public HttpsClient(SSLSocketFactory sslsocketfactory, URL url, String proxyHost, int proxyPort) throws IOException {
		setSSLSocketFactory(sslsocketfactory);
		if (proxyHost != null) {
			setProxy(proxyHost, proxyPort);
		}
		super.proxyDisabled = true;
		try {
			InetAddress inetaddress = InetAddress.getByName(url.getHost());
			super.host = inetaddress.getHostAddress();
		} catch (UnknownHostException unknownhostexception) {
			super.host = url.getHost();
		}
		super.url = url;
		super.port = url.getPort();
		if (super.port == -1) {
			super.port = getDefaultPort();
		}
		openServer();
	}

	static int getDefaultConnectTimeout() {
		return NetworkClient.defaultConnectTimeout;
	}

	@Override
	protected int getDefaultPort() {
		return defaultPort;
	}

	/*
	 * private int getProxyPort() { // }
	 */
	@Override
	public int getProxyPortUsed() {
		return this.proxyPortUsed;
	}
	@Override @SuppressWarnings("resource")
	public void afterConnect() throws IOException, 
									  UnknownHostException {
		if (!isCachedConnection()) {
			SSLSocket sslsocket = null;
			SSLSocketFactory sslsocketfactory = this.sslSocketFactory;
			try {
				if (!(super.serverSocket instanceof SSLSocket)) {
					sslsocket = (SSLSocket) sslsocketfactory.createSocket(super.serverSocket, super.host, super.port, true);
				} else {
					sslsocket = (SSLSocket) super.serverSocket;
				}
			} catch (IOException ioexception) {
				try {
					sslsocket = (SSLSocket) sslsocketfactory.createSocket(super.host, super.port);
				} catch (IOException ioexception1) {
					throw ioexception;
				}
			}
			// SSLSocketFactoryImpl.checkCreate(sslsocket);
			String protocols[] = _getProtocols();
			String ciphers[] = _getCipherSuites();
			if (protocols != null) {
				sslsocket.setEnabledProtocols(protocols);
			}
			if (ciphers != null) {
				sslsocket.setEnabledCipherSuites(ciphers);
			}
			sslsocket.addHandshakeCompletedListener(this);
			sslsocket.startHandshake();
			this.sslSession = sslsocket.getSession();
			super.serverSocket = sslsocket;
			try {
				super.serverOutput = new PrintStream(new BufferedOutputStream(super.serverSocket.getOutputStream()), false, NetworkClient.encoding);
			} catch (UnsupportedEncodingException unsupportedencodingexception) {
				throw new InternalError(NetworkClient.encoding + " encoding not found");
			}
			checkURLSpoofing(this.hostNameVerifier);
		} else {
			sslSession = ((SSLSocket) super.serverSocket).getSession();
		}
	}
	@Override
	protected synchronized void putInKeepAliveCache() {
		HttpClient.kac.put(super.url, this.sslSocketFactory, this);
	}

	private boolean isNonProxyHost() {
		RegexpPool nonProxyHosts;
		nonProxyHosts = _getNonProxyHosts();
		if (nonProxyHosts.match(super.url.getHost().toLowerCase()) != null) {
			return true;
		}
		String s;
		InetAddress inetaddress;
		try {
			inetaddress = InetAddress.getByName(super.url.getHost());

			s = inetaddress.getHostAddress();
			if (nonProxyHosts.match(s) != null) {
				return true;
			}
		} catch (UnknownHostException ex) {
			ex.printStackTrace(System.out);
		}

		/*
		 * break MISSING_BLOCK_LABEL_54; UnknownHostException
		 * unknownhostexception; unknownhostexception;
		 */
		return false;
	}
	@Override
	public boolean needsTunneling() {
		return this.proxyHostUsed != null && !isNonProxyHost();
	}

	String b() {
		return this.sslSession.getCipherSuite();
	}
	@Override
	public String getProxyHostUsed() {
		if (!needsTunneling()) return null;
		return this.proxyHostUsed;
	}
	private static String[] _getCipherSuites() {
		String cipherString = AccessController.doPrivileged(new GetPropertyAction("https.cipherSuites"));
		String ciphers[];
		if (cipherString == null || "".equals(cipherString)) {
			ciphers = null;
		} else {
			Vector<String> vector = new Vector<String>();
			for (StringTokenizer stringtokenizer = new StringTokenizer(cipherString, ","); stringtokenizer.hasMoreElements(); vector.addElement((String) stringtokenizer.nextElement())) {
				/* nothing */
			}
			ciphers = new String[vector.size()];
			for (int i = 0; i < ciphers.length; i++) {
				ciphers[i] = vector.elementAt(i);
			}

		}
		return ciphers;
	}
	private static String[] _getProtocols() {
		String protocolString = AccessController.doPrivileged(new GetPropertyAction("https.protocols"));
		String protocols[];
		if (protocolString == null || "".equals(protocolString)) {
			protocols = null;
		} else {
			Vector<String> vector = new Vector<String>();
			for (StringTokenizer stringtokenizer = new StringTokenizer(protocolString, ","); stringtokenizer.hasMoreElements(); vector.addElement((String) stringtokenizer.nextElement())) {
				/* empty */
			}
			protocols = new String[vector.size()];
			for (int i = 0; i < protocols.length; i++) {
				protocols[i] = vector.elementAt(i);
			}
		}
		return protocols;
	}

	void setProxy(String proxyHost, int proxyPort) {
		this.proxyHostUsed = proxyHost;
		this.proxyPortUsed = proxyPort >= 0 ? proxyPort : getDefaultPort();
	}

	Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException {
		return this.sslSession.getPeerCertificates();
	}

	public Certificate[] getLocalCertificates() {
		return this.sslSession.getLocalCertificates();
	}
	@Override
	public void handshakeCompleted(HandshakeCompletedEvent handshakecompletedevent) {
		this.sslSession = handshakecompletedevent.getSession();
	}

	void setHostNameVerifier(HostnameVerifier hostNameVerifier) {
		this.hostNameVerifier = hostNameVerifier;
	}

	private void checkURLSpoofing(HostnameVerifier hostnameVerifier) throws IOException {
		String theHost = super.url.getHost();
		if (theHost != null && theHost.startsWith("[") && theHost.endsWith("]")) {
			theHost = theHost.substring(1, theHost.length() - 1);
		}
		try {
			Certificate peerCerts[] = this.sslSession.getPeerCertificates();
			X509Certificate peerCert;
			if (peerCerts[0] instanceof X509Certificate) {
				peerCert = (X509Certificate) peerCerts[0];
			} else {
				throw new SSLPeerUnverifiedException("");
			}
			HostnameChecker checker = HostnameChecker.getInstance((byte) 1);
			checker.match(theHost, peerCert);
			return;
		} catch (SSLPeerUnverifiedException sslpeerunverifiedexception) {
			/* ignore */
		} catch (CertificateException certificateexception) {
			/* ignore */
		}
		String cipher = this.sslSession.getCipherSuite();
		if (cipher != null && cipher.indexOf("_anon_") != -1) {
			return;
		}
		if (hostnameVerifier != null && hostnameVerifier.verify(theHost, this.sslSession)) return;

		super.serverSocket.close();
		this.sslSession.invalidate();
		throw new IOException("HTTPS hostname wrong:  should be <" + super.url.getHost() + ">");
	}

	SSLSocketFactory getSSLSocketFactory() {
		return this.sslSocketFactory;
	}

	void setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
		this.sslSocketFactory = sslSocketFactory;
	}

	public javax.security.cert.X509Certificate[] getPeerCertificateChain() throws SSLPeerUnverifiedException {
		return this.sslSession.getPeerCertificateChain();
	}

	private static RegexpPool _getNonProxyHosts() {
		RegexpPool regexppool = new RegexpPool();
		String nonProxyHosts = AccessController.doPrivileged(new GetPropertyAction("http.nonProxyHosts"));
		if (nonProxyHosts != null) {
			StringTokenizer stringtokenizer = new StringTokenizer(nonProxyHosts, "|", false);
			try {
				while (stringtokenizer.hasMoreTokens()) {
					regexppool.add(stringtokenizer.nextToken().toLowerCase(), new Boolean(true));
				}
			} catch (Exception exception) {
				exception.printStackTrace(System.out);
			}
		}
		return regexppool;
	}

	static int getProxyPortUsed(HttpsClient httpsclient) {
		return httpsclient.proxyPortUsed;
	}

	static String getProxyHostUsed(HttpsClient httpsclient) {
		return httpsclient.proxyHostUsed;
	}

	@Override
	protected Socket doConnect(final String aHost,final int aPort) throws IOException, 
																		UnknownHostException {
		Socket socket = this.sslSocketFactory.createSocket(aHost,aPort);
		return socket;
	}

}
