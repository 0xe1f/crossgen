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
	private Board board;

	public Solver(Vocab vocab, Board board)
	{
		this.vocab = vocab;
		this.board = board;
	}

	public boolean solve()
	{
		return solve(0);
	}

	private boolean solve(int wordIndex)
	{
		if (wordIndex >= board.words.size()) {
			return true; // Solved
		}

		Word word = board.words.get(wordIndex);
		char[] chars = board.squares(word);

		List<char[]> options = new ArrayList<>();
		vocab.options(options, vocab.tries.get(word.len), chars, 0);

		for (char[] option: options) {
			board.putSquares(word, option);
			if (solve(wordIndex + 1)) {
				return true;
			}
		}

		// Failed, so restore the board
		board.putSquares(word, chars);
		return false;
	}
}
