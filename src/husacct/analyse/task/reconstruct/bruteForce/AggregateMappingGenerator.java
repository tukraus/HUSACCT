package husacct.analyse.task.reconstruct.bruteForce;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// Calculating all possible mappings in the aggregation case is a bit of a pain. The current implementation can undoubtedly be sped up, possibly
// by the use of sets instead of lists whenever we don't care about ordering. This I reserve for future improvements. The crucial step in slowing down
// the pattern matching approach is not the mapping generation, anyway. -- Joeri Peters
public class AggregateMappingGenerator {
	List<List<List<Integer>>> result;
	List<List<List<Integer>>> finalResult;
	String[] stringNames;
	ArrayList<ArrayList<Integer>> permutations;
	Iterator<ArrayList<Integer>> permutationIterator;
	int numberOfGroups;
	int currentMappingSize;
	boolean isThereARemainder;

	// Constructor
	public AggregateMappingGenerator(String[] packageNames, int numberOfPatternModules, boolean remainder) {
		numberOfGroups = numberOfPatternModules;
		isThereARemainder = remainder;
		if (remainder)
			numberOfGroups++; // Call this class once with and once without remainder to get all possible mappings.
		List<Integer> listSU = convertToIntegers(packageNames);
		permutations = permute(listSU);
		permutationIterator = permutations.iterator();
		stringNames = packageNames;
		finalResult = new ArrayList<>(calculateExpectedLength(listSU.size(), numberOfGroups));
		currentMappingSize = 0;
	}

	private int calculateExpectedLength(int n, int k) {
		int sum = 0;
		for (int j = 0; j <= k; j++) {
			sum += Math.pow(-1, k - j) * Math.pow(j, n) * (factorial(k) / (factorial(j) * factorial(k - j)));
		}
		return sum;
	}

	private int factorial(int i) {
		if (i <= 1)
			return 1;
		return i * factorial(i - 1);
	}

	// Convert from strings to integers (makes things easier).
	private List<Integer> convertToIntegers(String[] packageNames) {
		List<Integer> list = new ArrayList<Integer>(packageNames.length);
		for (int i = 0; i < packageNames.length; i++)
			list.add(i, i);
		return list;
	}

	// Takes a list of integers and returns all possible ways to group them according to the given number of groups. For instance, [1,2,3,4] can be
	// arranged as [1],[2,3,4], as [1,2],[3,4] etc. The ordering within the groups has no effect.
	private void multipleSplit(List<Integer> individualPermutation, int numberOfGroups) {
		split(individualPermutation, numberOfGroups);
		removeDuplicates();
	}

	// The current way of calculating all possible distributions results in many duplicates due to the fact that [1,2],[3,4] is not truly different
	// from [1,2],[4,3]. This method removes such duplicates.
	private void removeDuplicates() {
		for (List<List<Integer>> singleGrouping : result)
			deepSort(singleGrouping);
		for (int i = 0; i < result.size(); i++) {
			if (!finalResult.contains(result.get(i))) {
				finalResult.add(currentMappingSize, result.get(i));
			}
		}
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

	// Sort the nested lists.
	private void deepSort(List<List<Integer>> temp) {
		for (List<Integer> group : temp)
			Collections.sort(group);
	}

	// Calculate all permutations, i.e. all ways by which to order a list of integers.
	private ArrayList<ArrayList<Integer>> permute(List<Integer> listSU2) {
		ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
		result.add(new ArrayList<Integer>());
		for (int i = 0; i < listSU2.size(); i++) {
			ArrayList<ArrayList<Integer>> current = new ArrayList<ArrayList<Integer>>();
			for (ArrayList<Integer> l : result) {
				for (int j = 0; j < l.size() + 1; j++) {
					l.add(j, listSU2.get(i));
					ArrayList<Integer> temp = new ArrayList<Integer>(l);
					current.add(temp);
					l.remove(j);
				}
			}
			result = new ArrayList<ArrayList<Integer>>(current);
		}
		return result;
	}

	public List<List<String>> next() {
		if (finalResult.size() == currentMappingSize) { // If there is no subsequent mapping known, then we want to know if there is at least a
														// subsequent permutation known. If so, use it to build more mappings.
			while (finalResult.size() == currentMappingSize) {
				if (permutationIterator.hasNext()) {
					multipleSplit(permutationIterator.next(), numberOfGroups);
				} else
					return null; // If not, there exist no more mappings and the brute force loop is complete.
			}
			return convertBack(finalResult.get(currentMappingSize));
		}
		return convertBack(finalResult.get(currentMappingSize - 1)); // Here we can return the next mapping, because
		// we simply had not yet reached the end of the list as it was. I don't use an iterator here due to the problem of adding to a list whilst
		// iterating over it and keeping track of the index. To be improved? Surely this whole mapper can be sped up.
	}

	private List<List<String>> convertBack(List<List<Integer>> next) {
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
		List<List<String>> bestOne = convertBack(finalResult.get(i));
		for (int j = 0; j < bestOne.size(); j++) {
			ArrayList<String> temp = new ArrayList<String>();
			for (int k = 0; k < bestOne.get(j).size(); k++) {
				temp.add(bestOne.get(j).get(k));
			}
			map.put(j, temp);
		}
		return map;
	}

}
