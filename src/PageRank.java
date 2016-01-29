import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class PageRank extends Thread implements Progressable{
	
	static int MAX_ITER = 100;
	
	public double alpha = 0.2;
	public ArrayList<Document> docs;
	public double[][] p;
	public int iter;
	public double diff;
	public double threshold;
	public Map<String, Integer> docid;
	public boolean terminated = true; 
	
	public PageRank (){
	}
	
	public double[][] buildTransitionMatrix(){
		docid = new TreeMap<String, Integer>();
		for (int i=0; i<(int)docs.size(); i++)
			docid.put(docs.get(i).id, i);
		int n = docs.size();
		p = new double[n][n];
		for (int i=0; i<n; i++)
			for (int j=0; j<n; j++)
				p[i][j] = 0.0;
		for (int i=0; i<n; i++){
			Document d = docs.get(i);
			int goodRef = 0;
			for (String ref: d.referenceId)
				if (docid.containsKey(ref) == true)
					goodRef++;
			if (goodRef == 0){
				for (int j=0; j<n; j++)
					p[i][j] = 1.0/n;
			}else{
				for (int j=0; j<n; j++)
					p[i][j] = alpha/n;
				for (String ref: d.referenceId)
					if (docid.containsKey(ref) == true){
						p[i][docid.get(ref)]+= (1-alpha) * 1.0/goodRef;
					}
			}
		}
		return p;
	}
	
	public double getN (){
		return docs.size();
	}
	
	@Override
	public void run(){
		buildTransitionMatrix();
		int n = docs.size();
		double[] init = new double[n];
		for (int i=0; i<n; i++)
			init[i] = 0.0;
		init[0] = 1.0;
		double[] temp = new double[n];
		this.iter = 0;
		this.terminated = false;
		while (true){
			for (int i=0; i<n; i++)
				temp[i] = 0.0;
			for (int i=0; i<n; i++)
				for (int j=0; j<n; j++)
					temp[j]+= init[i] * p[i][j];
			this.diff = 0.0;
			for (int i=0; i<n; i++){
				diff+= Math.abs(temp[i] - init[i]);
				init[i] = temp[i];
			}
			this.iter++;
			if ((diff < this.threshold) || (this.iter > MAX_ITER))
				break;
		}
		for (Document d: docs){
			d.pageRank = init[docid.get(d.id)];
			System.err.println(d.id + ": " + new DecimalFormat("#0.000000").format(d.pageRank));
		}
		this.terminated = true;
	}
	
	@Override
	public String getProgress() {
		if (terminated)
			return null;
		return "iter=" + iter + ", " + "diff with inital=" + diff;
	}
}

