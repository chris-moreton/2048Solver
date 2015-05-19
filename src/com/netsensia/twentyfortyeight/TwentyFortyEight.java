package com.netsensia.twentyfortyeight;

public class TwentyFortyEight {

	/**
	 * Average score for random moves = 834
	 * Average score for selecting best score each time = 2857
	 */
	
	public static final int RUNS = 100;
	
	public static void main(String args[]) {
		
		Board board = null;
		int totalScore = 0;
		int highScore = 0;
		int highestTileValue = 0;
		
		long start = System.currentTimeMillis();
		
		for (int i=0; i<RUNS; i++) {
			
			System.out.println("======================================================");
			System.out.println("Game: " + (i+1));
			
			try {
				board = playGame();
				System.out.println(board);
				int score = board.getScore();
				if (score > highScore) {
					highScore = score;
				}
				
				totalScore += score;
				int t = board.getHighestTileValue();
				if (t > highestTileValue) {
					highestTileValue = t;
				}
			} catch (Exception e) {
				System.out.println(e);
				System.exit(1);
			}
			
			System.out.println("Time: " + (System.currentTimeMillis() - start) + ", Average score = " + (totalScore / RUNS) + ", Highest score: " + highScore + ", Highest tile value: " + highestTileValue);
		}
		
	}
	
	public static Board playGame() throws Exception {
		Board board = new Board();
		board.setRandomStartPosition();
		
		System.out.println("Starting position:");
		System.out.println(board);
		
		Search search = new Search();
		
		while (!board.isGameOver()) {
			int direction;
			
			search.setMode(Search.SEARCH);
			search.setDepth(2);
			direction = search.getBestMove(board);
			
			if (board.isValidMove(direction)) {
				board.makeMove(direction, true);
				board.placeRandomPiece();
			} else {
				System.out.println("Illegal move: " + direction);
				System.out.println(board);
				System.exit(1);
			}
		}
		
		return (Board)board.clone();
	}
	
	public static Board getTestBoard() {
		Board board = new Board();
		
		int[] testBoard = {
				2,4,4,8,
				0,2,8,8,
				2,2,2,8,
				0,8,8,4,
		};
		
		board.setBoard(testBoard);
		
		return board;
	}
	
	public static void test() {
		
		Board board = getTestBoard();
		System.out.println("Start");
		System.out.println(board);
		
		board.makeMove(Board.DOWN, true);
		System.out.println("Slid down");
		System.out.println(board);
		
		board.makeMove(Board.LEFT, true);
		System.out.println("Slid left");
		System.out.println(board);
		
		board.makeMove(Board.RIGHT, true);
		System.out.println("Slid right");
		System.out.println(board);
		
		board.makeMove(Board.UP, true);
		System.out.println("Slid up");
		System.out.println(board);
		
		System.out.println("Valid moves:");
		if (board.isValidMove(Board.UP)) System.out.println("Up");
		if (board.isValidMove(Board.DOWN)) System.out.println("Down");
		if (board.isValidMove(Board.LEFT)) System.out.println("Left");
		if (board.isValidMove(Board.RIGHT)) System.out.println("Right");
		
		System.exit(0);
	}
	
}
