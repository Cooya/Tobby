package utilities;

public class BooleanByteWrapper {
	
	public static int setFlag(int i1, int i2, boolean b) {
		switch(i2) {
			case 0 : if(b) i1 |= 1; else i1 &= 0xFF - 1; break;
			case 1 : if(b) i1 |= 2; else i1 &= 0xFF - 2; break;
			case 2 : if(b) i1 |= 4; else i1 &= 0xFF - 4; break;
			case 3 : if(b) i1 |= 8; else i1 &= 0xFF - 8; break;
			case 4 : if(b) i1 |= 16; else i1 &= 0xFF - 16; break;
			case 5 : if(b) i1 |= 32; else i1 &= 0xFF - 32; break;
			case 6 : if(b) i1 |= 64; else i1 &= 0xFF - 64; break;
			case 7 : if(b) i1 |= 128; else i1 &= 0xFF - 128; break;
            default : throw new Error("Bytebox overflow.");
		}
		return i1;
	}

	public static boolean getFlag(int i1, int i2) {
		switch(i2) {
			case 0 : return (i1 & 1) != 0;
			case 1 : return (i1 & 2) != 0;
			case 2 : return (i1 & 4) != 0;
			case 3 : return (i1 & 8) != 0;
			case 4 : return (i1 & 16) != 0;
			case 5 : return (i1 & 32) != 0;
			case 6 : return (i1 & 64) != 0;
			case 7 : return (i1 & 128) != 0;
			default : throw new Error("Bytebox overflow.");
		}
	}
}
