package gamedata.currentmap;

import utilities.ByteArray;

public class IdentifiedEntityDispositionInformations extends EntityDispositionInformations {
	public double id = 0;

	public IdentifiedEntityDispositionInformations(ByteArray buffer) {
		super(buffer);
		this.id = buffer.readDouble();
	}
}