package prj1;

import java.io.File;
import java.io.PrintWriter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Page2File {

	public static void main(String[] args) throws Exception {
		HttpClient httpclnt = new DefaultHttpClient();
		HttpGet httpget = new HttpGet("http://tema.livejournal.com/2013/02/?format=light");
        HttpResponse httpresp = httpclnt.execute(httpget);       
        HttpEntity entity = httpresp.getEntity();         
        String html_text = new String();
        if (entity != null)
            html_text = EntityUtils.toString(entity, "UTF-8");
        File out_file = new File("F:\\tmp\\list_of_links.html");
        PrintWriter out = new PrintWriter(out_file);
        out.print(html_text);        
        out.close();
	}

}
