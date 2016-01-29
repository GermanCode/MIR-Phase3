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
public class ResearcherCrawler extends Thread
{
	int number;
	ArrayList<String> initUrls;
	Queue<String> queue;
	HashSet<Long> mark;
	HashSet<Long> parsed;
	HashMap<Long,HashSet<Long>> publicaton;
	Pattern resID,pubID;
	double merge;
	public ResearcherCrawler(int number, double m, ArrayList<String> initUrls)
	{
		merge=m;
		this.number = number;
		this.initUrls=initUrls;
		pubID=Pattern.compile("\\D*/publication/(\\d+)_.*");
		resID=Pattern.compile("\\D*/researcher/(\\d+)_.*");
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
				if (s.equals("publication")) id=getID2(elements.get(i).attr("abs:href"));
				if (id!=null)
					res.add(elements.get(i).attr("abs:href"));
			}
		}
		return res;
	}
	public ResearcherCrawler(double merge,ArrayList<String> initUrls)
	{
		this(100,merge,initUrls);
	}
	public void run()
	{
		queue= new LinkedList<>();
		mark=new HashSet<>();
		parsed=new HashSet<>();
		publicaton=new HashMap<>();
		if (initUrls!=null) for (String s:initUrls)
			add(s);
		while (!queue.isEmpty() && parsed.size()<number)
		{
			String now=queue.remove();
			try
			{
				if (getID(now)!=null)
				{
					Long id=getID(now);
					publicaton.put(id,new HashSet<>());
					ArrayList<String> authors=parse(id,now);
					for (int i=0;i<Math.min(authors.size(),10);i++) add(authors.get(i));
					parsed.add(id);
					System.out.print("\r");
					System.out.print("[");
					int p= (int) Math.floor((double)parsed.size()/number*50);
					for (int i=0;i<p;i++)
						System.out.print("#");
					if ((int)Math.ceil((double)parsed.size()/number*50)>p)
					{
						System.out.print(">");
						p++;
					}
					for (int i=0;i<50-p;i++)
						System.out.print(".");
					System.out.print("] "+Math.round((double)parsed.size()/number*1000)/10.+" %");
				}

			} catch (IOException e)
			{
				queue.add(now);
				e.printStackTrace();
			}

		}
		cluster();
	}

	private void cluster()
	{
		ArrayList<ArrayList<Long>> clusters=new ArrayList<>();
		for (Long p:parsed)
		{
			ArrayList<Long> temp=new ArrayList<>();
			temp.add(p);
			clusters.add(temp);
		}
		while (clusters.size()>1)
		{
			double mx=-1;
			int f=-1,s=-1;
			for (int i=0;i<clusters.size();i++)
				for (int j=i+1;j<clusters.size();j++)
				{
					double dis=average(clusters.get(i),clusters.get(j));
					if (dis>mx)
					{
						mx=dis;
						f=i;
						s=j;
					}
				}
			if (mx<merge) break;
			for (Long p:clusters.get(s))
				clusters.get(f).add(p);
			clusters.remove(s);
		}
		for (int i=0;i<clusters.size();i++)
		{
			System.out.print("{");
			boolean first=true;
			for (Long p:clusters.get(i))
			{
				if (!first)
					System.out.print(" ,");
				first=false;
				System.out.print(p);
			}
			System.out.println();
		}
	}

	private double average(ArrayList<Long> first, ArrayList<Long> second)
	{
		double sum=0;
		for (Long f:first)
			for (Long s:second)
				sum+=common(f,s);
		sum/=first.size()*second.size();
		return sum;
	}

	private double common(Long f, Long s)
	{
		int res=0;
		for (Long p:publicaton.get(f))
			if (publicaton.get(s).contains(p))
				res++;
		return res;
	}

	private ArrayList<String> parse(long id,String url) throws IOException
	{
		ArrayList<String> researchers=new ArrayList<>();
		HashSet<Long> set=new HashSet<>();
		if (url.charAt(url.length()-1)!='/')
			url=url+"/";
		for (int i=1;i<=20;i++)
		{
			Document d=get(url+"publications/"+i);
			int count=0;
			ArrayList<String> others=getLinks(d,"researcher");
			for (String o:others)
				researchers.add(o);
			ArrayList<String> pub=getLinks(d,"publication");
			for (String s:pub)
			{
				Long id2=getID2(s);
				if (id2==null) continue;
				if (set.contains(id2)) continue;
				count++;
				publicaton.get(id).add(id2);
				set.add(id2);
			}
			if (count==0) break;
		}
		return researchers;
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
		Matcher m=resID.matcher(s);
		if (!m.find())
			return null;
		return Long.parseLong(m.group(1));
	}
	private Long getID2(String s)
	{
		Matcher m=pubID.matcher(s);
		if (!m.find())
			return null;
		return Long.parseLong(m.group(1));
	}
	public static void main(String[] args) throws IOException
	{
		ResearcherCrawler crawler=new ResearcherCrawler(5,4.,new ArrayList<String>(Arrays.asList("http://www.researchgate.net/researcher/8159937_Zoubin_Ghahramani/")));
		crawler.start();
	}
}
