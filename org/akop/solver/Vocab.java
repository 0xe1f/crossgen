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
	final Map<Integer, Node> tries;
	final List<char[]> words;
	final int minLength;

	public Vocab()
	{
		this(3);
	}

	public Vocab(int minLength)
	{
		this.minLength = minLength;
		tries = new HashMap<>();
		words = new ArrayList<>();
	}

	public int scanFiles(String path)
			throws IOException
	{
		int count = 0;
		File dir = new File(path);
		for (File file: dir.listFiles()) {
			count += scanFile(file.toString());
		}

		return count;
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

				Node node = tries.get(word.length);
				if (node == null) {
					tries.put(word.length, node = new Node());
				}
				for (char ch: word) {
					node = node.append(ch);
				}
				if (node.wordIndex == -1) {
					count++;
					node.wordIndex = words.size();
					words.add(word);
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

	public int options(List<Integer> list, char[] word)
	{
		return options(list, tries.get(word.length), word, 0);
	}

	public char[] word(int index)
	{
		return words.get(index);
	}

	int options(List<Integer> list, Node n, char[] word, int index)
	{
		if (index >= word.length) {
			if (list != null) {
				list.add(n.wordIndex);
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

	static class Node
	{
		final Map<Character, Node> children;
		int wordIndex;

		Node()
		{
			children = new HashMap<>();
			wordIndex = -1;
		}

		Node append(char ch)
		{
			Node k = children.get(ch);
			if (k == null) {
				children.put(ch, k = new Node());
			}
			return k;
		}
	}
}
