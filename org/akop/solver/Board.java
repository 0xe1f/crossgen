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


public class Board
{
	final int width;
	final int height;
	final char[][] board;

	private Board(int width, int height)
	{
		this.width = width;
		this.height = height;
		board = new char[height][width];
	}

	public Board(Board orig)
	{
		// Not thread-safe
		this(orig.width, orig.height);
		for (int i = 0; i < height; i++) {
			System.arraycopy(orig.board, 0, board, 0, width);
		}
	}

	public static Board fromStrings(String[] strings)
	{
		int height = strings.length;
		if (height < 1) {
			throw new IllegalArgumentException("Zero height");
		}
		int width = strings[0].length();
		if (width < 1) {
			throw new IllegalArgumentException("Zero width");
		}

		Board board = new Board(width, height);
		for (int i = 0; i < height; i++) {
			char[] row = strings[i].toCharArray();
			if (row.length != width) {
				throw new IllegalArgumentException("Row " + i + " is uneven");
			}
			for (int j = 0; j < width; j++) {
				char ch = strings[i].charAt(j);
				if (ch == '*') {
					ch = '\0';
				}
				board.board[i][j] = ch;
			}
		}

		return board;
	}

	char[] squares(Word word)
	{
		char[] squares = new char[word.len];
		if (word.dir == Word.DIR_ACROSS) {
			System.arraycopy(board[word.row], word.col, squares, 0, word.len);
		} else {
			for (int i = word.row, k = 0, n = word.row + word.len; i < n; i++, k++) {
				squares[k] = board[i][word.col];
			}
		}

		return squares;
	}

	void putSquares(Word word, char[] chars)
	{
		if (chars.length != word.len) {
			throw new IllegalArgumentException("Length mismatch");
		}

		if (word.dir == Word.DIR_ACROSS) {
			System.arraycopy(chars, 0, board[word.row], word.col, word.len);
		} else {
			for (int i = word.row, k = 0, n = word.row + word.len; i < n; i++, k++) {
				board[i][word.col] = chars[k];
			}
		}
	}

	public void clear()
	{
		for (char[] row: board) {
			for (int j = 0; j < row.length; j++) {
				if (row[j] != '\0') {
					row[j] = ' ';
				}
			}
		}
	}

	private Word newWord(int dir, int number, int row, int col)
	{
		int length = 0;
		int completed = 0;

		if (dir == Word.DIR_ACROSS) {
			for (int j = col; j < width && board[row][j] != '\0'; j++) {
				length++;
				if (board[row][j] != ' ') {
					completed++;
				}
			}
		} else {
			for (int i = row; i < height && board[i][col] != '\0'; i++) {
				length++;
				if (board[i][col] != ' ') {
					completed++;
				}
			}
		}

		return new Word(dir, number, row, col, length,
			(float) completed / length);
	}

	List<Word> words()
	{
		List<Word> words = new ArrayList<>();
		for (int i = 0, count = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				if (board[i][j] == '\0') {
					continue;
				}
				Word word = null;
				if (j == 0 || board[i][j - 1] == '\0' && (j + 1 < width && board[i][j + 1] != '\0')) {
					word = newWord(Word.DIR_ACROSS, ++count, i, j);
					if (word.len >= 3) {
						words.add(word);
					}
				}
				if (i == 0 || board[i - 1][j] == '\0' && (i + 1 < height && board[i + 1][j] != '\0')) {
					if (word == null) {
						count++;
					}

					word = newWord(Word.DIR_DOWN, count, i, j);
					if (word.len >= 3) {
						words.add(word);
					}
				}
			}
		}

		return words;
	}

	public void dump()
	{
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				char ch = board[i][j];
				if (ch != '\0') {
					System.out.print("[" + ch + "]");
				} else {
					System.out.print("   ");
				}
			}
			System.out.println("");
		}
	}

	public void dumpLayout()
	{
		dumpLayout(words());
	}

	void dumpLayout(List<Word> words)
	{
		StringBuilder sb = new StringBuilder();
		for (Word word: words) {
			sb.append(String.format("%2d%s. (%d,%d) '%s' (%d, %.02f)%s\n", word.number,
				word.dir == Word.DIR_ACROSS ? "A" : "D", word.row, word.col,
				new String(squares(word)), word.len, word.complete * 100f,
				word.isSolved ? " solved" : ""));
		}
		System.out.println(sb);
	}

	static class Word
	{
		static final int DIR_ACROSS = 0;
		static final int DIR_DOWN   = 1;

		final int number;
		final int row;
		final int col;
		final int dir;
		final int len;
		final boolean isSolved;
		final float complete;

		private Word(int dir, int number, int row, int col, int len,
			float complete)
		{
			if (dir != DIR_ACROSS && dir != DIR_DOWN) {
				throw new IllegalArgumentException("Direction not valid");
			}

			this.dir = dir;
			this.number = number;
			this.row = row;
			this.col = col;
			this.len = len;
			this.isSolved = complete >= 1;
			this.complete = complete;
		}
	}
}
