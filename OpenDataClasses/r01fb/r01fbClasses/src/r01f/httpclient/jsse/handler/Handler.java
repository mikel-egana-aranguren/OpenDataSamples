package r01f.httpclient.jsse.handler;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

// Referenced classes of package sun.net.www.protocol.https:
//            HttpsURLConnectionImpl

public class Handler extends sun.net.www.protocol.http.Handler {

	private static final int defaultPort = 443;
	
	@SuppressWarnings("hiding")
	protected String proxy;
	
	@SuppressWarnings("hiding")
	protected int proxyPort;

	@Override
	protected int getDefaultPort() {
		return defaultPort;
	}

	public Handler() {
		this.proxy = null;
		this.proxyPort = -1;
	}

	public Handler(final String proxy,final int proxyPort) {
		this.proxy = proxy;
		this.proxyPort = proxyPort;
	}
	
	@Override
	protected URLConnection openConnection(URL url) throws IOException {
		return new HttpsURLConnectionImpl(url, this);
	}
}
