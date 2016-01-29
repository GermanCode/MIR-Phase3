import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: LGM
 * Date: 1/29/16
 * Time: 4:02 PM
 */
public class ElasticSearchAPI
{
	private String address="http://127.0.0.1:9200";
	private String indexName="researchgate";
	public String location;
	public int aw,tw,abw;

	public ElasticSearchAPI(String location, int aw, int tw, int abw)
	{
		this.location = location;
		this.aw = aw;
		this.tw = tw;
		this.abw = abw;
	}
	public void setWeights(int _aw,int _tw,int _abw)
	{
		aw=_aw;
		tw=_tw;
		abw=_abw;
	}
	public void addDocuments()
	{
		try
		{
			int count=0;
			request(address+"/"+indexName+"/","DELETE","");
			request(address+"/"+indexName+"/","PUT","");
			request(address+"/"+indexName+"/_mapping/docs/","PUT",createMap().toString());
			File folder = new File(location);
			int tot=folder.listFiles().length;
			for (File f:folder.listFiles())
				if (f.isFile())
				{
					String s=read(f);
					JsonParser jsonParser = new JsonParser();
					JsonObject jo = (JsonObject)jsonParser.parse(s);
					JsonObject doc=new JsonObject();
					doc.add("title",jo.get("title"));
					doc.add("authors", jo.get("authors"));
					doc.add("_abstract",jo.get("_abstract"));
					request(address +"/"+indexName+"/docs/"+f.getName()+"/","PUT",doc.toString());
					count++;
					System.out.print("\r");
					System.out.print("[");
					int p= (int) Math.floor((double)count/tot*50);
					for (int i=0;i<p;i++)
						System.out.print("#");
					if ((int)Math.ceil((double)count/tot*50)>p)
					{
						System.out.print(">");
						p++;
					}
					for (int i=0;i<50-p;i++)
						System.out.print(".");
					System.out.print("] " + Math.round((double) count / tot * 1000) / 10. + " %");
				}

		} catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	private JsonObject createMap()
	{
		JsonObject res=new JsonObject();
		JsonObject prop=new JsonObject();
		JsonObject title=new JsonObject();
		title.addProperty("type","String");
		prop.add("title",title);
		JsonObject authors=new JsonObject();
		authors.addProperty("type","String");
		prop.add("authors", authors);
		JsonObject abs=new JsonObject();
		abs.addProperty("type", "String");
		prop.add("_abstract", abs);
		res.add("properties", prop);
		return res;
	}

	private String read(File f) throws IOException
	{
		BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(f)));
		String res="";
		boolean first=true;
		while (reader.ready())
		{
			if (!first)
				res=res+"\n";
			first=false;
			res=res+reader.readLine();
		}
		reader.close();
		return res;
	}

	private void request(String address, String method,String com) throws IOException
	{
		URL url = new URL(address);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod(method);
		if (com.equals(""))
		{
			if (con.getResponseCode()>=400)
				return;
			return;
		}
		con.setDoOutput(true);
		Writer writer=new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));
		writer.write(com);
		writer.close();
		if (con.getResponseCode()>=400)
		{
			BufferedReader in = new BufferedReader(
					new InputStreamReader(con.getErrorStream()));
			String response ="";
			while (in.ready())
				response=response+in.readLine();
			in.close();
			System.err.println(response);
			return ;
		}
	}

	public void search(String query)
	{

	}

	public static void main(String[] args)
	{
		ElasticSearchAPI api=new ElasticSearchAPI("Documents",1,1,1);
		api.addDocuments();
	}
}
