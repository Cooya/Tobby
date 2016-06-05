package gamedata;

public class ParamsDecoder {

	public static String applyParams(String arg1, String[] arg2, char arg3) {
		char c;
		boolean b1 = false;
		boolean b2 = false;
		String str1 = "";
		String str2 = "";
		String str3 = "";
		int len = arg1.length();
		for(int i = 0; i < len; ++i) {
			c = arg1.charAt(i);
			if(c == '$')
				b1 = true;
			else {
				if(c == arg3) {
					if(i + 1 < len && arg1.charAt(i + 1) == arg3) {
						b1 = false;
						b2 = false;
						i++;
					}
					else {
						b1 = true;
						b2 = false;
					}	
				}
			}
			if(b1)
				str1 += c;
			else {
				if(b2) {
					if(c == arg3) {
						if(str2.length() == 0)
							str2 += c;
						else {
							str3 += processReplace(str1, str2, arg2);
							str1 = "";
							str2 = "" + c;
						}
					}
					else {
						if(c >= '0' && c <= '9') {
							str2 += c;
							if(i + 1 == len) {
								b2 = false;
								str3 += processReplace(str1, str2, arg2);
								str1 = "";
								str2 = "";
							}
						}
						else {
							b2 = false;
							str3 += processReplace(str1, str2, arg2) + c;
							str1 = "";
							str2 = "";
						}
					}
				}
				else {
					if(str2 != "") {
						str3 += processReplace(str1, str2, arg2);
						str1 = "";
						str2 = "";
					}
					str3 += c;
				}
			}
		}
		return str3;
	}

	private static String processReplace(String arg1, String arg2, String[] arg3) {
		int i = Integer.valueOf(arg2.substring(1)) - 1;
		String str = "";
		if(arg1 == "")
			str = arg3[i];
		else {
			switch(arg1) {
				case "$item" :
					str = "ITEM";
					break;
				case "$itemType" :
					str ="ITEM_TYPE";
					break;
				case "$quantity" :
					str = arg3[i];
					break;
				case "$job" :
					str = "JOB";
					break;
				case "$quest" :
					str = "QUEST";
					break;
				case "$achievement" :
					str = "ACHIEVEMENT";
					break;
				case "$title" :
					str = "TITLE";
					break;
				case "$ornament" :
					str = "ORNAMENT";
					break;
				case "$spell" :
					str = "SPELL";
					break;
				case "$spellState" :
					str = "SPELL_STATE";
					break;
				case "$breed" :
					str = "BREED";
					break;
				case "$area" :
					str = "AREA";
					break;
				case "$subarea" :
					str = "SUBAREA";
					break;
				case "$map" :
					str = "MAP";
					break;
				case "$emote" :
					str = "EMOTE";
					break;
				case "$monster" :
					str = "MONSTER";
					break;
				case "$monsterRace" :
					str = "MONSTER_RACE";
					break;
				case "$monsterSuperRace" :
					str = "MONSTER_SUPER_RACE";
					break;
				case "$challenge" :
					str = "CHALLENGE";
					break;
				case "$alignment" :
					str = "ALIGNMENT";
					break;
				case "$stat" :
					str = "STAT";
					break;
				case "$dungeon" :
					str = "DUNGEON";
					break;
				case "$time" :
					str = "TIME";
					break;
				case "$companion" :
				case "$sidekick" :
					str = "COMPANION";
					break;
			}
		}
		return str;
	}
}