package prj1;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ContentLight {

	public static void main(String[] args) throws Exception {
        HttpClient httpclnt = new DefaultHttpClient();
        HttpGet httpget = new HttpGet("http://hula-hooop.livejournal.com/556.html?format=light");
        
        HttpResponse httpresp = httpclnt.execute(httpget);       
        HttpEntity entity = httpresp.getEntity();         
        String html_text = new String();
        if (entity != null)
            html_text = EntityUtils.toString(entity, "UTF-8");
        
        Document doc = Jsoup.parse(html_text,"UTF-8");
        Elements content = doc.getElementsByClass("b-singlepost-title");
        System.out.println(content.text());
        content = doc.getElementsByClass("b-singlepost-body");
        System.out.println(content.text());
        content = doc.select("a[href]");
        for (Element t : content)
        	System.out.println("---------------"+"\n"+t.attributes());        
        content = doc.getElementsByAttributeValue("href", "http://hula-hooop.livejournal.com/");
        System.out.println(content.isEmpty());
        for (Element t : content)
        	System.out.println(t.text());
	}

}
