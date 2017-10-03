package gamedata.enums;

import java.lang.reflect.Field;

public abstract class ServerEnum {
	public static final int UNDEFINED = 0;
	public static final int Jiva = 1;
	//public static final int Rushu = 2;
	public static final int Djaul = 3;
	public static final int Raval = 4;
	public static final int Hecate = 5;
	public static final int Sumens = 6;
	public static final int Menalt = 7;
	//public static final int Rosal = 8;
	public static final int Maimane = 9;
	public static final int Silvosse = 10;
	public static final int Brumaire = 11;
	public static final int Pouchecot = 12;
	public static final int Silouate = 13;
	public static final int Domen = 14;
	public static final int Amayiro = 15;
	public static final int Rykke_Errel = 16;
	public static final int Hyrkul = 17;
	public static final int Helsephine = 18;
	public static final int Allister = 19;
	public static final int Otomai = 20;
	
	public static int[] getServerIdsList() {
		Field[] fields = ServerEnum.class.getFields();
		int[] serverIds = new int[fields.length - 1];
		for(int i = 0; i < fields.length - 1; ++i)
			try {
				serverIds[i] = fields[i + 1].getInt(null);
			} catch(IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		return serverIds;
	}
	
	public static String displayServersList() {
		StringBuffer str = new StringBuffer();
		Field[] fields = ServerEnum.class.getFields();
		for(Field field : fields) {
			if(field == fields[0])
				continue;
			try {
				str.append(field.getName() + " = " + field.getInt(null) + "\n");
			} catch(IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return str.toString();
	}
	
	public static boolean isHandledServer(int serverId) {
		if(serverId <= 0)
			return false;
		Field[] fields = ServerEnum.class.getFields();
		for(Field field : fields)
			try {
				if(field.getInt(null) == serverId)
					return true;
			} catch(IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		return false;
	}
	
	public static String getServerName(int serverId) {
		Field[] fields = ServerEnum.class.getFields();
		for(Field field : fields)
			try {
				if(field.getInt(null) == serverId)
					return field.getName();
			} catch(IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		return null;
	}
}