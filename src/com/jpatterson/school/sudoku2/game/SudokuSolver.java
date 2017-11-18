package com.jpatterson.school.sudoku2.game;

import com.jpatterson.school.sudoku2.ui.Sudoku;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SudokuSolver
{
	private final SudokuBoard board;

	public SudokuSolver(SudokuBoard board)
	{
		this.board = board;
	}

	public void start()
	{
		resetPossibleValues();
		///TODO: it would be nice to report back to the parent popup every time a value is found.  maybe the parent should control this so users can pause between found values if desired

		boolean valueFound;

		do
		{
			updatePossibleValues();

			valueFound = setBoardValues() // levels 1 & 2
				|| cullPossibleValues() // level 3
				|| removePossibleValuesFromGroupsWherePossible(); // level 4

		}
		while (valueFound);
	}

	private void resetPossibleValues()
	{
		for (int r = 0; r < 9; r++)
		{
			for (int c = 0; c < 9; c++)
			{
				board.getSudokuCell(r, c).resetPossibleValues();
			}
		}
	}

	private void updatePossibleValues()
	{
		for (int r = 0; r < 9; r++)
		{
			for (int c = 0; c < 9; c++)
			{
				SudokuCell sudokuCell = board.getSudokuCell(r, c);
				if (sudokuCell.getValue() == null)
				{
					int groupNumber = board.getGroupNumber(r, c);
					Set<Integer> usedValues = new HashSet<>();

					usedValues.addAll(board.getValuesForRow(r));
					usedValues.addAll(board.getValuesForCol(c));
					usedValues.addAll(board.getValuesForGroup(groupNumber));

					usedValues.stream()
						.forEach(sudokuCell::removePossibleValue);
				}
			}
		}
	}

	private boolean setBoardValues()
	{
		boolean valueSet = false;
		for (int r = 0; r < 9; r++)
		{
			for (int c = 0; c < 9; c++)
			{
				final SudokuCell sudokuCell = board.getSudokuCell(r, c);
				if (sudokuCell.getValue() == null)
				{
					if (sudokuCell.getPossibleValues().size() == 1) // level 1
					{
						Integer value = sudokuCell
							.getPossibleValues()
							.iterator()
							.next();

						if (Sudoku.DEBUG)
						{
							System.out.printf("SOLVER: "
								+ "Setting value of %d at [%d,%d] "
								+ "because it is the only possible "
								+ "value in the cell.\n",
								value, r + 1, c + 1);
						}

						setValue(r, c, value);
						valueSet = true;
					}
					else // level 2
					{
						int groupNumber = board.getGroupNumber(r, c);
						for (Integer possibleValue : sudokuCell.getPossibleValues())
						{
							if (noPossibleValuesInCollectionContain(board.getOtherSudokuCellsForGroup(groupNumber, r, c), possibleValue)
								|| noPossibleValuesInCollectionContain(board.getOtherSudokuCellsForRow(r, c), possibleValue)
								|| noPossibleValuesInCollectionContain(board.getOtherSudokuCellsForCol(c, r), possibleValue))
							{
								if (Sudoku.DEBUG)
								{
									System.out.printf("SOLVER: "
										+ "Setting value of %d at [%d,%d] "
										+ "because it is the only possible "
										+ "spot for the value in the "
										+ "group, row, or column.\n",
										possibleValue, r + 1, c + 1);
								}

								setValue(r, c, possibleValue);
								valueSet = true;
								break;
							}
						}
					}
				}
			}
		}

		return valueSet;
	}

	/**
	 * If two cells in a row, col, or group (the "collection") have possible
	 * values a&b, no other cells in that collection can have those possible
	 * values. If this situation exists, the 'n' possible values that can only
	 * be in the 'n' cells are removed from other cells in the collection. In
	 * this situation, let 'n' = 2 (a&b). The same applies for situations with n
	 * > 2.
	 *
	 * @return whether or not any possible values were culled.
	 */
	private boolean cullPossibleValues()
	{
		boolean valuesCulled = false;
		for (int sectuionNumber = 0; sectuionNumber < 9; sectuionNumber++)
		{
			valuesCulled
				|= cullPossibleValues(board.getSudokuCellsForGroup(sectuionNumber), "group", sectuionNumber)
				|| cullPossibleValues(board.getSudokuCellsForRow(sectuionNumber), "row", sectuionNumber)
				|| cullPossibleValues(board.getSudokuCellsForCol(sectuionNumber), "col", sectuionNumber);

		}
		return valuesCulled;
	}

	/**
	 * Removes possible values from groups ina row/column what the value must be
	 * in inside of a different group. For example, if the only positions for a
	 * 1 in a group are in the first row, 1 cannot be in the first row of the
	 * other two groups that are horizontal to the group in question.
	 *
	 * @return Whether or not any possible values were remove from groups where
	 * there were not needed.
	 */
	private boolean removePossibleValuesFromGroupsWherePossible()
	{
		for (int groupNumber = 0; groupNumber < 9; groupNumber++)
		{
			for (int i = 0; i < 3; i++)
			{
				
			}
		}
		return false; // throw new UnsupportedOperationException("Not supported yet.");
	}

	private boolean cullPossibleValues(Collection<SudokuCell> sectionSudokuCells, String sectionType, int sectionNumber)
	{
		Map<Set<Integer>, List<SudokuCell>> possibleValuesForSudokuCells = sectionSudokuCells.stream()
			.filter(sudokuCell -> sudokuCell.getPossibleValues().size() > 1)
			.collect(Collectors.groupingBy(SudokuCell::getPossibleValues));
		List<Set<Integer>> possibleValuesToCull = possibleValuesForSudokuCells.entrySet()
			.stream()
			.filter(entry -> entry.getKey().size() == entry.getValue().size())
			.map(Map.Entry::getKey)
			.collect(Collectors.toList());

		boolean valuesCulled = false;
		for (Set<Integer> cullGroup : possibleValuesToCull)
		{
			if (Sudoku.DEBUG)
			{
				System.out.printf(
					"SOLVER: Removing possible values %s from some cells "
					+ "in %s %d because %d other cells "
					+ "have only these possible values.\n",
					cullGroup,
					sectionType,
					sectionNumber + 1,
					cullGroup.size());
			}

			for (SudokuCell sudokuCell : sectionSudokuCells)
			{
				if (sudokuCell.getValue() == null
					&& !sudokuCell.getPossibleValues().equals(cullGroup))
				{
					for (Integer possibleValue : cullGroup)
					{
						valuesCulled |= sudokuCell.removePossibleValue(possibleValue);
					}
				}
			}
		}

		return valuesCulled;
	}

	private boolean noPossibleValuesInCollectionContain(
		Collection<SudokuCell> otherSudokuCells, Integer possibleValue)
	{
		return otherSudokuCells.stream()
			.noneMatch(otherSudokuCell -> otherSudokuCell.getPossibleValues()
				.contains(possibleValue));
	}

	private void setValue(int r, int c, Integer value)
	{
		board.getSudokuCell(r, c).setValue(value);

		board.getOtherSudokuCellsForRow(r, c)
			.forEach(sudokuCell -> sudokuCell.removePossibleValue(value));
		board.getOtherSudokuCellsForCol(c, r)
			.forEach(sudokuCell -> sudokuCell.removePossibleValue(value));
		int groupNumber = board.getGroupNumber(r, c);
		board.getOtherSudokuCellsForGroup(groupNumber, r, c)
			.forEach(sudokuCell -> sudokuCell.removePossibleValue(value));
	}
}
