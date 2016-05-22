package main;

import java.util.List;
import java.util.Vector;

import main.AccountsManager.Account;
import controller.CharacterBehaviour;
import controller.characters.Captain;
import controller.characters.Character;
import controller.characters.Soldier;

// g�re les escouades (fixes ou non) via un vecteur d'escouades
class SquadsManager {
	private static final String EOL = System.getProperty("line.separator");
	private static final int MAX_GROUP_SIZE = 8;
	
	private List<Squad> squads;
	private CharactersManager characters;
	private int incompleteSquadIndex; // correspond � l'index de l'escouade � compl�ter par un ou plusieurs combattants
	
	protected SquadsManager() {
		this.squads = new Vector<Squad>();
		this.squads.add(new Squad());
		this.incompleteSquadIndex = 0;
	}
	
	protected void setCharactersManager(CharactersManager characters) {
		this.characters = characters;
	}
	
	// cr�e une escouade fixe (qui ne peut �tre compl�t�e par de nouveaux membres)
	protected void createFixedSquad(String squadName, Vector<Account> members) {
		for(Squad squad : this.squads)
			if(squad.name.equals(squadName)) {
				Log.err("Squad name already taken.");
				return;
			}
		this.squads.add(new Squad(squadName, members));
	}
	
	// retourne l'identifiant d'une escouade gr�ce � son nom (unique)
	protected int getSquadId(String squadName) {
		for(Squad squad : this.squads)
			if(squad.name.equals(squadName))
				return squad.id;
		return -1;
	}
	
	protected Squad getSquad(int id) {
		for(Squad squad : this.squads)
			if(squad.id == id)
				return squad;
		return null;
	}
	
	protected List<Squad> getAllSquads() {
		return this.squads;
	}

	// d�termine si le prochain combattant sera un capitaine ou un soldat
	protected int nextFighterWillBe() {
		int incompleteSquadSize = this.squads.get(this.incompleteSquadIndex).members.size();
		if(incompleteSquadSize == 0)
			return CharacterBehaviour.CAPTAIN;
		else
			return CharacterBehaviour.SOLDIER;
	}

	// cr�e un nouveau combattant et l'affecte � une escouade s'il n'en a pas
	protected Character newSquadFighter(Account account, int serverId, int areaId, CharacterFrame frame, int captainId) {
		Character newFighter;
		if(captainId == -1) { // combattant n'ayant pas d'escouade fix�e
			Squad incompleteSquad = this.squads.get(this.incompleteSquadIndex);
			if(incompleteSquad.members.size() == 0) // capitaine
				newFighter = Character.create(account.id, CharacterBehaviour.CAPTAIN, account.login, account.password, serverId, areaId, new Log(account.login, frame));
			else { // soldat
				newFighter = Character.create(account.id, CharacterBehaviour.SOLDIER, account.login, account.password, serverId, 0, new Log(account.login, frame));
				((Captain) this.characters.getCharacter(incompleteSquad.members.firstElement().id)).newRecruit((Soldier) newFighter); // on ajoute ce nouveau soldat aux recrues du capitaine
			}
			incompleteSquad.members.add(account);
			if(incompleteSquad.members.size() == MAX_GROUP_SIZE) { // si l'escouade est compl�te
				int squadNumber = this.squads.size();
				Squad currentSquad;
				while(this.incompleteSquadIndex != squadNumber) {
					currentSquad = this.squads.get(++this.incompleteSquadIndex); // on change l'index vers une autre escouade
					if(!currentSquad.isFixed && currentSquad.members.size() < 8)
						return newFighter;
				}
				this.squads.add(new Squad()); // ou on cr�e une escouade vide � la fin du vecteur d'escouades
			}
		}
		else { // combattant ayant une escouade fix�e
			if(account.id == captainId) // capitaine
				newFighter = Character.create(account.id, CharacterBehaviour.CAPTAIN, account.login, account.password, serverId, areaId, new Log(account.login, frame));
			else {
				newFighter = Character.create(account.id, CharacterBehaviour.CAPTAIN, account.login, account.password, serverId, areaId, new Log(account.login, frame));
				((Captain) this.characters.getCharacter(captainId)).newRecruit((Soldier) newFighter);
			}
		}
		return newFighter;
	}

	// supprime un combattant du vecteur d'escouades et lib�re ainsi sa place dans l'escouade
	protected void removeSquadFighter(Character fighter) {
		int squadsNumber = this.squads.size();
		int currentSquadSize;
		Squad currentSquad;
		for(int i = 0; i < squadsNumber; ++i) {
			currentSquad = this.squads.get(i);
			if(currentSquad.isFixed)
				continue; // on ne touche pas aux escouades enregistr�es
			currentSquadSize = currentSquad.members.size();
			for(int j = 0; j < currentSquadSize; ++j)
				if(this.characters.getCharacter(currentSquad.members.get(j).id) == fighter) {
					currentSquad.members.remove(j); // TODO -> probl�me si c'est le capitaine
					if(i < this.incompleteSquadIndex)
						this.incompleteSquadIndex = i; // changement du groupe de combat � compl�ter
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
	
	// repr�sente un groupe de combat par le biais d'un nom et d'un vecteur de comptes
	// les escouades peuvent �tre fix�es (enregistr�es) ou non (dans ce cas, elles sont compos�es au fur et � mesure)
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