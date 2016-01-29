

import java.util.Map;
import java.util.TreeMap;

public class Point {
	
	public Document d;
	public Map<String, Double> coords = null;
	
	public Point () {}
	
	public Point (Point point){
		this.coords = new TreeMap<String,Double>();
		for (Map.Entry<String, Double> x: coords.entrySet())
			this.coords.put(x.getKey(), x.getValue());
	}
	
	public Point addTo (Point op){
		for (Map.Entry<String, Double> x: op.coords.entrySet()){
			if (this.coords.containsKey(x.getKey()))
				coords.put(x.getKey(), x.getValue() + this.coords.get(x.getKey()));
			else
				coords.put(x.getKey(), x.getValue());
		}
		return this;
	}
	
	public Point add (Point op){
		Point result = new Point(this);
		return result.addTo(op);
	}
	
	public Point multTo (double k){
		for (Map.Entry<String, Double> x: this.coords.entrySet())
			coords.put(x.getKey(), x.getValue() * k);
		return this;
	}
	
	public Point mult (double k){
		Point result = new Point(this);
		return result.multTo(k);
	}
	
	public void zero (){
		coords.clear();
	}
	
	public void normalize (){
		double norm = 0.0;
		for (Double x: coords.values())
			norm+= x*x;
		norm = Math.sqrt(norm);
		if (norm > 1e-6)
			this.multTo(1.0/norm);
	}
}
