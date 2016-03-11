package controller;

import gui.Controller;
import main.Instance;
import messages.EmptyMessage;
import messages.exchanges.ExchangeReadyMessage;
import messages.interactions.NpcDialogReplyMessage;
import messages.interactions.NpcGenericActionRequestMessage;

public class MuleController extends CharacterController {
	protected int waitingMapId;
	
	public MuleController(Instance instance, String login, String password, int serverId) {
		super(instance, login, password, serverId);
		//this.waitingMapId = 153879299; // temporaire bien s�r
		this.waitingMapId = 84674566; // banque d'Astrub
	}
	
	private void processExchange() {
		if(Controller.isWorkmate(this.roleplayContext.actorDemandingExchange)) {
			EmptyMessage EM = new EmptyMessage("ExchangeAcceptMessage"); // accepter l'�change
			this.instance.outPush(EM);
			waitState(CharacterState.EXCHANGE_VALIDATED); // attendre que l'�change soit valid�
			if(isInterrupted())
				return;
			ExchangeReadyMessage ERM = new ExchangeReadyMessage();
			ERM.serialize(true, 2); // car il y a eu 2 actions lors de l'�change
			this.instance.outPush(ERM); // on valide de notre c�t�
			this.instance.log.p("Exchange validated from my side.");
			updateState(CharacterState.EXCHANGE_VALIDATED, false); // on enl�ve cet �tat pour les prochains �changes
		}
		else { // on refuse l'�change
			try {
				Thread.sleep(2000); // pour faire un peu normal
			} catch(InterruptedException e) {
				interrupt();
				return;
			}
			EmptyMessage EM = new EmptyMessage("LeaveDialogRequestMessage");
			this.instance.outPush(EM);
		}
	}
	
	protected void returnTripToAstrubBank() {
		this.mvt.goTo(84674566); // map o� se situe la banque
		
		useInteractive(317, 465440, 140242); // porte de la banque
		
		waitState(CharacterState.IS_LOADED);
		NpcGenericActionRequestMessage NGARM = new NpcGenericActionRequestMessage();
		NGARM.serialize(-10001, 3, this.infos.currentMap.id, this.instance.id); // on parle au banquier
		this.instance.outPush(NGARM);
		
		try {
			sleep(1000); // on attend la question
		} catch (InterruptedException e) {
			interrupt();
			return;
		}
		
		NpcDialogReplyMessage NDRM = new NpcDialogReplyMessage();
		NDRM.serialize(259); // on s�lectionne la r�ponse
		this.instance.outPush(NDRM);
		
		try {
			sleep(2000); // on attend l'affichage de l'inventaire
		} catch (InterruptedException e) {
			interrupt();
			return;
		}
		
		EmptyMessage EM = new EmptyMessage("ExchangeObjectTransfertAllFromInvMessage");
		this.instance.outPush(EM); // on transf�re tous les objets de l'inventaire
		
		try {
			sleep(2000);
		} catch (InterruptedException e) {
			interrupt();
			return;
		}
		
		EM = new EmptyMessage("LeaveDialogRequestMessage");
		this.instance.outPush(EM); // on ferme l'inventaire
		
		try {
			sleep(1000); // on attend que l'inventaire se ferme
		} catch (InterruptedException e) {
			interrupt();
			return;
		}
		
		this.mvt.moveTo(396, false); // on sort de la banque
		if(interrupted())
			return;
		
		updateState(CharacterState.IS_LOADED, false);
	}
	 
	private boolean needToGoBank(float percentage) { // percentage < 1
		if(this.infos.weight > this.infos.weightMax * percentage)
			return true;
		return false;
	}
	
	public void run() {
		while(!isInterrupted() && waitState(CharacterState.IS_FREE)) { // attente d'�tat importante afin de laisser le temps aux pods de se mettre � jour apr�s un �change
			if(needToGoBank(0.5f)) { // + de 50% de l'inventaire occup�
				this.instance.log.p("Need to go to empty inventory at Astrub bank.");
				returnTripToAstrubBank();
			}
			this.mvt.goTo(this.waitingMapId);
			if(waitState(CharacterState.PENDING_DEMAND)) { // on attend qu'un combattant lance un �change
				this.instance.log.p("Exchange demand received.");
				processExchange();
			}
		}
		System.out.println("Thread controller of instance with id = " + this.instance.id + " terminated.");
	}
}