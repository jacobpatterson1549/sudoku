package com.jpatterson.school.sudoku2.game;

import java.util.Arrays;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SudokuBoard
{
	private final SudokuCell[][] board;

	public SudokuBoard(String boardString)
	{
		if (!boardString.matches("^\\d{81}$"))
		{
			throw new IllegalArgumentException("Illegal board: " + boardString);
		}

		this.board = IntStream.range(0, 9)
			.map(i -> i * 9)
			.mapToObj(i -> boardString.substring(i, i + 9)
				.chars()
				.mapToObj(ch -> (char) ch)
				.map(Character::valueOf)
				.map(character -> character.toString())
				.map(Integer::valueOf)
				.map(cellValue -> cellValue == 0 ? new MutableSudokuCell() : new ImmutableSudokuCell(cellValue))
				.toArray(SudokuCell[]::new))
			.toArray(SudokuCell[][]::new);
	}

	public SudokuBoard()
	{
//		this("000" + "000" + "000"
//			+ "000" + "000" + "000"
//			+ "000" + "000" + "000"
//			+ "000" + "000" + "000"
//			+ "000" + "000" + "000"
//			+ "000" + "000" + "000"
//			+ "000" + "000" + "000"
//			+ "000" + "000" + "000"
//			+ "000" + "000" + "000");

//		this("000000000000000000000000000000000000000000000000000000000000000000000000000000000");
//		this("768945123239617854145823976473261589582394617691758342827136495956482731314579268");0//0solved04/50stars
//		this("003070600000159020900000005700000010006040900040000006400000002070362000009080700");
		// May 18-24 2015
//		this("002689300849000020060470000170890402490020071206041089000054060080000195007918200"); // 1/5 stars
//		this("000086200000000069060039071140060920020070040037040015290610080680000000001520000"); // 2/5 stars
//		this("702186000050020000408070000600007002047205980200900004000090608000050040000841309"); // 3/5 stars
//		this("003761005000032000060000008008290006000070000400016900700000030000520000100643200"); // 4/5 stars
		//problem:
//		this("079010004000560080000070906104080090000000000050030701701040000040098000900050430"); // 5/5 stars March 22
//		this("000208009007040380050060000600900700070030010009007004000080060086070100500601000"); // 6/5 stars
//		this("900801000060020100200070004050130200080050090007096050700010006002080030000309002"); // 5/5 stars
		this("370095000600080090008300007000010050160000034040060000700002900020030008000950042"); // 6/5 stars 20170218
		// TODO: Fix the solver so this can be solved (March 22 puzzle as solved as level 2 solver can proceed)0
//		this("679810004410569087000070916104080090097020000050930701701040009040098170900050430");
	}

	@Override
	public String toString()
	{
		String boardValues = Arrays.stream(board)
			.flatMap(Arrays::stream)
			.map(sudokuCell -> sudokuCell.isEmpty() ? 0 : sudokuCell.getValue())
			.map(String::valueOf)
			.collect(Collectors.joining());

		return "{" + boardValues + "}";
	}

	public SudokuCell getSudokuCell(int r, int c)
	{
		validateCoords(r, c);

		return board[r][c];
	}

	/**
	 * @param r The row of the SudokuCell
	 * @param c The column of the SudokuCell
	 * @param value The value to add to the the possible values of the SudokuCell
	 * @return Whether or not the value was successfully removed from the possible values.
	 */
	public boolean addPossibleValue(int r, int c, Integer value)
	{
		return changePossibleValue(r, c, value, SudokuCell::addPossibleValue);
	}

	/**
	 * @param r The row of the SudokuCell
	 * @param c The column of the SudokuCell
	 * @param value The value to remove the the possible values of the SudokuCell
	 * @return Whether or not the value was successfully removed from the possible values.
	 */
	public boolean removePossibleValue(int r, int c, Integer value)
	{
		return changePossibleValue(r, c, value, SudokuCell::removePossibleValue);
	}
	
	private boolean changePossibleValue(int r, int c, Integer value, BiFunction<SudokuCell, Integer, Boolean> changePossibleValueFunction)
	{
		return changePossibleValueFunction.apply(getSudokuCell(r, c), value);
	}

	public int getGroupNumber(int r, int c)
	{
		validateCoords(r, c);

		return (r / 3) * 3 + c / 3;
	}

	public int getRowNumber(int r, int c)
	{
		validateCoords(r, c);

		return r;
	}

	public int getColNumber(int r, int c)
	{
		validateCoords(r, c);

		return c;
	}

	public Set<Integer> getUnusedValuesForGroup(int groupNumber)
	{
		validateCoord(groupNumber);

		int startingRow = 3 * (groupNumber / 3);
		int startingCol = 3 * (groupNumber % 3);
		Set<Integer> groupValues = IntStream.range(startingRow, startingRow + 3)
			.mapToObj(r -> IntStream.range(startingCol, startingCol + 3)
				.mapToObj(c -> board[r][c]))
			.flatMap(Function.identity())
			.filter(sudokuCell -> !sudokuCell.isEmpty())
			.map(SudokuCell::getValue)
			.collect(Collectors.toSet());

		return getRemainingValues(groupValues);
	}

	public Set<Integer> getUnusedValuesForRow(int rowNumber)
	{
		validateCoord(rowNumber);

		Set<Integer> rowValues = Arrays.stream(board[rowNumber])
			.filter(sudokuCell -> !sudokuCell.isEmpty())
			.map(SudokuCell::getValue)
			.collect(Collectors.toSet());

		return getRemainingValues(rowValues);
	}

	public Set<Integer> getUnusedValuesForCol(int colNumber)
	{
		validateCoord(colNumber);

		Set<Integer> colValues = Arrays.stream(board)
			.map(row -> row[colNumber])
			.filter(sudokuCell -> !sudokuCell.isEmpty())
			.map(SudokuCell::getValue)
			.collect(Collectors.toSet());

		return getRemainingValues(colValues);
	}

	public boolean isSolved()
	{
		return IntStream.range(0, 9)
			.allMatch(i -> getUnusedValuesForGroup(i).isEmpty()
				&& getUnusedValuesForRow(i).isEmpty()
				&& getUnusedValuesForCol(i).isEmpty());
	}

	private static Set<Integer> getRemainingValues(Set<Integer> sectionValues)
	{
		return SudokuCell.LEGAL_CELL_VALUES.stream()
			.filter(i -> !sectionValues.contains(i))
			.collect(Collectors.toSet());
	}

	private void validateCoords(int... coordinates) throws IllegalArgumentException
	{
		IntStream.of(coordinates)
			.forEach(this::validateCoord);
	}

	private void validateCoord(int coordinate) throws IllegalArgumentException
	{
		if (coordinate < 0 || coordinate >= 9)
		{
			throw new IllegalArgumentException(
				"Invalid coordinate: " + coordinate);
		}
	}
}
