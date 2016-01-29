

import java.util.Map;
import java.util.TreeMap;

class Posting{
	public Map<String, Integer> count = new TreeMap<String, Integer>();
	public void addTerm (String term){
		if (count.containsKey(term) == false)
			count.put(term, 1);
		else
			count.put(term, count.get(term)+1);
	}
	
	public void merge (Posting second, int maximum){
		for (Map.Entry<String, Integer> termCnt: second.count.entrySet()){
			if (this.count.containsKey(termCnt.getKey()) == false)
				this.count.put(termCnt.getKey(), Math.min(maximum, termCnt.getValue()));
			else
				this.count.put(termCnt.getKey(), Math.min(maximum, termCnt.getValue()) + this.count.get(termCnt.getKey()));
		}
	}
	
	public int totalTerm (){
		int ret = 0;
		for (Integer cnt: count.values())
			ret+= cnt;
		return ret;
	}
}


