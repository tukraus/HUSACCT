package husacct.analyse.task.reconstruct.bruteForce;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Calculating all possible mappings in the aggregation case is a bit of a pain. The current implementation can undoubtedly be sped up, possibly
// by the use of sets instead of lists whenever we don't care about ordering. This I reserve for future improvements. The crucial step in slowing down
// the pattern matching approach is not the mapping generation, anyway. Earlier version of this mapper tended to run into heap space problem due to
// the large amounts of data being stored. Now, however, it works as follows: the given set of software unit names is converted to integers in a
// reverse ordering, e.g. (5,4,3,2,1). This list in then split into the number of groups equal to the amount of pattern modules (+1 in case of a
// Remainder). After that, the mapping is excluded if the integers within a single sublist (a pattern module) are not in order. TODO: rewrite this
// algorithm so that this step is unnecessary. After that, subsequent permutations of the original list are used to regroup the integers, only
// accepting ordered results and thus preventing duplicates. The permutation ends when the completely ordered list, (1,2,3,4,5), is found. Because
// this guarantees that no new mapping will be the same as an earlier one, there is no need to store all mappings in a variable. This circumvents the
// heap space problem. Perhaps there is a simpler way of doing this, I assume there is, but this will do for now. -- Joeri Peters
public class AggregateMappingGenerator {
	List<List<List<Integer>>> result;
	Map<Integer, List<List<Integer>>> finalResult;
	String[] stringNames;
	int numberOfGroups;
	int currentMappingSize;
	boolean isThereARemainder;
	List<Integer> currentPermutation;
	Map<Integer, List<List<Integer>>> storedResults;
	int mappingSizeAtClearing;

	public AggregateMappingGenerator(String[] packageNames, int numberOfPatternModules, boolean remainder, int numberOfTops) {
		numberOfGroups = numberOfPatternModules;
		isThereARemainder = remainder;
		if (remainder)
			numberOfGroups++; // Call this class once with and once without remainder to get all possible mappings.
		stringNames = packageNames;
		currentPermutation = convertToIntegers(stringNames);
		finalResult = new HashMap<Integer, List<List<Integer>>>();
		currentMappingSize = -1;
		mappingSizeAtClearing = 0;
		storedResults = new HashMap<Integer, List<List<Integer>>>(numberOfTops);
	}

	// Convert from strings to integers (makes things easier).
	private List<Integer> convertToIntegers(String[] names) {
		List<Integer> list = new ArrayList<Integer>(names.length);
		for (int i = 0; i < names.length; i++)
			list.add(i, names.length - i - 1);
		return list;
	}

	// Takes a list of integers and returns all possible ways to group them according to the given number of groups. For instance, [1,2,3,4] can be
	// arranged as [1],[2,3,4], as [1,2],[3,4] etc. The ordering within the groups has no effect.
	private void multipleSplit(List<Integer> individualPermutation, int numberOfGroups) {
		split(individualPermutation, numberOfGroups);
		preventDuplicates();
	}

	// The current way of calculating all possible distributions results in many duplicates due to the fact that [1,2],[3,4] is not truly different
	// from [1,2],[4,3]. This method prevents such duplicates.
	private void preventDuplicates() {
		int index = currentMappingSize;
		for (List<List<Integer>> singleGrouping : result) {
			if (groupingIsOrdered(singleGrouping) != false) {
				finalResult.put(index, singleGrouping);
				index++;
			}
		}
	}

	private boolean groupingIsOrdered(List<List<Integer>> singleGrouping) {
		for (int j = 0; j < singleGrouping.size(); j++) {
			for (int i = 0; i < singleGrouping.get(j).size() - 1; i++) {
				if (singleGrouping.get(j).get(i) > singleGrouping.get(j).get(i + 1))
					return false;
			}
		}
		return true;
	}

	// Splitting a list into groups in all possible ways.
	private void split(List<Integer> listSU, int numberOfGroups) {
		if (numberOfGroups <= 0 || numberOfGroups > listSU.size())
			throw new IllegalArgumentException("Invalid number of groups");
		result = new ArrayList<>();
		computeSplit(listSU, 0, new List[numberOfGroups], 0, result);
	}

	// Recursive method to generate splits.
	@SuppressWarnings("unchecked")
	private void computeSplit(List<Integer> listSU, int i, List[] combination, int j, List<List<List<Integer>>> resultTemp) {
		if (combination.length - j == 1) {
			combination[j] = listSU.subList(i, listSU.size());
			List<List<Integer>> temp = new ArrayList<>();
			temp.addAll((Collection<? extends List<Integer>>) new ArrayList<>(Arrays.asList(combination)));
			if (!resultTemp.contains(temp))
				resultTemp.add(deepClone(temp));
		} else {
			for (int index = 0; index <= (listSU.size() - i) - (combination.length - j); index++) {
				combination[j] = listSU.subList(i, i + index + 1);
				computeSplit(listSU, i + index + 1, combination, j + 1, resultTemp);
			}
		}
	}

	// Java always passes by value, but that value is a reference in the case of a nested list (essentially). Deep cloning creates a brand new list.
	private ArrayList<List<Integer>> deepClone(List<List<Integer>> listOfLists) {
		ArrayList<List<Integer>> cloneList = new ArrayList<List<Integer>>(listOfLists.size());
		ArrayList<Integer> clone;
		for (List<Integer> nestedList : listOfLists) {
			clone = new ArrayList<Integer>(nestedList.size());
			for (Integer element : nestedList)
				clone.add(element);
			cloneList.add(clone);
		}
		return cloneList;
	}

	public List<List<String>> next(int remove) {
		if (remove != -2) { // The previous call (if there was one), was not in the top N.
			if (remove == -1) { // The previous call fills up the top N
				storeResult(true);
			} else { // The previous call replaces a top candidate.
				storeResult(true);
				removeStored(remove);
			}
		}
		if (currentMappingSize == -1) { // The next permutation must not be called the first time.
			currentMappingSize++;
			multipleSplit(currentPermutation, numberOfGroups);
		}
		while (currentMappingSize - mappingSizeAtClearing == finalResult.size()) {
			currentPermutation = nextPermutation(currentPermutation);
			if (currentPermutation != null) {
				multipleSplit(currentPermutation, numberOfGroups);
			} else
				return null; // If not, there exist no more mappings and the brute force loop is complete.
		}
		return convertBack(finalResult.get(currentMappingSize));
	}

	private List<List<String>> convertBack(List<List<Integer>> next) {
		if (next == null)
			System.out.println("wut?");
		List<List<String>> mapping = new ArrayList<List<String>>(next.size());
		ArrayList<String> temp;
		for (int i = 0; i < next.size(); i++) {
			temp = new ArrayList<String>();
			for (int j = 0; j < next.get(i).size(); j++) {
				temp.add(stringNames[next.get(i).get(j)]);
			}
			mapping.add(i, temp);
		}
		if (isThereARemainder)
			mapping.remove(numberOfGroups - 1);
		currentMappingSize++;
		return mapping;
	}

	public Map<Integer, ArrayList<String>> getMappingMap(int i) {
		Map<Integer, ArrayList<String>> map = new HashMap<>();
		List<List<String>> bestOne = convertBack(storedResults.get(i - 1));
		for (int j = 0; j < bestOne.size(); j++) {
			ArrayList<String> temp = new ArrayList<String>();
			for (int k = 0; k < bestOne.get(j).size(); k++) {
				temp.add(bestOne.get(j).get(k));
			}
			map.put(j, temp);
		}
		return map;
	}

	private List<Integer> nextPermutation(final List<Integer> currentPermutation) {
		int first = getFirst(currentPermutation);
		if (first == -1)
			return null; // no greater permutation
		int toSwap = currentPermutation.size() - 1;
		while (currentPermutation.get(first) <= currentPermutation.get(toSwap))
			--toSwap;
		swap(currentPermutation, first++, toSwap);
		toSwap = currentPermutation.size() - 1;
		while (first < toSwap)
			swap(currentPermutation, first++, toSwap--);
		return currentPermutation;
	}

	private static int getFirst(final List<Integer> currentPermutation) {
		for (int i = currentPermutation.size() - 2; i >= 0; --i)
			if (currentPermutation.get(i) > currentPermutation.get(i + 1))
				return i;
		return -1;
	}

	private static void swap(final List<Integer> currentPermutation, final int i, final int j) {
		final int tmp = currentPermutation.get(i);
		currentPermutation.set(i, currentPermutation.get(j));
		currentPermutation.set(j, tmp);
	}

	private void storeResult(boolean store) {
		if (store)
			storedResults.put(currentMappingSize - 1, finalResult.get(currentMappingSize - 1));
		if (currentMappingSize > 3000 && currentMappingSize == finalResult.size()) {
			finalResult.clear();
			mappingSizeAtClearing = currentMappingSize;
		}
	}

	private void removeStored(int i) {
		if (storedResults.containsKey(i - 1))
			storedResults.remove(i - 1);
	}

}
