package frames;

import gui.Controller;

import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import utilities.Reflection;
import controller.characters.Character;
import main.Instance;
import main.Log;
import messages.Message;

@SuppressWarnings("unchecked")
public class Processor {
	private static Vector<Class<? extends Frame>> processFrames = new Vector<Class<? extends Frame>>();
	
	private Map<String, Process> processTable;
	
	public static Vector<Long> perfTest = new Vector<Long>(); // TODO

	static {
		// récupération des différentes frames de traitement dans le package "frames"
		try {
			Class<?>[] classesArray = Reflection.getClasses("frames");
			for(Class<?> cl : classesArray)
				if(cl.getSuperclass() == Frame.class)
					processFrames.add((Class<? extends Frame>) cl);
		} catch(Exception e) {
			e.printStackTrace();
			Controller.getInstance().exit("Impossible to load frame classes.");
		}
		
		/*
		for(Class<? extends Frame> processFrame : processFrames) {
			for(Method method : processFrame.getDeclaredMethods())
				System.out.println(method);
			System.out.println();
		}
		*/
	}

	public Processor(Instance instance, Character character) {
		this.processTable = new Hashtable<String, Process>();
		Frame frame;
		Method[] methods;
		String msgName;
		for(Class<? extends Frame> processFrame : processFrames) {
			try {
				frame = processFrame.getConstructor(Instance.class, Character.class).newInstance(instance, character);
			} catch(Exception e) {
				e.printStackTrace();
				return;
			}
			methods = processFrame.getDeclaredMethods();
			for(Method method : methods)
				if(method.getName().equals("process")) {
					msgName = method.getParameterTypes()[0].getSimpleName();
					this.processTable.put(msgName, new Process(Message.getClassByName(msgName), frame));
				}
		}
	}

	public void processMessage(Message msg) {
		String name = msg.getName();
		if(name == null) { // message inconnu
			Log.warn("Unknown message with id = " + msg.getId() + ".");
			return;
		}
		Process process = this.processTable.get(msg.getName());
		if(process == null) // message n'ayant pas de traitement associé
			return;
		process.process(msg);
	}

	private class Process {
		private Class<Message> deserializationClass; // classe de désérialisation/sérialisation du message
		private Frame processFrame; // frame où se situe la méthode "process()"

		private Process(Class<Message> deserializationClass, Frame processFrame) {
			this.deserializationClass = deserializationClass;
			this.processFrame = processFrame;
		}

		private void process(Message msg) {
			try {
				msg = this.deserializationClass.cast(msg);
				msg = this.deserializationClass.getConstructor(Message.class).newInstance(msg);
				long startTime = System.nanoTime(); 
				this.processFrame.getClass().getDeclaredMethod("process", this.deserializationClass).invoke(processFrame, this.deserializationClass.cast(msg));
				perfTest.add(System.nanoTime() - startTime);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}