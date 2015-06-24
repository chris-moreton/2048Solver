package com.netsensia.twentyfortyeight;

public class TwentyFortyEight {

	public static final int MAX_DEPTH = 11;
	
	public static final int RUNS = 12;
    public static final int POWER_MAX = 32;
	
    public static final int[] DEPTH_RUNS = {
    	0,
    	100000,
    	25000,
    	10000,
    	5000,
    	2500,
    	1000,
    	500,
    	250,
    	100,
    	50,
    	12,
    };
    
	public static void main(String args[]) {

		// We don't want to leave it to the Board class
		// to create the static items on first use
		// as it would not be thread safe.
		Board.initStaticItems();
		
		long start = System.currentTimeMillis();
		
		int numCores = Runtime.getRuntime().availableProcessors();
		System.out.println("Number of available cores: " + numCores);
		
		int numThreads = numCores;
		
		for (int depth=1; depth<=MAX_DEPTH; depth++) {
		
			int gamesPerThread = (int)(DEPTH_RUNS[depth] / numThreads);
			int mod = DEPTH_RUNS[depth] % numThreads;
			int adjustedRuns = Math.max(numThreads, DEPTH_RUNS[depth] - mod);
			
			ResultsLogger resultsLogger = new ResultsLogger(adjustedRuns, depth, numThreads);
			resultsLogger.setPrintSummaryAfterNGames(adjustedRuns);
			resultsLogger.setIsCsvOnly(true);
			
			Thread[] threadGroup = new Thread[numThreads];
			
			for (int threadNum=0; threadNum<numThreads; threadNum++) {
				GameRunner gameRunner = new GameRunner(resultsLogger, depth, gamesPerThread);
				threadGroup[threadNum] = new Thread(gameRunner);
				threadGroup[threadNum].start();
			}
			
			// wait for these threads to finish before starting next depth
			for (int threadNum=0; threadNum<numThreads; threadNum++) {
				try {
					threadGroup[threadNum].join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		long millis = System.currentTimeMillis() - start;
		int minutes = (int)millis / 60000;
		int seconds = (int)millis % 60000 / 1000;
		
		System.out.println("Time taken: " + minutes + " minutes, " + seconds + " seconds");
		
	}
}
