package model;

public class Piece {
	
	/**
	 * 32 bits
	 * Upper 24 represent moves this piece can make (ignoring checks and pins)
	 * Upper 24 divided into 3 bits in each of 8 directions, the 3 bits equaling the # of moves in that direction
	 * Lower 8 represent info about the piece's state
	 * Bits 5 - 7 are the direction of the piece's pin
	 * Bit 4 = whether or not the piece is pinned
	 * Bits 3 - 1 is the piece type (1 pawn, 2 knight, 3 bishop, 4 rook, 5 queen, 6 king)
	 * Bit 0 is the piece color (1 is white)
	 */
	int encoding;
	
	public Piece(int val) {
		this.encoding = val;
	}
	
	public boolean getColor() {
		return (encoding & 1) == 1;
	}
	
	public int getType() {
		return (encoding >>> 1) & 7;
	}
	
	public String getTypeInitial() {
		int t = getType();
		if (t < 1 || t > 6) throw new IllegalArgumentException();
		String s = "";
		switch (t) {
		case 2: s = "N"; break;
		case 3: s = "B"; break;
		case 4: s = "R"; break;
		case 5: s = "Q"; break;
		case 6: s = "K"; break;
		}
		return s;
	}
	
	public int getPinDirection() {
		if ((encoding >>> 4 & 1) == 1) {
			return (encoding >>> 5) & 7;
		} else {
			return -1;
		}
	}
	
	public int[] getMoves() {
		
		int[] moves = new int[8];
		int mvs = encoding >>> 8;
		for (int i = 0; i < 8; ++i) {
			moves[i] = (mvs >>> (3*i)) & 7;
		}
		return moves;
		
	}
	
	public String getMovesInfo() {
		String s = "";
		int[] moves = getMoves();
		for (int i = 0; i < 8; ++i) {
			String st = "Dir " + i + ": " + moves[i] + "\n";
			s += st;
		}
		return s;
	}
	
	public void promoteTo(int newType) {
		this.encoding &= ~(14);
		this.encoding |= (newType << 1);
	}

	public void setMoves(int moves) {
		this.encoding |= (moves << 8);
	}
	
	public void setNMovesInDir(int direction, int num) {
		if (direction < 0 || direction > 7 || num < 0 || num > 7) throw new IllegalArgumentException();
		encoding &= ~(7 << 8+(3*direction));
		encoding |= (num << 8+(3*direction));
	}
	
	public void setIsPinned(boolean pinned) {
		this.encoding &= ~(16);
		if (pinned) this.encoding |= 16;
	}
	
	public void setPinDirection(int direction) {
		this.encoding &= ~(224);
		this.encoding |= (direction << 5);
	}
	
	public String getInfo() {
		int move = this.encoding >>> 8;
		
		boolean color = getColor();
		int type = getType();
		
		int[] moves = new int[8];
		for (int i = 0; i < 8; ++i) {
			int n = move >>> (3*i);
		    moves[i] = n & 7;
		}
		
		String col = color ? "White" : "Black";
		String typ = type == 0 || type > 6 ? "null" : type == 1 ? "pawn" : type == 2 ? "knight" : type == 3? "bishop" : 
			type == 4? "rook" : type == 5? "queen" : "king";
		
		return col + " " + typ;
		
//		System.out.println(" N " + moves[0]);
//		System.out.println("NE " + moves[1]);
//		System.out.println(" E " + moves[2]);
//		System.out.println("SE " + moves[3]);
//		System.out.println(" S " + moves[4]);
//		System.out.println("SW " + moves[5]);
//		System.out.println(" W " + moves[6]);
//		System.out.println("NW " + moves[7]);
		
	}
	
	public static void main(String[] args) {
		
		Piece p = new Piece(1179652);
		System.out.println(p.getInfo());
		System.out.println(p.encoding);
		p.setIsPinned(true);
		p.setPinDirection(0);
		System.out.println(p.getInfo());
		System.out.println(p.encoding);
		
	}
	
}
