package utilities;

// Etant donné que "low" est un entier non signé, certaines manipulations doivent être effectuées. 
class Int64 {
	protected int low;
	protected int high;

	public Int64(int low, int high) {
		this.low = low; // unsigned int
		this.high = high;
	}

	public Int64() {
		this.low = 0;
		this.high = 0;
	}

	// on convertit un "unsigned long" en "signed int"
	public static Int64 fromNumber(double nb) {
		return new Int64((int) ((long) nb & 0xFFFFFFFF), (int) Math.floor(nb / 4.294967296E9));
	}

	// on convertit un "signed int" en "unsigned long" 
	public double toNumber() {
		return high * 4.294967296E9 + (this.low & 0x00000000FFFFFFFFL);
	}

	public String toString() {
		return "[Int64 : high = " + this.high + ", low = " + this.low + "]";
	}
}