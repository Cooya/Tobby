package utilities;

import java.lang.reflect.Field;
import java.security.ProtectionDomain;

import main.Main;

public class Reflection {
	private static final ProtectionDomain currentDomain = Main.class.getProtectionDomain();
	
	public static void explore(Object o) {
		displayAllFields(o, "");
	}
	
	private static void displayAllFields(Object o, String gap) {
		Class<?> current = o.getClass();
		while(current.getSuperclass() != null) {
			Field[] fields = current.getDeclaredFields();
			for(Field field : fields) {
				try {
					field.setAccessible(true);
					System.out.println(gap + field.getName() + " = " + field.get(o));
					if(field.get(o).getClass().getProtectionDomain() == currentDomain)
						displayAllFields(field.get(o), gap + "   ");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		    current = current.getSuperclass();
		}
	}
}