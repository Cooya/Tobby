package frames;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import utilities.Reflection;
import controller.characters.Character;
import main.Main;
import messages.NetworkMessage;

@SuppressWarnings("unchecked")
public class Processor {
	private static Vector<Class<? extends Frame>> processFrames = new Vector<Class<? extends Frame>>();
	
	private Map<String, Process> processTable;
	
	static {
		// récupération des différentes frames de traitement dans le package "frames"
		try {
			Class<?>[] classesArray = Reflection.getClassesInPackage("frames");
			for(Class<?> cl : classesArray)
				if(cl.getSuperclass() == Frame.class)
					processFrames.add((Class<? extends Frame>) cl);
		} catch(Exception e) {
			e.printStackTrace();
			Main.exit("Impossible to load frame classes.");
		}
		
		/*
		for(Class<? extends Frame> processFrame : processFrames) {
			for(Method method : processFrame.getDeclaredMethods())
				System.out.println(method);
			System.out.println();
		}
		*/
	}

	public Processor(Character character, String login) {
		this.processTable = new HashMap<String, Process>();
		Frame frame;
		Method[] methods;
		String msgName;
		for(Class<? extends Frame> processFrame : processFrames) {
			try {
				frame = processFrame.getConstructor(Character.class).newInstance(character);
			} catch(Exception e) {
				e.printStackTrace();
				return;
			}
			methods = processFrame.getDeclaredMethods();
			for(Method method : methods)
				if(method.getName().equals("process")) {
					msgName = method.getParameterTypes()[0].getSimpleName();
					this.processTable.put(msgName, new Process(frame, method));
				}
		}
	}
	
	public void processMessage(NetworkMessage msg) {
		Process process = this.processTable.get(msg.getName());
		if(process == null) // message inconnu ou n'ayant pas de traitement implémenté
			return;
		process.process(msg);
	}

	private class Process {
		private Frame processFrame; // frame où se situe la méthode "process()"
		private Method processMethod; // méthode "process()"

		private Process(Frame processFrame, Method processMethod) {
			this.processFrame = processFrame;
			this.processMethod = processMethod;
		}

		private void process(NetworkMessage msg) {
			msg.deserialize(); // unique appel de la fonction "deserialize()"
			try {
				this.processMethod.invoke(processFrame, msg);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}