package main;

import java.util.List;
import java.util.Vector;

import main.AccountsManager.Account;
import controller.CharacterBehaviour;
import controller.characters.Captain;
import controller.characters.Character;
import controller.characters.Soldier;

// gère les escouades (fixes ou non) via un vecteur d'escouades
class SquadsManager {
	private static final String EOL = System.getProperty("line.separator");
	private static final int MAX_GROUP_SIZE = 8;
	private static SquadsManager self;
	
	private List<Squad> squads;
	private int incompleteSquadIndex; // correspond à l'index de l'escouade à compléter par un ou plusieurs combattants
	
	private SquadsManager() {
		this.squads = new Vector<Squad>(5);
		this.squads.add(new Squad());
		this.incompleteSquadIndex = 0;
	}
	
	protected static SquadsManager getInstance() {
		if(self == null)
			self = new SquadsManager();
		return self;
	}
	
	protected void createSquad(String name, int[] ids) {
		Vector<Account> members = new Vector<Account>(ids.length);
		Account account;
		for(int id : ids) {
			account = AccountsManager.retrieveAccount(id);
			if(account != null)
				members.add(account);
			else
				Log.err("Account with id = " + id  + " does not exist.");
		}
		createFixedSquad(name, members);
	}
	
	// crée une escouade fixe (qui ne peut être complétée par de nouveaux membres)
	protected void createFixedSquad(String squadName, Vector<Account> members) {
		for(Squad squad : this.squads)
			if(squad.name.equals(squadName)) {
				Log.err("Squad name already taken.");
				return;
			}
		this.squads.add(new Squad(squadName, members));
	}
	
	protected void connectSquad(int squadId, int serverId, int areaId, boolean fightTogether) {
		Squad squad = this.squads.get(squadId);
		if(squad == null) {
			Log.err("Squad with id = " + squadId + " does not exist.");
			return;
		}
		
		Vector<Account> members = squad.getMembers();
		// check le statut de connexion
		if(!fightTogether)
			for(Account member : members)
				CharactersManager.getInstance().connectCharacter(member, serverId, areaId, -1);
		else {
			Account account = members.get(0);
			int captainId = account.id;
			CharactersManager.getInstance().connectCharacter(account, serverId, areaId, captainId); // connexion du capitaine
			int squadSize = members.size();
			for(int i = 1; i < squadSize; ++i) {
				account = members.get(i);
				CharactersManager.getInstance().connectCharacter(account, serverId, 0, captainId); // connexion des soldats
			}
		}
	}
	
	protected void deconnectSquad(int squadId) {
		Squad squad = this.squads.get(squadId);
		if(squad != null) {
			Vector<Account> members = squad.getMembers();
			for(Account member : members)
				CharactersManager.getInstance().deconnectCharacter(member.id, "Deconnected by console interface.", true, false);
		}
		else
			Log.err("Squad with id = " + squadId + " does not exist.");
	}

	// détermine si le prochain combattant sera un capitaine ou un soldat
	protected int nextFighterWillBe() {
		int incompleteSquadSize = this.squads.get(this.incompleteSquadIndex).members.size();
		if(incompleteSquadSize == 0)
			return CharacterBehaviour.CAPTAIN;
		else
			return CharacterBehaviour.SOLDIER;
	}

	// crée un nouveau combattant et l'affecte à une escouade s'il n'en a pas
	protected Character newSquadFighter(Account account, int serverId, int areaId, int captainId) {
		Character newFighter;
		if(captainId == -1) { // combattant n'ayant pas d'escouade fixée
			Squad incompleteSquad = this.squads.get(this.incompleteSquadIndex);
			if(incompleteSquad.members.size() == 0) // capitaine
				newFighter = Character.create(account.id, CharacterBehaviour.CAPTAIN, account.login, account.password, serverId, areaId, new Log(account.login));
			else { // soldat
				newFighter = Character.create(account.id, CharacterBehaviour.SOLDIER, account.login, account.password, serverId, 0, new Log(account.login));
				((Captain) CharactersManager.getInstance().getInGameCharacter(incompleteSquad.members.firstElement().id)).newRecruit((Soldier) newFighter); // on ajoute ce nouveau soldat aux recrues du capitaine
			}
			incompleteSquad.members.add(account);
			if(incompleteSquad.members.size() == MAX_GROUP_SIZE) { // si l'escouade est complète
				int squadNumber = this.squads.size();
				Squad currentSquad;
				while(this.incompleteSquadIndex != squadNumber) {
					currentSquad = this.squads.get(++this.incompleteSquadIndex); // on change l'index vers une autre escouade
					if(!currentSquad.isFixed && currentSquad.members.size() < 8)
						return newFighter;
				}
				this.squads.add(new Squad()); // ou on crée une escouade vide à la fin du vecteur d'escouades
			}
		}
		else { // combattant ayant une escouade fixée
			if(account.id == captainId) // capitaine
				newFighter = Character.create(account.id, CharacterBehaviour.CAPTAIN, account.login, account.password, serverId, areaId, new Log(account.login));
			else {
				newFighter = Character.create(account.id, CharacterBehaviour.SOLDIER, account.login, account.password, serverId, areaId, new Log(account.login));
				((Captain) CharactersManager.getInstance().getInGameCharacter(captainId)).newRecruit((Soldier) newFighter);
			}
		}
		return newFighter;
	}

	// supprime un combattant du vecteur d'escouades et libère ainsi sa place dans l'escouade
	protected void removeSquadFighter(Character fighter) {
		int squadsNumber = this.squads.size();
		int currentSquadSize;
		Squad currentSquad;
		for(int i = 0; i < squadsNumber; ++i) {
			currentSquad = this.squads.get(i);
			if(currentSquad.isFixed)
				continue; // on ne touche pas aux escouades enregistrées
			currentSquadSize = currentSquad.members.size();
			for(int j = 0; j < currentSquadSize; ++j)
				if(CharactersManager.getInstance().getInGameCharacter(currentSquad.members.get(j).id) == fighter) {
					currentSquad.members.remove(j); // TODO problème si c'est le capitaine
					if(i < this.incompleteSquadIndex)
						this.incompleteSquadIndex = i; // changement du groupe de combat à compléter
					return;
				}
		}
	}
	
	protected String toFile() {
		StringBuilder str = new StringBuilder();
		for(Squad squad  : this.squads) {
			if(squad.isFixed) {
				str.append(squad.name);
				str.append(" ");
				str.append(squad.members.size());
				str.append(EOL);
				for(Account account : squad.members) {
					str.append(account.login);
					str.append(EOL);
				}
			}
		}
		return str.toString();
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		for(Squad squad : this.squads)
			if(squad.isFixed) {
				str.append(squad);
				str.append(EOL);
			}
		return str.toString();
	}
	
	// représente un groupe de combat par le biais d'un nom et d'un vecteur de comptes
	// les escouades peuvent être fixées (enregistrées) ou non (dans ce cas, elles sont composées au fur et à mesure)
	protected static class Squad {
		private static int nextId = 0;
		private int id;
		private String name;
		private Vector<Account> members;
		private boolean isFixed;
		
		// constructeur pour les escouades libres
		private Squad() {
			this.id = nextId++;
			this.name = "";
			this.members = new Vector<Account>(MAX_GROUP_SIZE);
			this.isFixed = false;
		}
		
		// constructeur pour les escouades fixes
		private Squad(String name, Vector<Account> members) {
			this.id = nextId++;
			this.name = name;
			this.members = members;
			this.isFixed = true;
		}
		
		protected Vector<Account> getMembers() {
			return this.members;
		}
		
		@Override
		public String toString() {
			StringBuilder str = new StringBuilder();
			str.append(this.id);
			str.append(" ");
			str.append(this.name);
			str.append(" (");
			str.append(this.members.size());
			str.append(" members) :");
			str.append(EOL);
			for(Account account : this.members) {
				str.append(account);
				str.append(EOL);
			}
			return str.toString();
		}
	}
}