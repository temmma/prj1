package prj1;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;

public class test {

	public static void main(String[] args) throws Exception {
		String username = "_argento_";
		HttpHost targetHost = new HttpHost(username+".livejournal.com", 80, "http");
		DefaultHttpClient httpclient = new DefaultHttpClient();
		BasicHttpContext localcontext = new BasicHttpContext();
		HttpGet httpget = new HttpGet("/");
		HttpResponse response = httpclient.execute(targetHost, httpget, localcontext);

		HttpEntity entity = response.getEntity();
        String html_text = new String();
        if (entity != null)
            html_text = EntityUtils.toString(entity, "UTF-8");
        System.out.println(html_text);
	}

}
