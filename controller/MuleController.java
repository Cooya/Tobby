package controller;

import main.Instance;
import main.Log;
import messages.EmptyMessage;
import messages.exchange.ExchangeReadyMessage;
import messages.interactions.InteractiveUseRequestMessage;
import messages.interactions.NpcDialogReplyMessage;
import messages.interactions.NpcGenericActionRequestMessage;
import messages.synchronisation.BasicPingMessage;

public class MuleController extends CharacterController {
	protected int waitingMapId;
	
	public MuleController(Instance instance, String login, String password, int serverId) {
		super(instance, login, password, serverId);
		this.waitingMapId = 153879299; // temporaire bien sûr
	}
	
	private void processExchange() {
		EmptyMessage EM = new EmptyMessage("ExchangeAcceptMessage"); // accepter l'échange
		this.instance.outPush(EM);
		waitState(CharacterState.EXCHANGE_VALIDATED); // attendre que l'échange soit validé
		if(isInterrupted())
			return;
		ExchangeReadyMessage ERM = new ExchangeReadyMessage();
		ERM.serialize(true, 1);
		this.instance.outPush(ERM); // on valide de notre côté
		this.instance.log.p("Exchange validation from my side.");
		updateState(CharacterState.EXCHANGE_VALIDATED, false); // on enlève cet état pour les prochains échanges
		waitState(CharacterState.IS_FREE);
	}
	
	protected void returnTripToAstrubBank() {
		MovementAPI.goTo(84674566, this); // map où se situe la banque
		if(interrupted())
			return;
		
		MovementAPI.moveTo(317, false, this); // entrée de la banque
		if(interrupted())
			return;
		
		InteractiveUseRequestMessage IURM = new InteractiveUseRequestMessage();
		IURM.serialize(465440, 140242, this.instance.id); // porte de la banque
		this.instance.outPush(IURM);
		
		this.states.put(CharacterState.IS_LOADED, false);
		waitState(CharacterState.IS_FREE);
		if(interrupted())
			return;
		
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
		NDRM.serialize(259); // on sélectionne la réponse
		this.instance.outPush(NDRM);
		
		try {
			sleep(2000); // on attend l'affichage de l'inventaire
		} catch (InterruptedException e) {
			interrupt();
			return;
		}
		
		EmptyMessage EM = new EmptyMessage("ExchangeObjectTransfertAllFromInvMessage");
		this.instance.outPush(EM); // on transfère tous les objets de l'inventaire
		
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
		
		MovementAPI.moveTo(396, false, this); // on sort de la banques
		if(interrupted())
			return;
		
		this.states.put(CharacterState.IS_LOADED, false);
	}
	 
	private boolean needToGoBank(float percentage) { // percentage < 1
		if(this.infos.weight > this.infos.weightMax * percentage)
			return true;
		return false;
	}
	
	public void run() {
		waitState(CharacterState.IS_FREE);
		
		while(!isInterrupted()) {
			if(needToGoBank(0.5f)) { // + de 50% de l'inventaire occupé
				this.instance.log.p("Need to go to empty inventory at Astrub bank.");
				returnTripToAstrubBank();
			}
			MovementAPI.goTo(this.waitingMapId, this);
			
			while(!isInterrupted()) {
				waitState(CharacterState.PENDING_DEMAND); // on attend qu'un combattant lance un échange
				if(this.states.get(CharacterState.PENDING_DEMAND)) {
					this.instance.log.p("Exchange demand received.");
					processExchange();
					break;
				}
				else {
					BasicPingMessage BPM = new BasicPingMessage();
					BPM.serialize(false);
					this.instance.outPush(BPM);
					this.instance.log.p("Sending a ping request to server for stay connected.");
				}	
			}	
		}
		this.instance.log.p(Log.Status.CONSOLE, "Thread controller of instance with id = " + this.instance.id + " terminated.");
	}
}