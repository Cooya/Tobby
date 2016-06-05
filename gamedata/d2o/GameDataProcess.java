package gamedata.d2o;

import java.util.Hashtable;
import java.util.Vector;

import utilities.ByteArray;

public class GameDataProcess {
	private Hashtable<String, Integer> _searchFieldIndex;
	private Hashtable<String, Integer> _searchFieldCount;
	private Hashtable<String, Integer> _searchFieldType;
	private Vector<String> _queryableField;
	private ByteArray _stream;
	//private ByteArray _currentStream;
	//private Hashtable _sortIndex;

	public GameDataProcess(ByteArray array) {
		this._stream = array;
		//this._sortIndex = new Hashtable();
		parseStream();
	}

	private void parseStream() {
		this._queryableField = new Vector<String>();
		this._searchFieldIndex = new Hashtable<String, Integer>();
		this._searchFieldType = new Hashtable<String, Integer>();
		this._searchFieldCount = new Hashtable<String, Integer>();
		int i1 = this._stream.readInt();
		int i2 = this._stream.getPos() + i1 + 4;
		String str;
		int i3;
		while(i1 > 0) {
			i3 = this._stream.remaining();
			str = this._stream.readUTF();
			this._queryableField.add(str);
			this._searchFieldIndex.put(str, this._stream.readInt() + i2);
			this._searchFieldType.put(str, this._stream.readInt());
			this._searchFieldCount.put(str, this._stream.readInt());
			i1 -= i3 - this._stream.remaining();
		}
	}
}