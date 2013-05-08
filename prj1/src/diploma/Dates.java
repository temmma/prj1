package diploma;

import java.util.*;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Dates {
	public static void main(String[] args) throws Exception {
        HttpClient httpclnt = new DefaultHttpClient();
        String[] names ="lytdybr".split(",");
        for (String str : names) { 
        System.out.println("http://"+str+".livejournal.com/profile");
        HttpGet httpget = new HttpGet("http://"+str+".livejournal.com/profile");
        
        HttpResponse httpresp = httpclnt.execute(httpget);       
        HttpEntity entity = httpresp.getEntity();         
        String html_text = new String();
        if (entity != null)
            html_text = EntityUtils.toString(entity, "UTF-8");
        
        Document doc = Jsoup.parse(html_text,"UTF-8");      
        Elements content = doc.getElementsByClass("b-account-level");
         
        if (!content.text().isEmpty()){
        	Map<String,GregorianCalendar>userDates=Chernovik.getDates(content.text());
        	System.out.println(userDates.get("dateCreated").getTime());
        	System.out.println(userDates.get("dateUpdated").getTime());
        }
        httpget.abort();
        }
        
	}

}
