package utilities;

public class ByteArray {	
    private static final int INT_SIZE = 32;  
    private static final int SHORT_SIZE = 16;
    //private static final int SHORT_MIN_VALUE = -32768;
    private static final int SHORT_MAX_VALUE = 32767;
    private static final int UNSIGNED_SHORT_MAX_VALUE = 65536;
    private static final int CHUNCK_BIT_SIZE = 7;
    //private static final int MAX_ENCODING_LENGTH = (int) Math.ceil(INT_SIZE / CHUNCK_BIT_SIZE);
    private static final int MASK_10000000 = 128;
    private static final int MASK_01111111 = 127;
	private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

	private static final int defaultSize = 8192;
	private byte[] array;
	private int pos;
	private int size;

	public ByteArray() {
		this(defaultSize);
	}

	public ByteArray(int size) {
		this.array = new byte[size];
		this.pos = 0;
		this.size = 0;
	}

	public ByteArray(byte[] array) { // tableau complet
		this.array = array;
		this.pos = 0;
		this.size = array.length;
	}
	
	public ByteArray(byte[] array, int size) { // à utiliser lorsque le tableau passé est incomplet
		this.array = new byte[size];
		for(int i = 0; i < size; ++i)
			this.array[i] = array[i];
		this.pos = 0;
		this.size = size;
	}
	
	private void extendArray() {
		byte[] newArray = new byte[this.array.length * 2];
		for(int i = 0; i < this.array.length; ++i)
			newArray[i] = this.array[i];
		this.array = newArray;
	}

	public int getPos() {
		return this.pos;
	}
	
	public void incPos(int nb) {
		this.pos += nb;
	}
	
	public void setPos(int nb) {
		this.pos = nb;
	}

	public int getSize() {
		return this.size;
	}
	
	public void trimArray(int nb) {
		this.size -= nb;
	}

	public int remaining() {
		return this.size - this.pos;
	}

	public boolean endOfArray() {
		return this.pos == this.size;
	}

	public static void printBytes(byte[] bytes, String format, int size) {
		System.out.print(bytes.length + " bytes : ");
		if(format == "dec") {
			for(int i = 0; i < size; ++i)
				System.out.print(bytes[i] + " ");
			System.out.println();
		}
		else if(format == "hex") {
			char[] hexChars = new char[size * 3];
			for ( int j = 0; j < size; j++ ) {
				int v = bytes[j] & 0xFF;
				hexChars[j * 3] = hexArray[v >>> 4];
				hexChars[j * 3 + 1] = hexArray[v & 0x0F];
				hexChars[j * 3 + 2] = ' ';
			}
			System.out.println(new String(hexChars));
		}
		else if(format == "ascii") {
			for(int i = 0; i < size; ++i)
				System.out.print((char) bytes[i]);
			System.out.println();
		}
		else
			System.out.println("Unknown display format.");
	}
	
	public static void printBytes(byte[] bytes, String format) {
		printBytes(bytes, format, bytes.length);
	}
	
	public static void printBytes(byte[] bytes) {
		printBytes(bytes, "hex", bytes.length);
	}

	public void printArray(String format) {
		printBytes(bytes(), format);
	}

	public byte[] bytesFromPos() {
		if(this.size <= this.pos)	
			throw new Error("Size lower than position");
		byte[] clone = new byte[this.size - this.pos];
		for(int i = 0; i < clone.length; ++i)
			clone[i] = this.array[i + this.pos];
		return clone;
	}

	public byte[] bytes() {
		byte[] clone = new byte[this.size];
		for(int i = 0; i < this.size; ++i)
			clone[i] = this.array[i];
		return clone;
	}
	
	public static byte[] toBytes(int[] array) {
		byte[] bytes = new byte[array.length];
		for(int i = 0; i < array.length; ++i)
			bytes[i] = (byte) array[i];
		return bytes;
	}

	public int readByte() {
		return this.array[this.pos++] & 0xFF;
	}

	public boolean readBoolean() {
		return readByte() == 1 ? true : false;
	}

	public byte[] readBytes(int size) {
		byte[] bytes = new byte[size];
		for(int i = 0; i < size; ++i)
			bytes[i] = (byte) readByte();
		return bytes;
	}

	public short readShort() { // pas de unsigned short en Java
		short s = (short) ((short) readByte() * 256 + readByte());
		return s;
	}

	public String readUTF() {
		return readUTFBytes(readShort());
	}
	
	public String readUTFBytes(int len) {
		char[] utf = new char[len];
		for(int i = 0; i < len; ++i)
			utf[i] = (char) readByte();
		return new String(utf);
	}

	public void writeByte(byte b) {
		if(this.size == this.array.length)
			extendArray();
		this.array[this.pos++] = b;
		this.size++;
	}

	public void writeBytes(ByteArray buffer) {
		for(int i = 0; i < buffer.size; ++i)
			writeByte(buffer.array[i]);
	}

	public void writeBytes(byte[] bytes) {
		for(int i = 0; i < bytes.length; ++i)
			writeByte(bytes[i]);
	}

	public void writeShort(short s) {
		writeByte((byte) (s >> 8));
		writeByte((byte) (s & 0xff));
	}

	public void writeInt(int i) {
		writeByte((byte) (i >>> 24));
		writeByte((byte) (i >>> 16));
		writeByte((byte) (i >>> 8));
		writeByte((byte) (i));
	}

	public void writeUTF(String utf) {
		writeShort((short) utf.length());
		writeUTFBytes(utf);
	}

	public void writeUTFBytes(String utf) {
		int length = utf.length();
		for(int i = 0; i < length; ++i)
			writeByte((byte) utf.charAt(i));
	}

	public int readVarInt() {
		int var4 = 0;
		int var1 = 0;
		int var2 = 0;
		boolean var3 = false;
		while(var2 < INT_SIZE) {
			var4 = readByte();
			var3 = (var4 & MASK_10000000) == MASK_10000000;
			if(var2 > 0)
				var1 += ((var4 & MASK_01111111) << var2);
			else
				var1 += var4 & MASK_01111111;
			var2 += CHUNCK_BIT_SIZE;
			if(!var3)
				return var1;
		}
		throw new Error("Too much data");
	}
	
	public int readVarShort() {
		int var4 = 0;
		int var1 = 0;
		int var2 = 0;
		boolean var3 = false;
		while(var2 < SHORT_SIZE) {
			var4 = readByte();
			var3 = (var4 & MASK_10000000) == MASK_10000000;
			if(var2 > 0)
				var1 += (var4 & MASK_01111111) << var2;
			else
				var1 += var4 & MASK_01111111;
			var2 += CHUNCK_BIT_SIZE;
			if(!var3) {
				if(var1 > SHORT_MAX_VALUE)
					var1 -= UNSIGNED_SHORT_MAX_VALUE;
				return var1;
			}
		}
		throw new Error("Too much data");
	}
	
	public Int64 readVarLong() {
		int var3 = 0;
		Int64 var2 = new Int64();
		int var4 = 0;
		while(true) {
			var3 = readByte();
			System.out.println(var3);
			if(var4 == 28)
				break;
			if(var3 >= 128) {
				var2.low = var2.low | (var3 & 127) << var4;
				var4 += 7;
				continue;
			}
			var2.low = var2.low | var3 << var4;
			return var2;
		}
		if(var3 >= 128) {
			var3 = var3 & 127;
			var2.low = var2.low | var3 << var4;
			var2.high = var3 >>> 4;
			var4 = 3;
			while(true) {
				var3 = readByte();
				System.out.println(var3);
				if(var4 < 32)
					if(var3 >= 128)
						var2.high = var2.high | (var3 & 127) << var4;
					else
						break;
				var4 = var4 + 7;
			}
			var2.high = var2.high | var3 << var4;
			return var2;
		}
		var2.low = var2.low | var3 << var4;
		var2.high = var3 >>> 4;
		return var2;
	}

	public void writeVarInt(int i) {
		int var5 = 0;
		ByteArray var2 = new ByteArray();
		if(i >= 0 && i <= MASK_01111111) {
			var2.writeByte((byte) i); 
			writeBytes(var2);
			return;
		}
		int var3 = i;
		ByteArray var4 = new ByteArray();
		while(var3 != 0) {
			var4.writeByte((byte) (var3 & MASK_01111111));
			var5 = var3 & MASK_01111111;
			var3 = var3 >>> CHUNCK_BIT_SIZE;
				if(var3 > 0)
					var5 = var5 | MASK_10000000;
				var2.writeByte((byte) var5);
		}
		writeBytes(var2);
	}

	public void writeVarShort(int s) {
		int var5 = 0;
		ByteArray var2 = new ByteArray();
		if(s >= 0 && s <= MASK_01111111) {
			var2.writeByte((byte) s);
			writeBytes(var2);
			return;
		}
		int var3 = s & 65535;
		ByteArray var4 = new ByteArray();
		while(var3 != 0) {
			var4.writeByte((byte) (var3 & MASK_01111111));
			var5 = var3 & MASK_01111111;
			var3 = var3 >>> CHUNCK_BIT_SIZE;
			if(var3 > 0)
				var5 = var5 | MASK_10000000;
			var2.writeByte((byte) var5);
		}
		writeBytes(var2);
	}

	public void writeVarLong(Int64 db) {
		int var3 = 0;
		Int64 var2 = db;
		if(var2.low == 0)
			writeInt32(var2.low);
		else {
			var3 = 0;
			while(var3 < 4) {
				writeByte((byte) (var2.low & 127 | 128));
				var2.low = var2.low >>> 7;
				var3++;
			}
			if((var2.high & 268435455 << 3) == 0)
				writeByte((byte) (var2.high << 4 | var2.low));
			else {
				writeByte((byte) ((var2.high << 4 | var2.low) & 127 | 128));
				writeInt32(var2.high >>> 3);
			}
		}
	}
	
	private void writeInt32(int i) {
		while(i >= 128) {
			writeByte((byte) (i & 127 | 128));
			i = i >>> 7;
		}
		writeByte((byte) i);
	}
}
