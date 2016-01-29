import java.util.ArrayList;

public class Kmeans {
	
	public ArrayList<Point> points;
	public int k;
	public Metric metric;
	
	public ArrayList<Cluster> run(){
		int[] cluster = new int[points.size()];
		Point[] center = new Point[k];
		int[] count = new int[k];
		for (int i=0; i<k; i++)
			center[i] = new Point(points.get(i));
		while (true){
			for (int i=0; i<points.size(); i++){
				cluster[i] = 0;
				double minDis = metric.dist(points.get(i), center[cluster[i]]); 
				for (int j=1; j<k; j++){
					double tempDis = metric.dist(points.get(i), center[j]);
					if (tempDis < minDis){
						minDis = tempDis;
						cluster[i] = j;
					}
				}
				count[cluster[i]]++;
			}
			for (int i=0; i<k; i++)
				center[i].zero();
			for (int i=0; i<points.size(); i++)
				center[cluster[i]].addTo(points.get(i));
			for (int i=0; i<k; i++)
				if (count[i] != 0)
					center[i].multTo(1.0/count[i]);
				else
					center[i].zero();
			if (k < points.size())
				break;
		}
		ArrayList<Cluster> ret = new ArrayList<Cluster>();
		for (int i=0; i<k; i++) if (count[i] != 0){
			Cluster c = new Cluster();
			for (int j=0; j<points.size(); j++) if (cluster[j] == i)
				c.addPoint(points.get(j));
			ret.add(c);
		}
		return ret;
	}
}
