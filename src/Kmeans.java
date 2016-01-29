import java.util.ArrayList;

public class Kmeans extends Thread implements Progressable{
	
	static int MAX_ITER = 100;
	
	public ArrayList<Point> points;
	public int k;
	public int iter;
	public Metric metric;
	public double threshold;
	public boolean terminated = true;
	public ArrayList<Cluster> ret;
	
	@Override
	public void run(){
		this.terminated = false;
		int[] cluster = new int[points.size()];
		Point[] center = new Point[k];
		int[] count = new int[k];
		for (int i=0; i<k; i++)
			center[i] = new Point(points.get(i));
		double prevDist = 1e10;
		this.iter = 0;
		while (true){
			for (int i=0; i<k; i++)
				count[i] = 0;
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
			for (int i=0; i<k; i++){
				if (count[i] != 0)
					center[i].multTo(1.0/count[i]);
				else
					center[i].zero();
			}
			double totalDist = 0;
			for (int i=0; i<points.size(); i++)
				totalDist += metric.dist(center[cluster[i]], points.get(i));
			if (Math.abs(totalDist - prevDist) < this.threshold)
				break;
			if (this.iter > MAX_ITER)
				break;
			this.iter++;
			prevDist = totalDist;
			
		}
		ret.clear();
		for (int i=0; i<k; i++) if (count[i] != 0){
			Cluster c = new Cluster();
			for (int j=0; j<points.size(); j++) if (cluster[j] == i)
				c.addPoint(points.get(j));
			ret.add(c);
		}
		System.err.println("Score: " + -prevDist);
		Labeling labeling = new Labeling();
		labeling.clusters = UI.cluster;
		labeling.run();
		for (Cluster c: UI.cluster){
			System.out.println(c.titles);
			for (Point p: c.points)
				System.out.print(p.d.id + " ");
			System.out.println();
		}
		terminated = true;
	}
	
	@Override
	public String getProgress() {
		if (terminated)
			return null;
		return "(kmeans) iter=" + this.iter; 
	}
}

