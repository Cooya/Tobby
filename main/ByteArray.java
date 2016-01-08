package main;

public class ByteArray {	
    private static final int INT_SIZE = 32;  
    private static final int SHORT_SIZE = 16;
    private static final int SHORT_MIN_VALUE = -32768;
    private static final int SHORT_MAX_VALUE = 32767;
    private static final int UNSIGNED_SHORT_MAX_VALUE = 65536;
    private static final int CHUNCK_BIT_SIZE = 7;
    private static final int MAX_ENCODING_LENGTH = (int) Math.ceil(INT_SIZE / CHUNCK_BIT_SIZE);
    private static final int MASK_10000000 = 128;
    private static final int MASK_01111111 = 127;
	
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
		this.size = size;
	}
	
	public ByteArray(byte[] array) {
		this.array = array;
		this.pos = 0;
		this.size = 0;
	}
	
	public byte readByte() {
		return this.array[this.pos++];
	}
	
	public short readShort() { // un short est toujours signé en Java
		return (short) ((short) this.array[this.pos++] * 256 + this.array[this.pos++ + 1]);
	}
	
	public char readUShort() { // un char n'est pas signé en Java et est codé sur 2 octets comme un short
		return (char) readShort();
	}
	
	public char[] readUTF() {
		char len = readUShort();
		char[] utf = new char[len];
		for(int i = 0; i < len; ++i)
			utf[i] = (char) this.array[this.pos++];
		utf[len] = '\0';
		return utf;
	}
	
	public int readVarInt() { // copie des sources du jeu
		int val4 = 0;
		int val1 = 0;
		int val2 = 0;
		boolean val3 = false;
		while(val2 < INT_SIZE) {
			val4 = readByte();
			val3 = (val4 & MASK_10000000) == MASK_10000000;
			if(val2 > 0)
				val1 += ((val4 & MASK_01111111) << val2);
			else
				val1 += val4 & MASK_01111111;
			val2 += CHUNCK_BIT_SIZE;
			if(!val3)
				return val1;
		}
		assert true;
		return 0;
	}
	
	public void writeByte(byte b) {
		assert(this.pos >= this.array.length);
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
		writeByte((byte) (s & 0xff));
		writeByte((byte) ((s >> 8) & 0xff));
	}
	
	public void writeUShort(char s) {
		writeByte((byte) (s & 0xff));
		writeByte((byte) ((s >> 8) & 0xff));
	}
	
	public void writeUTF(char[] utf) {
		writeUShort((char) utf.length);
		for(int i = 0; i < utf.length; ++i)
			this.array[this.pos++ + i ] = (byte) utf[i];
		this.size += utf.length + 2;
	}
	
	public void writeVarInt(int i) { // copie des sources du jeu
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
}
