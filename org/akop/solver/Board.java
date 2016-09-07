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
	final List<Word> words;

	public Board(int width, int height)
	{
		this.width = width;
		this.height = height;
		board = new char[height][width];
		words = new ArrayList<>();
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

		board.scanWords();

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

	public void dump()
	{
		for (char[] row: board) {
			for (char ch: row) {
				if (ch != '\0') {
					System.out.print("[" + ch + "]");
				} else {
					System.out.print("   ");
				}
			}
			System.out.println("");
		}
	}

	public static void dump(char[] charray)
	{
		String ret = "";
		for (char ch: charray) {
			ret += "[";
			ret += ch;
			ret += "]";
		}

		ret += " (" + charray.length + ")";

		System.out.println(ret);
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
		if (dir == Word.DIR_ACROSS) {
			for (int j = col; j < width && board[row][j] != '\0'; j++) {
				length++;
			}
		} else {
			for (int i = row; i < height && board[i][col] != '\0'; i++) {
				length++;
			}
		}

		return new Word(dir, number, row, col, length);
	}

	private void scanWords()
	{
		words.clear();

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
	}
}
