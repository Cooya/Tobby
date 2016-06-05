package main;

import gamedata.enums.ServerEnum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import utilities.Reflection;
import controller.CharacterBehaviour;
import controller.characters.Character;
import controller.informations.FightOptions;

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
					displayLog();
				else if(args.length == 2 && isInteger(args[1]))
					displayLog(Integer.valueOf(args[1]));
				else {
					System.out.println("Usage :");
					System.out.println("log");
					System.out.println("log [characterId]");
				}
				break;
			case "list" :
				if(args.length == 2 && args[1].equals("-s"))
					displayAllSquads();
				else {
					System.out.println("Usage :");
					System.out.println("list -a");
					System.out.println("list -s");
				}
				break;
			case "add" :
				if(args.length == 4 && args[1].equals("-a"))
					AccountsManager.newAccount(args[2], args[3]);
				else if(args.length > 3 && args.length < 12 && args[1].equals("-s")) {
					int[] ids = new int[args.length - 3];
					for(int i = 3, j = 0; i < ids.length; ++i, ++j) {
						if(isInteger(args[i]))
							ids[j] = Integer.valueOf(args[i]);
						else {
							System.out.println("Bad id row.");
							return;
						}
					}
					SquadsManager.getInstance().createSquad(args[2], ids);
				}
				else {
					System.out.println("Usage :");
					System.out.println("add -a [login] [password]");
					System.out.println("add -s [name] [id1] [id2] ... [id8]");
				}
				break;
			case "co" :
				if(args.length == 4 && args[1].equals("-a") && isInteger(args[2]) && checkServerId(args[3]))
					CharactersManager.getInstance().connectCharacter(Integer.valueOf(args[2]), Integer.valueOf(args[3]), 0);
				else if(args.length == 5 && args[1].equals("-a") && isInteger(args[2]) && checkServerId(args[3]) && checkFightAreaId(args[4]))
					CharactersManager.getInstance().connectCharacter(Integer.valueOf(args[2]), Integer.valueOf(args[3]), Integer.valueOf(args[4]));
				else if(args.length == 5 && args[1].equals("-s") && isInteger(args[2]) && checkServerId(args[3]) && isBoolean(args[4]))
					SquadsManager.getInstance().connectSquad(Integer.valueOf(args[2]), Integer.valueOf(args[3]), 0, Boolean.valueOf(args[4]));
				else if(args.length == 6 && args[1].equals("-s") && isInteger(args[2]) && checkServerId(args[3]) && checkFightAreaId(args[4]) && isBoolean(args[5]))
					SquadsManager.getInstance().connectSquad(Integer.valueOf(args[2]), Integer.valueOf(args[3]), Integer.valueOf(args[4]), Boolean.valueOf(args[5]));
				else {
					System.out.println("Usage :");
					System.out.println("Can only connect lone wolves."); // TODO pouvoir connecter des groupes de combat
					System.out.println("co -a [id] [serverId] (areaId)");
					System.out.println("co -s [id] [serverId] (areaId) [fightTogether:boolean]");
				}
				break;
			case "run" :
				if(args.length == 3 && isInteger(args[1]) && checkServerId(args[2]))
					CharactersManager.getInstance().connectCharacters(Integer.valueOf(args[1]), Integer.valueOf(args[2]), 0);
				else if(args.length == 4 && isInteger(args[1]) && checkServerId(args[2]) && isInteger(args[3]))
					CharactersManager.getInstance().connectCharacters(Integer.valueOf(args[1]), Integer.valueOf(args[2]), Integer.valueOf(args[3]));
				else {
					System.out.println("Usage :");
					System.out.println("run [number] [serverId] (areaId)");
				}
				break;
			case "deco" :
				if(args.length == 3 && args[1].equals("-a") && isInteger(args[2]))
					CharactersManager.getInstance().deconnectCharacter(Integer.valueOf(args[2]), "Deconnected by command line inteface.", false, false);
				else if(args.length == 3 && args[1].equals("-s") && isInteger(args[2]))
					SquadsManager.getInstance().deconnectSquad(Integer.valueOf(args[2]));
				else {
					System.out.println("Usage :");
					System.out.println("deco -a [id]");
					System.out.println("deco -s [id]");
				}
				break;
			case "infos" :
				if(args.length == 1)
					displayGlobalInfos();
				else if(args.length == 2 && isInteger(args[1]))
					displayPersonalInfos(Integer.valueOf(args[1]));
				else if(args.length == 2)
					displayPersonalInfos(args[1]);
				else {
					System.out.println("Usage :");
					System.out.println("infos");
					System.out.println("infos [id]");
					System.out.println("infos [login]");
				}
				break;
			case "options" :
				if(args.length == 1) {
					System.out.println("Character behaviours :");
					System.out.println(CharacterBehaviour.LONE_WOLF + " = lw -> lone wolf");
					System.out.println(CharacterBehaviour.CAPTAIN + " = cp -> captain");
					System.out.println(CharacterBehaviour.SOLDIER + " = so -> soldier");
					System.out.println();
					System.out.println("Game servers :");
					ServerEnum.displayServersList();
					System.out.println();
					System.out.println("Fight areas :");
					FightOptions.displayFightAreas();
				}
				else {
					System.out.println("Usage :");
					System.out.println("options");
				}
				break;
			case "exit" :
				if(args.length == 1) {
					CharactersManager.getInstance().deconnectCharacters("Application closed by user.", 0, false, false);
					Main.exit(null);
				}
				else if(args.length == 2 && args[1].equals("-f"))
					Main.exit(null);		
				else {
					System.out.println("Usage :");
					System.out.println("exit");
					System.out.println("exit -f");
				}
				break;
			default : System.out.println("Invalid command.");
		}
	}
	
	private static boolean checkServerId(String arg) {
		if(!isInteger(arg)) {
			System.out.println("Invalid server id.");
			return false;
		}
		if(!ServerEnum.isHandledServer(Integer.valueOf(arg))) {
			System.out.println("Unhandled server.");
			return false;
		}
		return true;
	}
	
	private static boolean checkFightAreaId(String arg) {
		if(!isInteger(arg)) {
			System.out.println("Invalid fight area id.");
			return false;
		}
		if(!FightOptions.isHandledFightArea(Integer.valueOf(arg))) {
			System.out.println("Unhandled fight area.");
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("unused")
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
	
	@SuppressWarnings("unused")
	private static int translateBehaviourId(String arg) {
		if(isInteger(arg)) {
			int id = Integer.valueOf(arg);
			switch(id) {
				case CharacterBehaviour.LONE_WOLF : return id;
				case CharacterBehaviour.CAPTAIN : return id;
				case CharacterBehaviour.SOLDIER : return id;
				default : return -1; 
			}
		}
		switch(arg) {
			case "lw" : return CharacterBehaviour.LONE_WOLF;
			case "cp" : return CharacterBehaviour.CAPTAIN;
			case "so" : return CharacterBehaviour.SOLDIER;
			default : return -1;
		}
	}
	
	private static boolean isInteger(String s) {
	    return isInteger(s, 10);
	}

	private static boolean isInteger(String s, int radix) {
	    if(s.isEmpty()) 
	    	return false;
	    for(int i = 0; i < s.length(); i++) {
	        if(i == 0 && s.charAt(i) == '-') {
	            if(s.length() == 1) 
	            	return false;
	            else 
	            	continue;
	        }
	        if(java.lang.Character.digit(s.charAt(i), radix) < 0) 
	        	return false;
	    }
	    return true;
	}
	
	private static boolean isBoolean(String s) {
		return s.equals("true") || s.equals("false");
	}
	
	private static void displayAllSquads() {
		System.out.print(SquadsManager.getInstance());
	}
	
	private static void displayLog() {
		Log.displayGlobalLog();
	}
	
	private static void displayLog(int accountId) {
		Character character = CharactersManager.getInstance().getInGameCharacter(accountId);
		if(character != null)	
			character.log.displayLog(20);
		else
			System.out.println("Character not connected on this computer.");
	}
	
	private static void displayPersonalInfos(int accountId) {
		Character character = CharactersManager.getInstance().getInGameCharacter(accountId);
		if(character != null)	
			Reflection.explore(character.infos, 1);
		else
			System.out.println("Character not connected on this computer.");
	}
	
	private static void displayPersonalInfos(String login) {
		Character character = CharactersManager.getInstance().getInGameCharacter(login);
		if(character != null)	
			Reflection.explore(character.infos, 1);
		else
			System.out.println("Character not connected on this computer.");
	}
	
	private static void displayGlobalInfos() {
		Character[] characters = CharactersManager.getInstance().getInGameCharacters(0);
		StringBuilder str = new StringBuilder();
		for(Character character : characters) {
			str.append(character.id);
			str.append(" ");
			str.append(character.infos.getLogin());
			str.append(" -> win : ");
			str.append(character.infos.getFightsWonCounter());
			str.append(", lost : ");
			str.append(character.infos.getFightsLostCounter());
			str.append(", level : ");
			str.append(character.infos.getLevel());
			str.append(", weight : ");
			str.append(character.infos.getWeight());
			str.append("/");
			str.append(character.infos.getWeightMax());
			System.out.println(str);
			str.setLength(0);
		}
	}
}