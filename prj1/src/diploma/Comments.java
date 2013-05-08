package diploma;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

class Comment{
	String journal_url;
	long thread;
	long parent;
	String ctime;
	String article;
	int level;
	boolean collapsed;
	
	public Comment(String journal_url, long thread, long parent, String ctime, String article, int level){
		this.journal_url = journal_url;
		this.thread = thread;
		this.parent = parent;
		this.ctime = ctime;
		this.article = article;
		this.level = level;
		this.collapsed = false;
	}
	
	public Comment(String threadURL){
		this.journal_url = threadURL;
		this.collapsed = true;
	}
	
	public String toString(){
		String blink = "";
		if (collapsed) return journal_url;
		for (int i=0; i*2<level; i++)
			blink += " ";
//		return blink+journal_url+"\n"+blink+article;
		return blink+article;
	}
}


public class Comments {
	
    ArrayList<String> keys;
    int number = 0;
    ArrayList<Comment> commentList;
    ArrayList<String> linkList;
    
    public Comments() throws Exception{
               keys = new ArrayList<String>(Arrays.asList("journal_url,thread,parent,ctime,article,level,leafclass".split(",")));
        commentList = new ArrayList<Comment>();
           linkList = new ArrayList<String>();
    }
   
    JSONArray retrieveJson(String url) throws Exception{
        HttpClient httpclnt = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(url);
        
        HttpResponse httpresp = httpclnt.execute(httpget);
        HttpEntity entity = httpresp.getEntity();
        String html_text = new String();
        if (entity != null)
            html_text = EntityUtils.toString(entity, "UTF-8");
        Document doc = Jsoup.parse(html_text,"UTF-8");       
        Elements temp = doc.getElementsByAttributeValue("id", "comments_json");
//        System.out.println(html_text);
//        parseJson(new JSONArray(temp.html()));
        httpget.abort();
        return new JSONArray(temp.html());
    }
//    /\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\ NEW BLOCK
    void retrieveComments2(JSONArray initial) throws Exception{
    	String href = "";
    	ArrayList<String> localKeys = null;
    	System.out.println(initial.length());
    	for (int i = 0; i < initial.length(); i++) {
        		JSONObject currentComment = initial.getJSONObject(i);
        		localKeys = new ArrayList<String>(Arrays.asList(JSONObject.getNames(currentComment)));
        		if (!localKeys.contains("actions"))
        			continue;
        		if (currentComment.getInt("level")!=1)
        			continue;
        		href = simpleCheck(currentComment.get("actions"));
        		if (!href.equals("")){
        			getComments(retrieveJson(href));
                	  JSON2File(retrieveJson(href));
        		}
		}
    }
    
    String simpleCheck(Object toCheck){
    	if (toCheck.getClass().equals(JSONArray.class)){
    		JSONArray ttt = (JSONArray) toCheck;
    		for (int i=0; i<ttt.length(); i++){
    			JSONObject tmp = ttt.getJSONObject(i);
    			if (JSONObject.valueToString(tmp.get("name")).equals("\"expand\""))
    				return tmp.getString("href");
    		}
    	}
    	return "";
    }
    
    void getComments(JSONArray initial){
    	String journal_url;
    	long thread;
    	long parent;
    	String ctime;
    	String article;
    	int level;
    	for (int i = 0; i < initial.length(); i++) {
			JSONObject currentComment = initial.getJSONObject(i);
			if (JSONObject.valueToString(currentComment.get("username")).equals("null"))
				journal_url = "Anonimous";
			else {
    			thread 	=   Long.parseLong(JSONObject.valueToString(currentComment.get("thread")));
    			parent 	=   Long.parseLong(JSONObject.valueToString(currentComment.get("parent")));
    			ctime 	=                  JSONObject.valueToString(currentComment.get("ctime"));
    			article = 				   JSONObject.valueToString(currentComment.get("article"));
    			level   = Integer.parseInt(JSONObject.valueToString(currentComment.get("level")));
    			JSONObject username = currentComment.getJSONArray("username").getJSONObject(0);
    			journal_url = JSONObject.valueToString(username.get("journal_url"));
    			commentList.add(new Comment(journal_url, thread, parent, ctime, article, level));
			}
		}
    }
    
//    \/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/ NEW BLOCK
    
    void retrieveComments(boolean skipFirst, JSONArray initial) throws Exception{
    	String journal_url;
    	long thread;
    	long parent;
    	String ctime;
    	String article;
    	int level;
    	int i = 0;
    	boolean linked = false;
    	
    	while(i < initial.length()){
    		JSONObject first_level = initial.getJSONObject(i);
    		ArrayList<String> localKeys = new ArrayList<String>(Arrays.asList(JSONObject.getNames(first_level)));
    		if (!localKeys.contains("moreusers") & !localKeys.contains("more")){//!!!!!!!!!!ddddddddddAAAAAAAAAAAAAAAAA!!@!!
    			if (JSONObject.valueToString(first_level.get("leafclass")).equals("null")){
    				if (JSONObject.valueToString(first_level.get("username")).equals("null")) continue;
        			thread 	= Long.parseLong(JSONObject.valueToString(first_level.get("thread")));
        			parent 	= Long.parseLong(JSONObject.valueToString(first_level.get("parent")));
        			ctime 	= JSONObject.valueToString(first_level.get("ctime"));
        			article = JSONObject.valueToString(first_level.get("article"));
        			level   = Integer.parseInt(JSONObject.valueToString(first_level.get("level")));
        			JSONObject username = first_level.getJSONArray("username").getJSONObject(0);
        			journal_url = JSONObject.valueToString(username.get("journal_url"));
        			commentList.add(new Comment(journal_url, thread, parent, ctime, article, level));
        			linked = false;
        			i++;
    			} else if (linked){
    				// проматываем лишние, потому что ссылку уже установили
    					i++;
    			} else if (!linked && !skipFirst){
    				if (JSONObject.valueToString(first_level.get("actions")).equals("null")) continue;
    				// создать объект со ссылкой и пропустить пустые объекты
    				JSONArray actions = first_level.getJSONArray("actions");
    				for (int j=0; j<actions.length(); j++){
    					JSONObject tmp = actions.getJSONObject(j);
    					if (JSONObject.valueToString(tmp.get("name")).equals("\"expand\""))
//    							System.out.println((tmp.getString("href")));
    						retrieveComments(true, retrieveJson(tmp.getString("href")));
//    						JSON2File(retrieveJson(tmp.getString("href")));
//    						commentList.add(new Comment(tmp.getString("href")));
//    							retrieveJson(tmp.getString("href"));
    				}
    				linked = true;
    				i++;
    			} else if (!linked && skipFirst){
    				skipFirst = false;
    				i++;
    			}
    		} else i++;    		
    	}
    }
    
    void JSON2File(JSONArray initial) throws Exception{
    	ArrayList<String> localKeys = new ArrayList<String>(Arrays.asList("username,article,level".split(",")));    	
        File out_file = new File("F:\\tmp\\20130401\\tema1368645_thread"+number++ +".comments");
//    	File out_file = new File("F:\\tmp\\20130401\\tema1368153_.comments");
        PrintWriter out = new PrintWriter(out_file);

    	for (int i=0; i<initial.length(); i++){
    		out.println("-----------------------------------------------New Comment #"+(i+1));
    		JSONObject first_level = initial.getJSONObject(i);
    		for(String aName:JSONObject.getNames(first_level)){
    			if (!localKeys.contains(aName)) continue;    			
    			Object obj = first_level.get(aName);
    			if (obj.getClass() == JSONArray.class){
					JSONArray tmp = first_level.getJSONArray(aName);
//					out.println("["+aName+"]");
					
					for (int j = 0; j < tmp.length(); j++) {
						JSONObject second_level = tmp.getJSONObject(j);
						for(String bName:JSONObject.getNames(second_level)){
							out.println("  "+bName+": "+JSONObject.valueToString(second_level.get(bName)));
						}
					}    				
    			} else {
					out.println(aName+": "+JSONObject.valueToString(first_level.get(aName)));					    				
    			}
    		}
    	}
    	out.close();
    }
    
    public static void main(String[] args) throws Exception {
        Comments t = new Comments();
        for (int i=1; i<8; i++){
        	t.retrieveComments2(t.retrieveJson("http://tema.livejournal.com/1368645.html?page="+i+"&format=light"));
        	System.out.println("http://tema.livejournal.com/1368645.html?page="+i+"&format=light");
        	System.out.println(new Date());	
        }
    	File out_file = new File("F:\\tmp\\20130401\\tema1368153_all.comments");
        PrintWriter out = new PrintWriter(out_file);        
    	for (Comment aComment:t.commentList)
    		out.println(aComment);
    	out.close();
    	System.out.println(t.commentList.size());
    	System.out.print(new Date());
    }
}