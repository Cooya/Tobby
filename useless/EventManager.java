package main;

import java.util.Vector;

import utilities.Log;

public class EventManager {
	private static final boolean DEBUG = true;
	private Vector<Event> eventsVector;
	
	public EventManager() {
		eventsVector = new Vector<Event>();
	}
	
	// seul le thread principal entre ici
	public synchronized void emit(Event event) {
		if(DEBUG)
			Log.p("Event emitted : " + event);
		eventsVector.add(event);
		notify();
	}
	
	// seul le CC entre ici
	public synchronized Event listen() {
		if(eventsVector.size() == 0) {	
			try {
				wait();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Event caughtEvent = eventsVector.firstElement();
		eventsVector.remove(caughtEvent);
		if(DEBUG)
			Log.p("Event caught : " + caughtEvent);
		
		System.out.println("///" + eventsVector.size());
		
		return caughtEvent;
		
	}
	
	public synchronized Event listen(int timeout, Event expectedEvent) {
		for(Event event : eventsVector)
			if(event == expectedEvent) { // si l'event attendu est déjà là
				eventsVector.remove(event);
				if(DEBUG)
					Log.p("Event caught : " + event);
				
				System.out.println("///" + eventsVector.size());
				
				return expectedEvent;
			}
		long startTime = System.currentTimeMillis();
		long currentTime;
		while((currentTime = System.currentTimeMillis() - startTime) < timeout) {
			try {
				wait(timeout - currentTime);  // sinon on l'attend
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(!eventsVector.isEmpty()) {
				Event lastEvent = eventsVector.lastElement();
				if(lastEvent == expectedEvent) { // si le dernier event reçu est celui attendu
					eventsVector.remove(lastEvent);
					if(DEBUG)
						Log.p("Event caught : " + expectedEvent);
					
					System.out.println("///" + eventsVector.size());
					
					return expectedEvent; 
				}
			}
		}
		
		System.out.println("///" + eventsVector.size());
		
		System.out.println("TIMEOUT");
		return Event.TIMEOUT; // si on ne l'a pas reçu à temps
	}
}