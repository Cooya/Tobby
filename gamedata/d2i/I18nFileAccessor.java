package gamedata.d2i;

import java.io.File;
import java.net.URI;
import java.util.Dictionary;
import java.util.Hashtable;

import main.FatalError;
import utilities.ByteArray;

public class I18nFileAccessor {
	private static I18nFileAccessor _self;
	private static final String d2iPath = "Ressources/Antibot/data/i18n/i18n_fr.d2i";
	private ByteArray _stream; // pas vraiment un stream mais beaucoup plus pratique
	private Dictionary<Integer, Integer> _indexes;
	private Dictionary<Integer, Integer> _unDiacriticalIndex;
	private Dictionary<String, Integer> _textIndexes;
	private Dictionary<String, String> _textIndexesOverride;
	private Dictionary<Integer, Integer> _textSortIndex;
	//private int _startTextIndex = 4;
	//private int _textCount;
	private ByteArray _directBuffer;

	public I18nFileAccessor() {
		if(_self != null)
			throw new FatalError("Instance already instanciated.");
		init(d2iPath);
	}

	public static I18nFileAccessor getInstance() {
		if(_self == null)
			_self = new I18nFileAccessor();
		return _self;
	}

	public void init(String d2iPath) {
		File file = new File(d2iPath);
		if(file == null || !file.exists())
			throw new FatalError("I18n file not readable.");
		this._stream = ByteArray.fileToByteArray(d2iPath);
		this._indexes = new Hashtable<Integer, Integer>();
		this._unDiacriticalIndex = new Hashtable<Integer, Integer>();
		this._textIndexes = new Hashtable<String, Integer>();
		this._textIndexesOverride = new Hashtable<String, String>();
		this._textSortIndex = new Hashtable<Integer, Integer>();
		//this._textCount = 0;
		this._stream.setPos(this._stream.readInt());
		int nb = this._stream.readInt();
		int i1;
		int i2;
		boolean b;
		for(int i = 0; i < nb; i += 9) {
			i1 = this._stream.readInt();
			b = this._stream.readBoolean();
			i2 = this._stream.readInt();
			this._indexes.put(i1, i2);
			if(b) {
				i += 4;
				this._unDiacriticalIndex.put(i1, this._stream.readInt());
			}
			else
				this._unDiacriticalIndex.put(i1, i2);
		}
		nb = this._stream.readInt();
		String str;
		while(nb > 0) {
			i1 = this._stream.getPos();
			str = this._stream.readUTF();
			i2 = this._stream.readInt();
			//this._textCount++;
			this._textIndexes.put(str, i2);
			nb -= this._stream.getPos() - i1;
		}
		nb = this._stream.readInt();
		i2 = 0;
		while(nb > 0) {
			i1 = this._stream.getPos();
			this._textSortIndex.put(this._stream.readInt(), ++i2);
			nb -= this._stream.getPos() - i1; 
		}
	}

	public void overrideId(int i1, int i2) {
		this._indexes.put(i1, this._indexes.get(i2));
		this._unDiacriticalIndex.put(i1, this._unDiacriticalIndex.get(i2));
	}

	public void addOverrideFile(URI file) {
		// méthode utilisée pour réécrire certaines valeurs depuis dans un fichier XML
	}

	public int getOrderIndex(int i) {
		return this._textSortIndex.get(i);
	}

	public String getText(int i) {
		if(this._indexes == null)
			return null;
		Integer index = this._indexes.get(i);
		if(index == null)
			return null;
		if(this._directBuffer == null) {
			this._stream.setPos(index);
			return this._stream.readUTF();
		}
		this._directBuffer.setPos(index);
		return this._directBuffer.readUTF();
	}

	public String getUnDiacriticalText(int i ) {
		if(this._unDiacriticalIndex == null)
			return null;
		Integer index = this._unDiacriticalIndex.get(i);
		if(index == null)
			return null;
		if(this._directBuffer == null) {
			this._stream.setPos(index);
			return this._stream.readUTF();
		}
		this._directBuffer.setPos(index);
		return this._directBuffer.readUTF();
	}

	public boolean hasText(int i) {
		return this._indexes != null && this._indexes.get(i) != null;
	}
	
	public String getNamedText(String str) {
		if(this._textIndexes == null)
			return null;
		if(this._textIndexesOverride.get(str) != null)
			str = this._textIndexesOverride.get(str);
		Integer index = this._textIndexes.get(str);
		if(index == null)
			return null;
		this._stream.setPos(index);
		return this._stream.readUTF();
	}
	
	public boolean hasNamedText(String str) {
		return this._textIndexes != null && this._textIndexes.get(str) != null;
	}
	
	public void useDirectBuffer(boolean b) {
		if((this._directBuffer == null) == b)
			return;
		if(!b)
			this._directBuffer = null;
		else
			this._directBuffer = new ByteArray(this._stream.bytes());
	}
	
	public void close() {
		this._stream = null;
        this._indexes = null;
        this._textIndexes = null;
        this._directBuffer = null;	
	}
}