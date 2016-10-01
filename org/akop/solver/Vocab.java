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
	private final Map<Integer, INode> tries;
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

				INode node = tries.get(word.length);
				if (node == null) {
					tries.put(word.length, node = new INode());
				}
				if (node.append(word, words.size())) {
					count++;
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

	public int[] options(char[] word)
	{
		List<Leaf> list = new ArrayList<>();
		options(list, tries.get(word.length), word, 0);

		int index = 0;
		int[] options = new int[list.size()];
		for (Leaf leaf: list) {
			options[index++] = leaf.wordIndex;
		}

		return options;
	}

	public char[] word(int index)
	{
		return words.get(index);
	}

	int options(List<Leaf> list, char[] chars)
	{
		return options(list, tries.get(chars.length), chars, 0);
	}

	int options(List<Leaf> list, Node n, char[] word, int index)
	{
		int count = 0;
		if (n instanceof Leaf) {
			Leaf l = (Leaf) n;
			if (!l.taken) {
				if (list != null) {
					list.add(l);
				}
				count++;
			}
		} else if (n instanceof INode) {
			INode in = (INode) n;
			char ch = word[index];
			int nextIndex = index + 1;

			if (ch != ' ') {
				Node k = in.children.get(ch);
				if (k != null) {
					count += options(list, k, word, nextIndex);
				}
			} else {
				for (Map.Entry<Character, Node> entry: in.children.entrySet()) {
					word[index] = entry.getKey();
					count += options(list, entry.getValue(), word, nextIndex);
				}
				word[index] = ch;
			}
		}

		return count;
	}

	static abstract class Node
	{
	}

	private static class INode
			extends Node
	{
		final Map<Character, Node> children;

		INode()
		{
			children = new HashMap<>();
		}

		boolean append(char[] chars, int newIndex)
		{
			INode node = this;

			int end = chars.length - 1;
			for (int i = 0; i < end; i++) {
				node = node.appendINode(chars[i]);
			}

			return node.appendLeaf(chars[end], newIndex);
		}

		private INode appendINode(char ch)
		{
			INode in = (INode) children.get(ch);
			if (in == null) {
				children.put(ch, in = new INode());
			}
			return in;
		}

		private boolean appendLeaf(char ch, int newIndex)
		{
			Leaf leaf = (Leaf) children.get(ch);
			if (leaf == null) {
				children.put(ch, new Leaf(newIndex));
			}

			return leaf == null;
		}
	}

	static class Leaf
			extends Node
	{
		final int wordIndex;
		boolean taken;

		Leaf(int wordIndex)
		{
			this.wordIndex = wordIndex;
		}
	}
}
