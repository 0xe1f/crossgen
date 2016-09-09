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
	private Vocab vocab;
	private boolean done;

	public Solver(Vocab vocab)
	{
		this.vocab = vocab;
	}

	public boolean solve(Board board)
	{
		return solve(board, sortByCompletion(board), 0);
	}

	private List<Board.Word> sortByCompletion(Board board)
	{
		List<Board.Word> words = board.words();
		Collections.sort(words, new Comparator<Board.Word>()
		{
			public int compare(Board.Word one, Board.Word two)
			{
				return (int) ((two.complete - one.complete) * 100f);
			}
		});

		return words;
	}

	private boolean solve(Board board, List<Board.Word> words, int wordIndex)
	{
		if (wordIndex >= words.size()) {
			return true; // Solved
		}

		int nextIndex = wordIndex + 1;
		Board.Word word = words.get(wordIndex);
		if (word.isSolved) {
			return solve(board, words, nextIndex);
		}

		char[] chars = board.squares(word);
		List<Integer> options = new ArrayList<>();
		vocab.options(options, vocab.tries.get(word.len), chars, 0);

		for (int option: options) {
			board.putSquares(word, vocab.words.get(option));
			if (solve(board, words, nextIndex)) {
				return true;
			}
		}

		// Failed, so restore the board
		board.putSquares(word, chars);
		return false;
	}
}
