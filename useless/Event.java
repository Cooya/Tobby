package controller;

public enum Event {
	CHARACTER_LOADED,
	FIGHT_START,
	FIGHT_END,
	GAME_TURN_START,
	WEIGHT_MAX,
	LEVEL_UP,
	EXCHANGE_DEMAND,
	EXCHANGE_START,
	EXCHANGE_VALIDATION,
	EXCHANGE_LEAVE,
	NEW_ACTOR
}

/*
private void switchEvent(Event event) {
	switch(event) {
		case CHARACTER_LOADED : this.isLoaded.state = true; break;
		case FIGHT_START : this.inFight.state = true; break;
		case FIGHT_END : this.inFight.state = false; break;
		case GAME_TURN_START : this.inGameTurn.state = true; break;
		case WEIGHT_MAX : this.needToEmptyInventory.state = true; break;
		case LEVEL_UP : this.levelUp.state = true; break;
		case EXCHANGE_DEMAND : this.inExchange.state = true; break;
		case EXCHANGE_VALIDATION : this.exchangeValidated.state = true; break;
		case EXCHANGE_LEAVE : this.inExchange.state = false; break;
		case EXCHANGE_START : this.inExchange.state = true; break;
		case NEW_ACTOR : this.newActorOnMap.state = true; break;
		default : new FatalError("Unexpected event caught : " + event); break;
	}
}
*/

/*
protected class State {
	protected boolean state;
	
	protected State(boolean state) {
		this.state = state;
	}
	
	protected void update(boolean newState) {
		this.state = newState;
	}
}

protected State isLoaded; // entrée en jeu et changement de map
protected State inMovement;
protected State inFight;
protected State inGameTurn;
protected State inRegeneration;
protected State needToEmptyInventory;
protected State levelUp;
protected State inExchange;
protected State exchangeValidated;
protected State newActorOnMap;

this.isLoaded = new State(false);
this.inMovement = new State(false); 
this.inFight = new State(false); 
this.inGameTurn = new State(false); 
this.inRegeneration = new State(false); 
this.needToEmptyInventory = new State(false);
this.levelUp = new State(false);
this.inExchange = new State(false);
this.exchangeValidated = new State(false);
this.newActorOnMap = new State(false);
*/