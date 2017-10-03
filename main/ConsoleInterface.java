package main;

import gamedata.enums.ServerEnum;

import java.util.Scanner;

import utilities.Reflection;
import controller.CharacterBehaviour;
import controller.characters.Character;
import controller.informations.FightOptions;

public class ConsoleInterface {
	private static StringBuffer output = new StringBuffer();

	public static void start() {
		Scanner sc = new Scanner(System.in);
		Log.info("Command line interface ready.");
		while(sc.hasNext()) {
			processCommand(sc.nextLine());
			if(output.length() > 0)
				System.out.print(output);
		}
		sc.close();
	}

	public synchronized static String processCommand(String command) {
		output.setLength(0);
		if(command.equals(""))
			return "";
		String[] args = command.split(" ");
		switch(args[0]) {
			case "log" :
				if(args.length == 1)
					appendLine(Log.getGlobalLog());
				else if(args.length == 2 && isInteger(args[1]))
					displayLog(Integer.valueOf(args[1]));
				else {
					appendLine("Usage :");
					appendLine("log");
					appendLine("log [characterId]");
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
							appendLine("Bad id row.");
							return output.toString();
						}
					}
					SquadsManager.getInstance().createSquad(args[2], ids);
				}
				else {
					appendLine("Usage :");
					appendLine("add -a [login] [password]");
					appendLine("add -s [name] [id1] [id2] ... [id8]");
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
					appendLine("Usage :");
					appendLine("Can only connect lone wolves."); // TODO pouvoir connecter des groupes de combat
					appendLine("co -a [id] [serverId] (areaId)");
					appendLine("co -s [id] [serverId] (areaId) [fightTogether:boolean]");
				}
				break;
			case "run" :
				if(args.length == 3 && isInteger(args[1]) && checkServerId(args[2]))
					CharactersManager.getInstance().connectCharacters(Integer.valueOf(args[1]), Integer.valueOf(args[2]), 0);
				else if(args.length == 4 && isInteger(args[1]) && checkServerId(args[2]) && isInteger(args[3]))
					CharactersManager.getInstance().connectCharacters(Integer.valueOf(args[1]), Integer.valueOf(args[2]), Integer.valueOf(args[3]));
				else if(args.length == 4 && args[1].equals("-r") && isInteger(args[2]) && isInteger(args[3])) {
					int charactersCount = Integer.valueOf(args[2]);
					int serversCount = Integer.valueOf(args[3]);
					int i = 0;
					for(int serverId : ServerEnum.getServerIdsList())
						if(i++ < serversCount)
							CharactersManager.getInstance().connectCharacters(charactersCount, serverId, 0);
						else
							break;
				}
				else if(args.length == 5 && args[1].equals("-r") && isInteger(args[2]) && checkServerId(args[3]) && checkServerId(args[4])) {
					int charactersCount = Integer.valueOf(args[2]);
					int serverIdFrom = Integer.valueOf(args[3]);
					int serverIdTo = Integer.valueOf(args[4]);
					if(serverIdTo < serverIdFrom) {
						appendLine("Invalid options.");
						return output.toString();
					}
					for(int serverId : ServerEnum.getServerIdsList())
						if(serverId >= serverIdFrom && serverId <= serverIdTo)
							CharactersManager.getInstance().connectCharacters(charactersCount, serverId, 0);
						else if(serverId > serverIdTo)
							break;
				}
				else {
					appendLine("Usage :");
					appendLine("run [charactersCount] [serverId] (areaId)");
					appendLine("run -r [charactersCount] [serversCount]");
					appendLine("run -r [charactersCount] [serverIdFrom] [serverIdTo]");
				}
				break;
			case "deco" :
				if(args.length == 3 && args[1].equals("-a") && isInteger(args[2]))
					CharactersManager.getInstance().deconnectCharacter(Integer.valueOf(args[2]), "Deconnected by command line inteface.", false, false);
				else if(args.length == 3 && args[1].equals("-s") && isInteger(args[2]))
					SquadsManager.getInstance().deconnectSquad(Integer.valueOf(args[2]));
				else {
					appendLine("Usage :");
					appendLine("deco -a [id]");
					appendLine("deco -s [id]");
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
					appendLine("Usage :");
					appendLine("infos");
					appendLine("infos [id]");
					appendLine("infos [login]");
				}
				break;
			case "options" :
				if(args.length == 1) {
					appendLine("Character behaviours :");
					appendLine(CharacterBehaviour.LONE_WOLF + " = lw -> lone wolf");
					appendLine(CharacterBehaviour.CAPTAIN + " = cp -> captain");
					appendLine(CharacterBehaviour.SOLDIER + " = so -> soldier");
					appendLine("");
					appendLine("Game servers :");
					output.append(ServerEnum.displayServersList());
					appendLine("");
					appendLine("Fight areas :");
					output.append(FightOptions.displayFightAreas());
				}
				else {
					appendLine("Usage :");
					appendLine("options");
				}
				break;
			case "exit" :
				if(args.length == 1) {
					CharactersManager.getInstance().deconnectCharacters("Application closed by user.", 0, false, false);
					Main.askExit();
				}
				else if(args.length == 2 && args[1].equals("-f"))
					Main.exit(null);		
				else {
					appendLine("Usage :");
					appendLine("exit");
					appendLine("exit -f");
				}
				break;
			default : appendLine("Invalid command.");
		}
		return output.toString();
	}
	
	private static void appendLine(String line) {
		output.append(line);
		output.append("\n");
	}
	
	private static boolean checkServerId(String arg) {
		if(!isInteger(arg)) {
			appendLine("Invalid server id.");
			return false;
		}
		if(!ServerEnum.isHandledServer(Integer.valueOf(arg))) {
			appendLine("Unhandled server.");
			return false;
		}
		return true;
	}
	
	private static boolean checkFightAreaId(String arg) {
		if(!isInteger(arg)) {
			appendLine("Invalid fight area id.");
			return false;
		}
		if(!FightOptions.isHandledFightArea(Integer.valueOf(arg))) {
			appendLine("Unhandled fight area.");
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("unused")
	private static int translateCharacterType(String arg) {
		if(isInteger(arg)) {
			appendLine("Invalid character type.");
			return -1;
		}
		if(arg.equals("salesman"))
			return 0;
		else if(arg.equals("fighter"))
			return 10;
		else {
			appendLine("Invalid character type.");
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
	
	private static void displayLog(int accountId) {
		Character character = CharactersManager.getInstance().getInGameCharacter(accountId);
		if(character != null)	
			appendLine(character.log.getCharacterLog(20));
		else
			appendLine("Character not connected on this computer.");
	}
	
	private static void displayPersonalInfos(int accountId) {
		Character character = CharactersManager.getInstance().getInGameCharacter(accountId);
		if(character != null)	
			Reflection.explore(character.infos, 1);
		else
			appendLine("Character not connected on this computer.");
	}
	
	private static void displayPersonalInfos(String login) {
		Character character = CharactersManager.getInstance().getInGameCharacter(login);
		if(character != null)	
			Reflection.explore(character.infos, 1);
		else
			appendLine("Character not connected on this computer.");
	}
	
	private static void displayGlobalInfos() {
		Character[] characters = CharactersManager.getInstance().getInGameCharacters(0);
		StringBuilder str = new StringBuilder();
		for(Character character : characters) {
			str.append(character.id);
			str.append(" ");
			str.append(character.infos.getLogin());
			str.append(" (");
			str.append(ServerEnum.getServerName(character.infos.getServerId()));
			str.append(") -> win : ");
			str.append(character.infos.getFightsWonCounter());
			str.append(", lost : ");
			str.append(character.infos.getFightsLostCounter());
			str.append(", level : ");
			str.append(character.infos.getLevel());
			str.append(", weight : ");
			str.append(character.infos.getWeight());
			str.append("/");
			str.append(character.infos.getWeightMax());
			appendLine(str.toString());
			str.setLength(0);
		}
	}
}