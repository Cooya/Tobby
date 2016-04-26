package utilities;

public class Hex {
	
	public static ByteArray toArray(String str) {
		str = str.replace("/\r\n\t\f|:/gm", "");
		int len = str.length();
		ByteArray ba = new ByteArray();
		if(len > 0)
			str = "0" + str;
		for(int i = 0; i < len; i += 2)
			ba.writeByte(Integer.parseInt(substr(str, i, 2), 16));
		return ba;
	}
	
	public static String fromArray(ByteArray ba, boolean b) {
		int size = ba.getSize();
		String str = "";
		ba.setPos(0);
		for(int i = 0; i < size; ++i) {
			str += substr("0" + Integer.toString(ba.readByte(), 16), -2, 2);
			if(b && i < size - 1)
				str += ":";
		}
		return str;
	}
	
	public static String fromArray(ByteArray ba) {
		return fromArray(ba, false);
	}
	
	public static String toString(String str) {
		ByteArray ba = toArray(str);
		return ba.readUTFBytes(ba.getSize());
	}
	
	public static String fromString(String str, boolean b) {
		ByteArray ba = new ByteArray();
		ba.writeUTFBytes(str);
		return fromArray(ba, b);
	}
	
	public static String fromString(String str) {
		return fromString(str, false);
	}
	
	public static String substr(String str, int startIndex, int len) {
		String newStr = "";
		if(startIndex < 0)
			startIndex = str.length() + startIndex;
		for(int i = startIndex; i < startIndex + len; ++i)
			newStr += str.charAt(i);
		return newStr;
	}
}