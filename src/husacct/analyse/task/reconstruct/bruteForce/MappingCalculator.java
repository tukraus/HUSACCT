package husacct.analyse.task.reconstruct.bruteForce;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MappingCalculator {

	public List<List<List<Integer>>> split(List<Integer> listSU, int numberOfGroups) {
		if (numberOfGroups <= 0 || numberOfGroups > listSU.size())
			throw new IllegalArgumentException("Invalid number of groups");

		List<List<List<Integer>>> result = new ArrayList<>();
		computeSplit(listSU, 0, new List[numberOfGroups], 0, result);
		return result;
	}

	private void computeSplit(List<Integer> listSU, int i, List[] combination, int j, List<List<List<Integer>>> result) {
		if (combination.length - j == 1) {
			combination[j] = listSU.subList(i, listSU.size());
			result.add(new ArrayList<>(Arrays.asList(combination)));
		} else {
			for (int index = 0; index <= (listSU.size() - i) - (combination.length - j); index++) {
				combination[j] = listSU.subList(i, i + index + 1);
				computeSplit(listSU, i + index + 1, combination, j + 1, result);
			}
		}
	}

	public List<List<List<Integer>>> reorder(List<List<List<Integer>>> list) {
		return list;
	
	}

}
