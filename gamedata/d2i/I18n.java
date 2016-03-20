package gamedata.d2i;

public abstract class I18n {

	public static void addOverride(int i1, int i2) {
		I18nFileAccessor.getInstance().overrideId(i1, i2);
	}
	
	public static String getText(int id, String[] array, String str) {
		if(id == 0)
			return null;
		String text = I18nFileAccessor.getInstance().getText(id);
		if(text == null || text.equals("null"))
			return "[UNKNOWN_TEXT_ID_" + id + "]";
		return replaceParams(text, array, str);
	}
	
	public static String getText(int id) {
		return getText(id, null, "%");
	}
	
	public static String getUiText(String textName, String[] array, String str) {
		String text = I18nFileAccessor.getInstance().getNamedText(textName);
		if(text == null || text.equals("null"))
			return "[UNKNOWN_TEXT_NAME_" + textName + "]";
		return replaceParams(text, array, str);
	}
	
	public static boolean hasUiText(String textName) {
        return I18nFileAccessor.getInstance().hasNamedText(textName);
    }
	
	public static String replaceParams(String text, String[] array, String str) {
		if(array == null  || array.length == 0)
			return text;
		for(int i = 1; i < array.length; ++i)
			text = text.replace(str + i, array[i - 1]);
		return text;
	}
}