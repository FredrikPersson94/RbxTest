package REST;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.logging.Logger;

import org.glassfish.grizzly.utils.Pair;

import com.google.gson.JsonElement;

import user.UserPlugin;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.StringUtils;

import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;

public class Upload {

	/**
	 * Uploads a JSON-string to HTTPConnection
	 * 
	 * @param json
	 * @param url
	 * @param requestMethod
	 * @param credentials
	 * @return the response from the server as a Pair<Integer, JsonElement>. The
	 *         integer is the response code and the JsonElement is an eventual JSON
	 *         response from the server
	 */
	public static Pair<Integer, JsonElement> uploadToServer(String json, URL url, String requestMethod,
			String credentials, int timeOut) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		Pair<Integer, JsonElement> response = upload(json, conn, requestMethod, credentials, timeOut);
		return response;
	}

	/**
	 * Uploads a JSON-string to HTTPSConnection
	 * 
	 * @param json
	 * @param url
	 * @param requestMethod
	 * @param credentials
	 * @return the response from the server as a Pair<Integer, JsonElement>. The
	 *         integer is the response code and the JsonElement is an eventual JSON
	 *         response from the server
	 */
	public static Pair<Integer, JsonElement> uploadHTTPSToServer(String json, URL url, String requestMethod,
			String credentials, int timeOut) throws IOException {

		SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
		conn.setSSLSocketFactory(sslsocketfactory);
		Pair<Integer, JsonElement> response = upload(json, conn, requestMethod, credentials, timeOut);
		return response;
	}

	/**
	 * Uploads a JSON-string to a given connection
	 * 
	 * @param json
	 * @param conn
	 * @param requestMethod
	 * @param credentials
	 * @return the response from the server as a Pair<Integer, JsonElement>. The
	 *         integer is the response code and the JsonElement is an eventual JSON
	 *         response from the server
	 */
	public static Pair<Integer, JsonElement> upload(String json, HttpURLConnection conn, String requestMethod,
			String credentials, int timeOut) {
		Pair<Integer, JsonElement> p = new Pair<Integer, JsonElement>();
		try {

			conn.setConnectTimeout(timeOut);
			conn.setReadTimeout(timeOut);
			if (StringUtils.isNotEmpty(credentials)) {
				String encoding = DatatypeConverter.printBase64Binary(credentials.getBytes("UTF-8"));
				conn.setRequestProperty("Authorization", "Basic " + encoding);
			}
			conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestMethod(requestMethod);

//			System.out.println("Upload " + json + " to " + conn.getURL().toString());
			if (StringUtils.isNotBlank(json)) {

				OutputStream os = conn.getOutputStream();
				os.write(json.getBytes("UTF-8"));
				os.close();
			}

			// read the response
			int status = conn.getResponseCode();
			p.setFirst(status);

			BufferedReader inp = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String inputLine;
			JsonElement jE = null;
			StringBuffer response = new StringBuffer();
			while ((inputLine = inp.readLine()) != null) {
				response.append(inputLine);
			}
			String jsonAsString = response.toString();
			if (StringUtils.isNotBlank(jsonAsString)) {
				char firstChar = jsonAsString.charAt(0);

				if (firstChar == '{') {
					jE = JSONHelper.parseStringAsJsonObject(jsonAsString);
				} else if (firstChar == '[') {
					jE = JSONHelper.parseStringAsJsonArray(jsonAsString);
				}
			}
			p.setSecond(jE);
			inp.close();
		} catch (ConnectException e) {
			System.err.println(e);

		}catch (SocketTimeoutException e) {
			System.err.println("Timeout");
			System.err.println(e);
		} 
		catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("General error in upload ");
			System.err.println(e);
		}
 		if (conn != null) {
			conn.disconnect();
		}

		try {
			Logger logger = UserPlugin.getLogger();

			String response = "Upload " + json + " to " + conn.getURL().toString() + ".\n" + "Response = " + p.getFirst() + " - "+ p.getSecond();
			if (logger == null) {
				System.out.println(response);
			} else {
				if (p.getFirst() == null || p.getFirst() > 300) {
					logger.warning(response);
					JsonElement second = p.getSecond();
					if(second != null ) {
						if(second.toString().length() > 100) {
							String substring = second.toString().substring(0, 99);
							logger.warning(substring);
						}
						else {
							logger.warning(second.toString());
						}
					}
				} else {
					String urlStr = conn.getURL().toString();
					if(!urlStr.contains("127.0.0.1:9999")) {
						logger.info(response);
					}
					System.out.println(response);

				}
			}
		} catch (Exception e) {
			System.out.println("Could not log ");
			e.printStackTrace();
		}

		return p;
	}

	public static URL getUrlFromString(String urlStr) {
		try {
			return new URL(urlStr);
		} catch (MalformedURLException e) {
			return null;
		}
	}
}
