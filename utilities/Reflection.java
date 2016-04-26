package utilities;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import main.Main;
import messages.Message;

public class Reflection {
	private static final ProtectionDomain currentDomain = Main.class.getProtectionDomain();
	/*
	private static Class<?>[] messageClasses;
	
	static {
		try {
			messageClasses = getClasses("messages");
		} catch(ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void displayMessageFields(Message msg) {
		String msgName = msg.getName();
		for(Class<?> cl : messageClasses)
			if(cl.getSimpleName().equals(msgName))
				try {
					explore(cl.getConstructor(Message.class).newInstance(msg));
				} catch(Exception e) {
					e.printStackTrace();
				}
	}
	*/
	
	public static void explore(Object object) {
		displayAllFields(object, "");
	}
	
	private static void displayAllFields(Object o, String gap) {
		Class<?> current = o.getClass();
		while(current.getSuperclass() != null) {
			Field[] fields = current.getDeclaredFields();
			for(Field field : fields) {
				try {
					field.setAccessible(true);
					if(field.getDeclaringClass() == Message.class) // les attributs de la classe Message ne nous intéressent pas
						continue;
					System.out.println(gap + field.getName() + " = " + field.get(o));
					if(field.get(o).getClass().getProtectionDomain() == currentDomain) // si c'est un objet dont la classe est une classe du projet courant
						displayAllFields(field.get(o), gap + "   ");
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		    current = current.getSuperclass();
		}
	}
	
	public static Class<?>[] getClasses(String packageName) throws ClassNotFoundException, IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<File>();
		while(resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(new File(resource.getFile()));
		}
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
		for(File directory : dirs)
			classes.addAll(findClasses(directory, packageName));
		return classes.toArray(new Class[classes.size()]);
	}

	private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		if(!directory.exists())
			return classes;
		File[] files = directory.listFiles();
		for(File file : files) {
			if(file.isDirectory())
				classes.addAll(findClasses(file, packageName + "." + file.getName()));
			else if(file.getName().endsWith(".class"))
				classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
		}
		return classes;
	}
}