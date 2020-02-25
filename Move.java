package model;

public class Move {
	
	int pieceIndex;   // index in pieces of moved piece
	int start;     // starting index of move
	int end;       // ending index of move
	int endPieceIndex;  // index in pieces of captured piece
	
	int special; //0 = none, 1 = EP, 2 = o-o-o, 3 = o-o-o-o, 4 = promote to Q, 5 = promote to R, 6 = promote to B, 7 = promote to N,
	             //8 = first king move, 9 = first king's rook move, 10 = first queen's rook move
	
	public Move(int p, int s, int e, int epi, int sp) {
		this.pieceIndex = p;
		this.start = s;
		this.end = e;
		this.endPieceIndex = epi;
		this.special = sp;
	}
	
	public String getName(Position pos) {
		String s = "";
		String initial = pos.pieces[pieceIndex].getTypeInitial();
		
		switch (special) {
		case 0:
			if (endPieceIndex > -1) {
				if (initial.equals("")) {
					initial = "abcdefgh".substring(start%8, (start%8)+1);
				}
				s = initial + "x" + "abcdefgh".substring(end%8, (end%8) + 1) + ((end/8)+1);
			} else {
				s = initial + "abcdefgh".substring(end%8, (end%8) + 1) + ((end/8)+1);
			}
			break;
		case 1:
			s = "abcdefgh".substring(start%8, (start%8)+1) + "x" + "abcdefgh".substring(end%8, (end%8)+1) + ((end/8) + 1);
			break;
		case 2:
			s = "O-O";
			break;
		case 3:
			s = "O-O-O";
			break;
		case 4:
			if (endPieceIndex > -1) {
				s = "abcdefgh".substring(start%8, (start%8)+1) + "x" + "abcdefgh".substring(end%8, (end%8)+1) + ((end/8) + 1);
			} else {
				s = "abcdefgh".substring(end%8, (end%8)+1) + ((end/8) + 1);
			}
			s += "=Q";
			break;
		case 5:
			if (endPieceIndex > -1) {
				s = "abcdefgh".substring(start%8, (start%8)+1) + "x" + "abcdefgh".substring(end%8, (end%8)+1) + ((end/8) + 1);
			} else {
				s = "abcdefgh".substring(end%8, (end%8)+1) + ((end/8) + 1);
			}
			s += "=R";
			break;
		case 6:
			if (endPieceIndex > -1) {
				s = "abcdefgh".substring(start%8, (start%8)+1) + "x" + "abcdefgh".substring(end%8, (end%8)+1) + ((end/8) + 1);
			} else {
				s = "abcdefgh".substring(end%8, (end%8)+1) + ((end/8) + 1);
			}
			s += "=B";
			break;
		case 7:
			if (endPieceIndex > -1) {
				s = "abcdefgh".substring(start%8, (start%8)+1) + "x" + "abcdefgh".substring(end%8, (end%8)+1) + ((end/8) + 1);
			} else {
				s = "abcdefgh".substring(end%8, (end%8)+1) + ((end/8) + 1);
			}
			s += "=N";
			break;
		case 8:
			s = "K" + "abcdefgh".substring(end%8, (end%8) + 1) + ((end/8)+1);
			break;
		case 9:
			s = "R" + "abcdefgh".substring(end%8, (end%8) + 1) + ((end/8)+1);
			break;
		case 10:
			s = "R" + "abcdefgh".substring(end%8, (end%8) + 1) + ((end/8)+1);
			break;
		}
		return s;
	}

	public String getDescription() {
		String s = "Move\n";
		s += ("   PieceIndex: " + pieceIndex + "\n");
		s += ("   Start     : " + start + "\n");
		s += ("   End       : " + end + "\n");
		s += ("   EndIndex  : " + endPieceIndex + "\n");
		s += ("   Special   : " + special + "\n");
		return s;
	}
	
}
