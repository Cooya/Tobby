package gui;

import gui.Model.Account;

import java.util.Vector;

import controller.CharacterBehaviour;
import main.Instance;

// gère les escouades (fixes ou non) via un vecteur d'escouades
public class SquadsManager {
	private static final int MAX_GROUP_SIZE = 8;
	
	private Model model;
	private Vector<Squad> squads;
	private int incompleteSquadIndex; // correspond à l'index de l'escouade à compléter par un ou plusieurs combattants
	
	public SquadsManager(Model model) {
		this.model = model;
		this.squads = new Vector<Squad>();
		this.squads.add(new Squad());
		this.incompleteSquadIndex = 0;
	}
	
	// crée une escouade fixe (qui ne peut être complétée par de nouveaux membres)
	protected void createFixedSquad(String squadName, Vector<Account> members) {
		this.squads.add(new Squad(squadName, members));
	}
	
	// retourne l'escouade correspondant au nom donné
	protected Vector<Account> getSquad(String squadName) {
		for(Squad squad : this.squads)
			if(squad.name.equals(squadName))
				return squad.members;
		return null;
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
	protected Instance newSquadFighter(Account account, int areaId, CharacterFrame frame, Account captain) {
		Instance newFighter;
		if(captain == null) { // combattant n'ayant pas d'escouade fixée
			Squad incompleteSquad = this.squads.get(this.incompleteSquadIndex);
			if(incompleteSquad.members.size() == 0) // capitaine
				newFighter = new Instance(account.id, account.behaviour, account.login, account.password, account.serverId, areaId, frame);
			else { // soldat
				newFighter = new Instance(account.id, account.behaviour, account.login, account.password, account.serverId, 0, frame);
				this.model.getInstance(incompleteSquad.members.firstElement()).newRecruit(newFighter); // on ajoute ce nouveau soldat aux recrues du capitaine
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
			newFighter = new Instance(account.id, account.behaviour, account.login, account.password, account.serverId, areaId, frame);
			if(account.behaviour == CharacterBehaviour.SOLDIER)
				this.model.getInstance(captain).newRecruit(newFighter);
		}
		return newFighter;
	}

	// supprime un combattant du vecteur d'escouades et libère ainsi sa place dans l'escouade
	protected void removeSquadFighter(Instance fighter) {
		int squadsNumber = this.squads.size();
		int currentSquadSize;
		Squad currentSquad;
		for(int i = 0; i < squadsNumber; ++i) {
			currentSquad = this.squads.get(i);
			if(currentSquad.isFixed)
				continue; // on ne touche pas aux escouades enregistrées
			currentSquadSize = currentSquad.members.size();
			for(int j = 0; j < currentSquadSize; ++j)
				if(this.model.getInstance(currentSquad.members.get(j)) == fighter) {
					currentSquad.members.remove(j); // TODO -> problème si c'est le capitaine
					if(i < this.incompleteSquadIndex)
						this.incompleteSquadIndex = i; // changement du groupe de combat à compléter
					return;
				}
		}
	}
	
	// représente un groupe de combat par le biais d'un nom et d'un vecteur de comptes
	// les escouades peuvent être fixées (enregistrées) ou non (dans ce cas, elles sont composées au fur et à mesure)
	private class Squad {
		private String name;
		private Vector<Account> members;
		private boolean isFixed;
		
		// constructeur pour les escouades libres
		private Squad() {
			this.name = new String(); // permet d'avoir un identifiant unique
			this.members = new Vector<Account>(MAX_GROUP_SIZE);
			this.isFixed = false;
		}
		
		// constructeur pour les escouades fixes
		private Squad(String name, Vector<Account> members) {
			this.name = name;
			this.members = members;
			this.isFixed = true;
		}
	}
}