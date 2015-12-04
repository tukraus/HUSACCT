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
				// System.out.println("(" + subList + ")");

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
}
