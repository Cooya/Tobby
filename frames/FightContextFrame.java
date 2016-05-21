package frames;

import gamedata.fight.FightResultListEntry;
import gamedata.fight.FightResultPlayerListEntry;
import controller.CharacterState;
import controller.characters.Character;
import controller.characters.Fighter;
import messages.fights.GameActionAcknowledgementMessage;
import messages.fights.GameActionFightNoSpellCastMessage;
import messages.fights.GameActionFightPointsVariationMessage;
import messages.fights.GameActionFightSpellCastMessage;
import messages.fights.GameFightEndMessage;
import messages.fights.GameFightShowFighterMessage;
import messages.fights.GameFightStartingMessage;
import messages.fights.GameFightSynchronizeMessage;
import messages.fights.GameFightTurnEndMessage;
import messages.fights.GameFightTurnReadyMessage;
import messages.fights.GameFightTurnReadyRequestMessage;
import messages.fights.GameFightTurnStartPlayingMessage;
import messages.fights.SequenceEndMessage;

public class FightContextFrame extends Frame {
	private Fighter fighter;

	public FightContextFrame(Character character) {
		super(character);
		if(character instanceof Fighter)
			this.fighter = (Fighter) character; // petit raccourci
	}
	
	protected void process(GameFightStartingMessage GFSM) {
		this.character.log.p("Starting a fight.");
	}
	
	protected void process(GameFightShowFighterMessage GFSFM) {
		this.fighter.fightContext.newFighter(GFSFM.informations);
		this.character.updateState(CharacterState.NEW_ACTOR_IN_FIGHT, true);
	}
	
	protected void process(GameFightTurnReadyRequestMessage GFTRRM) {
		GameFightTurnReadyMessage GFTRM = new GameFightTurnReadyMessage();
		GFTRM.isReady = true;
		this.character.net.send(GFTRM);
		this.character.log.p("Next turn.");
	}
	
	protected void process(GameFightTurnStartPlayingMessage GFTSPM) {
		this.character.log.p("Begin of my game turn.");
		this.character.updateState(CharacterState.IN_GAME_TURN, true);
	}
	
	protected void process(GameFightSynchronizeMessage GFSM) {
		this.fighter.fightContext.setFightContext(GFSM.fighters);
		this.character.log.p("Fight context set.");
	}
	
	protected void process(GameActionFightSpellCastMessage GAFSCM) {
		if(GAFSCM.sourceId == this.character.infos.getCharacterId())
			this.character.updateState(CharacterState.SPELL_CASTED, true);
	}
	
	protected void process(GameActionFightNoSpellCastMessage GAFNSCM) {
		this.character.updateState(CharacterState.SPELL_CASTED, true);
	}
	
	protected void process(GameActionFightPointsVariationMessage GAFPVM) {
		if(this.fighter.fightContext.self != null)
			this.fighter.fightContext.self.stats.actionPoints -= GAFPVM.delta;
	}
	
	protected void process(SequenceEndMessage SEM) {
		if(SEM.authorId == this.character.infos.getCharacterId()) {
			GameActionAcknowledgementMessage GAAM = new GameActionAcknowledgementMessage();
			GAAM.valid = true;
			GAAM.actionId = SEM.actionId;
			this.character.net.send(GAAM);
		}
	}
	
	protected void process(GameFightTurnEndMessage GFTEM) {
		if(GFTEM.fighterId == this.character.infos.getCharacterId()) {
			this.character.updateState(CharacterState.IN_GAME_TURN, false);
			this.character.log.p("End of my game turn.");
		}
	}
	
	protected void process(GameFightEndMessage GFEM) {
		for(FightResultListEntry result : GFEM.results)
			if(result instanceof FightResultPlayerListEntry && ((FightResultPlayerListEntry) result).id == this.character.infos.getCharacterId()) {
				this.character.roleplayContext.lastFightOutcome = result.outcome == 2; // 2 = gagné, 0 = perdu
				break;
			}	
		this.character.log.p("Fight done.");
	}
}