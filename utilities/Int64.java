package utilities;

public class Int64 {
	protected long high;
	protected long low;
	
	public Int64(long high, long low) {
		this.high = high;
		this.low = low;
	}
	
	public Int64() {
		this.high = 0;
		this.low = 0;
	}
	
	// modifiée par rapport à la traduction
    public static Int64 fromNumber(double nb) {
    	return new Int64((long) Math.floor(nb / 4.294967296E9), (long) nb);
    }
    
    public double toNumber() {
    	return this.high * 4.294967296E9 + this.low;
    }
}