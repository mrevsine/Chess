package model;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author MahlerRevsine
 * 
 * Chess engine program. In this version, we attempt to retain as much information from one move to the next as possible.
 * 
 * Pieces are encoded as a 32-bit integer where the bottom 4 bits encode the type and color, the next 4 represent pins, 
 * and the upper 24 are the number of available moves in 3 bits for each of 8 directions.
 * 
 * Pieces are stored in an unchanging master list called pieces. We then have an array of integers named board that
 * contains in each index either a -1 for an empty square or an integer representing the index in the pieces array of 
 * that square's occupant. Squares are indexed with index 0 representing a1, index 2 as b2, and so on, with index 8 as a2 up to index 63 as h8.
 * board[3], for example, represents the index in "pieces" of the piece on d1. Piece info is also stored in the arrays 
 * wpI (white piece indices) and bpI (black piece indices). These arrays contain a list of remaining pieces of that 
 * color and their associated position in the board array. The first index in each array is the king of that color. 
 * wpI[0] is the location in board of the white king; the piece itself is found at pieces[board[wpI[0]]]. 
 * 
 * Some other information is stored as well to retain data move to move. wAttacks and bAttacks are arrays that contain
 * the number of attacks by each color on each square. A 2 in bAttacks[3] indicates that 2 black pieces are attacking the 
 * d1 square. This information is useful both in determining safe king moves and in evaluating positional advantages.
 * 
 * Moves are manually recorded for the start position, something that is fairly trivial to do. After each move, we update
 * all data fields affected. 1. Nf3 for example opens up squares for white at the start square (Rg1), limits squares for
 * white at the end square (f3 or f4), and changes the squares that white controls (g1, d4, e5, g5, and h4 in exchange 
 * for h3). By updating these values as pieces move rather than reevaluating each time, we save lots of computation at 
 * minimal space costs.
 * 
 * The functions that update values on moves are in theory reversible. To play a series of moves "backwards", we simply
 * convert the played moves into their inverses and replay them. All values should hopefully be updated to their former 
 * values. This is very helpful for 1) allowing users to backtrack moves and 2) building an in-place move tree
 * 
 * Constructing the tree of potential moves ought to be done in place. A game will consist of a list of played moves, a 
 * complementary list of reverse moves, and a single Position object that always refers to the current position. Therefore
 * as we generate the tree, we only ever need one large object. To calculate, we play a bunch of moves, "rewind" them, 
 * and play the next line. 
 * 
 * There is lots of work left. We need to implement wAttacks and bAttacks, add provisions and functions for reverse moves,
 * make the game tree data structure, and come up with a smart way to evaluate positions. Good luck!
 */

public class Position {
	
	int[] board;  //indices of pieces in pieces array, a.k.a. pointers
	int enPassent;
	int castlingRights; // lowest 6 bits are bqr,bkr,bk,wqr,wkr,wk
	int halfTurnNumber;
	int movesWithoutCaptureOrPawn;
	Piece[] pieces;
	int nWhitePieces;
	int nBlackPieces;
	int[] wpI;
	int[] bpI;
	int nchecks;
	int[] checkSquares;
	
	//TODO - make castling rights reversible with special moves for first rook (k/q) and king moves
	//TODO - wAttacks and bAttacks arrays
	
	public Position() {
		board = new int[64];
		enPassent = -1;
		castlingRights = 63; // 0b111111
		halfTurnNumber = 1;
		movesWithoutCaptureOrPawn = 0;
		pieces = new Piece[32];
		nWhitePieces = 16;
		nBlackPieces = 16;
		wpI = new int[16];
		bpI = new int[16];
		nchecks = 0;
		checkSquares = new int[8];
	}
	
	public static void main(String[] args) {

//		String[] mate = {"f4","e6","g4","Qh4"};
//		String[] promotion = {"b4","a5","bxa5","b6","axb6","Ra7","bxa7","Nf6"};
//		String[] enPassent = {"h4","Nc6","h5","g5"};
		String[] doubleCheck = {"e4","e5","d4","d5","exd5","exd4","Bd3","Nf6","Be4","Ng8","d6","Bxd6","Qe2","Na6","Bc6"};
//		String[] revealPin = {"e4","d5","Bb5","c6","Ba4","Nd7","exd5","cxd5"};
//		String[] capturePinner = {"d4","e6","Nc3","Bb4","a3","Nf6","axb4"};
//		String[] breakPin = {"e4","d5","Bb5","Nc6","Be2"};
//		String[] interceptPin = {"e4","d5","Bb5","Nc6","Ba4","Nf6","b4","e6","b5"};
//		String[] kingChangePins = {"e4","d6","Ke2","Bg4","Nf3","Qd7","d3","Qb5","Ke3"};
//		String[] ksc = {"e4","e5","Nf3","Nc6","Bb5","Nf6"};
//		String[] qsc = {"d4","d5","Nc3","g6","Bg5","Nf6","Qd2","c5"};
//		String[] castlewk = {"e4","e5","Nf3","Nc6","Bb5","Nf6","O-O","a6"};
//		String[] castlewq = {"d4","d5","Nc3","g6","Bg5","Nf6","Qd2","c5","O-O-O","h6"};
//		String[] loseksc = {"e4","e5","Nf3","Nc6","Bb5","Nf6","Rg1","h6","Rh1","g6"};
		String[] moves = doubleCheck;
		playMovesAndPrintData(moves);	

	}
	
	public void setNew() {
		
		Piece wk = new Piece(13);
		Piece wq = new Piece(11);
		Piece wr0 = new Piece(9);
		Piece wr7 = new Piece(9);
		Piece wb2 = new Piece(7);
		Piece wb5 = new Piece(7);
		Piece wn1 = new Piece(536871173);
		Piece wn6 = new Piece(536871173);
		Piece wp0 = new Piece(515);
		Piece wp1 = new Piece(515);
		Piece wp2 = new Piece(515);
		Piece wp3 = new Piece(515);
		Piece wp4 = new Piece(515);
		Piece wp5 = new Piece(515);
		Piece wp6 = new Piece(515);
		Piece wp7 = new Piece(515);
		
		Piece bk = new Piece(12);
		Piece bq = new Piece(10);
		Piece br0 = new Piece(8);
		Piece br7 = new Piece(8);
		Piece bb2 = new Piece(6);
		Piece bb5 = new Piece(6);
		Piece bn1 = new Piece(1179652);
		Piece bn6 = new Piece(1179652);
		Piece bp0 = new Piece(2097154);
		Piece bp1 = new Piece(2097154);
		Piece bp2 = new Piece(2097154);
		Piece bp3 = new Piece(2097154);
		Piece bp4 = new Piece(2097154);
		Piece bp5 = new Piece(2097154);
		Piece bp6 = new Piece(2097154);
		Piece bp7 = new Piece(2097154); 
		
		pieces[0] = wk; pieces[1] = wq; pieces[2] = wr0; pieces[3] = wr7;
		pieces[4] = wb2; pieces[5] = wb5; pieces[6] = wn1; pieces[7] = wn6;
		pieces[8] = wp0; pieces[9] = wp1; pieces[10] = wp2; pieces[11] = wp3; 
		pieces[12] = wp4; pieces[13] = wp5; pieces[14] = wp6; pieces[15] = wp7; 
		pieces[16] = bk; pieces[17] = bq; pieces[18] = br0; pieces[19] = br7;
		pieces[20] = bb2; pieces[21] = bb5; pieces[22] = bn1; pieces[23] = bn6;
		pieces[24] = bp0; pieces[25] = bp1; pieces[26] = bp2; pieces[27] = bp3; 
		pieces[28] = bp4; pieces[29] = bp5; pieces[30] = bp6; pieces[31] = bp7; 
		
		for (int i = 0; i < 64; ++i) {
			board[i] = -1;
		}
		board[0] = 2; board[1] = 6; board[2] = 4; board[3] = 1;
		board[4] = 0; board[5] = 5; board[6] = 7; board[7] = 3;
		board[8] = 8; board[9] = 9; board[10] = 10; board[11] = 11;
		board[12] = 12; board[13] = 13; board[14] = 14; board[15] = 15;
		board[48] = 24; board[49] = 25; board[50] = 26; board[51] = 27;
		board[52] = 28; board[53] = 29; board[54] = 30; board[55] = 31;
		board[56] = 18; board[57] = 22; board[58] = 20; board[59] = 17;
		board[60] = 16; board[61] = 21; board[62] = 23; board[63] = 19;
		
		wpI[0] = 4; wpI[1] = 3; wpI[2] = 0; wpI[3] = 7; wpI[4] = 2; wpI[5] = 5; wpI[6] = 1; wpI[7] = 6;
		for (int i = 0; i < 8; ++i) {
			wpI[i+8] = i+8;
		}
		bpI[0] = 60; bpI[1] = 59; bpI[2] = 56; bpI[3] = 63; bpI[4] = 58; bpI[5] = 61; bpI[6] = 57; bpI[7] = 62;
		for (int i = 0; i < 8; ++i) {
			bpI[i+8] = 48+i;
		}
		for (int i = 0; i < 8; ++i) {
			checkSquares[i] = -1;
		}
		
	}
	
	// n: 0 = wk, 1 = wkr, 2 = wqr, 3 = bk, 4 = bkr, 5 = bqr
	public boolean getCastlingRights(int n) {
		return (((castlingRights >>> n) & 1) == 1);
	}
	
	// just assume that the direction is valid
	public void setCheckSquares(int start, int end, int dir) {
		int step = 0;
		switch (dir) {
		case 0:
			step = 8;
			break;
		case 1:
			step = 9;
			break;
		case 2:
			step = 1;
			break;
		case 3:
			step = -7;
			break;
		case 4:
			step = -8;
			break;
		case 5:
			step = -9;
			break;
		case 6:
			step = -1;
			break;
		case 7:
			step = 7;
			break;
		}
		int index = 0;
		for (int i = start; i != end; i+=step) {
			checkSquares[index++] = i;
		}
		checkSquares[index] = -1;
	}
	
	public List<Move> getMoves() {
		
		int[] lsteps = {8,9,1,-7,-8,-9,-1,7};
		int[] nsteps = {17,10,-6,-15,-17,-10,6,15};
		
		List<Move> moves = new ArrayList<Move>();
		boolean turn = halfTurnNumber%2 == 1;
		int[] pcs = turn ? wpI : bpI;
			
		if (nchecks == 2) {
			Piece p = pieces[board[pcs[0]]];
			int special = (turn ? getCastlingRights(0) : getCastlingRights(3)) ? 8 : 0; // if first king move or not
			int[] mvs = p.getMoves();
			for (int i = 0; i < 8; ++i) {
				if (mvs[i] > 0) {
					int end = pcs[0] + lsteps[i];
					if (!isAttacked(end,!turn,(i+4)%8)) {
						moves.add(new Move(board[pcs[0]],pcs[0],end,board[end],special));
					}
				}
			}
		} else if (nchecks == 1) {
			boolean[] validSquares = new boolean[64];
			for (int i = 0; i < 8; ++i) {
				int val = checkSquares[i];
				if (val == -1) break;
				validSquares[val] = true;
			}
			Piece k = pieces[board[pcs[0]]];
			int kspecial = (turn ? getCastlingRights(0) : getCastlingRights(3)) ? 8 : 0; // if first king move or not
			int[] kmvs = k.getMoves();
			for (int i = 0; i < 8; ++i) {
				if (kmvs[i] > 0) {
					int end = pcs[0] + lsteps[i];
					if (!isAttacked(end,!turn,(i+4)%8)) {
						moves.add(new Move(board[pcs[0]],pcs[0],end,board[end],kspecial));
					}
				}
			}
			for (int i = 1; i < pcs.length; ++i) {
				if (pcs[i] > -1) {
					int index = pcs[i];
					int pieceIndex = board[index];
					Piece p = pieces[pieceIndex];
					int special = 0;
					int type = p.getType();
					int[] mvs = p.getMoves();
					int pinDir = p.getPinDirection();
					if (pinDir > -1) {
						if (type != 2) {
							int currSquare = index;
							int step = lsteps[pinDir];
							for (int n = 0; n < mvs[pinDir]; ++n) {
								currSquare+=step;
								if (validSquares[currSquare]) {
									int endIndex = board[currSquare];
									if (type == 1) {
										if (currSquare == enPassent) {
											special = 1;
										} else if (turn ? currSquare>55 : currSquare < 8) {
											moves.add(new Move(pieceIndex,index,currSquare,endIndex,4));
											moves.add(new Move(pieceIndex,index,currSquare,endIndex,5));
											moves.add(new Move(pieceIndex,index,currSquare,endIndex,6));
											special = 7;
										}
									} else if (type == 4) {
										if (turn) {
											if (pieceIndex == 3) {
												if (getCastlingRights(1)) special = 9;
											} else if (pieceIndex == 2) {
												if (getCastlingRights(2)) special = 10;
											}
										} else {
											if (pieceIndex == 19) {
												if (getCastlingRights(4)) special = 9;
											} else if (pieceIndex == 18) {
												if (getCastlingRights(5)) special = 10;
											}
										}
									} else if (type == 6) {
										if (Math.abs(currSquare-index) == 2) {
											if (currSquare == 6 || currSquare == 62) {
												special = 2;
											} else {
												special = 3;
											}
										} 
									}
									moves.add(new Move(pieceIndex,index,currSquare,endIndex,special));
								}
							}
						}
					} else {
						for (int j = 0; j < 8; ++j) {
							if (mvs[j] > 0) {
								int currSquare = index;
								int step = p.getType() == 2 ? nsteps[j] : lsteps[j];
								for (int n = 0; n < mvs[j]; ++n) {
									currSquare+=step;
									if (validSquares[currSquare]) {
										int endIndex = board[currSquare];
										if (type == 1) {
											if (currSquare == enPassent) {
												special = 1;
											} else if (turn ? currSquare>55 : currSquare < 8) {
												moves.add(new Move(pieceIndex,index,currSquare,endIndex,4));
												moves.add(new Move(pieceIndex,index,currSquare,endIndex,5));
												moves.add(new Move(pieceIndex,index,currSquare,endIndex,6));
												special = 7;
											}
										} else if (type == 4) {
											if (turn) {
												if (pieceIndex == 3) {
													if (getCastlingRights(1)) special = 9;
												} else if (pieceIndex == 2) {
													if (getCastlingRights(2)) special = 10;
												}
											} else {
												if (pieceIndex == 19) {
													if (getCastlingRights(4)) special = 9;
												} else if (pieceIndex == 18) {
													if (getCastlingRights(5)) special = 10;
												}
											}
										}
										moves.add(new Move(pieceIndex,index,currSquare,endIndex,special));
									}
								}
							}
						}
					}
				}
			}
			
		// No checks
		} else {
			
			Piece k = pieces[board[pcs[0]]];
			int kspecial = (turn ? getCastlingRights(0) : getCastlingRights(3)) ? 8 : 0; // if first king move or not
			int[] kmvs = k.getMoves();
			boolean ksc = false; // kingside castle
			boolean qsc = false; // queenside castle
			if (turn) {
				if (getCastlingRights(0)) {
					if (getCastlingRights(1)) {
						ksc = true;
					}
					if (getCastlingRights(2)) {
						qsc = true;
					}
				}
			} else {
				if (getCastlingRights(3)) {
					if (getCastlingRights(4)) {
						ksc = true;
					}
					if (getCastlingRights(5)) {
						qsc = true;
					}
				}
			}
			for (int i = 0; i < 8; ++i) {
				if (kmvs[i] > 0) {
					int end = pcs[0] + lsteps[i];
					if (!isAttacked(end,!turn,(i+4)%8)) {
						moves.add(new Move(board[pcs[0]],pcs[0],end,board[end],kspecial));
						if (i == 2 && ksc) {
							if (turn) {
								if (board[6] == -1 && !isAttacked(6,false,6)) {
									moves.add(new Move(board[pcs[0]],pcs[0],6,-1,2));
								}
							} else {
								if (board[62] == -1 && !isAttacked(62,true,6)) {
									moves.add(new Move(board[pcs[0]],pcs[0],62,-1,2));
								}
							}
						} else if (i == 6 && qsc) {
							if (turn) {
								if (board[2] == -1 && !isAttacked(2,false,2)) {
									moves.add(new Move(board[pcs[0]],pcs[0],2,-1,3));
								}
							} else {
								if (board[58] == -1 && !isAttacked(58,true,2)) {
									moves.add(new Move(board[pcs[0]],pcs[0],58,-1,3));
								}
							}
						}
					}
				}
			}
			for (int i = 1; i < pcs.length; ++i) {
				if (pcs[i] > -1) {
					int index = pcs[i];
					int pieceIndex = board[index];
					Piece p = pieces[pieceIndex];
					int special = 0;
					int type = p.getType();
					int[] mvs = p.getMoves();
					int pinDir = p.getPinDirection();
					if (pinDir > -1) {
						if (type != 2) {
							int currSquare = index;
							int step = lsteps[pinDir];
							for (int n = 0; n < mvs[pinDir]; ++n) {
								currSquare+=step;
								int endIndex = board[currSquare];
								if (type == 1) {
									if (currSquare == enPassent) {
										special = 1;
									} else if (turn ? currSquare>55 : currSquare < 8) {
										moves.add(new Move(pieceIndex,index,currSquare,endIndex,4));
										moves.add(new Move(pieceIndex,index,currSquare,endIndex,5));
										moves.add(new Move(pieceIndex,index,currSquare,endIndex,6));
										special = 7;
									}
								} else if (type == 4) {
									if (turn) {
										if (pieceIndex == 3) {
											if (getCastlingRights(1)) special = 9;
										} else if (pieceIndex == 2) {
											if (getCastlingRights(2)) special = 10;
										}
									} else {
										if (pieceIndex == 19) {
											if (getCastlingRights(4)) special = 9;
										} else if (pieceIndex == 18) {
											if (getCastlingRights(5)) special = 10;
										}
									}
								} else if (type == 6) {
									if (Math.abs(currSquare-index) == 2) {
										if (currSquare == 6 || currSquare == 62) {
											special = 2;
										} else {
											special = 3;
										}
									} 
								}
								moves.add(new Move(pieceIndex,index,currSquare,endIndex,special));
							}
						}
					} else {
						for (int j = 0; j < 8; ++j) {
							if (mvs[j] > 0) {
								int currSquare = index;
								int step = p.getType() == 2 ? nsteps[j] : lsteps[j];
								for (int n = 0; n < mvs[j]; ++n) {
									currSquare+=step;
									int endIndex = board[currSquare];
									if (type == 1) {
										if (currSquare == enPassent) {
											special = 1;
										} else if (turn ? currSquare>55 : currSquare < 8) {
											moves.add(new Move(pieceIndex,index,currSquare,endIndex,4));
											moves.add(new Move(pieceIndex,index,currSquare,endIndex,5));
											moves.add(new Move(pieceIndex,index,currSquare,endIndex,6));
											special = 7;
										}
									} else if (type == 4) {
										if (turn) {
											if (pieceIndex == 3) {
												if (getCastlingRights(1)) {
													special = 9;
												}
											} else if (pieceIndex == 2) {
												if (getCastlingRights(2)) special = 10;
											}
										} else {
											if (pieceIndex == 19) {
												if (getCastlingRights(4)) special = 9;
											} else if (pieceIndex == 18) {
												if (getCastlingRights(5)) special = 10;
											}
										}
									}
									moves.add(new Move(pieceIndex,index,currSquare,endIndex,special));
								}
							}
						}
					}
				}
			}
		}
		
		return moves;
		
	}
	
	public void move(String s) {
		
		//a3
		//axb3
		//a3+
		//a8=Q
		//Nf6
		//Nef6
		//N7f6
		//Nf6+
		//Nef6+
		//Nexf6+
		//O-O
		//O-O-O
		
		boolean turn = halfTurnNumber%2==1;
		
		if (s.equals("O-O")) {
			if (turn) {
				Move m = new Move(0,4,6,-1,2);
				move(m);
				return;
			} else {
				Move m = new Move(16,60,62,-1,2);
				move(m);
				return;
			}
		} else if (s.equals("O-O-O")) {
			if (turn) {
				Move m = new Move(0,4,2,-1,3);
				move(m);
				return;
			} else {
				Move m = new Move(16,60,58,-1,3);
				move(m);
				return;
			}
		}
		
		int special = 0;
		int specifier = -1; // Knight on e file, rook on 2nd rank; 0-7 = a-h, 8-15 = 0-7
		int sFile = -1;
		int eFile = sFile;
		int eRank = -1;
		int startIndex = -1;
		int endIndex = -1;
		int endPieceIndex = -1;
		boolean invalid = false;
		String eFileName = "";
		String eRankName = "";
		String specifierName = "";
		String pieceName = "";
		
		String s1 = s.substring(0, 1);
		if (s1.toUpperCase().equals(s1)) {
			pieceName = s1;
			String s2 = s.substring(1, 2);
			String s3 = s.substring(2, 3);
			if (s2.equals("x")) {
				String s4 = s.substring(3, 4);
				eFileName = s3;
				eRankName = s4;
			} else {
				boolean specified = false;
				try {
					eRank = Integer.parseInt(s3) - 1;
				} catch (Exception e) {
					specified = true;
				}
				if (specified) {
					String s4 = s.substring(3, 4);
					if (s3.equals("x")) {
						String s5 = s.substring(4, 5);
						eFileName = s4;
						eRankName = s5;
						specifierName = s2;
					} else {
						eFileName = s3;
						eRankName = s4;
						specifierName = s2;
					}
				} else {
					eFileName = s2;
					eRankName = s3;
				}
			}
//			System.out.println("End Filename: " + eFileName);
//			System.out.println("End Rankname: " + eRankName);
			boolean valid = false;
			switch (eFileName) {
			case "a":
				eFile = 0; valid = true; break;
			case "b":
				eFile = 1; valid = true; break;
			case "c":
				eFile = 2; valid = true; break;
			case "d":
				eFile = 3; valid = true; break;
			case "e":
				eFile = 4; valid = true; break;
			case "f":
				eFile = 5; valid = true; break;
			case "g":
				eFile = 6; valid = true; break;
			case "h":
				eFile = 7; valid = true; break;
			}
			if (!valid) invalid = true;
			try {
				eRank = Integer.parseInt(eRankName) - 1;
			} catch (Exception e) {
				invalid = true;
			}
			if (specifier > -1) {
				try {
					specifier = Integer.parseInt(specifierName) + 7;
				} catch (Exception e) {
					switch (specifierName) {
					case "a":
						specifier = 0; valid = true; break;
					case "b":
						specifier = 1; valid = true; break;
					case "c":
						specifier = 2; valid = true; break;
					case "d":
						specifier = 3; valid = true; break;
					case "e":
						specifier = 4; valid = true; break;
					case "f":
						specifier = 5; valid = true; break;
					case "g":
						specifier = 6; valid = true; break;
					case "h":
						specifier = 7; valid = true; break;
					}
					if (!valid) invalid = true;
				}
			}
			endIndex = eRank*8 + eFile;
			endPieceIndex = endIndex;
			switch (pieceName) {
			case "N":
				int[] nI = getPieceIndicesInKnightDirections(endIndex);
				for (int i = 0; i < 8; ++i) {
					if (nI[i] > -1) {
						Piece p = pieces[board[nI[i]]];
						if (p.getType() == 2 && (p.getColor() == turn)) {
							if (specifier > -1) {
								if (specifier < 8) {
									if (nI[i] % 8 == specifier) {
										startIndex = nI[i];
										break;
									}
								} else {
									if (nI[i]/8 == (specifier-8)) {
										startIndex = nI[i];
										break;
									}
								}
							} else {
								startIndex = nI[i];
								break;
							}
						}
					}
				}
				valid = true;
				break;
			case "B":
				int[] bI = getPieceIndicesInLineDirections(endIndex);
				for (int i = 1; i < 8; i+=2) {
					if (bI[i] > -1) {
						Piece p = pieces[board[bI[i]]];
						if (p.getType() == 3 && (p.getColor() == turn)) {
							if (specifier > -1) {
								if (specifier < 8) {
									if (bI[i] % 8 == specifier) {
										startIndex = bI[i];
										break;
									}
								} else {
									if (bI[i]/8 == (specifier-8)) {
										startIndex = bI[i];
										break;
									}
								}
							} else {
								startIndex = bI[i];
								break;
							}
						}
					}
				}
				valid = true;
				break;
			case "R":
				int[] rI = getPieceIndicesInLineDirections(endIndex);
				for (int i = 0; i < 8; i+=2) {
					if (rI[i] > -1) {
						Piece p = pieces[board[rI[i]]];
						if (p.getType() == 4 && (p.getColor() == turn)) {
							if (specifier > -1) {
								if (specifier < 8) {
									if (rI[i] % 8 == specifier) {
										startIndex = rI[i];
										break;
									}
								} else {
									if (rI[i]/8 == (specifier-8)) {
										startIndex = rI[i];
										break;
									}
								}
							} else {
								startIndex = rI[i];
								break;
							}
						}
					}
				}
				valid = true;
				break;
			case "Q":
				int[] qI = getPieceIndicesInLineDirections(endIndex);
				for (int i = 0; i < 8; i++) {
					if (qI[i] > -1) {
						Piece p = pieces[board[qI[i]]];
						if (p.getType() == 5 && (p.getColor() == turn)) {
							if (specifier > -1) {
								if (specifier < 8) {
									if (qI[i] % 8 == specifier) {
										startIndex = qI[i];
										break;
									}
								} else {
									if (qI[i]/8 == (specifier-8)) {
										startIndex = qI[i];
										break;
									}
								}
							} else {
								startIndex = qI[i];
								break;
							}
						}
					}
				}
				valid = true;
				break;
			case "K":
				startIndex = turn ? wpI[0] : bpI[0];
				valid = true;
				break;
			}
			if (!valid) invalid = true;
			
		} else { // Pawn move
			
			switch(s1) {
			case "a":
				sFile = 0; break;
			case "b": 
				sFile = 1; break;
			case "c":
				sFile = 2; break;
			case "d":
				sFile = 3; break;
			case "e":
				sFile = 4; break;
			case "f":
				sFile = 5; break;
			case "g":
				sFile = 6; break;
			case "h":
				sFile = 7; break;
			}
			String s2 = s.substring(1,2);
			boolean capture = false;
			if (s2.equals("x")) {
				capture = true;
				String s3 = s.substring(2, 3);
				switch(s3) {
				case "a":
					eFile = 0; break;
				case "b": 
					eFile = 1; break;
				case "c":
					eFile = 2; break;
				case "d":
					eFile = 3; break;
				case "e":
					eFile = 4; break;
				case "f":
					eFile = 5; break;
				case "g":
					eFile = 6; break;
				case "h":
					eFile = 7; break;
				}
				String s4 = s.substring(3, 4);
				try {
					eRank = Integer.parseInt(s4) - 1;
				} catch (Exception e) {
					invalid = true;
				}
				if (s.length() > 5) { //promotion 
					if (!s.substring(4, 5).equals("=")) invalid = true;
					boolean valid = false;
					String promotion = s.substring(5, 6);
					switch (promotion) {
					case "Q":
						special = 4; valid = true; break;
					case "R":
						special = 5; valid = true; break;
					case "B":
						special = 6; valid = true; break;
					case "N":
						special = 7; valid = true; break;
					
					}
					if (!valid) invalid = true;
				}
			} else {
				eFile = sFile;
				try {
					eRank = Integer.parseInt(s2) - 1;
				} catch (Exception e) {
					invalid = true;
				}
				if (s.length() > 3) { //promotion
					if (!s.substring(2, 3).equals("=")) invalid = true;
					boolean valid = false;
					String promotion = s.substring(3, 4);
					switch (promotion) {
					case "Q":
						special = 4; valid = true; break;
					case "R":
						special = 5; valid = true; break;
					case "B":
						special = 6; valid = true; break;
					case "N":
						special = 7; valid = true; break;
					
					}
					if (!valid) invalid = true;
				}
			}
			endIndex = eRank*8 + eFile;
			if (halfTurnNumber%2 == 1) {
				if (capture) {
					startIndex = sFile < eFile ? endIndex - 9 : endIndex - 7;
				} else {
					startIndex = board[endIndex - 8] < 0 ? endIndex-16 : endIndex-8;
				}
				if (endIndex == enPassent) {
					special = 1;
					endPieceIndex = enPassent - 8;
				} else {
					endPieceIndex = endIndex;
				}
			} else {
				if (capture) {
					startIndex = sFile < eFile ? endIndex + 7 : endIndex + 9;
				} else {
					startIndex = board[endIndex + 8] < 0 ? endIndex+16 : endIndex+8;
				}
				if (endIndex == enPassent) {
					special = 1;
					endPieceIndex = enPassent + 8;
				} else {
					endPieceIndex = endIndex;
				}
			}
		}
//		System.out.println("HalfTurnNumber: " + halfTurnNumber);
//		System.out.println("Start File: " + sFile);
//		System.out.println("Start Index: " + startIndex);
//		System.out.println("End File: " + eFile);
//		System.out.println("End Rank: " + eRank);
//		System.out.println("End Index: " + endIndex);
//		System.out.println("Piece Index: " + board[startIndex]);
//		System.out.println("---------");
		if (invalid) throw new IllegalArgumentException("Invalid Move: " + s);
		Move m = new Move(board[startIndex], startIndex, endIndex, endPieceIndex == -1 ? -1 : board[endPieceIndex], special);
		move(m);
		
	}
	
	public void move(Move m) {
		
		//TODO - clean up adding/removing piece by wrapping edits at board and pieceIndices
		
		nchecks = 0; // i think?
		
		Piece p = pieces[m.pieceIndex];
		boolean whiteTurn = halfTurnNumber%2 == 1;
		
		if (board[m.end] > -1) {
			editPieceIndex(m.end,!(whiteTurn), -1);
		}
		
		board[m.end] = board[m.start]; // "move" the piece to its end square
		board[m.start] = -1; // remove the piece from starting square
		editPieceIndex(m.start,whiteTurn,m.end);
		
		if (m.special > 0) { // some sort of special move
			switch (m.special) {
			case 1: // en passent
				if (p.getColor()) {
					board[m.end-8] = -1;
					editPieceIndex(m.end-8,!(whiteTurn), -1);
				} else {
					board[m.end+8] = -1;
					editPieceIndex(m.end-8,!(whiteTurn), -1);
				}
				modifyMovesAtSquare(p.getColor() ? m.end-8 : m.end+8,null);
				break;
			case 2: // kingside castle
				if (p.getColor()) {
					board[5] = board[7];
					board[7] = -1;
					editPieceIndex(7,true,5);
					modifyMovesAtSquare(4,null);
					modifyMovesAtSquare(5,pieces[board[5]]);
					modifyMovesAtSquare(7,null);
				} else {
					board[61] = board[63];
					board[63] = -1;
					editPieceIndex(63,false,61);
					modifyMovesAtSquare(60,null);
					modifyMovesAtSquare(61,pieces[board[61]]);
					modifyMovesAtSquare(63,null);
				}
				break;
			case 3: // queenside castle
				if (p.getColor()) {
					board[3] = board[0];
					board[0] = -1;
					editPieceIndex(0,true,3);
					modifyMovesAtSquare(4,null);
					modifyMovesAtSquare(3,pieces[board[3]]);
					modifyMovesAtSquare(1,null);
					modifyMovesAtSquare(0,null);
				} else {
					board[59] = board[56];
					board[56] = -1;
					editPieceIndex(56,false,59);
					modifyMovesAtSquare(60,null);
					modifyMovesAtSquare(59,pieces[board[59]]);
					modifyMovesAtSquare(57,null);
					modifyMovesAtSquare(56,null);
				}
				break;
			case 4: // promote to Q
				p.promoteTo(5);
				break;
			case 5: // promote to R
				p.promoteTo(4);
				break;
			case 6: // promote to B
				p.promoteTo(3);
				break;
			case 7: // promote to N
				p.promoteTo(2);
				break;
			case 8: // first king move
				if (p.getColor()) {
					castlingRights &= 62; //0b111110
				} else {
					castlingRights &= 55; //0b110111
				}
				break;
			case 9: //first king's rook move
//				System.out.println("First Rook Move");
				if (p.getColor()) {
					castlingRights &= 61; //0b111101
				} else {
					castlingRights &= 47; //0b101111
				}
				break;
			case 10: //first queen's rook move
				if (p.getColor()) {
					castlingRights &= 59; //0b111011
				} else {
					castlingRights &= 31; //0b011111
				}
				break;
			}
		}

		
		// AT START
		modifyMovesAtSquare(m.start,null);
		
		// AT END
		modifyMovesAtSquare(m.end,p);
		
		// SET MOVES FOR PIECE THAT MOVED
		setMovesForPiece(p,m.end);
		
		// en passent
		if (p.getType() == 1 && Math.abs(m.end-m.start) == 16) {
			enPassent = p.getColor() ? m.end - 8 : m.end + 8;
			if (m.end % 8 > 0) {
				int left = m.end-1;
				if (board[left] > -1) {
					Piece pc = pieces[board[left]];
					if (pc.getType() == 1) {
						if (pc.getColor()) {
							if (!p.getColor()) {
								pc.setNMovesInDir(1, 1);
							}
						} else {
							if (p.getColor()) {
								pc.setNMovesInDir(3, 1);
							}
						}
					}
				}
			}
			if (m.end % 8 < 7) {
				int right = m.end+1;
				if (board[right] > -1) {
					Piece pc = pieces[board[right]];
					if (pc.getType() == 1) {
						if (pc.getColor()) {
							if (!p.getColor()) {
								pc.setNMovesInDir(7, 1);
							}
						} else {
							if (p.getColor()) {
								pc.setNMovesInDir(5, 1);
							}
						}
					}
				}
			}
		} else {
			enPassent = -1;
		}
		
		if (m.endPieceIndex > -1 || p.getType() == 1) {
			movesWithoutCaptureOrPawn = 0;
		} else {
			movesWithoutCaptureOrPawn++;
		}
		halfTurnNumber++;
		
	}
	
	// white = color of attacker
	// TODO - make arrays wAttacks, bAttacks and change this method to work on those
	// retain info move to move on which squares are attacked by each side
	public boolean isAttacked(int index, boolean white, int exemptDir) {
		if (white) {
			//king
			if (Math.abs(wpI[0]%8-index%8) < 2 && Math.abs(wpI[0]/8-index/8) < 2) {
				return true;
			}
			//pawns
			if (index > 15) {
				if (exemptDir != 5 && index%8 > 0) {
					if (board[index-9] > -1) {
						Piece p = pieces[board[index-9]];
						if (p.getType() == 1 && p.getColor()) {
							return true;
						}
					}
				}
				if (exemptDir != 3 && index%8 < 7) {
					if (board[index-7] > -1) {
						Piece p = pieces[board[index-7]];
						if (p.getType() == 1 && p.getColor()) {
							return true;
						}
					}
				}
			}
			//knights
			int[] kI = getPieceIndicesInKnightDirections(index);
			for (int i = 0; i < 8; ++i) {
				if (kI[i] > -1) {
					Piece p = pieces[board[kI[i]]];
					if (p.getType() == 2 && p.getColor()) {
						return true;
					}
				}
			}
			//others
			int[] lI = getPieceIndicesInLineDirections(index);
			for (int i = 0; i < exemptDir; ++i) {
				if (lI[i] > -1) {
					Piece p = pieces[board[lI[i]]];
					if (i%2 == 0) {
						if (p.getType() == 4 || p.getType() == 5) {
							if (p.getColor()) {
								return true;
							}
						}
					} else {
						if (p.getType() == 3 || p.getType() == 5) {
							if (p.getColor()) {
								return true;
							}
						}
					}
				}
			}
			for (int i = exemptDir+1; i < 8; ++i) {
				if (lI[i] > -1) {
					Piece p = pieces[board[lI[i]]];
					if (i%2 == 0) {
						if (p.getType() == 4 || p.getType() == 5) {
							if (p.getColor()) {
								return true;
							}
						}
					} else {
						if (p.getType() == 3 || p.getType() == 5) {
							if (p.getColor()) {
								return true;
							}
						}
					}
				}
			}
		} else {
			//king
			if (Math.abs(bpI[0]%8-index%8) < 2 && Math.abs(bpI[0]/8-index/8) < 2) {
				return true;
			}
			//pawns
			if (index < 48) {
				if (exemptDir != 7 && index%8 > 0) {
					if (board[index+7] > -1) {
						Piece p = pieces[board[index+7]];
						if (p.getType() == 1 && !p.getColor()) {
							return true;
						}
					}
				}
				if (exemptDir != 1 && index%8 < 7) {
					if (board[index+9] > -1) {
						Piece p = pieces[board[index+9]];
						if (p.getType() == 1 && !p.getColor()) {
							return true;
						}
					}
				}
			}
			//knights
			int[] kI = getPieceIndicesInKnightDirections(index);
			for (int i = 0; i < 8; ++i) {
				if (kI[i] > -1) {
					Piece p = pieces[board[kI[i]]];
					if (p.getType() == 2 && !p.getColor()) {
						return true;
					}
				}
			}
			//others
			int[] lI = getPieceIndicesInLineDirections(index);
			for (int i = 0; i < exemptDir; ++i) {
				if (lI[i] > -1) {
					Piece p = pieces[board[lI[i]]];
					if (i%2 == 0) {
						if (p.getType() == 4 || p.getType() == 5) {
							if (!p.getColor()) {
								return true;
							}
						}
					} else {
						if (p.getType() == 3 || p.getType() == 5) {
							if (!p.getColor()) {
								return true;
							}
						}
					}
				}
			}
			for (int i = exemptDir+1; i < 8; ++i) {
				if (lI[i] > -1) {
					Piece p = pieces[board[lI[i]]];
					if (i%2 == 0) {
						if (p.getType() == 4 || p.getType() == 5) {
							if (!p.getColor()) {
								return true;
							}
						}
					} else {
						if (p.getType() == 3 || p.getType() == 5) {
							if (!p.getColor()) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	//TODO - optimize by only searching at certain piece types indices
	public void editPieceIndex(int index, boolean white, int val) {
		if (white) {
			for (int i = 0; i < 16; ++i) {
				if (wpI[i] == index) {
					wpI[i] = val;
					break;
				}
			}
		} else {
			for (int i = 0; i < 16; ++i) {
				if (bpI[i] == index) {
					bpI[i] = val;
					break;
				}
			}
		}
	}
	
	public void setMovesForPiece(Piece p, int index) {
		
		int rank = index/8;
		int file = index%8;
		boolean color = p.getColor();
		switch (p.getType()) {
		case 1:
			if (color) {
				if (index%8 < 7) {
					if (board[index+9] > -1 && !pieces[board[index+9]].getColor()) {
						p.setNMovesInDir(1, 1);
					} else {
						p.setNMovesInDir(1, 0);
					}
				} else {
					p.setNMovesInDir(1, 0);
				}
				if (index%8 > 0) {
					if (board[index+7] > -1 && !pieces[board[index+7]].getColor()) {
						p.setNMovesInDir(7, 1);
					} else {
						p.setNMovesInDir(7, 0);
					}
				} else {
					p.setNMovesInDir(7, 0);
				}
				if (board[index+8] == -1) {
					if (index/8 == 1 && board[index+16] == -1) {
						p.setNMovesInDir(0, 2);
					} else {
						p.setNMovesInDir(0, 1);
					}
				} else {
					p.setNMovesInDir(0, 0);
				}
			} else {
				if (index%8 < 7) {
					if (board[index-7] > -1 && pieces[board[index-7]].getColor()) {
						p.setNMovesInDir(3, 1);
					} else {
						p.setNMovesInDir(3, 0);
					}
				} else {
					p.setNMovesInDir(3, 0);
				}
				if (index%8 > 0) {
					if (board[index-9] > -1 && pieces[board[index-9]].getColor()) {
						p.setNMovesInDir(5, 1);
					} else {
						p.setNMovesInDir(5, 0);
					}
				} else {
					p.setNMovesInDir(5, 0);
				}
				if (board[index-8] == -1) {
					if (index/8 == 6 && board[index-16] == -1) {
						p.setNMovesInDir(4, 2);
					} else {
						p.setNMovesInDir(4, 1);
					}
				} else {
					p.setNMovesInDir(4, 0);
				}
			}
			break;
		case 2:
			int[] kIndices = {-1,-1,-1,-1,-1,-1,-1,-1};
			if (rank < 7) {
				if (file <  6) {
					kIndices[1] = index+10;
				}
				if (file > 1) {
					kIndices[6] = index+6;
				}
			}
			if (rank < 6) {
				if (file < 7) {
					kIndices[0] = index+17;
				}
				if (file > 0) {
					kIndices[7] = index+15;
				}
			}
			if (rank > 1) {
				if (file < 7) {
					kIndices[3] = index-15;
				}
				if (file > 0) {
					kIndices[4] = index-17;
				}
			}
			if (rank > 0) {
				if (file < 6) {
					kIndices[2] = index-6;
				}
				if (file > 1) {
					kIndices[5] = index-10;
				}
			}
			for (int i = 0; i < 8; ++i) {
				if (kIndices[i] > -1) {
					if (board[kIndices[i]] < 0 || pieces[board[kIndices[i]]].getColor() != color) {
						p.setNMovesInDir(i, 1);
					} else {
						p.setNMovesInDir(i, 0);
					}
				} else {
					p.setNMovesInDir(i, 0);
				}
			}
			break;
		case 3:
			int[] bI = new int[4];
			bI[0] = getPieceIndexInLineDirection(index,1);
			bI[1] = getPieceIndexInLineDirection(index,3);
			bI[2] = getPieceIndexInLineDirection(index,5);
			bI[3] = getPieceIndexInLineDirection(index,7);
			int[] bN = new int[4];
			bN[0] = bI[0] < 0 ? 7 - Math.max(rank, file) : 
				pieces[board[bI[0]]].getColor() == color ? ((bI[0]-index)/9) - 1 : (bI[0]-index)/9;
			bN[1] = bI[1] < 0 ? 7 - Math.max(7-rank, file) : 
				pieces[board[bI[1]]].getColor() == color ? ((index-bI[1])/7) - 1 : (index-bI[1])/7;
			bN[2] = bI[2] < 0 ? Math.min(rank, file) : 
				pieces[board[bI[2]]].getColor() == color ? ((index-bI[2])/9) - 1 : (index-bI[2])/9;
			bN[3] = bI[3] < 0 ? Math.min(7-(rank), file) :
				pieces[board[bI[3]]].getColor() == color ? ((bI[3]-index)/7) - 1 : (bI[3]-index)/7;
			p.setNMovesInDir(1, bN[0]);
			p.setNMovesInDir(3, bN[1]);
			p.setNMovesInDir(5, bN[2]);
			p.setNMovesInDir(7, bN[3]);
			break;
		case 4:
			int[] rI = new int[4];
			rI[0] = getPieceIndexInLineDirection(index,0);
			rI[1] = getPieceIndexInLineDirection(index,2);
			rI[2] = getPieceIndexInLineDirection(index,4);
			rI[3] = getPieceIndexInLineDirection(index,6);
			int[] rN = new int[4];
			rN[0] = rI[0] < 0 ? 7 - rank : 
				pieces[board[rI[0]]].getColor() == color ? ((rI[0]-index)/8) - 1 : (rI[0]-index)/8;
			rN[1] = rI[1] < 0 ? 7 - file : 
				pieces[board[rI[1]]].getColor() == color ? (rI[1]-index) - 1 : rI[1]-index;
			rN[2] = rI[2] < 0 ? rank : 
				pieces[board[rI[2]]].getColor() == color ? ((index-rI[2])/8) - 1 : (index-rI[2])/8;
			rN[3] = rI[3] < 0 ? file :
				pieces[board[rI[3]]].getColor() == color ? (index-rI[3]) - 1 : index-rI[3];
			p.setNMovesInDir(0, rN[0]);
			p.setNMovesInDir(2, rN[1]);
			p.setNMovesInDir(4, rN[2]);
			p.setNMovesInDir(6, rN[3]);
			break;
		case 5:
			int[] qI = new int[8];
			for (int i = 0; i < 8; ++i) {
				qI[i] = getPieceIndexInLineDirection(index,i);
			}
			int[] qN = new int[8];
			qN[0] = qI[0] < 0 ? 7 - rank : 
				pieces[board[qI[0]]].getColor() == color ? ((qI[0]-index)/8) - 1 : (qI[0]-index)/8;
			qN[1] = qI[1] < 0 ? 7 - Math.max(rank, file) : 
				pieces[board[qI[1]]].getColor() == color ? ((qI[1]-index)/9) - 1 : (qI[1]-index)/9;
			qN[2] = qI[2] < 0 ? 7 - file : 
				pieces[board[qI[2]]].getColor() == color ? (qI[2]-index) - 1 : qI[2]-index;
			qN[3] = qI[3] < 0 ? 7 - Math.max(7-rank, file) : 
				pieces[board[qI[3]]].getColor() == color ? ((index-qI[3])/7) - 1 : (index-qI[3])/7;
			qN[4] = qI[4] < 0 ? rank : 
				pieces[board[qI[4]]].getColor() == color ? ((index-qI[4])/8) - 1 : (index-qI[4])/8;
			qN[5] = qI[5] < 0 ? Math.min(rank, file) : 
				pieces[board[qI[5]]].getColor() == color ? ((index-qI[5])/9) - 1 : (index-qI[5])/9;
			qN[6] = qI[6] < 0 ? file :
				pieces[board[qI[6]]].getColor() == color ? (index-qI[6]) - 1 : index-qI[6];
			qN[7] = qI[7] < 0 ? Math.min(7-(rank), file) :
				pieces[board[qI[7]]].getColor() == color ? ((qI[7]-index)/7) - 1 : (qI[7]-index)/7;
			for (int i = 0; i < 8; ++i) {
				p.setNMovesInDir(i, qN[i]);
			}
			break;
		case 6: // complicated
			int[] kI = {-1,-1,-1,-1,-1,-1,-1,-1};
			boolean N = index < 56;
			boolean S = index > 7;
			boolean E = index%8 < 7;
			boolean W = index%8 > 0;
			if (S) {
				kI[4] = index-8;
				if (W) {
					kI[5] = index-9;
				}
				if (E) {
					kI[3] = index-7;
				}
			}
			if (N) {
				kI[0] = index+8;
				if (W) {
					kI[7] = index+7;
				}
				if (E) {
					kI[1] = index+9;
				}
			}
			if (W) {
				kI[6] = index-1;
			}
			if (E) {
				kI[2] = index+1;
			}
			for (int i = 0; i < 8; ++i) {
				if (kI[i] > -1) {
					if (board[kI[i]] < 0 || pieces[board[kI[i]]].getColor() != color) {
						if (isAttacked(kI[i],!color,(i+4)%8)) {
							p.setNMovesInDir(i, 0);
						} else {
							p.setNMovesInDir(i, 1);
						}
					} else {
						p.setNMovesInDir(i, 0);
					}
				}
			}
			break;
		}
		
	}
	
	//TODO - castling rights
	//TODO - take away king squares on Qh4-esque moves
	//TODO - remove pins on king moves
	//Optimization - add exemption direction
	public void modifyMovesAtSquare(int index, Piece p) {
		
		if (p != null) {
			
			boolean color = p.getColor();
			int type = p.getType();
			
			if (type != 6) {
			
				int toOwnDir = getLineDirection(index, color ? wpI[0] : bpI[0]);
				int toOppDir = getLineDirection(index, color ? bpI[0] : wpI[0]);
				
				if (toOwnDir > -1) {
					int toOwn1 = getPieceIndexInLineDirection(index, toOwnDir);
					Piece p1 = pieces[board[toOwn1]]; // has to exist, might be the king
					if (p1.getColor() == color) { // if not, it do not matter
						p1.setIsPinned(false); // king cannot be in check after a move, and we have just removed any pins on non-kings
						if (p1.getType() == 6) {
							int awayOwnDir = (toOwnDir+4)%8;
							int awayOwn1 = getPieceIndexInLineDirection(index, awayOwnDir);
							if (awayOwn1 > -1) {
								Piece p2 = pieces[board[awayOwn1]];
								if (p2.getColor() != color) { // p could be pinned by this piece
									if (toOwnDir%2 == 0) { 
										if (p2.getType() == 4 || p2.getType() == 5) {
											p.setIsPinned(true);
											p.setPinDirection(awayOwnDir);
										}
									} else {
										if (p2.getType() == 3 || p2.getType() == 5) {
											p.setIsPinned(true);
											p.setPinDirection(awayOwnDir);
										}
									}
								} else {
									p2.setIsPinned(false);
								}
							}
						}
					} 
				}
				
				//checks and pins to opposing king
				int oppKingIndex = color ? bpI[0] : wpI[0];
				switch (type) {
				case 1: // no pins
					boolean check = false;
					if (toOppDir > -1) {
						if (color) {
							if (toOppDir == 1 && (index+9 == oppKingIndex)) {
								check = true;
							} else if (toOppDir == 7 && (index+7 == oppKingIndex)) {
								check = true;
							}
						} else {
							if (toOppDir == 3 && (index-7 == oppKingIndex)) {
								check = true;
							} else if (toOppDir == 5 && (index-9 == oppKingIndex)) {
								check = true;
							}
						}
					}
					if (check) { // check
						nchecks++;
						if (nchecks == 1) {
							checkSquares[0] = index;
							checkSquares[1] = -1;
						}
					} else { // block team pin (potentially)
						if (toOppDir > -1) {
							int toOpp1 = getPieceIndexInLineDirection(index,toOppDir);
							Piece p1 = pieces[board[toOpp1]];
							if (p1.getColor() != color) {
								if (p1.getType() != 6) {
									p1.setIsPinned(false);
								}
							}
						}
					}
					break;
				case 2: // no pins
					if (toOppDir > -1) {
						int toOpp1 = getPieceIndexInLineDirection(index,toOppDir);
						Piece p1 = pieces[board[toOpp1]];
						if (p1.getColor() != color) {
							if (p1.getType() != 6) { // block team pin (potentially)
								p1.setIsPinned(false);
							}
 						}
					} else {
						int nDir = getKnightDirection(oppKingIndex, index);
						if (nDir > -1) { // check
							nchecks++;
							if (nchecks == 1) {
								checkSquares[0] = index;
								checkSquares[1] = -1;
							}
						}
					}
				case 3:
					if (toOppDir > -1) {
						int toOpp1 = getPieceIndexInLineDirection(index,toOppDir);
						Piece p1 = pieces[board[toOpp1]];
						if (toOppDir%2 == 1) {
							if (p1.getColor() != color) {
								if (p1.getType() == 6) { // check
									nchecks++;
									if (nchecks == 1) {
										setCheckSquares(index, toOpp1, toOppDir);
									}
								} else {
									int toOpp2 = getPieceIndexInLineDirection(toOpp1, toOppDir);
									if (toOpp2 == oppKingIndex) { // pin
										p1.setIsPinned(true);
										p1.setPinDirection((toOppDir+4)%8);
									}
								}
							}
						} else {
							if (p1.getColor() != color) {
								if (p1.getType() != 6) { // block team pin (potentially)
									p1.setIsPinned(false);
								}
							}
						}
					}
					break;
				case 4:
					if (toOppDir > -1) {
						int toOpp1 = getPieceIndexInLineDirection(index,toOppDir);
						Piece p1 = pieces[board[toOpp1]];
						if (toOppDir%2 == 0) {
							if (p1.getColor() != color) {
								if (p1.getType() == 6) { // check
									nchecks++;
									if (nchecks == 1) {
										setCheckSquares(index, toOpp1, toOppDir);
									}
								} else {
									int toOpp2 = getPieceIndexInLineDirection(toOpp1, toOppDir);
									if (toOpp2 == oppKingIndex) { // pin
										p1.setIsPinned(true);
										p1.setPinDirection((toOppDir+4)%8);
									}
								}
							}
						} else {
							if (p1.getColor() != color) {
								if (p1.getType() != 6) { // block team pin (potentially)
									p1.setIsPinned(false);
								}
							}
						}
					}
					break;
				case 5: // cannot block team pin
					if (toOppDir > -1) {
						int toOpp1 = getPieceIndexInLineDirection(index,toOppDir);
						Piece p1 = pieces[board[toOpp1]];
						if (p1.getColor() != color) {
							if (p1.getType() == 6) { // check
								nchecks++;
								if (nchecks == 1) {
									setCheckSquares(index, toOpp1, toOppDir);
								}
							} else {
								int toOpp2 = getPieceIndexInLineDirection(toOpp1, toOppDir);
								if (toOpp2 == oppKingIndex) { // pin
									p1.setIsPinned(true);
									p1.setPinDirection((toOppDir+4)%8);
								}
							}
						}
					}
					break;
				}
			
			} else { //king move
				
				//may have blocked a pin to other king
				int toOppDir = getLineDirection(index, color ? bpI[0] : wpI[0]);
				if (toOppDir > -1) {
					int toOpp1 = getPieceIndexInLineDirection(index, toOppDir);
					Piece p1 = pieces[board[toOpp1]]; // must exist
					if (p1.getColor() != color) {
						if (p1.getType() != 6) { // block team pin (potentially)
							p1.setIsPinned(false);
						}
					}
				}
				
				//update pins to self
				int[] lineIndices = getPieceIndicesInLineDirections(index);
				for (int i = 0; i < 8; ++i) {
					if (lineIndices[i] > -1) {
						Piece p1 = pieces[board[lineIndices[i]]];
						if (p1.getColor() == color) {
							int secondLineIndex = getPieceIndexInLineDirection(lineIndices[i],i);
							if (secondLineIndex > -1) {
								Piece p2 = pieces[board[secondLineIndex]];
								if (p2.getColor() != color) {
									if (i%2 == 0) {
										if (p2.getType() == 4 || p2.getType() == 5) {
											p1.setIsPinned(true);
											p1.setPinDirection(i);
										}
									} else {
										if (p2.getType() == 3 || p2.getType() == 5) {
											p1.setIsPinned(true);
											p1.setPinDirection(i);
										}
									}
								}
							}
						}
					}
				}
				
			}
			
			// modify all knights 1 knight move away from end
			int[] kei = getPieceIndicesInKnightDirections(index);
			for (int i = 0; i < 8; ++i) {
				if (kei[i] > -1) {
					Piece pc = pieces[board[kei[i]]];
					if (pc.getType() == 2) { 
						if (pc.getColor() == color) {
							pc.setNMovesInDir((i+4)%8, 0);
						} else { // or on a capture?
							pc.setNMovesInDir((i+4)%8, 1);
						}
					}
				}
			}
			
			int[] lei = getPieceIndicesInLineDirections(index);
			Piece[] lep = getPiecesAtBoardIndices(lei);
			
			// N-S
			if (lep[0] != null) {
				int dToA = lep[0].getColor() == color ? ((lei[0]-index)/8) - 1 : (lei[0]-index)/8;
				int nMoves = dToA;
				
				switch (lep[0].getType()) {
				case 1:
					if (!lep[0].getColor() && dToA < 3) {
						if (color) nMoves--; // pawns cannot take vertically
						lep[0].setNMovesInDir(4, nMoves); 
					}
					break;
				case 4:
					lep[0].setNMovesInDir(4, nMoves);
					break;
				case 5:
					lep[0].setNMovesInDir(4, nMoves);
					break;
				case 6:
					if (lei[0]-index == 8) {
						if (lep[0].getColor() == color) {
							nMoves = 0;
						} else {
							if (isAttacked(index,!lep[0].getColor(),0)) {
								nMoves = 0;
							} else {
								nMoves = 1;
							}
						}
						lep[0].setNMovesInDir(4, nMoves);
					}
					break;
				}
			} 
			if (lep[4] != null) {
				int dToA = lep[4].getColor() == color ? ((index-lei[4])/8) - 1 : (index-lei[4])/8;
				int nMoves = dToA;
				
				switch (lep[4].getType()) {
				case 1:
					if (lep[4].getColor() && dToA < 3) {
						if (!color) nMoves--; // pawns cannot take vertically
						lep[4].setNMovesInDir(0, nMoves); 
					}
					break;
				case 4:
					lep[4].setNMovesInDir(0, nMoves);
					break;
				case 5:
					lep[4].setNMovesInDir(0, nMoves);
					break;
				case 6: 
					if (index-lei[4] == 8) {
						if (lep[4].getColor() == color) {
							nMoves = 0;
						} else {
							if (isAttacked(index,!lep[4].getColor(),4)) {
								nMoves = 0;
							} else {
								nMoves = 1;
							}
						}
						lep[4].setNMovesInDir(0, nMoves);
					}
					break;
				}
			} 
			
			//NE-SW
			if (lep[1] != null) {
				int dToA = lep[1].getColor() == color ? ((lei[1]-index)/9) - 1 : (lei[1]-index)/9;
				int nMoves = dToA;
				
				switch (lep[1].getType()) {
				case 1:
					if (!lep[1].getColor() && dToA == 1) {
						if (!color) nMoves--; // nMoves is 0 here
						lep[1].setNMovesInDir(5, nMoves); 
					}
					break;
				case 3:
					lep[1].setNMovesInDir(5, nMoves);
					break;
				case 5:
					lep[1].setNMovesInDir(5, nMoves);
					break;
				case 6: 
					if (lei[1]-index == 9) {
						if (lep[1].getColor() == color) {
							nMoves = 0;
						} else {
							if (isAttacked(index,!lep[1].getColor(),1)) {
								nMoves = 0;
							} else {
								nMoves = 1;
							}
						}
						lep[1].setNMovesInDir(5, nMoves);
					}
					break;
				}
			}
			if (lep[5] != null) {
				int dToA = lep[5].getColor() == color ? ((index-lei[5])/9) - 1 : (index-lei[5])/9;
				int nMoves = dToA;
				
				switch (lep[5].getType()) {
				case 1:
					if (lep[5].getColor() && dToA == 1) {
						if (color) nMoves--; // nMoves would be 0
						lep[5].setNMovesInDir(1, nMoves); 
					}
					break;
				case 3:
					lep[5].setNMovesInDir(1, nMoves);
					break;
				case 5:
					lep[5].setNMovesInDir(1, nMoves);
					break;
				case 6:
					if (index-lei[5] == 9) {
						if (lep[5].getColor() == color) {
							nMoves = 0;
						} else {
							if (isAttacked(index,!lep[5].getColor(),5)) {
								nMoves = 0;
							} else {
								nMoves = 1;
							}
						}
						lep[5].setNMovesInDir(1, nMoves);
					}
					break;
				}
			}
			
			// E-W
			if (lep[2] != null) {
				int dToA = lep[2].getColor() == color ? (lei[2]-index) - 1 : lei[2]-index;
				int nMoves = dToA;
				
				switch (lep[2].getType()) {
				case 4:
					lep[2].setNMovesInDir(6, nMoves);
					break;
				case 5:
					lep[2].setNMovesInDir(6, nMoves);
					break;
				case 6: 
					if (lei[2]-index == 1) {
						if (lep[2].getColor() == color) {
							nMoves = 0;
						} else {
							if (isAttacked(index,!lep[2].getColor(),2)) {
								nMoves = 0;
							} else {
								nMoves = 1;
							}
						}
						lep[2].setNMovesInDir(6, nMoves);
					}
					break;
				}
			}
			if (lep[6] != null) {
				int dToA = lep[6].getColor() == color ? (index-lei[6]) - 1 : index-lei[6];
				int nMoves = dToA;
				
				switch (lep[6].getType()) {
				case 4:
					lep[6].setNMovesInDir(2, nMoves);
					break;
				case 5:
					lep[6].setNMovesInDir(2, nMoves);
					break;
				case 6:
					if (index-lei[6] == 1) {
						if (lep[6].getColor() == color) {
							nMoves = 0;
						} else {
							if (isAttacked(index,!lep[6].getColor(),6)) {
								nMoves = 0;
							} else {
								nMoves = 1;
							}
						}
						lep[6].setNMovesInDir(2, nMoves);
					}
					break;
				}
			}
			
			// SE-NW
			if (lep[3] != null) {
				int dToA = lep[3].getColor() == color ? ((index-lei[3])/7) - 1 : (index-lei[3])/7;
				int nMoves = dToA;
				
				switch (lep[3].getType()) {
				case 1:
					if (lep[3].getColor() && dToA == 1) {
						if (color) nMoves--; // pawns cannot take vertically
						lep[3].setNMovesInDir(7, nMoves); 
					}
					break;
				case 3:
					lep[3].setNMovesInDir(7, nMoves);
					break;
				case 5:
					lep[3].setNMovesInDir(7, nMoves);
					break;
				case 6:
					if (index-lei[3] == 7) {
						if (lep[3].getColor() == color) {
							nMoves = 0;
						} else {
							if (isAttacked(index,!lep[3].getColor(),3)) {
								nMoves = 0;
							} else {
								nMoves = 1;
							}
						}
						lep[3].setNMovesInDir(7, nMoves);
					}
					break;
				}
			}
			if (lep[7] != null) {
				int dToA = lep[7].getColor() == color ? ((lei[7]-index)/7) - 1 : (lei[7]-index)/7;
				int nMoves = dToA;
				
				switch (lep[7].getType()) {
				case 1:
					if (!lep[7].getColor() && dToA == 1) {
						if (!color) nMoves--; // pawns cannot take vertically
						lep[7].setNMovesInDir(3, nMoves); 
					}
					break;
				case 3:
					lep[7].setNMovesInDir(3, nMoves);
					break;
				case 5:
					lep[7].setNMovesInDir(3, nMoves);
					break;
				case 6: // 
					if (lei[7]-index == 7) {
						if (lep[7].getColor() == color) {
							nMoves = 0;
						} else {
							if (isAttacked(index,!lep[7].getColor(),7)) {
								nMoves = 0;
							} else {
								nMoves = 1;
							}
						}
						lep[7].setNMovesInDir(3, nMoves);
					}
					break;
				}
			}
			
		} else {
			
			boolean turn = halfTurnNumber%2 == 1;
			// justification: halfTurnNumber is odd for forward white move and backwards black move
			//                in either case, the black king can now be attacked
			int attackedKingIndex = turn ? bpI[0] : wpI[0];
			int safeKingIndex = turn ? wpI[0] : bpI[0];
			
			int fromAttackedDir = getLineDirection(attackedKingIndex, index);
			int fromSafeDir = getLineDirection(safeKingIndex, index);
			
			if (fromAttackedDir > -1) {
				int fromAttacked1 = getPieceIndexInLineDirection(attackedKingIndex, fromAttackedDir);
				if (fromAttacked1 > -1) {
					Piece p1 = pieces[board[fromAttacked1]];
					//very confusing logic but turn != attacked king color
					if (p1.getColor() == turn) {  // check
						if (fromAttackedDir%2 == 0) {
							if (p1.getType() == 4 || p1.getType() == 5) {
								nchecks++;
								if (nchecks == 1) {
									setCheckSquares(fromAttacked1, attackedKingIndex, (fromAttackedDir+4)%8);
								}
							}
						} else {
							if (p1.getType() == 3 || p1.getType() == 5) {
								nchecks++;
								if (nchecks == 1) {
									setCheckSquares(fromAttacked1, attackedKingIndex, (fromAttackedDir+4)%8);
								}
							}
						}
					} else { // pins
						boolean pinning = false;
						int fromAttacked2 = getPieceIndexInLineDirection(fromAttacked1, fromAttackedDir);
						if (fromAttacked2 > -1) {
							Piece p2 = pieces[board[fromAttacked2]];
							if (p2.getColor() == turn) {
								if (fromAttackedDir%2 == 0) {
									if (p1.getType() == 4 || p1.getType() == 5) {
										pinning = true;
									}
								} else {
									if (p1.getType() == 3 || p1.getType() == 5) {
										pinning = true;
									}
								}
							}
						}
						if (pinning) {
							p1.setIsPinned(true);
							p1.setPinDirection(fromAttackedDir);
						} else {
							p1.setIsPinned(false);
						}
					}
				}
			}
			
			if (fromSafeDir > -1) { // leave behind pin on safe king
				int fromSafe1 = getPieceIndexInLineDirection(safeKingIndex, fromSafeDir);
				if (fromSafe1 > -1) {
					Piece p1 = pieces[board[fromSafe1]];
					if (p1.getColor() == turn) { //same color as safe king
						int fromSafe2 = getPieceIndexInLineDirection(fromSafe1, fromSafeDir);
						if (fromSafe2 > -1) {
							Piece p2 = pieces[board[fromSafe2]];
							if (p2.getColor() != turn) { //opposite color as safe king and p1
								if (fromSafeDir%2 == 0) {
									if (p2.getType() == 4 || p2.getType() == 5) {
										p1.setIsPinned(true);
										p1.setPinDirection(fromSafeDir);
									}
								} else {
									if (p2.getType() == 3 || p2.getType() == 5) {
										p1.setIsPinned(true);
										p1.setPinDirection(fromSafeDir);
									}
								}
							}
						}
					}
				}
			}
			
			// add that square for all knights 1 knight move away
			int[] ksi = getPieceIndicesInKnightDirections(index);
			for (int i = 0; i < 8; ++i) {
				if (ksi[i] > -1) {
					Piece pc = pieces[board[ksi[i]]];
					if (pc.getType() == 2) {   // check if knight is of opposite color?
						pc.setNMovesInDir((i+4)%8, 1);
					}
				}
			}
			
			int[] lsi = getPieceIndicesInLineDirections(index);
			Piece[] lsp = getPiecesAtBoardIndices(lsi);
			
			// N-S
			if (lsp[0] != null) {
				int dToA = (lsi[0]-index)/8; // distance to the move start square including start square
				int dToB = lsp[4] == null ? lsi[0]/8 : 
					lsp[4].getColor() == lsp[0].getColor() ? (lsi[0]-lsi[4])/8 - 1 : (lsi[0]-lsi[4])/8; // distance to piece in S direction
				int nMoves = dToB;
				
				switch (lsp[0].getType()) {
				case 1:
					if (!lsp[0].getColor() && dToA < 3) {
						nMoves = lsi[0]/8 == 6 ? 2 : 1;
						lsp[0].setNMovesInDir(4, nMoves); 
					}
					break;
				case 4:
					lsp[0].setNMovesInDir(4, nMoves);
					break;
				case 5:
					lsp[0].setNMovesInDir(4, nMoves);
					break;
				case 6:
					if (dToA == 1) {
						if (isAttacked(index,!lsp[0].getColor(),0)) {
							nMoves = 0;
						} else {
							nMoves = 1;
						}
						lsp[0].setNMovesInDir(4, nMoves);
					}
					break;
				}
			} 
			if (lsp[4] != null) {
				int dToA = (index-lsi[4])/8; // distance to the move start square including start square
				int dToB = lsp[0] == null ? 7-(lsi[4]/8) : 
					lsp[0].getColor() == lsp[4].getColor() ? (lsi[0]-lsi[4])/8 - 1 : (lsi[0]-lsi[4])/8; // distance to piece in S direction
				int nMoves = dToB;
				
				switch (lsp[4].getType()) {
				case 1:
					if (lsp[4].getColor() && dToA < 3) {
						nMoves = lsi[4]/8 == 1 ? 2 : 1;
						lsp[4].setNMovesInDir(0, nMoves); 
					}
					break;
				case 4:
					lsp[4].setNMovesInDir(0, nMoves);
					break;
				case 5:
					lsp[4].setNMovesInDir(0, nMoves);
					break;
				case 6: 
					if (dToA == 1) {
						if (isAttacked(index,!lsp[4].getColor(),4)) {
							nMoves = 0;
						} else {
							nMoves = 1;
						}
						lsp[4].setNMovesInDir(0, nMoves);
					}
					break;
				}
			}
			
			//NE-SW
			if (lsp[1] != null) {
				int dToA = (lsi[1]-index)/9; // distance to the move start square including start square
				int dToB = lsp[5] == null ? Math.min(lsi[1]/8, lsi[1]%8) : 
					lsp[5].getColor() == lsp[1].getColor() ? (lsi[1]-lsi[5])/9 - 1 : (lsi[1]-lsi[5])/9; // distance to piece in S direction
				int nMoves = dToB;
				
				switch (lsp[1].getType()) {
				case 1:
					if (!lsp[1].getColor() && dToA == 1) {
						lsp[1].setNMovesInDir(5, 0);
					}
					break;
				case 3:
					lsp[1].setNMovesInDir(5, nMoves);
					break;
				case 5:
					lsp[1].setNMovesInDir(5, nMoves);
					break;
				case 6:
					if (dToA == 1) {
						if (isAttacked(index,!lsp[1].getColor(),1)) {
							nMoves = 0;
						} else {
							nMoves = 1;
						}
						lsp[1].setNMovesInDir(5, nMoves);
					}
					break;
				}
			} 
			if (lsp[5] != null) {
				int dToA = (index-lsi[5])/9; // distance to the move start square including start square
				int dToB = lsp[1] == null ? 7 - Math.max(lsi[5]/8, lsi[5]%8) : 
					lsp[1].getColor() == lsp[5].getColor() ? (lsi[1]-lsi[5])/9 - 1 : (lsi[1]-lsi[5])/9; // distance to piece in S direction
				int nMoves = dToB;
				
				switch (lsp[5].getType()) {
				case 1:
					if (lsp[5].getColor() && dToA == 1) {
						lsp[5].setNMovesInDir(1, 0); 
					}
					break;
				case 3:
					lsp[5].setNMovesInDir(1, nMoves);
					break;
				case 5:
					lsp[5].setNMovesInDir(1, nMoves);
					break;
				case 6: 
					if (dToA == 1) {
						if (isAttacked(index,!lsp[5].getColor(),5)) {
							nMoves = 0;
						} else {
							nMoves = 1;
						}
						lsp[5].setNMovesInDir(1, nMoves);
					}
					break;
				}
			} 
			
			// E-W
			if (lsp[2] != null) {
				int dToA = lsi[2]-index; // distance to the move start square including start square
				int dToB = lsp[6] == null ? lsi[2]%8 : 
					lsp[6].getColor() == lsp[2].getColor() ? (lsi[2]-lsi[6]) - 1 : lsi[2]-lsi[6]; // distance to piece in S direction
				int nMoves = dToB;
				
				switch (lsp[2].getType()) {
				case 4:
					lsp[2].setNMovesInDir(6, nMoves);
					break;
				case 5:
					lsp[2].setNMovesInDir(6, nMoves);
					break;
				case 6: 
					if (dToA == 1) {
						if (isAttacked(index,!lsp[2].getColor(),2)) {
							nMoves = 0;
						} else {
							nMoves = 1;
						}
						lsp[2].setNMovesInDir(6, nMoves);
					}
					break;
				}
			} 
			if (lsp[6] != null) {
				int dToA = index-lsi[6]; // distance to the move start square including start square
				int dToB = lsp[2] == null ? 7-(lsi[6]%8) : 
					lsp[2].getColor() == lsp[6].getColor() ? (lsi[2]-lsi[6]) - 1 : lsi[2]-lsi[6]; // distance to piece in S direction
				int nMoves = dToB;
				
				switch (lsp[6].getType()) {
				case 4:
					lsp[6].setNMovesInDir(2, nMoves);
					break;
				case 5:
					lsp[6].setNMovesInDir(2, nMoves);
					break;
				case 6: 
					if (dToA == 1) {
						if (isAttacked(index,!lsp[6].getColor(),6)) {
							nMoves = 0;
						} else {
							nMoves = 1;
						}
						lsp[6].setNMovesInDir(2, nMoves);
					}
					break;
				}
			}
			
			// SE-NW
			if (lsp[3] != null) {
				int dToA = (index - lsi[3])/7; // distance to the move start square including start square
				int dToB = lsp[7] == null ? Math.min(7-(lsi[3]/8), lsi[3]%8) : 
					lsp[7].getColor() == lsp[3].getColor() ? (lsi[7]-lsi[3])/7 - 1 : (lsi[7]-lsi[3])/7; // distance to piece in S direction
				int nMoves = dToB;
				
				switch (lsp[3].getType()) {
				case 1:
					if (lsp[3].getColor() && dToA == 1) {
						lsp[3].setNMovesInDir(7, 0);
					}
					break;
				case 3:
					lsp[3].setNMovesInDir(7, nMoves);
					break;
				case 5:
					lsp[3].setNMovesInDir(7, nMoves);
					break;
				case 6: 
					if (dToA == 1) {
						if (isAttacked(index,!lsp[3].getColor(),3)) {
							nMoves = 0;
						} else {
							nMoves = 1;
						}
						lsp[3].setNMovesInDir(7, nMoves);
					}
					break;
				}
			} 
			if (lsp[7] != null) {
				int dToA = (lsi[7] - index)/7; // distance to the move start square including start square
				int dToB = lsp[3] == null ? 7 - Math.max(7-(lsi[7]/8), lsi[7]%8) : 
					lsp[3].getColor() == lsp[7].getColor() ? (lsi[7]-lsi[3])/7 - 1 : (lsi[7]-lsi[3])/7; // distance to piece in S direction
				int nMoves = dToB;
				
				switch (lsp[7].getType()) {
				case 1:
					if (!lsp[7].getColor() && dToA == 1) {
						lsp[7].setNMovesInDir(3, 0); 
					}
					break;
				case 3:
					lsp[7].setNMovesInDir(3, nMoves);
					break;
				case 5:
					lsp[7].setNMovesInDir(3, nMoves);
					break;
				case 6: 
					if (dToA == 1) {
						if (isAttacked(index,!lsp[7].getColor(),7)) {
							nMoves = 0;
						} else {
							nMoves = 1;
						}
						lsp[7].setNMovesInDir(3, nMoves);
					}
					break;
				}
			} 
			
		}
		
	}
	
	public Piece[] getPiecesAtBoardIndices(int[] indices) {
		Piece[] pieces = new Piece[indices.length];
		for (int i = 0; i < pieces.length; ++i) {
			pieces[i] = indices[i] < 0 ? null : this.pieces[this.board[indices[i]]];
		}
		return pieces;
	}
	
	public int[] getPieceIndicesInLineDirections(int start) {
		int[] indices = new int[8];
		for (int i = 0; i < 8; ++i) {
			indices[i] = getPieceIndexInLineDirection(start,i);
		}
		return indices;
	}
	
	public int[] getPieceIndicesInKnightDirections(int start) {
		int[] indices = new int[8];
		for (int i = 0; i < 8; ++i) {
			indices[i] = getPieceIndexInKnightDirection(start,i);
		}
		return indices;
	}
	
	public int getPieceIndexInKnightDirection(int start, int dir) {
		if (dir < 0 || dir > 7 || start < 0 || start > 63) throw new IllegalArgumentException(); 
		int rawIndex = -1;
		switch (dir) {
		case 0:
			rawIndex = start + 17;
			break;
		case 1:
			rawIndex = start + 10;
			break;
		case 2:
			rawIndex = start - 6;
			break;
		case 3:
			rawIndex = start - 15;
			break;
		case 4:
			rawIndex = start - 17;
			break;
		case 5:
			rawIndex = start - 10;
			break;
		case 6:
			rawIndex = start + 6;
			break;
		case 7:
			rawIndex = start + 15;
			break;
		}
		return Math.abs(start/8 - rawIndex/8) > 2 || Math.abs(start%8 - rawIndex%8) > 2 || rawIndex > 63 || rawIndex < 0 ? 
			-1 : board[rawIndex] > -1 ? rawIndex : -1;
	}
	
	public int getPieceIndexInLineDirection(int start, int dir) {
		if (dir < 0 || dir > 7 || start < 0 || start > 63) throw new IllegalArgumentException(); 
		int step = 0;
		int iters = 0;
		switch(dir) {
		case 0:
			step = 8;
			iters = 7 - (start/8);
			break;
		case 1:
			step = 9;
			iters = 7 - Math.max(start/8, start%8);
			break;
		case 2:
			step = 1;
			iters = 7 - (start%8);
			break;
		case 3:
			step = -7;
			iters = 7 - Math.max(7-(start/8), start%8);
			break;
		case 4:
			step = -8;
			iters = start/8;
			break;
		case 5:
			step = -9;
			iters = Math.min(start/8, start%8);
			break;
		case 6:
			step = -1;
			iters = start%8;
			break;
		case 7:
			step = 7;
			iters = Math.min(7-(start/8), start%8);
			break;
		}
		
		if (iters == 0) return -1;
		int index = start;
		for (int i = 0; i < iters; ++i) {
			index += step;
			if (board[index] > -1) return index;
		}
		
		return -1;
		
	}
	
	//TODO - Optimize
	public static int getLineDirection(int a, int b) {
		int aF = a%8;
		int bF = b%8;
		int aR = a/8;
		int bR = b/8;
		if (aF == bF) {
			return a>b ? 4 : 0;
		} else if (aR == bR) {
			return a>b ? 6 : 2;
		} else if (aF-aR == bF-bR) {
			return a>b ? 5 : 1;
		} else if (aF+aR == bF+bR) {
			return a>b ? 3 : 7;
		} else {
			return -1;
		}
	}
	
	public static int getKnightDirection(int a, int b) {
		int aF = a%8;
		int bF = b%8;
		int aR = a/8;
		int bR = b/8;
		if (aF > bF) {
			if (aR > bR) {
				if (b == a-17) {
					return 4;
				} else if (b == a-10) {
					return 5;
				}
			} else if (bR > aR) {
				if (b == a+6) {
					return 6;
				} else if (b == a+15) {
					return 7;
				}
			}
		} else if (bF > aF) {
			if (aR > bR) {
				if (b == a-6) {
					return 2;
				} else if (b == a-15) {
					return 3;
				}
			} else if (bR > aR) {
				if (b == a+17) {
					return 0;
				} else if (b == a+10) {
					return 1;
				}
			}
		} 
		return -1;
	}
	
	public static String squareName(int index) {
		return "abcdefgh".substring(index%8, (index%8) + 1) + ((index / 8) + 1);
	}
	
	public static void printBoardIndices() {

		for (int i = 0; i < 64; ++i) {
			int val = ((7-(i/8))*8) + (i%8);
			System.out.print(String.format("%3d", val));
			System.out.print(" ");
			if ((i+1)%8 == 0) {
				System.out.print("\n");
			}
		}
	}
	
	public void printBoard() {

		for (int i = 0; i < 64; ++i) {
			int val = ((7-(i/8))*8) + (i%8);
			if (board[val] == -1) {
				System.out.print("  ");
			} else {
				Piece p = pieces[board[val]];
				String letter = p.getType() == 1 ? "P" : p.getTypeInitial();
				if (p.getColor()) letter = letter.toLowerCase();
				if (letter.equalsIgnoreCase("k")) {
					if (nchecks>0 && (halfTurnNumber%2 == 1) == p.getColor()) {
						System.out.print(letter + "+");
					} else {
						System.out.print(letter + " ");
					}
				} else {
					if (p.getPinDirection() > -1) {
						System.out.print(letter + "*");
					} else {
						System.out.print(letter + " ");
					}
				}
			}
			if ((i+1)%8 == 0) {
				System.out.print("\n");
			}
		}
	}
	
	public void printPieceIndices() {
		System.out.print("White: ");
		for (int i = 0; i < 16; ++i) {
			System.out.print(wpI[i] + " ");
		}
		System.out.print("\n");
		System.out.print("Black: ");
		for (int i = 0; i < 16; ++i) {
			System.out.print(bpI[i] + " ");
		}
		System.out.print("\n");
	}
	
	public static void playMovesAndPrintData(String[] moves) {
		Position pos = new Position();
		pos.setNew();
		for (int i = 0; i < moves.length; ++i) {
			pos.move(moves[i]);
		}
		pos.printBoard();
//		pos.printPieceIndices();
//		System.out.print("Check Squares: ");
//		for (int i = 0; i < 8; ++i) {
//			if (pos.checkSquares[i] == -1) break;
//			System.out.print(pos.checkSquares[i] + " ");
//		}
//		System.out.print("\n");
//		int[] pcs = pos.halfTurnNumber % 2 == 0 ? pos.bpI : pos.wpI;
//		for (int i = 0; i < pcs.length; ++i) {
//			Piece p = pos.pieces[pos.board[pcs[i]]];
//			System.out.print(p.getTypeInitial() + " on square " + pcs[i] + ": ");
//			int[] mvs = p.getMoves();
//			for (int j = 0; j < 8; ++j) {
//				System.out.print(mvs[j] + " ");
//			}
//			System.out.print("\n");
//		}
		System.out.println(pos.getMoves().size() + " move(s)");
		for (Move m : pos.getMoves()) {
//			System.out.println(pos.pieces[m.pieceIndex].getInfo() + " on index " + m.start + ": " + m.getName(pos));
			try {
				System.out.println(m.getName(pos)); 
			} catch (Exception e) {
				System.out.print("Bad move: " + m.start + " to " + m.end);
			}
//			System.out.println(m.getDescription());
//			System.out.println(m.start + " to " + m.end);
		}
	}

	public void runoff() {
		
		//getMoves()
		
//		for (int i = 0; i < 64; ++i) {
//			
//			if (board[i] < 0) { continue; }
//			Piece p = pieces[board[i]];
//			if (p.getColor() == (halfTurnNumber % 2 == 1)) {
//				int pieceIndex = board[i];
//				int special = 0;
//				int type = p.getType();
//				boolean color = p.getColor();
//				int[] mvs = p.getMoves();
//				for (int j = 0; j < 8; ++j) {
//					if (mvs[j] > 0) {
//						int currSquare = i;
//						int step = 0;
//						if (p.getType() != 2) {
//							switch (j) {
//							case 0:
//								step = 8; break;
//							case 1:
//								step = 9; break;
//							case 2:
//								step = 1; break;
//							case 3:
//								step = -7; break;
//							case 4:
//								step = -8; break;
//							case 5:
//								step = -9; break;
//							case 6:
//								step = -1; break;
//							case 7:
//								step = 7; break;
//							}
//						} else {
//							switch (j) {
//							case 0:
//								step = 17; break;
//							case 1:
//								step = 10; break;
//							case 2:
//								step = -6; break;
//							case 3:
//								step = -15; break;
//							case 4:
//								step = -17; break;
//							case 5:
//								step = -10; break;
//							case 6:
//								step = 6; break;
//							case 7:
//								step = 15; break;
//							}
//						}
//						for (int n = 0; n < mvs[j]; ++n) {
//							currSquare+=step;
//							int endIndex = board[currSquare];
//							if (type == 1) {
//								if (currSquare == enPassent) {
//									special = 1;
//								} else if (color ? (currSquare/8) == 7 : (currSquare/8) == 0) {
//									moves.add(new Move(pieceIndex,i,currSquare,endIndex,4));
//									moves.add(new Move(pieceIndex,i,currSquare,endIndex,5));
//									moves.add(new Move(pieceIndex,i,currSquare,endIndex,6));
//									special = 7;
//								}
//							} else if (type == 6) {
//								if (Math.abs(currSquare-i) == 2) {
//									if (currSquare == 6 || currSquare == 62) {
//										special = 2;
//									} else {
//										special = 3;
//									}
//								}
//							}
//							moves.add(new Move(pieceIndex,i,currSquare,endIndex,special));
//						}
//					}
//				}
//				
//			}
//			
//		}
		
		//---------------------
		
		//move(Move)
//		case 2: // kingside castle
//			if (p.getColor()) {
//				board[5] = board[7];
//				board[7] = -1;
//				editPieceIndex(7,whiteTurn,5);
//				//rook moves
//				//W
//				int wkrW = getPieceIndexInLineDirection(5,6);
//				if (wkrW == -1) {
//					pieces[board[5]].setNMovesInDir(6, 5);
//				} else {
//					pieces[board[5]].setNMovesInDir(6, pieces[board[wkrW]].getColor() ? (5-wkrW) - 1 : 5-wkrW);
//				}
//				//N
//				int wkrN = getPieceIndexInLineDirection(5,0);
//				if (wkrN == -1) {
//					pieces[board[5]].setNMovesInDir(0, 7);
//				} else {
//					pieces[board[5]].setNMovesInDir(0, pieces[board[wkrN]].getColor() ? (wkrN-5)/8 - 1 : (wkrN-5)/8);
//				}
//				//moves to h1
//				if (board[13] > -1) {
//					Piece pc = pieces[board[13]];
//					if (pc.getType() == 2) {
//						pc.setNMovesInDir(2, 1);
//					}
//				}
//				if (board[22] > -1) {
//					Piece pc = pieces[board[22]];
//					if (pc.getType() == 2) {
//						pc.setNMovesInDir(3, 1);
//					}
//				}
//				int h1NW = getPieceIndexInLineDirection(7,7);
//				if (h1NW > -1) {
//					//this cannot be a relevant pawn or king by the rules of castling
//					Piece pc = pieces[board[h1NW]];
//					if (pc.getType() == 3 || pc.getType() == 5) {
//						pc.setNMovesInDir(3, (h1NW-7)/7);
//					}
//				}
//				//moves to e1
//				
//			} else {
//				board[61] = board[63];
//				board[63] = -1;
//				editPieceIndex(63,whiteTurn,61);
//				//rook moves
//				//W
//				int bkrW = getPieceIndexInLineDirection(61,6);
//				if (bkrW == -1) {
//					pieces[board[61]].setNMovesInDir(6, 5);
//				} else {
//					pieces[board[61]].setNMovesInDir(6, !pieces[board[bkrW]].getColor() ? (61-bkrW) - 1 : 61-bkrW);
//				}
//				//S
//				int bkrS = getPieceIndexInLineDirection(61,4);
//				if (bkrS == -1) {
//					pieces[board[61]].setNMovesInDir(4, 7);
//				} else {
//					pieces[board[61]].setNMovesInDir(4, !pieces[board[bkrS]].getColor() ? (61-bkrS)/8 - 1 : (61-bkrS)/8);
//				}
//				//moves to h8
//				
//				//moves to e8
//				
//			}
//			//todo - update moves for now abandoned start squares
//			break;
//		case 3: // queenside castle
//			if (p.getColor()) {
//				board[3] = board[0];
//				board[0] = -1;
//				editPieceIndex(3,whiteTurn,0);
//				//rook moves
//				//E
//				int wqrE = getPieceIndexInLineDirection(3,2);
//				if (wqrE == -1) {
//					pieces[board[3]].setNMovesInDir(2, 4);
//				} else {
//					pieces[board[3]].setNMovesInDir(2, pieces[board[wqrE]].getColor() ? (wqrE-3) - 1 : wqrE-3);
//				}
//				//N
//				int wqrN = getPieceIndexInLineDirection(3,0);
//				if (wqrN == -1) {
//					pieces[board[3]].setNMovesInDir(0, 7);
//				} else {
//					pieces[board[3]].setNMovesInDir(0, pieces[board[wqrN]].getColor() ? (wqrN-3)/8 - 1 : (wqrN-3)/8);
//				}
//				//moves to a1
//				
//				//moves to b1
//				
//				//moves to e1
//				
//			} else {
//				board[59] = board[56];
//				board[56] = -1;
//				editPieceIndex(59,whiteTurn,56);
//				//rook moves
//				//E
//				int bqrE = getPieceIndexInLineDirection(59,2);
//				if (bqrE == -1) {
//					pieces[board[59]].setNMovesInDir(2, 4);
//				} else {
//					pieces[board[59]].setNMovesInDir(2, !pieces[board[bqrE]].getColor() ? (bqrE-59) - 1 : bqrE-59);
//				}
//				//S
//				int bqrS = getPieceIndexInLineDirection(59,4);
//				if (bqrS == -1) {
//					pieces[board[59]].setNMovesInDir(4, 7);
//				} else {
//					pieces[board[59]].setNMovesInDir(4, !pieces[board[bqrS]].getColor() ? (59-bqrS)/8 - 1 : (59-bqrS)/8);
//				}
//				//moves to a8
//				
//				//moves to b8
//				
//				//moves to e8
//				
//			}
//			//todo - update moves for now abandoned start squares
//			break;
	}
}
