package com.domaado.mobileapp;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CrashReportSender implements ReportSender {
    private Uri mFormUri = null;
    private Map<ReportField, String> mMapping = null;
    private static final String CONTENT_TYPE;

    private String reportID;
    
    static {
        CONTENT_TYPE = "application/json";
    }

    /**
     * <p>
     * Create a new HttpPostSender instance.
     * </p>
     *
     * @param formUri The URL of your server-side crash report collection script.
     * @param mapping If null, POST parameters will be named with
     *                {@link org.acra.ReportField} values converted to String with
     *                .toString(). If not null, POST parameters will be named with
     *                the result of mapping.get(ReportField.SOME_FIELD);
     */
    public CrashReportSender(String formUri, Map<ReportField, String> mapping) {
        mFormUri = Uri.parse(formUri);
        mMapping = mapping;
    }

    private static boolean isNull(String aString) {
        return aString == null || aString == org.acra.ACRAConstants.NULL_VALUE;
    }
    
    private ReportField[] getSupportField() {
    	 ReportField[] fields =  {
    			 ReportField.REPORT_ID,
    			 ReportField.APP_VERSION_CODE,
    			 ReportField.APP_VERSION_NAME,
    			 ReportField.PACKAGE_NAME,
    			 ReportField.PHONE_MODEL,
    			 ReportField.BRAND,
    			 ReportField.PRODUCT,
    			 ReportField.ANDROID_VERSION,
    			 ReportField.USER_CRASH_DATE,
    			 ReportField.TOTAL_MEM_SIZE,
    			 ReportField.AVAILABLE_MEM_SIZE,
    			 ReportField.STACK_TRACE
    	 };
    	 
    	 return fields;
    }

    private JSONObject createJSON(Map<ReportField, String> report) {
        JSONObject json = new JSONObject();
        
        ReportField[] fields =  ACRA.getConfig().customReportContent();
        if (fields.length == 0) {
            //fields = org.acra.ACRAConstants.DEFAULT_REPORT_FIELDS; //Total Field
            fields = getSupportField();
        }
        
        for (ReportField field : fields) {
        	
            try {
                if (mMapping == null || mMapping.get(field) == null) {
                    json.put(field.toString(), report.get(field));

                } else {
                    json.put(mMapping.get(field), report.get(field));
                }
                
                if(field.compareTo(ReportField.REPORT_ID) == 0) {
                	reportID =  report.get(field);
                }
                
            } catch (JSONException e) {
                Log.e("JSONException", "There was an error creating JSON", e);
            }
        }
        
        if(json != null ) {
        	
			try {
				String aMemSize = (String) json.get("AVAILABLE_MEM_SIZE");
				String tMEmSize = (String) json.get("TOTAL_MEM_SIZE");
				String stackTrace = (String) json.get("STACK_TRACE");
				String temp = new String();
	        	if(aMemSize != null &&  aMemSize.length() >0) {
	        		temp += "\n\t" + "AVAILABLE_MEM_SIZE :" + aMemSize;
	        	}
	        	
	        	if(tMEmSize != null &&  tMEmSize.length() >0) {
	        		temp += "\n\t" + "TOTAL_MEM_SIZE : " + tMEmSize;
	        	}
	        	
	        	if(temp.length() > 0) {
	        		stackTrace += temp;
	        		json.remove("STACK_TRACE");
	        		json.put("STACK_TRACE", "<pre>"+stackTrace+"</pre>");

	        	}

	        	json.remove("AVAILABLE_MEM_SIZE");
	        	json.remove("TOTAL_MEM_SIZE");
			} catch (JSONException e) {

				e.printStackTrace();
			}

        }

        return json;
    }
 
    private void sendHttpPost(String data, URL url, String login, String password) {
        DefaultHttpClient httpClient = new DefaultHttpClient();

        try {
            HttpPost httPost = new HttpPost(url.toString());

/*           
 			if(!isNull(login) && !isNull(password) ) {
                httpClient.getCredentialsProvider().setCredentials(
    					new AuthScope(url.getHost(), url.getPort(),AuthScope.ANY_SCHEME),
    					new UsernamePasswordCredentials(login, password));

    			// Set timeout
                httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10 * 1000);
            }
  */          
            String stringData = "\"json\":" + data;
            StringEntity se = new StringEntity(stringData, "UTF_8");
            String temp = EntityUtils.toString(se);
            
            Log.e("byteArray", bytArrayToHex(temp.getBytes()));
 
            Log.e("toString", se.toString());
            Log.e("JSON Data", stringData);

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            BasicNameValuePair dic = new BasicNameValuePair("JSON", data);
            nameValuePairs.add(dic);

            httPost.setEntity(se);
            String temp1 =  EntityUtils.toString(httPost.getEntity());
            
            HttpResponse httpResponse = httpClient.execute(httPost);
            
            File file = new File(Environment.getExternalStorageDirectory() + File.separator +
//            		getResources().getString(R.string.cfg_path) + File.separator +
            		reportID + ".txt");
            file.createNewFile();

            if(file.exists())
            {
                 OutputStream fo = new FileOutputStream(file);
                 fo.write(data.getBytes());
                 fo.close();
            }    
            
        } catch (ClientProtocolException e) {
        	//Log.d("sendHttpPost", "ClientProtocolException");
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
        	//Log.d("sendHttpPost", "UnsupportedEncodingException");
            e.printStackTrace();
        } catch (IOException e) {
        	//Log.d("sendHttpPost", "IOException");
            e.printStackTrace();
        } finally {
        	//Log.d("sendHttpPost", "finally");
            httpClient.getConnectionManager().shutdown();
        }
    }
    
    String bytArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder();
        for(final byte b: a)
            sb.append(String.format("%02x ", b&0xff));
        return sb.toString();
    }
    
	@Override
	public void send(org.acra.collector.CrashReportData arg0) throws ReportSenderException {

        try {
            URL reportUrl;
            reportUrl = new URL(mFormUri.toString());
            JSONObject json = createJSON(arg0);
            
            //sendHttpPost(json.toString(), reportUrl, ACRA.getConfig().formUriBasicAuthLogin(), ACRA.getConfig().formUriBasicAuthPassword());
            sendHttp(json,reportUrl, ACRA.getConfig().formUriBasicAuthLogin(), ACRA.getConfig().formUriBasicAuthPassword());
            
        } catch (Exception e) {
            throw new ReportSenderException("Error while sending report to Http Post Form.", e);
        }
	}
	
	private void sendHttp(JSONObject object, URL url, String login, String password) {
		
		HttpURLConnection conn    = null;
		OutputStream os   = null;
		/*
		InputStream           is   = null;
		ByteArrayOutputStream baos = null;
		 //*/
		try {
			
			conn = (HttpURLConnection)url.openConnection();
			conn.setConnectTimeout(10 * 1000);
			conn.setReadTimeout(10 * 1000);

			try {
				conn.setRequestMethod("POST");
			} catch (ProtocolException e) {
				e.printStackTrace();
			}
			conn.setRequestProperty("Cache-Control", "no-cache");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Accept", "application/json");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			 
			try {
				os = conn.getOutputStream();
				String jsonString = object.toString();
				os.write(jsonString.getBytes());
				os.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			int responseCode = conn.getResponseCode();

            File file = new File(Environment.getExternalStorageDirectory() + File.separator +
//            		getResources().getString(R.string.cfg_path) + File.separator +
            		reportID + ".txt");
            file.createNewFile();

            //write the bytes in file
            if(file.exists())
            {
                 OutputStream fo = new FileOutputStream(file);
                 fo.write(object.toString().getBytes());
                 fo.close();
                 System.out.println("file created: "+file);
                 //url = upload.upload(file);
            }    
			
			if(responseCode == HttpURLConnection.HTTP_OK) {
		/*	 
		 		//Log 출력시 사용 
			    is = conn.getInputStream();
			    baos = new ByteArrayOutputStream();
			    byte[] byteBuffer = new byte[1024];
			    byte[] byteData = null;
			    int nLength = 0;
			    while((nLength = is.read(byteBuffer, 0, byteBuffer.length)) != -1) {
			        baos.write(byteBuffer, 0, nLength);
			    }
			    byteData = baos.toByteArray();
			     
			    String response = new String(byteData);
			     
				Log.i("response", "6 : ");
			     
			    Log.i("response", "DATA response = " + response);
		//*/
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try { os.close(); os = null; } catch(Throwable t) {}
			try { conn.disconnect(); conn = null; } catch(Throwable t) {}
		}

	}
}
//ACRA 사용 끝