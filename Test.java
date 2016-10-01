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


// http://www.learn-english-today.com/idioms/idioms_alphalistsA-Z.html
public class Test
{
	interface MyRunnable
	{
		int run();
	}

	boolean done;

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
		Solver solver = new Solver(board, vocab);
		long start = System.currentTimeMillis();

		Thread t = new Thread(new Runnable()
		{
			public void run()
			{
				solver.solve();
				done = true;
			}
		});
		t.start();

		while (!done) {
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}

			Board snap = new Board(board);
			snap.dump();
		}

		System.out.printf("done (%.04fs)\n",
			(System.currentTimeMillis() - start) / 1000f);
	}

	void run()
	{
		Vocab vocab = new Vocab();

		try {
			System.out.printf("Read %d unique words\n",
				vocab.scanFiles("vocab"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// duration
		// timeIt(() -> {
		// 	return vocab.options("         ".toCharArray()).length;
		// }, "options()", 100, false);

		// solving
		solveCrossword(vocab, new String[] {
			// "JMBARRIE*PSYCHO",
			// "IBELIEVE*OCELOT",
			// "BALLPEEN*THWART",
			// "**IMPLY*BLU*YAO",
			// "HOKIES*FRIST***",
			// "ONEND*WEAKSAUCE",
			// "TETE*CHICKENPOX",
			// "DOH*PRANKED*FOP",
			// "ONAPLATTER*JOKE",
			// "GETSUPSET*RARIN",
			// "***ASSAD*DANGED",
			// "AHH*SHY*HADER**",
			// "COOLIO*SAMADAMS",
			// "MAYIGO*THEROBOT",
			// "EXTENT*PASSESON",

			"JMBARRIE*PSYCHO",
			"IBE IEVE*OCELOT",
			"        *THWART",
			"**     *   *YAO",
			"HOK ES*FRIST***",
			"ONE D*         ",
			"TET *C ICKENP  ",
			"DOH*       *F  ",
			"ONAPLA TER*JO  ",
			"GETSUP ET*RAR  ",
			"***ASS D*DANG  ",
			"AHH*SH *HADER**",
			"COOLIO*        ",
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
