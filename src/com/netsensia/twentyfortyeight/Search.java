package com.netsensia.twentyfortyeight;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class Search {
	
	public static final int RANDOM_MOVES_TO_PLAY = 4;
	
	public static final int RANDOM = 0;
	public static final int SCORE = 1;
	public static final int SEARCH = 2;
	
	private boolean evaluateBlankSpaces = true;
	
	Random r = new Random();
	
	int depth = 1;

	int mode = RANDOM;
	
	private final int MAX_TILE_VALUE = 16384;
	
	private int squareLookup[] = new int[Board.COLS];
	private int log2Lookup[] = new int[MAX_TILE_VALUE+1];
	
	public Search() {
		for (int x=0; x<Board.COLS; x++) {
			squareLookup[x] = (x+1) * (x+1);
		}
		
		for (int i=2; i<=MAX_TILE_VALUE; i++) {
			log2Lookup[i] = (int)(Math.log(i) / Math.log(2));
		}
		
		// log2Lookup[2048] = 11
		// log2Lookup[4096] = 12
	}
	
	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public int getDepth() {
		return depth;
	}
	
	public void setDepth(int depth) {
		this.depth = depth;
	}
	
	public boolean isEvaluateBlankSpaces() {
		return evaluateBlankSpaces;
	}

	public void setEvaluateBlankSpaces(boolean evaluateBlankSpaces) {
		this.evaluateBlankSpaces = evaluateBlankSpaces;
	}

	public int score(Board board, int move) {
		int bestScore = 0;
		
		try {
			Board newBoard = (Board) board.clone();
			
			newBoard.makeMove(move, true);
			int score = newBoard.getScore();
			if (score > bestScore) {
				bestScore = score;
			}
			
		} catch (CloneNotSupportedException e) {
			
		}
		
		return bestScore;
	}
	
	public ArrayList<SolverMove> getSolverMoves(Board board) {
		ArrayList<SolverMove> legalMoves = new ArrayList<SolverMove>();
		
		for (int i=Board.UP; i<=Board.RIGHT; i++) {
			if (board.isValidMove(i)) {
					
				SolverMove solverMove = new SolverMove(i);
				legalMoves.add(solverMove);
				
			}
		}

		return legalMoves;
	}
	
	public ArrayList<SolverMove> getOrderedSolverMoves(Board board) {
		ArrayList<SolverMove> legalMoves = new ArrayList<SolverMove>();
		
		Board newBoard;
		
		for (int i=Board.UP; i<=Board.RIGHT; i++) {
			if (board.isValidMove(i)) {
				
				try {
					newBoard = (Board)board.clone();
					newBoard.makeMove(i, false);
					
					SolverMove solverMove = new SolverMove(i);
					solverMove.setScore(evaluate(newBoard));
					legalMoves.add(solverMove);
					
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
				
			}
		}

		Comparator<SolverMove> moveComp = (SolverMove m1, SolverMove m2) -> (int)(m1.getScore() > m2.getScore() ? -1 : 1);
		Collections.sort(legalMoves, moveComp);

		return legalMoves;
	}
	
	private void addBlockerMove(ArrayList<BlockerMove> blockerMoves, Board board, int x, int y, int piece) {
		
		BlockerMove blockerMove = new BlockerMove(x, y, piece);
		// Slightly favour moves further to the right of the board 
		blockerMove.setScore(x + (Math.random() * x));
		blockerMoves.add(blockerMove);	
	}
	
	public ArrayList<BlockerMove> getOrderedBlockerMoves(Board board) {
		ArrayList<BlockerMove> blockerMoves = new ArrayList<BlockerMove>();
		
		for (int x=0; x<Board.COLS; x++) {
			for (int y=0; y<Board.ROWS; y++) {
				if (board.getSquare(x, y) == 0) {
					addBlockerMove(blockerMoves, board, x, y, 2);
					addBlockerMove(blockerMoves, board, x, y, 4);
				}
			}
		}
		
		Comparator<BlockerMove> moveComp = (BlockerMove m1, BlockerMove m2) -> (int)(m1.getScore() > m2.getScore() ? -1 : 1);
		Collections.sort(blockerMoves, moveComp);

		return blockerMoves;
	}
	
	public SolverMove getRandomMove(Board board) {
		ArrayList<SolverMove> legalMoves = getOrderedSolverMoves(board);
		
		return legalMoves.get(r.nextInt(legalMoves.size()));
	}
	
	public SolverMove getMoveBasedOnImmediateScore(Board board) {
		SolverMove bestMove = new SolverMove(-1);
		int bestScore = -1;
		
		ArrayList<SolverMove> legalMoves = getOrderedSolverMoves(board);
		
		for (SolverMove move : legalMoves) {
			int score = score(board, move.getDirection());
			if (score > bestScore) {
				bestScore = score;
				bestMove = move;
			}
		}
		
		return bestMove;
		
	}
	
	public SolverMove getBestMove(Board board) throws Exception {
		
		switch (getMode()) {
			case Search.RANDOM: return getRandomMove(board);
			case Search.SCORE: return getMoveBasedOnImmediateScore(board);
			case Search.SEARCH: return getMoveFromSearch(board);
			default:
				return getRandomMove(board);
		}
	}
	
	public int evaluate(Board board) {
		int score = board.getScore();
		double weight = 0.0;

		for (int x=0; x<Board.COLS; x++) {
			
			for (int y=0; y<Board.ROWS; y++) {
				
				int piece = board.getSquare(x, y);
				
				if (piece > 0) {
					
					// Get large numbers to the right-hand side
					weight = squareLookup[x];
					
					boolean ordered = true;
					
					// Are all pieces below this one of a higher value?
					for (int i=y+1; ordered && i<Board.ROWS; i++) {
						int neighbourPiece = board.getSquare(x, i);
						
						if (piece > neighbourPiece) {
							ordered = false;
						}
						
					}
					
					boolean closeValues = true;
					
					if (x < Board.COLS - 1) {
						int neighbourPiece = board.getSquare(x + 1, y);
				
						if (neighbourPiece > 0) {
	
							if (Math.abs(log2Lookup[piece] - log2Lookup[neighbourPiece]) > 1) {
								closeValues = false;
							}
						}
					}
					
					// If so, give this piece a bonus, it has no need to move down 
					if (ordered) {
						weight *= 1.25;
					}
					
					if (closeValues) {
						weight *= 1.25;
					}
					
					score += (int)(piece * weight);
				}
			}
		}
		
		int lastPiece;
		int rowTouchers = 0;
		int columnTouchers = 0;
		
		for (int x=0; x<Board.COLS; x++) {
			lastPiece = 0;
			for (int y=0; y<Board.ROWS; y++) {
				int piece = board.getSquare(x,y);
				if (piece > 0) {
					if (piece == lastPiece) {
						columnTouchers += piece;
						lastPiece = 0;
					} else {
						lastPiece = piece;
					}
				}
			}
		}
		
		for (int y=0; y<Board.ROWS; y++) {
			lastPiece = 0;
			for (int x=0; x<Board.COLS; x++) {
				int piece = board.getSquare(x,y);
				if (piece > 0) {
					if (piece == lastPiece) {
						rowTouchers += piece;
						lastPiece = 0;
					} else {
						lastPiece = piece;
					}
				}
			}
		}
		
		// Bonus for having tiles of the same value next to each other
		score += (Math.max(rowTouchers,  columnTouchers));
		
		if (rowTouchers + columnTouchers == 0 && board.countBlankSpaces() == 0) {
			// Game over
			score *= 0.8;
		}
		
		ArrayList<SolverMove> legalMoves = getSolverMoves(board);
		if (legalMoves.size() == 1) {
			if (legalMoves.get(0).getDirection() == Board.LEFT) {
				score *= 0.75;
			}
		}
		
		return score; 
	   		
	}
	
	public int negamax(Board board, final int depth, int low, int high, int mover, StringBuilder moveString) throws Exception {
		
		if (depth == 0) {
			return mover * evaluate(board);
		}

		int bestScore = Integer.MIN_VALUE;
		
		if (mover == 1) {
			
			ArrayList<SolverMove> legalMoves = getOrderedSolverMoves(board);
			
			if (legalMoves.size() == 0) {
				return mover * evaluate(board);
			}
			
			Board newBoard;
			
			for (SolverMove move : legalMoves) {
				
				try {
					newBoard = (Board)board.clone();
					newBoard.makeMove(move.getDirection(), true);
					
					int score = -negamax(newBoard, depth-1, -high, -low, -1, null);
					
					if (score > bestScore) {
						bestScore = score;
						if (moveString != null) {
							moveString.setLength(0);
							moveString.append(move);
						}
					}
					
					low = Math.max(low, score);
					
					if (low >= high) {
						return bestScore;
					}
					
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
			}
		} else {
			
			ArrayList<BlockerMove> legalMoves = getOrderedBlockerMoves(board);
			
			if (legalMoves.size() == 0) {
				return mover * evaluate(board);
			}

			int count = 0;
			int totalScore = 0;

			Board newBoard;
			
			for (BlockerMove move : legalMoves) {
			
				try {
					count ++;
					newBoard = (Board)board.clone();
					newBoard.place(move.getX(), move.getY(), move.getPiece());
					
					totalScore += -negamax(newBoard, depth-1, -high, -low, 1, null);
					
					if (count == (RANDOM_MOVES_TO_PLAY + 1)) {
						return (int)(totalScore / (RANDOM_MOVES_TO_PLAY + 1));
					}
	
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
			}
			
			return (int)(totalScore / count);
		}
		
		if (bestScore == Integer.MIN_VALUE) {
			throw new Exception("Best score is not set for " + (mover == 1 ? "Solver" : "Blocker") + "\n" + board);
		}
		
		return bestScore;
		
	}
	
	public SolverMove getMoveFromSearch(Board board) throws Exception {

		ArrayList<SolverMove> legalMoves = getOrderedSolverMoves(board);
		
		if (legalMoves.size() == 0) {
			throw new Exception("No legal moves for position\n " + board);
		} else
			if (legalMoves.size() == 1) {
				return legalMoves.get(0);
			}
		
		StringBuilder move = new StringBuilder();
		
		negamax(board, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, move);
		
		switch (move.charAt(1)) {
		case 'U' : return new SolverMove(Board.UP);
		case 'D' : return new SolverMove(Board.DOWN);
		case 'L' : return new SolverMove(Board.LEFT);
		case 'R' : return new SolverMove(Board.RIGHT);
		default:
			throw new Exception("Unknown move in " + move);
		}
		
	}
	
}
