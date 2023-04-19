package com.domaado.mobileapp.network;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.domaado.mobileapp.Constant;
import com.domaado.mobileapp.widget.myLog;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * http 통신 class
 * 
 */

@SuppressLint("DefaultLocale")
public final class HttpRequestor 
{
	private String TAG = HttpRequestor.class.getSimpleName();
	
	//--xx 라인 구분자
	public static final String CRLF = "\r\n";
   
	//--xx 연결할 URL	
	private URL m_cURL;
	
	//--xx 파라미터 목록을 저장, 파라미터 이름과 값이 차례대로 저장
	private ArrayList m_arrParameters;
	private static String CHARSET = "UTF-8";
	
	//커텍트 타임
	public static final int CONNECT_TIME = 3 * 1000;
	private static int ADD_TIMEOUT = 0;
	
	/**
	 * HttpRequestor
	 * 
	 * @param cURL
	 * @param add_timeout	3 Seconds + Add Timeout Seconds
	 */
	public HttpRequestor(URL cURL, int add_timeout) {
		this(cURL, 20, add_timeout);
	}
	
	/**
	 * HttpRequestor
	 * 
	 * default ArrayList is 20
	 * default 3 seconds;
	 * 
	 * @param cURL
	 * 
	 */
	public HttpRequestor(URL cURL)
	{
		this(cURL, 20, 0);
	}
        
	/**
	 * --xx HttpRequest를 생성한다.
	 * --xx @param objURL : HTTP 메시지를 전송할 대상 URL
	 * 
	 * @param cURL
	 * @param iInitialCapicity
	 * @param add_timeout 3 Seconds + Add Timeout Seconds
	 */
	public HttpRequestor(URL cURL, int iInitialCapicity, int add_timeout)
	{
		this.m_cURL = cURL;
		this.m_arrParameters = new ArrayList(iInitialCapicity);
		ADD_TIMEOUT = add_timeout * 1000;
	}


	/**
	 * 파라미터를 추가한다.
	 * @param parameterName 파라미터 이름
	 * @param parameterValue 파라미터 값
	 * @exception IllegalArgumentException parameterValue가 null일 경우
	 */

	/**
	 *
	 * @param strParameterName
	 * @param strParameterValue
	 */
	public synchronized void addParameter(String strParameterName, String strParameterValue) {
		if (strParameterValue == null) {
			strParameterValue = "";
			// throw new IllegalArgumentException("parameterValue can't be null!");
		}
			
		m_arrParameters.add(strParameterName);
		m_arrParameters.add(strParameterValue);
	}
		
	
	/**
	 * 파일 파라미터를 추가한다.
	 * 만약 parameterValue가 null이면(즉, 전송할 파일을 지정하지 않는다면
	 * 서버에 전송되는 filename 은 "" 이 된다.
	 * 
	 * @param parameterName 파라미터 이름
	 * @param parameterValue 전송할 파일
	 * @exception IllegalArgumentException parameterValue가 null일 경우
	 */
	/**
	 *
	 * @param strParameterName
	 * @param strParameterValue
	 */
	public void addFile(String strParameterName, File strParameterValue) {
		// paramterValue가 null일 경우 NullFile을 삽입한다.
		if (strParameterValue == null) {
			m_arrParameters.add(strParameterName);
			m_arrParameters.add(new NullFile());
		} else {
			m_arrParameters.add(strParameterName);
			m_arrParameters.add(strParameterValue);
		}
	}    
	
	
	@SuppressWarnings("deprecation")
	private static String encodeString(ArrayList arrParameters) {
		StringBuffer cBuffer = new StringBuffer(256);
        
		Object[] cObject = new Object[arrParameters.size()];
		arrParameters.toArray(cObject);
		
		for (int i = 0 ; i < cObject.length ; i += 2) {
			if ( cObject[i+1] instanceof File || cObject[i+1] instanceof NullFile ) {
				continue;            
			}
			try {
				cBuffer.append(URLEncoder.encode((String)cObject[i], CHARSET));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			cBuffer.append('=');
			try {
				cBuffer.append(URLEncoder.encode((String)cObject[i+1], CHARSET) );
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}            
			if (i + 2 < cObject.length) {
				cBuffer.append('&');
			}
		}		
		return cBuffer.toString();
	}
	

	/**
     * GET 방식으로 대상 URL에 파라미터를 전송한 후 응답을 InputStream으로 리턴한다.
     * 
     * @return InputStream
     */
	public InputStream _sendGet() throws IOException {
		String strParam = null;
		
		if (m_arrParameters.size() > 0) {
			strParam = "?" + encodeString(m_arrParameters);
		} else {
			strParam = "";
		}
		
		URL cURL = new URL(m_cURL.toExternalForm() + strParam);
		
		myLog.d("HttpRequestor", "URL == "+ m_cURL.toExternalForm() + strParam);
		System.setProperty("http.keepAlive", "false");
		
		HttpsURLConnection cHttpsURLCon = null;
		HttpURLConnection cHttpURLConnect = null;
		InputStream cIntput = null;
		int resCode = 0;
		
		try {
	    	if(m_cURL.getProtocol().toLowerCase().equals("https")) {
	    		trustAllHosts();        	       	
	    		cHttpsURLCon = (HttpsURLConnection) cURL.openConnection();
	    		cHttpsURLCon.setHostnameVerifier(hostnameVerifier);
	    		cHttpURLConnect = cHttpsURLCon;
	    	} else {
	    		cHttpURLConnect = (HttpURLConnection) cURL.openConnection();
	    	}
	    	
	    	cHttpURLConnect.setConnectTimeout(CONNECT_TIME + ADD_TIMEOUT);
	    	cHttpURLConnect.setReadTimeout(CONNECT_TIME + ADD_TIMEOUT);
	      	
	    	cHttpURLConnect.setRequestMethod("GET");
	    	cHttpURLConnect.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	    	cHttpURLConnect.setDoInput(true);
	    	cHttpURLConnect.setDoOutput(true);
	    	cHttpURLConnect.setUseCaches(false);
			cIntput = cHttpURLConnect.getInputStream(); 
			
			resCode = cHttpURLConnect.getResponseCode();
			
			myLog.d("HttpRequestor", "resCode == "+ resCode);

		} catch (UnknownHostException e){
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 여기서 닫으면 안되죠~!
			//try { cIntput.close(); cIntput = null; } catch(Throwable t) {}
			//try { cHttpURLConnect.disconnect(); cHttpURLConnect = null; } catch(Throwable t) {}
			//try { if(cHttpsURLCon != null) { cHttpsURLCon.disconnect(); cHttpsURLCon = null; } } catch(Throwable t) {}
		}
		
        return (resCode == HttpURLConnection.HTTP_OK ? cIntput : null);
	}

	public synchronized InputStream sendGet(String contentType, String body) throws IOException, TimeoutException {
		String strParam = null;
		if (m_arrParameters.size() > 0) {
			strParam = encodeString(m_arrParameters);
		} else {
			strParam = "";
		}
		System.setProperty("http.keepAlive", "false");

		HttpURLConnection cHttpURLConnect = null;
		if(m_cURL.getProtocol().toLowerCase().equals("https")) {
			trustAllHosts();
			HttpsURLConnection cHttpsURLCon = (HttpsURLConnection) m_cURL.openConnection();
			cHttpsURLCon.setHostnameVerifier(hostnameVerifier);
			cHttpURLConnect = cHttpsURLCon;

//    		cHttpsURLCon.disconnect();
		} else {
			cHttpURLConnect = (HttpURLConnection) m_cURL.openConnection();
		}

		cHttpURLConnect.setConnectTimeout(CONNECT_TIME + ADD_TIMEOUT);
		cHttpURLConnect.setReadTimeout(CONNECT_TIME + ADD_TIMEOUT);

		cHttpURLConnect.setRequestMethod("GET");
		if(!TextUtils.isEmpty(contentType))
			cHttpURLConnect.setRequestProperty("Content-Type", contentType);
		else
			cHttpURLConnect.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		cHttpURLConnect.setRequestProperty("Accept-Encoding", Constant.ENCODING);
		cHttpURLConnect.setDoInput(true);
		cHttpURLConnect.setDoOutput(true);
		cHttpURLConnect.setUseCaches(false);
		cHttpURLConnect.setAllowUserInteraction(true);

		InputStream cInput = null;
		DataOutputStream cOutput = null;
		try {

			cOutput = new DataOutputStream(cHttpURLConnect.getOutputStream());
			cOutput.writeBytes(strParam);
			//cOutput.flush();
			if(!TextUtils.isEmpty(body)) {
				cOutput.writeBytes("\r\n\r\n");
				cOutput.writeBytes(body);
				cOutput.flush();
			}

		} finally {
			if (cOutput != null) {
				try {
					cOutput.close();
				} catch (IOException ignored) {}
			}
		}

		int resCode = cHttpURLConnect.getResponseCode();

		if (resCode == HttpURLConnection.HTTP_OK) {
			cInput = cHttpURLConnect.getInputStream();
			return cInput;
		} else {
			cInput = cHttpURLConnect.getErrorStream();
			return cInput;
		}

	}

	//--xx POST 방식으로 대상 URL에 파라미터를 전송한 후 응답을 InputStream으로 리턴한다.
	public synchronized InputStream sendPost(String contentType, String body) throws IOException, TimeoutException {
		String strParam = null;
		if (m_arrParameters.size() > 0) {
			strParam = encodeString(m_arrParameters);
		} else {
			strParam = "";
		}
		System.setProperty("http.keepAlive", "false");
		
    	HttpURLConnection cHttpURLConnect = null;
      	if(m_cURL.getProtocol().toLowerCase().equals("https")) {
    		trustAllHosts();        	       	
    		HttpsURLConnection cHttpsURLCon = (HttpsURLConnection) m_cURL.openConnection();
    		cHttpsURLCon.setHostnameVerifier(hostnameVerifier);
    		cHttpURLConnect = cHttpsURLCon;
    		
//    		cHttpsURLCon.disconnect();
    	} else {
    		cHttpURLConnect = (HttpURLConnection) m_cURL.openConnection();
    	}

      	cHttpURLConnect.setConnectTimeout(CONNECT_TIME + ADD_TIMEOUT);
      	cHttpURLConnect.setReadTimeout(CONNECT_TIME + ADD_TIMEOUT);
      	
      	cHttpURLConnect.setRequestMethod("POST");
		if(!TextUtils.isEmpty(contentType))
      		cHttpURLConnect.setRequestProperty("Content-Type", contentType);
		else
			cHttpURLConnect.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		cHttpURLConnect.setRequestProperty("Accept-Encoding", Constant.ENCODING);
      	cHttpURLConnect.setDoInput(true);
      	cHttpURLConnect.setDoOutput(true);
      	cHttpURLConnect.setUseCaches(false);
      	cHttpURLConnect.setAllowUserInteraction(true);

		InputStream cInput = null;
		DataOutputStream cOutput = null;
		try {

			cOutput = new DataOutputStream(cHttpURLConnect.getOutputStream());
			cOutput.writeBytes(strParam);
			//cOutput.flush();
			if(!TextUtils.isEmpty(body)) {
				cOutput.writeBytes("\r\n\r\n");
				cOutput.writeBytes(body);
				cOutput.flush();
			}

		} finally {
			if (cOutput != null) {
				try {
					cOutput.close();
				} catch (IOException ignored) {}
			}
		}

		int resCode = cHttpURLConnect.getResponseCode();
        
        if (resCode == HttpURLConnection.HTTP_OK) {
			cInput = cHttpURLConnect.getInputStream();
			return cInput;
		} else {
        	cInput = cHttpURLConnect.getErrorStream();
        	return cInput;
		}

		
	}

	public synchronized int getPostResult() throws IOException {
		String strParam = null;
		if (m_arrParameters.size() > 0) {
			strParam = encodeString(m_arrParameters);
		} else {
			strParam = "";
		}
		System.setProperty("http.keepAlive", "false");
		
		HttpURLConnection cHttpURLConnect = null;
		if(m_cURL.getProtocol().toLowerCase().equals("https")) {
			trustAllHosts();        	       	
			HttpsURLConnection cHttpsURLCon = (HttpsURLConnection) m_cURL.openConnection();
			cHttpsURLCon.setHostnameVerifier(hostnameVerifier);
			cHttpURLConnect = cHttpsURLCon;
			
//    		cHttpsURLCon.disconnect();
		} else {
			cHttpURLConnect = (HttpURLConnection) m_cURL.openConnection();
		}
		
		cHttpURLConnect.setConnectTimeout(CONNECT_TIME + ADD_TIMEOUT);
		cHttpURLConnect.setReadTimeout(CONNECT_TIME + ADD_TIMEOUT);
		
		cHttpURLConnect.setRequestMethod("POST");
		cHttpURLConnect.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		cHttpURLConnect.setDoInput(true);
		cHttpURLConnect.setDoOutput(true);
		cHttpURLConnect.setUseCaches(false);
		cHttpURLConnect.setAllowUserInteraction(true);
		
		
		DataOutputStream cOutput = null;
		try  {
			cOutput = new DataOutputStream(cHttpURLConnect.getOutputStream());
			cOutput.writeBytes(strParam);
			cOutput.flush();
		} finally {
			if (cOutput != null)  {
				cOutput.close();
				cOutput = null;
			}
		}
		
		InputStream cInput = cHttpURLConnect.getInputStream();
		
//		cHttpURLConnect.disconnect();
//		cHttpURLConnect = null;
		
		int resCode = cHttpURLConnect.getResponseCode();

		return resCode;
		
	}
	
	public InputStream sendMultipartPost() throws IOException {

		// Delimeter 생성
		String delimeter = makeDelimeter();
		byte[] yNewLine = CRLF.getBytes();
		byte[] yDelimeter = delimeter.getBytes();
		byte[] yDisposition = "Content-Disposition: form-data; name=".getBytes();
		byte[] yQuotation = "\"".getBytes();
		byte[] yContentType = "Content-Type: application/octet-stream".getBytes();
		byte[] yFileName = "; filename=".getBytes();
		byte[] yTwoDash = "--".getBytes();
		
		System.setProperty("http.keepAlive", "false");
		
		HttpURLConnection cHttpURLCon = null;
    	if(m_cURL.getProtocol().toLowerCase().equals("https")) {
    		trustAllHosts();        	       	
    		HttpsURLConnection cHttpsURLCon = (HttpsURLConnection) m_cURL.openConnection();
    		cHttpsURLCon.setHostnameVerifier(hostnameVerifier);
    		cHttpURLCon = cHttpsURLCon;
    	} else {
    		cHttpURLCon = (HttpURLConnection) m_cURL.openConnection();
    	}    	
		
    	cHttpURLCon.setConnectTimeout(CONNECT_TIME + ADD_TIMEOUT);
      	cHttpURLCon.setReadTimeout(CONNECT_TIME + ADD_TIMEOUT);
    	
    	cHttpURLCon.setRequestMethod("POST");
    	cHttpURLCon.setRequestProperty("Content-Type", "multipart/form-data; boundary="+delimeter);
    	cHttpURLCon.setDoInput(true);
    	cHttpURLCon.setDoOutput(true);
    	cHttpURLCon.setUseCaches(false);
		
		BufferedOutputStream cOutput = null;
		try  {
			cOutput = new BufferedOutputStream(cHttpURLCon.getOutputStream());
			Object[] cObj = new Object[m_arrParameters.size()];
			m_arrParameters.toArray(cObj);
			for (int i = 0 ; i < cObj.length ; i += 2)  {
				// Delimeter 전송
				cOutput.write(yTwoDash);
				cOutput.write(yDelimeter);
				cOutput.write(yNewLine);
				// 파라미터 이름 출력
				cOutput.write(yDisposition);
				cOutput.write(yQuotation);
				cOutput.write( ((String)cObj[i]).getBytes() );
				cOutput.write(yQuotation);
				if ( cObj[i+1] instanceof String)  {
					// String 이라면
					cOutput.write(yNewLine);
					cOutput.write(yNewLine);
					// 값 출력
					cOutput.write( ((String)cObj[i+1]).getBytes() );
					cOutput.write(yNewLine);
				} else  {
					// 파라미터의 값이 File 이나 NullFile인 경우
					if ( cObj[i+1] instanceof File)  {
						File file = (File)cObj[i+1];
						// File이 존재하는 지 검사한다.
						cOutput.write(yFileName);
						cOutput.write(yQuotation);
						cOutput.write(file.getAbsolutePath().getBytes() );
						cOutput.write(yQuotation);
					} else  {
						// NullFile 인 경우
						cOutput.write(yFileName);
						cOutput.write(yQuotation);
						cOutput.write(yQuotation);
					}
					cOutput.write(yNewLine);
					cOutput.write(yContentType);
					cOutput.write(yNewLine);
					cOutput.write(yNewLine);
					// File 데이터를 전송한다.
					if (cObj[i+1] instanceof File)  {
						File file = (File)cObj[i+1];
						// file에 있는 내용을 전송한다.
						BufferedInputStream is = null;
						try  {
							is = new BufferedInputStream(new FileInputStream(file));
							byte[] fileBuffer = new byte[1024 * 8]; // 8k
							int len = -1;
							while ( (len = is.read(fileBuffer)) != -1)  {
								cOutput.write(fileBuffer, 0, len);
							}
						} finally {
							if (is != null) try { is.close(); } catch(IOException ex) {}
						}
					}
					cOutput.write(yNewLine);
				} 
				// 파일 데이터의 전송 블럭 끝
				if ( i + 2 == cObj.length ) {
					// 마지막 Delimeter 전송
					cOutput.write(yTwoDash);
					cOutput.write(yDelimeter);
					cOutput.write(yTwoDash);
					cOutput.write(yNewLine);
				}
			} // for 루프의 끝
			cOutput.flush();
		} finally {
			if (cOutput != null) cOutput.close();
		}
		return cHttpURLCon.getInputStream();
	}

	private static String makeDelimeter() {
		return "---------------------------7d115d2a20060c";
	}

	private class NullFile {
		NullFile() {
		}
		
		public String toString() {
			return "";
		}
	}

	private void trustAllHosts() {

		TrustManager[] trustAllHosts = new TrustManager[]{
				new X509TrustManager() {
					public X509Certificate[] getAcceptedIssuers() {

						X509Certificate[] myTrustedAnchors = new X509Certificate[0];

						return myTrustedAnchors;
					}

					@Override
					public void checkClientTrusted(X509Certificate[] certs, String authType) {
					}

					@Override
					public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
						if(chain == null || chain.length == 0)throw new IllegalArgumentException("Certificate is null or empty");
						if(authType == null || authType.length() == 0) throw new IllegalArgumentException("Authtype is null or empty");
						if(!authType.equalsIgnoreCase("ECDHE_RSA") &&
								!authType.equalsIgnoreCase("ECDHE_ECDSA") &&
								!authType.equalsIgnoreCase("RSA") &&
								!authType.equalsIgnoreCase("ECDSA")) throw new CertificateException("Certificate is not trust");
						try {
							chain[0].checkValidity();
						} catch (Exception e) {
							throw new CertificateException("Certificate is not valid or trusted");
						}
					}
				}
		};

		// Create an SSLContext that uses our TrustManager 
		try {
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, trustAllHosts, null);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}

	}

	HostnameVerifier hostnameVerifier = new HostnameVerifier() {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();

			String url = m_cURL.getHost();
			myLog.d(TAG, "*** HostnameVerifier url:"+url+", hostname:"+hostname);

			if(!TextUtils.isEmpty(url) && url.equalsIgnoreCase(hostname)) return true;
			else return false;

		}
	};
}
