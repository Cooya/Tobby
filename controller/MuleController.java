package controller;

import controller.pathfinding.MapsPathfinder;
import controller.pathfinding.Path;
import main.Instance;
import main.Log;
import messages.EmptyMessage;
import messages.exchange.ExchangeReadyMessage;

public class MuleController extends CharacterController {
	protected int waitingMapId;
	
	public MuleController(Instance instance, String login, String password, int serverId) {
		super(instance, login, password, serverId);
		this.waitingMapId = 153879299; // temporaire bien s�r
	}
	
	private void goToWaitingPosition() {
		if(this.infos.currentMap.id == this.waitingMapId)
			return;
		MapsPathfinder pathfinder = new MapsPathfinder(this.infos.currentCellId);
		Path path = pathfinder.compute(this.infos.currentMap.id, this.waitingMapId);
		path.run(this);
	}
	
	private void processExchange() {
		EmptyMessage EM = new EmptyMessage("ExchangeAcceptMessage"); // accepter l'�change
		this.instance.outPush(EM);
		this.inExchange.state = false;
		waitState(4); // attendre que l'�change soit valid�
		if(isInterrupted())
			return;
		ExchangeReadyMessage ERM = new ExchangeReadyMessage();
		ERM.serialize(true, 2);
		this.instance.outPush(ERM); // on valide de notre c�t�
		this.exchangeValidated.state = false;
	}
	/*
	public void run() {
		goToWaitingPosition();
		waitState(3); // on attend qu'un combattant lance un �change
		if(isInterrupted())
			return;
		processExchange();
		
		this.instance.log.p(Log.Status.CONSOLE, "Thread controller of instance with id = " + this.instance.id + " terminated.");
	}
	*/
}