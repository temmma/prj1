package diploma;


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

public class Page2File {

	public static void main(String[] args) throws Exception {
		HttpClient httpclnt = new DefaultHttpClient();
//		HttpGet httpget = new HttpGet("http://hula-hooop.livejournal.com/556.html?format=light");
		HttpGet httpget = new HttpGet("http://tema.livejournal.com/1398212.html?format=light");
        HttpResponse httpresp = httpclnt.execute(httpget);       
        HttpEntity entity = httpresp.getEntity();
        String html_text = new String();
        if (entity != null)
            html_text = EntityUtils.toString(entity, "UTF-8");
        int count = Jsoup.parse(html_text).select("li.b-pager-page").size()/2;
//        Document doc = Jsoup.parse(html_text);
//        Elements links = doc.select("li.b-pager-page");
//        System.out.println(links.size());
//        for (Element aElement:links)
//        	System.out.println(aElement.html());
      System.out.println(count);
        
//        File out_file = new File("F:\\tmp\\list_of_links.html");
//        PrintWriter out = new PrintWriter(out_file);
//        out.print(html_text);        
//        out.close();
	}

}
