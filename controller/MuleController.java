package controller;

import controller.pathfinding.PathsCache;
import main.Instance;
import main.Log;
import messages.EmptyMessage;
import messages.exchange.ExchangeReadyMessage;

public class MuleController extends CharacterController {
	protected int waitingMapId;
	
	public MuleController(Instance instance, String login, String password, int serverId) {
		super(instance, login, password, serverId);
		this.waitingMapId = 153879299; // temporaire bien sûr
	}
	
	private void goToWaitingPosition() {
		if(this.infos.currentMap.id == this.waitingMapId)
			return;
		PathsCache.moveTo(this.waitingMapId, this);
	}
	
	private void processExchange() {
		EmptyMessage EM = new EmptyMessage("ExchangeAcceptMessage"); // accepter l'échange
		this.instance.outPush(EM);
		updateState(CharacterState.IN_EXCHANGE, true);
		waitState(CharacterState.EXCHANGE_VALIDATED); // attendre que l'échange soit validé
		if(isInterrupted())
			return;
		ExchangeReadyMessage ERM = new ExchangeReadyMessage();
		ERM.serialize(true, 2);
		this.instance.outPush(ERM); // on valide de notre côté
		updateState(CharacterState.EXCHANGE_VALIDATED, false);
	}
	/*
	public void run() {
		goToWaitingPosition();
		waitState(CharacterState.PENDING_DEMAND); // on attend qu'un combattant lance un échange
		if(isInterrupted())
			return;
		processExchange();
		
		this.instance.log.p(Log.Status.CONSOLE, "Thread controller of instance with id = " + this.instance.id + " terminated.");
	}
	*/
}