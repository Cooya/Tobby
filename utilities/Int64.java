package utilities;

public class Int64 {
	protected int high;
	protected int low;
	
	public Int64(int high, int low) {
		this.high = high;
		this.low = low;
	}
	
	public Int64() {
		this.high = 0;
		this.low = 0;
	}
	
    public static Int64 fromNumber(double nb) {
    	return new Int64((int) nb, (int) Math.floor(nb / 4.294967296E9));
    }
    
    public long toNumber() {
    	return (long) (this.high * 4.294967296E9 + this.low);
    }
}
