package gamedata.d2o.modules;

import java.util.Arrays;

import gamedata.d2i.I18n;
import gamedata.d2o.GameData;
import gamedata.d2o.GameDataFileAccessor;

public class InfoMessage {
	public static final String MODULE = "InfoMessages";
	
	static {
    	GameDataFileAccessor.getInstance().init(MODULE);
    }

	public int typeId;
	public int messageId;
	public int textId;
	private String _text;

	public static InfoMessage getInfoMessageById(int id) {
		return (InfoMessage) GameData.getObject(MODULE, id);
	}
	public static InfoMessage[] getInfoMessages() {
		Object[] objArray = GameData.getObjects(MODULE);
        return Arrays.copyOf(objArray, objArray.length, InfoMessage[].class);
	}

	public String getText() {
		if(this._text == null)
			this._text = I18n.getText(this.textId);
		return this._text;
	}
}