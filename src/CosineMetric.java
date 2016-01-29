import java.util.Map;



public class CosineMetric extends Metric{
	
	@Override
	public double dist(Object obj0, Object obj1) {
		Point a = (Point)obj0;
		Point b = (Point)obj1;
		double result = 0.0;
		for (Map.Entry<String, Double> x: a.coords.entrySet())
			if (b.coords.containsKey(x.getKey()))
				result += x.getValue() * b.coords.get(x.getKey());
		return result;
	}
}