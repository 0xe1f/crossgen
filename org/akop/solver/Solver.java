// Copyright (c) 2016 Akop Karapetyan
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

package org.akop.solver;

import java.util.*;


public class Solver
{
	private final Board board;
	private final Vocab vocab;
	private boolean done;
	private final List<Board.Word> words;
	private final Board.Word[][] across;
	private final Board.Word[][] down;

	public Solver(Board board, Vocab vocab)
	{
		this.vocab = vocab;
		this.board = board;

		words = board.words();

		across = new Board.Word[board.height][board.width];
		down = new Board.Word[board.height][board.width];

		for (Board.Word word: words) {
			if (word.dir == Board.Word.DIR_ACROSS) {
				for (int j = word.col, k = 0; k < word.len; j++, k++) {
					across[word.row][j] = word;
				}
			} else {
				for (int i = word.row, k = 0; k < word.len; i++, k++) {
					down[i][word.col] = word;
				}
			}
		}
	}

	private List<Board.Word> sortedByCompletion()
	{
		List<Board.Word> sortedWords = new ArrayList<>(words);
		Collections.sort(words, new Comparator<Board.Word>()
		{
			public int compare(Board.Word one, Board.Word two)
			{
				return (int) ((two.complete - one.complete) * 100f);
			}
		});

		return sortedWords;
	}

	private List<Board.Word> sortedByDirection()
	{
		List<Board.Word> sortedWords = new ArrayList<>(words);
		Collections.sort(words, new Comparator<Board.Word>()
		{
			public int compare(Board.Word one, Board.Word two)
			{
				return one.dir - two.dir;
			}
		});

		return sortedWords;
	}

	private List<Board.Word> sortedByProximity()
	{
		Queue<Board.Word> q = new ArrayDeque<>();
		Set<Board.Word> v = new HashSet<>();
		List<Board.Word> list = new ArrayList<>();

		for (Board.Word w: words) {
			if (!w.isSolved && !v.contains(w)) {
				q.add(w);
				while (!q.isEmpty()) {
					Board.Word qw = q.remove();
					if (!v.contains(qw)) {
						v.add(qw);
						list.add(qw);

						if (qw.dir == Board.Word.DIR_ACROSS) {
							for (int j = qw.col, k = 0; k < qw.len; j++, k++) {
								if (board.board[qw.row][j] == ' ') {
									q.add(down[qw.row][j]);
								}
							}
						} else {
							for (int i = qw.row, k = 0; k < qw.len; i++, k++) {
								if (board.board[i][qw.col] == ' ') {
									q.add(across[i][qw.col]);
								}
							}
						}
					}
				}
			}
		}

		return list;
	}

	private boolean canPlace(Board.Word word, char[] chars)
	{
		int row = word.row;
		int col = word.col;

		if (word.dir == Board.Word.DIR_ACROSS) {
			for (int k = 0; k < word.len; col++, k++) {
				if (board.board[row][col] == ' ') {
					Board.Word x = down[row][col];
					if (x != null) {
						char[] xchars = board.squares(x);
						xchars[row - x.row] = chars[k];
						if (vocab.options(null, xchars) <= 0) {
							return false;
						}
					}
				}
			}
		} else {
			for (int k = 0; k < word.len; row++, k++) {
				if (board.board[row][col] == ' ') {
					Board.Word x = across[row][col];
					if (x != null) {
						char[] xchars = board.squares(x);
						xchars[col - x.col] = chars[k];
						if (vocab.options(null, xchars) <= 0) {
							return false;
						}
					}
				}
			}
		}

		return true;
	}

	public boolean solve()
	{
		 return solve(sortedByProximity(), 0);
	}

	private boolean solve(List<Board.Word> unsolved, int index)
	{
		if (index >= unsolved.size()) {
			return true; // Solved
		}

		int nextIndex = index + 1;
		Board.Word word = unsolved.get(index);
		if (word.isSolved) {
			return solve(unsolved, nextIndex);
		}

		char[] chars = board.squares(word);
		List<Vocab.Leaf> options = new ArrayList<>();
		vocab.options(options, chars);

		for (Vocab.Leaf option: options) {
			char[] optWord = vocab.words.get(option.wordIndex);
			if (canPlace(word, optWord)) {
				option.taken = true;
				board.putSquares(word, optWord);

				boolean solved = solve(unsolved, nextIndex);
				option.taken = false;

				if (solved) {
					return true;
				}
			}
		}

		// Failed, so restore the board
		board.putSquares(word, chars);
		return false;
	}
}
