package diploma;
import java.net.URI;
import java.text.DateFormat;
import java.util.*;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class LJdwn {

	public static void main(String[] args) throws Exception {
		int year = 2013;
		int month = 2;
		System.out.println(year + " "+ month);
		
		HttpClient httpclnt = new DefaultHttpClient();
        URIBuilder builder = new URIBuilder();
        List<String> hrefs = new ArrayList<String>();

        while (year > 2005) {
			
		        builder.setScheme("http").setHost("www.livejournal.com").setPath("/view/")
	            .setParameter("type", "month")
	            .setParameter("user", "art5")
	            .setParameter("y", String.valueOf(year))
	            .setParameter("m", String.valueOf(month))
	            .setParameter("format", "light");
		        URI uri = builder.build();
		        HttpGet httpget = new HttpGet(uri);
		        System.out.println(httpget.getURI());
		        HttpResponse httpresp = httpclnt.execute(httpget);
		        HttpEntity entity = httpresp.getEntity();
		        String html_text = new String();
		        if (entity != null)
		        	html_text = EntityUtils.toString(entity, "UTF-8");
		        httpget.abort();
		        Document doc = Jsoup.parse(html_text);
		        Elements links = doc.select("a[href]");
//		        запилить итератор в обратном порядке???
		        for (Element link : links) {
		        	if ((link.toString().contains("art5")) & (link.toString().contains("html")))
		        		hrefs.add(link.attr("href"));
		        }
		        
		        if (month == 1){
		        	year--;
		        	month=12;
		        }
		        month--;
			
		}
        for (String str : hrefs)
        	System.out.println(str);
	}

}
