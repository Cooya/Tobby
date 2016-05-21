package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import controller.CharacterBehaviour;
import controller.informations.FightOptions;

// TODO -> ajouter les aires de combat
// TODO -> checker les serverId au niveau des escouades

public class ConsoleInterface {

	public static void start() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			Log.info("Command line interface ready.");
			String input;
			while((input = br.readLine()) != null)
				processCommand(input);

		} catch(IOException e) {
			e.printStackTrace();
		}	
	}

	private static void processCommand(String command) {
		if(command.equals(""))
			return;
		String[] args = command.split(" ");
		switch(args[0]) {
			case "log" :
				if(args.length == 1)
					Controller.getInstance().displayLog();
				else if(args.length == 2)
					Controller.getInstance().displayLog(Integer.valueOf(args[1]));
				else {
					System.out.println("Usage :");
					System.out.println("log");
					System.out.println("log [characterId]");
				}
				break;
			case "list" :
				if(args.length == 2 && args[1].equals("-a"))
					Controller.getInstance().displayAllAccounts();
				else if(args.length == 2 && args[1].equals("-s"))
					Controller.getInstance().displayAllSquads();
				else {
					System.out.println("Usage :");
					System.out.println("list -a");
					System.out.println("list -s");
				}
				break;
			case "add" :
				// TODO -> plus besoin du characterType
				if(args.length == 6 && args[1].equals("-a")) {
					int serverId = Integer.valueOf(args[4]);
					if(serverId != 11) {
						System.out.println("Unhandled server id.");
						return;
					}
					int characterTypeId = translateCharacterType(args[5]);
					if(characterTypeId == -1)
						return;
					Controller.getInstance().newAccount(args[2], args[3], serverId);
				}
				else if(args.length > 3 && args.length < 12 && args[1].equals("-s")) {
					int[] ids = new int[args.length - 3];
					for(int i = 3, j = 0; i < ids.length; ++i, ++j)
						ids[j] = Integer.valueOf(args[i]);
					Controller.getInstance().createSquad(args[2], ids);
				}
				else {
					System.out.println("Usage :");
					System.out.println("add -a [login] [password] [serverId] [characterType:string]");
					System.out.println("add -s [name] [id1] [id2] ... [id8]");
				}
				break;
			case "co" :
				// TODO -> plus besoin du behaviourId pour le moment
				if(args.length == 4 && args[1].equals("-a")) {
					int behaviourId = translateBehaviourId(args[3]);
					if(behaviourId == -1) {
						System.out.println("Invalid behaviour id.");
						return;
					}
					Controller.getInstance().connectCharacter(Integer.valueOf(args[2]), 11, 0, -1); // TODO -> serveur
				}
				else if(args.length == 4 && args[1].equals("-s"))
					Controller.getInstance().connectSquad(Integer.valueOf(args[2]), Boolean.valueOf(args[3]), 11, 0); // TODO -> serveur
				else {
					System.out.println("Usage :");
					System.out.println("co -a [id] [behaviourId]");
					System.out.println("co -s [id] [fightTogether:boolean]");
				}
				break;
			case "run" :
				if(args.length == 3)
					Controller.getInstance().connectCharacters(Integer.valueOf(args[1]), Integer.valueOf(args[2]));
				else {
					System.out.println("Usage :");
					System.out.println("run [number] [serverId]");
				}
				break;
			case "deco" :
				if(args.length == 3 && args[1].equals("-a"))
					Controller.getInstance().deconnectCharacter(Integer.valueOf(args[2]));
				else if(args.length == 3 && args[1].equals("-s"))
					Controller.getInstance().deconnectSquad(Integer.valueOf(args[2]));
				else {
					System.out.println("Usage :");
					System.out.println("deco -a [id]");
					System.out.println("deco -s [id]");
				}
				break;
			case "infos" :
				if(args.length == 2)
					if(args[1].equals("-f"))
						Controller.getInstance().displayFightsCounters();
					else 
						Controller.getInstance().displayInfos(Integer.valueOf(args[1]));
				else {
					System.out.println("Usage :");
					System.out.println("infos [characterId]");
					System.out.println("infos -f");
				}
				break;
			case "options" :
				if(args.length == 1) {
					System.out.println("Character types :");
					System.out.println("salesman/fighter");
					System.out.println();
					System.out.println("Character behaviours :");
					System.out.println("0  = wm -> waiting mule");
					System.out.println("1  = tm -> training mule");
					System.out.println("2  = se -> seller");
					System.out.println("10 = lw -> lone wolf");
					System.out.println("11 = cp -> captain");
					System.out.println("12 = so -> soldier");
					System.out.println();
					System.out.println("Fight areas :");
					for(int fightAreaId : FightOptions.fightAreasId)
						System.out.println(fightAreaId + " -> \"" + FightOptions.getAreaNameFromId(fightAreaId) + "\"");
					System.out.println();
					System.out.println("Handled servers :");
					System.out.println("11 -> Brumaire");
				}
				else {
					System.out.println("Usage :");
					System.out.println("options");
				}
				break;
			case "exit" :
				if(args.length == 1) {
					Controller.getInstance().globalDeconnection("Application closed by user.", false, false);
					Controller.getInstance().exit(null);
				}
				else if(args.length == 2 && args[1].equals("-f"))
					Controller.getInstance().exit(null);		
				else {
					System.out.println("Usage :");
					System.out.println("exit");
					System.out.println("exit -f");
				}
				break;
			default : System.out.println("Invalid command.");
		}
	}
	
	@SuppressWarnings("unused")
	private static boolean checkFightAreaId(String arg) {
		if(!isInteger(arg)) {
			System.out.println("Invalid fight area id.");
			return false;
		}
		if(!FightOptions.isHandledFightArea(Integer.valueOf(arg))) {
			System.out.println("Unhandled fight area id.");
			return false;
		}
		return true;
	}
	
	private static int translateCharacterType(String arg) {
		if(isInteger(arg)) {
			System.out.println("Invalid character type.");
			return -1;
		}
		if(arg.equals("salesman"))
			return 0;
		else if(arg.equals("fighter"))
			return 10;
		else {
			System.out.println("Invalid character type.");
			return -1;
		}
	}
	
	private static int translateBehaviourId(String arg) {
		if(isInteger(arg)) {
			int id = Integer.valueOf(arg);
			switch(id) {
				case CharacterBehaviour.WAITING_MULE : return id;
				case CharacterBehaviour.TRAINING_MULE : return id;
				case CharacterBehaviour.SELLER : return id;
				case CharacterBehaviour.LONE_WOLF : return id;
				case CharacterBehaviour.CAPTAIN : return id;
				case CharacterBehaviour.SOLDIER : return id;
				default : return -1; 
			}
		}
		switch(arg) {
			case "wm" : return CharacterBehaviour.WAITING_MULE;
			case "tm" : return CharacterBehaviour.TRAINING_MULE;
			case "se" : return CharacterBehaviour.SELLER;
			case "lw" : return CharacterBehaviour.LONE_WOLF;
			case "cp" : return CharacterBehaviour.CAPTAIN;
			case "so" : return CharacterBehaviour.SOLDIER;
			default : return -1;
		}
	}
	
	public static boolean isInteger(String s) {
	    return isInteger(s, 10);
	}

	public static boolean isInteger(String s, int radix) {
	    if(s.isEmpty()) 
	    	return false;
	    for(int i = 0; i < s.length(); i++) {
	        if(i == 0 && s.charAt(i) == '-') {
	            if(s.length() == 1) 
	            	return false;
	            else 
	            	continue;
	        }
	        if(Character.digit(s.charAt(i), radix) < 0) 
	        	return false;
	    }
	    return true;
	}
}