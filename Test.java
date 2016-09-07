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

import java.io.*;
import java.util.*;

import org.akop.solver.*;


public class Test
{
	interface MyRunnable
	{
		int run();
	}

	void timeIt(MyRunnable r, String label, int count, boolean aggregate)
	{
		long start = System.currentTimeMillis();
		int sum = 0;

		for (int i = 0; i < count; i++) {
			if (!aggregate) {
				sum = 0;
			}
			sum += r.run();
		}

		System.out.printf("%s: %d sum (%d times, %.04fs)\n", label,
			sum, count, (System.currentTimeMillis() - start) / 1000f);
	}

	void solveCrossword(Vocab vocab, String[] boardString)
	{
		Board board = Board.fromStrings(boardString);
		board.clear();
		board.dump();

		Solver solver = new Solver(vocab, board);
		timeIt(() -> {
			boolean complete = solver.solve();
			System.out.println("Complete? " + complete);
			return 0;
		}, "compete()", 1, false);

		board.dump();
	}

	// void runOptions(Vocab vocab, String str)
	// {
	// 	char[] charray = str.toCharArray();
	// 	List<char[]> opts = new ArrayList<>();
	// 	timeIt(() -> {
	// 		opts.clear();
	// 		return vocab.options(opts, vocab.tries.get(charray.length), charray, 0);
	// 	}, "options()", 100, false);
	// }

	void run()
	{
		Vocab vocab = new Vocab();
		try {
			vocab.scanFile("vocab/web2.txt");
			vocab.scanFile("vocab/web2a.txt");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// runOptions(vocab, "         ");

		solveCrossword(vocab, new String[] {
			"JMBARRIE*PSYCHO",
			"IBELIEVE*OCELOT",
			"BALLPEEN*THWART",
			"**IMPLY*BLU*YAO",
			"HOKIES*FRIST***",
			"ONEND*WEAKSAUCE",
			"TETE*CHICKENPOX",
			"DOH*PRANKED*FOP",
			"ONAPLATTER*JOKE",
			"GETSUPSET*RARIN",
			"***ASSAD*DANGED",
			"AHH*SHY*HADER**",
			"COOLIO*SAMADAMS",
			"MAYIGO*THEROBOT",
			"EXTENT*PASSESON",
		});
	}	

	public static void main(String args[])
	{
		Test main = new Test();
		main.run();
	}
}
