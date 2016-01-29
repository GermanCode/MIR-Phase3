

import java.util.ArrayList;
import java.util.Map;

public class Labeling {
	
	public ArrayList<Cluster> clusters;
	public static int CANDIDATE = 10;
	
	public void run(){
		Posting totalPosting = new Posting();
		int n = 0;
		for (Cluster cluster: clusters){
			cluster.setPosting();
			totalPosting.merge(cluster.posting, Integer.MAX_VALUE);
			n+= cluster.points.size();
		}
		for (Cluster cluster: clusters){
			ArrayList<String> titles = new ArrayList<String>();
			ArrayList<Double> infos = new ArrayList<Double>();
			for (Map.Entry<String, Integer> termCnt: cluster.posting.count.entrySet()){
				double info = 0.0;
				int n11 = termCnt.getValue();
				int n10 = totalPosting.count.get(termCnt.getKey()) - n11;
				int n01 = cluster.points.size() - n11;
				int n00 = n - n01 - n10 - n11;
				if (n11 > 0)
					info += (double)n11/n * Math.log10((double)n * n11 / ((double)n10 + n11) / ((double)n01 + n11));
				if (n01 > 0)
					info += (double)n01/n * Math.log10((double)n * n01 / ((double)n01 + n00) / ((double)n01 + n11));
				if (n10 > 0)
					info += (double)n10/n * Math.log10((double)n * n10 / ((double)n10 + n11) / ((double)n10 + n00));
				if (n00 > 0)
					info += (double)n00/n * Math.log10((double)n * n00 / ((double)n01 + n00) / ((double)n10 + n00));
				if (titles.size() < CANDIDATE){
					titles.add(termCnt.getKey());
					infos.add(info);
				}else{
					int pos = -1;
					for (int i=0; i<infos.size(); i++) if (infos.get(i) < info){
						if (pos==-1)
							pos = i;
						else if (infos.get(i) < infos.get(pos))
							pos = i;
					}
					if (pos != -1){
						titles.set(pos, termCnt.getKey());
						infos.set(pos, info);
					}
				}
			}
			for (int i=0; i<titles.size(); i++)
				for (int j=i+1; j<titles.size(); j++){
					if (infos.get(i) < infos.get(j)){
						String tempTitle= titles.get(i);
						double tempInfo = infos.get(i);
						titles.set(i, titles.get(j));
						infos.set(i, infos.get(j));
						titles.set(j, tempTitle);
						infos.set(j, tempInfo);						
					}
				}
			cluster.informations = infos;
			cluster.titles = titles;
		}
	}
}

