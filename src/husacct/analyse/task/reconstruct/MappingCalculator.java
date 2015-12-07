package husacct.analyse.task.reconstruct;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MappingCalculator {
	List<List<Integer>> results;
	boolean mapped = false;
	Iterator<List<Integer>> sublistIterator;

	public void findAllSublists(List<Integer> list, int minimalLengthOfSublist) {
		results = new ArrayList<List<Integer>>();
		List<List<Integer>> sublists = new ArrayList<List<Integer>>();
		for (int i = 0; i <= list.size(); i++) {
			recursiveSublist(list, sublists, i, new ArrayList<Integer>(), 0);
		}
		for (List<Integer> subList : sublists) {
			if (subList.size() >= minimalLengthOfSublist) {
				results.add(subList);
			}
		}
		sublistIterator = results.iterator();
		mapped = true;
	}

	public List<Integer> next() {
		if (mapped == false) {
			System.out.println("Error: sublists not yet determined. ");
		} else {
			if (sublistIterator.hasNext())
				return sublistIterator.next();
		}
		return null;
	}

	private static void recursiveSublist(List<Integer> list, List<List<Integer>> subLists, int sublistSize, List<Integer> currentSubList, int startIndex) {
		if (sublistSize == 0) {
			subLists.add(currentSubList);
		} else {
			sublistSize--;
			for (int i = startIndex; i < list.size(); i++) {
				List<Integer> newSubList = new ArrayList<Integer>(currentSubList);
				newSubList.add(list.get(i));
				recursiveSublist(list, subLists, sublistSize, newSubList, i + 1);
			}
		}
	}
	private static List<List<List<Integer>>> split(List<Integer> list, int groups) {
	    if (groups <= 0 || groups > list.size())
	        throw new IllegalArgumentException("Invalid number of groups: " + groups +
	                                           " (list size: " + list.size() + ")");
	    List<List<List<Integer>>> result = new ArrayList<>();
	    split(list, 0, new List[groups], 0, result);
	    return result;
	}
	private static void split(List<Integer> list, int listIdx,
	                          List<Integer>[] combo, int comboIdx,
	                          List<List<List<Integer>>> result) {
	    if (combo.length - comboIdx == 1) {
	        combo[comboIdx] = list.subList(listIdx, list.size());
	        result.add(new ArrayList<>(Arrays.asList(combo)));
	    } else {
	        for (int i = 0; i <= (list.size() - listIdx) - (combo.length - comboIdx); i++) {
	            combo[comboIdx] = list.subList(listIdx, listIdx + 1 + i);
	            split(list, listIdx + 1 + i, combo, comboIdx + 1, result);
	        }
	    }
	}
}
