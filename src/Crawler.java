import com.google.gson.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: LGM
 * Date: 1/28/16
 * Time: 9:46 PM
 */
public class Crawler extends Thread
{
	int numberOfDocuments,inDegree,outDegree;
	ArrayList<String> initUrls;
	Queue<String> queue;
	HashSet<Long> mark;
	HashSet<Long> parsed;
	Pattern pubID;
	public Crawler(int numberOfDocuments, int inDegree, int outDegree, ArrayList<String> initUrls)
	{
		this.numberOfDocuments = numberOfDocuments;
		this.inDegree = inDegree;
		this.outDegree = outDegree;
		this.initUrls = initUrls;
		pubID=Pattern.compile("\\D*/publication/(\\d+)_.*");
	}
	public Document get(String url) throws IOException
	{
		Document d= Jsoup.connect(url).get();
		return d;
	}
	public ArrayList<String> getLinks(Document d,String s)
	{
		Elements elements=d.select("a");
		ArrayList<String> res=new ArrayList<>();
		for (int i=0;i<elements.size();i++)
		{
			if (elements.get(i).attr("href").startsWith(s))
			{
				Long id=getID(elements.get(i).attr("abs:href"));
				if (id!=null)
					res.add(elements.get(i).attr("abs:href"));
			}
		}
		return res;
	}
	public Crawler(int numberOfDocuments, int inDegree, ArrayList<String> initUrls)
	{
		this(numberOfDocuments,inDegree,10,initUrls);
	}
	public Crawler(int numberOfDocuments, ArrayList<String> initUrls)
	{
		this(numberOfDocuments,10,10,initUrls);
	}
	public Crawler(ArrayList<String> initUrls)
	{
		this(1000,10,10,initUrls);
	}
	public void run()
	{
		queue= new LinkedList<>();
		mark=new HashSet<>();
		parsed=new HashSet<>();
		if (initUrls!=null) for (String s:initUrls)
			queue.add(s);
		while (!queue.isEmpty() && parsed.size()<numberOfDocuments)
		{
			String now=queue.remove();
			try
			{
				Document d=get(now);
				if (getID(now)!=null)
				{
					Long id=getID(now);
					JsonObject document=parse(d, id);
					ArrayList<String> cited_in=getJson("https://www.researchgate.net/publicliterature.PublicationIncomingCitationsList.html?publicationUid=" + id + "&showCitationsSorter=true&showAbstract=true&showType=true&showPublicationPreview=true&swapJournalAndAuthorPositions=false&limit=100000");
					JsonArray cited=new JsonArray();
					for (String s:cited_in)
					{
						JsonObject temp=new JsonObject();
						temp.addProperty("id",getID(s));
						temp.addProperty("url",s);
						cited.add(temp);
					}
					document.add("cited_in",cited);
					ArrayList<String> references=getJson("https://www.researchgate.net/publicliterature.PublicationCitationsList.html?publicationUid=" + id + "&showCitationsSorter=true&showAbstract=true&showType=true&showPublicationPreview=true&swapJournalAndAuthorPositions=false&limit=100000");
					JsonArray reference=new JsonArray();
					for (String s:references)
					{
						JsonObject temp = new JsonObject();
						temp.addProperty("id", getID(s));
						temp.addProperty("url", s);
						reference.add(new JsonPrimitive(s));
					}
					document.add("reference", reference);
					save(document, id);
					parsed.add(id);
					System.out.print("\r");
					System.out.print("[");
					int p= (int) Math.floor((double)parsed.size()/numberOfDocuments*50);
					for (int i=0;i<p;i++)
						System.out.print("#");
					if ((int)Math.ceil((double)parsed.size()/numberOfDocuments*50)>p)
					{
						System.out.print(">");
						p++;
					}
					for (int i=0;i<50-p;i++)
						System.out.print(".");
					System.out.print("] "+Math.round((double)parsed.size()/numberOfDocuments*1000)/10.+" %");
					for (int i=0;i<Math.min(inDegree,cited_in.size());i++)
						add(cited_in.get(i));
					for (int i=0;i<Math.min(outDegree, references.size());i++)
						add(references.get(i));

				}
				else
				{
					ArrayList<String> links = getLinks(d, "publication");
					for (String s : links)
						add(s);
				}

			} catch (IOException e)
			{
				queue.add(now);
				e.printStackTrace();
			}

		}
	}

	private void save(JsonObject document, Long id)
	{
		try
		{
			PrintStream out=new PrintStream(new File("Documents/"+id));
			out.print(document.toString());
			out.close();
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}

	}

	private JsonObject parse(Document d,long id)
	{
		JsonObject jsonObject=new JsonObject();
		jsonObject.addProperty("id",id);
		jsonObject.addProperty("title", d.select(".pub-title,.publication-title").get(0).text());
		Elements authors=d.select(".item-name,.publication-author-name");
		JsonArray array=new JsonArray();
		for (Element e:authors)
			array.add(new JsonPrimitive(e.text()));
		jsonObject.add("authors", array);
		jsonObject.addProperty("_abstract", d.select(".pub-abstract>div>div,.publication-abstract-text").size() == 0 ? "" : d.select(".pub-abstract>div>div,.publication-abstract-text").get(0).text());
		return jsonObject;
	}

	private ArrayList<String> getJson(String s)
	{
		ArrayList<String> res=new ArrayList<>();
		try
		{
			URL url = new URL(s);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("accept", "application/json");
			BufferedReader in = new BufferedReader(
					new InputStreamReader(con.getInputStream()));
			String response = in.readLine();
			in.close();
			JsonParser jsonParser = new JsonParser();
			JsonObject jo = (JsonObject) jsonParser.parse(response);
			JsonArray array = jo.get("result").getAsJsonObject().get("data").getAsJsonObject().get("citationItems").getAsJsonArray();
			for (JsonElement element : array)
			{
				try
				{
					String u = element.getAsJsonObject().get("data").getAsJsonObject().get("publicationUrl").getAsString();
					u = "https://www.researchgate.net/" + u;
					res.add(u);
				}
				catch (Exception e)
				{

				}
			}
		}
		catch (IOException e)
		{

		}
		return res;
	}

	private void add(String s)
	{
		Long id=getID(s);
		if (id==null) return ;
		if (mark.contains(id)) return ;
		mark.add(id);
		queue.add(s);
	}

	private Long getID(String s)
	{
		Matcher m=pubID.matcher(s);
		if (!m.find())
			return null;
		return Long.parseLong(m.group(1));
	}
	public static void main(String[] args) throws IOException
	{
		Crawler crawler=new Crawler(new ArrayList<String>(Arrays.asList("http://www.researchgate.net/researcher/8159937_Zoubin_Ghahramani")));
		crawler.start();
	}
}
