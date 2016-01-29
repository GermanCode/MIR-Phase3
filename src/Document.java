import java.util.ArrayList;


public class Document implements Comparable<Document>{
	public String id;
	public String title;
	public String _abstract;
	public String[] authors;
	public cite[] cited_in;
	public String[] reference;
	public transient ArrayList<String> referenceId = new ArrayList<String>();
	public transient double pageRank = -1.0;
	public transient Posting posting = new Posting();
	
	@Override
	public boolean equals(Object arg0) {
		if ((arg0 instanceof Document) == false)
			return false;
		return this.id.equals(((Document)arg0).id);
	}
	
	@Override
	public int compareTo(Document arg0) {
		return this.id.compareTo(arg0.id);
	}
	
//	public static void main(String[] args) {
//		try{
//			Scanner sc = new Scanner(new File("Documents/1889902"));
//			Gson gson = new Gson();
//			String json = "";
//			while (sc.hasNext())
//				json+= sc.nextLine();
//			Document x = gson.fromJson(json, Document.class);
//			System.err.println(x.title);
//			System.err.println(x._abstract);
//			System.err.println(x.cited_in[1].id);
//			System.err.println(x.authors);
//		}catch (Exception e){
//			e.printStackTrace();
//		}
//	}
}

class cite{
	public String id;
	public String url;
}

