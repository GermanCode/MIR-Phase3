

import java.util.ArrayList;

public class Cluster {
	
	public ArrayList<Point> points = new ArrayList<Point>();
	public Point center;
	public ArrayList<String> titles;
	public ArrayList<Double> informations;
	public Posting posting = null;
	
	public Cluster () {}
	
	public void addPoint (Point x){
		points.add(x);
	}
	
	public void setPosting(){
		posting = new Posting();
		for (Point point: points)
			posting.merge(point.d.posting, 1);
	}
}

