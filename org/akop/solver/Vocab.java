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

import java.io.*;
import java.util.*;


public class Vocab
{
	Map<Integer, Node> tries;
	final int minLength;

	public Vocab()
	{
		this(3);
	}

	public Vocab(int minLength)
	{
		this.minLength = minLength;
		tries = new HashMap<>();
	}

	public int scanFile(String path)
			throws IOException
	{
		int count = 0;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(path));
			String line;
			while ((line = reader.readLine()) != null) {
				char[] word = sanitize(line);
				if (word == null) {
					continue;
				}

				Node n = tries.get(word.length);
				if (n == null) {
					tries.put(word.length, n = new Node());
				}
				for (char ch: word) {
					n = n.add(ch);
				}
			}
		} finally {
			if (reader != null) {
				try { reader.close(); }
				catch (IOException e2) { }
			}
		}

		return count;
	}
	
	private char[] sanitize(String word)
	{
		char[] charz = word.toCharArray();
		int delta = 0;
		for (int i = 0; i < charz.length; i++) {
			char ch = charz[i];
			if (ch >= 'A' && ch <= 'Z') {
				charz[i - delta] = ch;
			} else if (ch >= 'a' && ch <= 'z') {
				charz[i - delta] = (char) ('A' + (ch - 'a'));
			} else {
				delta++;
			}
		}

		int len = charz.length - delta;
		if (len < minLength) {
			return null;
		}

		char[] sanitized;
		if (delta > 0) {
			sanitized = new char[len];
			System.arraycopy(charz, 0, sanitized, 0, len);
		} else {
			sanitized = charz;
		}

		return sanitized;
	}

	int options(List<char[]> list, Node n, char[] word, int index)
	{
		if (index >= word.length) {
			if (list != null) {
				char[] copy = new char[word.length];
				System.arraycopy(word, 0, copy, 0, word.length);
				list.add(copy);
			}
			return 1;
		}

		int count = 0;
		if (n != null) {
			char ch = word[index];
			int nextIndex = index + 1;

			if (ch != ' ') {
				Node k = n.children.get(ch);
				if (k != null) {
					count += options(list, k, word, nextIndex);
				}
			} else {
				for (Map.Entry<Character, Node> entry: n.children.entrySet()) {
					word[index] = entry.getKey();
					count += options(list, entry.getValue(), word, nextIndex);
				}
				word[index] = ch;
			}
		}

		return count;
	}
}
