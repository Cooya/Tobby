package frames;

import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Vector;

import controller.characters.Character;
import main.Instance;
import messages.Message;

public class Processor {
	private static Vector<Class<? extends Frame>> processFrames = new Vector<Class<? extends Frame>>();
	private static Hashtable<String, ProcessMethod> processMethods = new Hashtable<String, ProcessMethod>();
	
	static {
		processFrames.add(ConnectionFrame.class);
		processFrames.add(SynchronisationFrame.class);
		processFrames.add(FightContextFrame.class);
		processFrames.add(FightContextFrame.class);
	}
	
	public Processor(Instance instance, Character character) {
		Frame frame;
		Method[] methods;
		for(Class<? extends Frame> processFrame : processFrames) {
			try {
				frame = processFrame.getConstructor(Instance.class, Character.class).newInstance(instance, character);
			} catch(Exception e) {
				e.printStackTrace();
				return;
			}
			methods = processFrame.getDeclaredMethods();
			for(Method method : methods)
				processMethods.put(method.getName(), new ProcessMethod(method, frame));
		}
	}

	public void processMessage(Message msg) {
		String name = msg.getName();
		if(name == null) // message inconnu
			return;
		ProcessMethod processMethod = processMethods.get(msg.getName());
		if(processMethod == null) // message n'ayant pas de traitement associé
			return;
		processMethod.invoke();
	}
	
	private class ProcessMethod {
		private Method method;
		private Frame frame;
		
		private ProcessMethod(Method method, Frame frame) {
			this.method = method;
			this.frame = frame;
		}
		
		private void invoke() {
			try {
				this.method.invoke(this.frame);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}