package gui;

import gui.Model.Account;

import java.util.Vector;

import main.Log;
import controller.CharacterBehaviour;
import controller.characters.Captain;
import controller.characters.Character;
import controller.characters.Soldier;

// g�re les escouades (fixes ou non) via un vecteur d'escouades
public class SquadsManager {
	private static final int MAX_GROUP_SIZE = 8;
	
	private Model model;
	private Vector<Squad> squads;
	private int incompleteSquadIndex; // correspond � l'index de l'escouade � compl�ter par un ou plusieurs combattants
	
	public SquadsManager(Model model) {
		this.model = model;
		this.squads = new Vector<Squad>();
		this.squads.add(new Squad());
		this.incompleteSquadIndex = 0;
	}
	
	// cr�e une escouade fixe (qui ne peut �tre compl�t�e par de nouveaux membres)
	protected void createFixedSquad(String squadName, Vector<Account> members) {
		this.squads.add(new Squad(squadName, members));
	}
	
	// retourne l'escouade correspondant au nom donn�
	protected Vector<Account> getSquad(String squadName) {
		for(Squad squad : this.squads)
			if(squad.name.equals(squadName))
				return squad.members;
		return null;
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
	protected Character newSquadFighter(Account account, int areaId, CharacterFrame frame, Account captain) {
		Character newFighter;
		if(captain == null) { // combattant n'ayant pas d'escouade fix�e
			Squad incompleteSquad = this.squads.get(this.incompleteSquadIndex);
			if(incompleteSquad.members.size() == 0) // capitaine
				newFighter = Character.create(account.id, account.behaviour, account.login, account.password, account.serverId, areaId, new Log(account.login, frame));
			else { // soldat
				newFighter = Character.create(account.id, account.behaviour, account.login, account.password, account.serverId, 0, new Log(account.login, frame));
				((Captain) this.model.getCharacter(incompleteSquad.members.firstElement())).newRecruit((Soldier) newFighter); // on ajoute ce nouveau soldat aux recrues du capitaine
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
			newFighter = Character.create(account.id, account.behaviour, account.login, account.password, account.serverId, areaId, new Log(account.login, frame));
			if(account.behaviour == CharacterBehaviour.SOLDIER)
				((Captain) this.model.getCharacter(captain)).newRecruit((Soldier) newFighter);
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
				if(this.model.getCharacter(currentSquad.members.get(j)) == fighter) {
					currentSquad.members.remove(j); // TODO -> probl�me si c'est le capitaine
					if(i < this.incompleteSquadIndex)
						this.incompleteSquadIndex = i; // changement du groupe de combat � compl�ter
					return;
				}
		}
	}
	
	// repr�sente un groupe de combat par le biais d'un nom et d'un vecteur de comptes
	// les escouades peuvent �tre fix�es (enregistr�es) ou non (dans ce cas, elles sont compos�es au fur et � mesure)
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