package roleplay.currentmap;

import utilities.ByteArray;

public class IdentifiedEntityDispositionInformations extends EntityDispositionInformations{

	public IdentifiedEntityDispositionInformations(ByteArray buffer) {
		super(buffer);
		buffer.readDouble();
	}

}
