

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class Indexer {
	
	private int numberOfDocs = 0;
	public Map<String, Integer> df = new TreeMap<String, Integer>();
	public ArrayList<Document> docs = new ArrayList<Document>();
	
	public void addDoc (Document d){
		Tokenizer tokenizer = new Tokenizer(d.title, TokenizationMode.Query);
		while (true){
			String term = tokenizer.nextToken();
			if (term.equals("$") == true)
				break;
			d.posting.addTerm(term);
		}
		for (String term: d.posting.count.keySet())
			if (df.containsKey(term) == false){
				df.put(term, 1);
			}else
				df.put(term, df.get(term) + 1);
		docs.add(d);
		numberOfDocs++;
	}
	
	public ArrayList<Point> getPoints(){
		ArrayList<Point> points = new ArrayList<Point>();
		for (Document doc: docs){
			Point point = new Point();
			point.d = doc;
			for (Map.Entry<String, Integer> termCnt: doc.posting.count.entrySet()){
				double tf = Math.log10(termCnt.getValue() + 1);
				double idf= Math.log10((double)numberOfDocs/(double)df.get(termCnt.getKey()));
				point.coords.put(termCnt.getKey(), tf*idf);
			}
			point.normalize();
		}
		return points;
	}
}