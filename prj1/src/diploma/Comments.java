package diploma;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

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
	String expand_url;
	String ctime;
	String article;
	String username;
	long thread;
	long parent;
	 int level;
	boolean collapsed;
	
	public Comment(String username, long thread, long parent, String ctime, String article, int level){
		this.username = username;
		this.expand_url = "";
		this.thread = thread;
		this.parent = parent;
		this.ctime = ctime;
		this.article = article;
		this.level = level;
		this.collapsed = false;
	}
	
	public Comment(String threadURL){
		this.expand_url = threadURL;
		this.collapsed = true;
	}
	
	public String toString(){
		String blink = "";
		if (collapsed) return expand_url;
		for (int i=0; i*2<level; i++)
			blink += " ";
		return blink+article;
	}
	
	@Override
	public boolean equals(Object Obj){
		Comment aComment = (Comment) Obj;
		return (this.collapsed == aComment.collapsed);
	}
	
	@Override
	public int hashCode(){
		int code = 1;
		if (collapsed) code = 0;
		return code;
	}
}


public class Comments {
	
    List<Comment> commentList;
    List<String> linkList;
    int number = 0;
    int pagesCount = -1;
    String username;
    String postId;
    
    public Comments(String username, String postId) throws Exception{
    	this.username = username;
    	this.postId = postId;
        commentList = new ArrayList<Comment>();
           linkList = new ArrayList<String>();
           
        List<Comment> tmpList = new ArrayList<Comment>();
        tmpList = initList(retrieveJson("http://"+username+".livejournal.com/"+postId+".html?format=light"));
        commentList.addAll(tmpList);
        //pagesCount определяется в retrieveJson
           for (int i = 2; i <= pagesCount; i++) {
   			tmpList = initList(retrieveJson("http://"+username+".livejournal.com/"+postId+".html?page="+i+"&format=light"));
   			System.out.println("http://"+username+".livejournal.com/"+postId+".html?page="+i+"&format=light");
   			commentList.addAll(tmpList);
   		}
        makeList(commentList);
        writeList();
    }
   
    private void writeList() throws Exception {
       	File out_file = new File("F:\\tmp\\20130510\\"+username+postId+".comments");
        PrintWriter out = new PrintWriter(out_file);
    	for (Comment aComment:commentList)
    		out.println(aComment);
    	out.close();
    	System.out.println(commentList.size());
    	System.out.print(new Date());
//    	test encoding
	}

	//    /\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\ NEW BLOCK
    void getFirstLevelList(JSONArray initial) throws Exception{
//    	Принимает набор первоначальных комментариев, открывает все 1е уровни, грузит из них комменты
    	String href = "";
    	List<String> localKeys = null;
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
                	  Json2File(retrieveJson(href));
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
//  \/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/ TRash BLOCK
    
    JSONArray retrieveJson(String url) throws Exception{
    	
//    	Возвращает массив с комментариями из страницы по адресу url
        HttpClient   httpclnt = new DefaultHttpClient();
        HttpGet       httpget = new HttpGet(url);
        httpget.setHeader("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)");
        HttpResponse httpresp = httpclnt.execute(httpget);
        HttpEntity     entity = httpresp.getEntity();
        String 		html_text = new String();
        if (entity != null)
            html_text = EntityUtils.toString(entity, "UTF-8");
        httpget.abort();
        
        Document  		  doc = Jsoup.parse(html_text,"UTF-8");       
        Elements 	 comments = doc.getElementsByAttributeValue("id", "comments_json");
//      Вычисляем количество страниц с комментариями
        if (pagesCount == -1)
        	pagesCount = Jsoup.parse(html_text).select("li.b-pager-page").size()/2;
        return new JSONArray(comments.html());
    }

    
    List<Comment> initList(JSONArray input){
    	String expand_url = "";
    	String ctime;
    	String article;
    	String username;
    	long thread;
    	long parent;
    	 int level;
    	List<Comment> result = new ArrayList<Comment>();
    	 
    	List<Comment> tmpFull = new ArrayList<Comment>();
    	List<Comment> tmpLink = new ArrayList<Comment>();
    	for (int i=0; i<input.length(); i++){
    		JSONObject first_level = input.getJSONObject(i);
    		String[] aNames = JSONObject.getNames(first_level);
    		if (Arrays.binarySearch(aNames, "moreusers") < -1) continue; //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    		if (tmpFull.isEmpty() & Integer.parseInt(JSONObject.valueToString(first_level.get("collapsed"))) == 1) continue;
    		if (Integer.parseInt(JSONObject.valueToString(first_level.get("deleted"))) == 1){
    			JSONObject usernameJson = first_level.getJSONArray("username").getJSONObject(0);
    			username = JSONObject.valueToString(usernameJson.get("journal_url"));
    			ctime 	= "February 14 1980, 00:00:00 UTC";
    			article = "Deleted comment.";
    			thread 	=   Long.parseLong(JSONObject.valueToString(first_level.get("thread")));
    			parent 	=   Long.parseLong(JSONObject.valueToString(first_level.get("parent")));
    			level   = Integer.parseInt(JSONObject.valueToString(first_level.get("level")));
        		tmpFull.add(new Comment(username, thread, parent, ctime, article, level));
        		tmpLink.add(new Comment("www.google.com"));
        		continue;
    		}
//    		Проверили необходимость сохранения текущего комментария
			ctime 	=                  JSONObject.valueToString(first_level.get("ctime"));
			article = 				   JSONObject.valueToString(first_level.get("article"));
			thread 	=   Long.parseLong(JSONObject.valueToString(first_level.get("thread")));
			parent 	=   Long.parseLong(JSONObject.valueToString(first_level.get("parent")));
			level   = Integer.parseInt(JSONObject.valueToString(first_level.get("level")));
//    		Сохранили все данные из первого уровня
			Object tryUsername = first_level.get("username");
			if (tryUsername.getClass().equals(JSONArray.class)){
				JSONObject usernameJson = ((JSONArray) tryUsername).getJSONObject(0);
				username = JSONObject.valueToString(usernameJson.get("journal_url"));
				}
			else 
				username = "Anonymous";
//			Сохранили название журнала из второго, если username не массив - значит автор анонимус
			JSONArray actionsArray = first_level.getJSONArray("actions");
    		for (int j=0; j<actionsArray.length(); j++){
    			JSONObject tmp = actionsArray.getJSONObject(j);
    			if (JSONObject.valueToString(tmp.get("name")).equals("\"expand\""))
    				expand_url = tmp.getString("href");
    		}
//			Извлекли ссылку из внутреннего массива
    		tmpFull.add(new Comment(username, thread, parent, ctime, article, level));
    		tmpLink.add(new Comment(expand_url));
    		if (Integer.parseInt(JSONObject.valueToString(first_level.get("collapsed"))) == 1){
//    			Текущая ветка скрыта, поднимаемся на до корня текущей ветки уровня выше
//    			Проверяем, если корень - удаленный коммент, то на него нет ссылки ((
//    			до ссылки на развернуть может быть как 2 (если это корень) так и 1 коммент
    			if (tmpFull.size() == 2){
    				result.add(tmpFull.get(0));
    				if (!tmpLink.get(1).toString().equals("www.google.com"))
    					result.add(tmpLink.get(1));
    			} else if (tmpFull.size() > 2) {
        			for (int j = 0; j < tmpFull.size()-3; j++)
        				result.add(tmpFull.get(j));
        			if (!tmpLink.get(tmpLink.size()-3).toString().equals("www.google.com"))
        				result.add(tmpLink.get(tmpLink.size()-3));    				
    			}
    			tmpFull.clear();
    			tmpLink.clear();
    		} else
    		if (i == input.length()-1)
    			result.addAll(tmpFull);
//    			дошли до конца и не свернуто
    	}
//    	ЦИкл окончен, возвращаем результат
    	return result;
    }
    
    void makeList(List<Comment> input) throws Exception{
//    	рекурсивный метод, проверят есть ли в списке ссылки на треды, и раскрывает их, добавляя результат во временный список
//    	если временный список не содержит ссылок, возвращает итоговый список    	
    	List<Comment> tempList = new ArrayList<Comment>();
    	Comment dummyComment = new Comment("www.google.com");
//    	dummyComment индикатор ссылочного коммента
    	int i=0;
    	if (input.contains(dummyComment)){
    		for (Comment aComment:input){
    			if (aComment.equals(dummyComment)){
    				i++;
//    				нашли, вытягиваем из него список
    				List<Comment> dummyList = new ArrayList<Comment>();
    				dummyList = initList(retrieveJson(aComment.expand_url)); 
    				tempList.addAll(dummyList);
    			} else
    				tempList.add(aComment);
    		}
    	}
		System.out.println(i + " threads " + new Date().toString());   
		System.out.println(number++ + " - всего проходов.");
//    	если временный список содержит ссылки, повторный прогон, если нет - сохраняем результат    	
    	if (tempList.contains(dummyComment))
    		makeList(tempList);
    	else
    		commentList = tempList;
    }

    void Json2File(JSONArray initial) throws Exception{
    	File out_file = new File("D:\\aovodov\\tmp\\20130510\\trololoshka.json");
        PrintWriter out = new PrintWriter(out_file);

    	for (int i=0; i<initial.length(); i++){
    		out.println("-----------------------------------------------New Comment #"+(i+1));
    		JSONObject first_level = initial.getJSONObject(i);
    		for(String aName:JSONObject.getNames(first_level)){
    			if (aName.equals("leafclass")) continue;
//    			if (!localKeys.contains(aName)) continue;
    			Object obj = first_level.get(aName);
    			if (obj.getClass() == JSONArray.class){
    				out.println("\n"+aName+":");
					JSONArray tmp = first_level.getJSONArray(aName);
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
        Comments t = new Comments("mi3ch","2302043");
    }
    
}