package messages;

import java.util.zip.Inflater;

import utilities.ByteArray;

public class NetworkDataContainerMessage extends NetworkMessage {

	@Override
	public void serialize() {
		
	}

	@Override
	public void deserialize() {
		this.content = new ByteArray(this.content.readBytes(this.content.readVarInt()));
		uncompress(this.content);
	}
	
	public ByteArray getContent() {
		return this.content;
	}
	
	private void uncompress(ByteArray array) {
		// cr�ation de l'objet de d�compression avec passage du tableau d'octets � d�compresser
		Inflater inflater = new Inflater();
		inflater.setInput(array.bytes());
		
		// vidage du tableau � recompl�ter et cr�ation du buffer utilis� lors de la d�compression
		array.flushArray();
		byte[] buffer = new byte[ByteArray.BUFFER_DEFAULT_SIZE];
		int bufferSize = 0;
		
		// boucle de d�compression
		while(!inflater.finished()) {
			try {
				// on r�cup�re progressivement les octets d�compress�s dans le buffer
				bufferSize = inflater.inflate(buffer);
			} catch(Exception e) {
				e.printStackTrace();
			}
			// que l'on �crit dans le tableau d'octets initial
			array.writeBytes(buffer, bufferSize);
		}
		
		// on retourne au d�but du tableau
		array.setPos(0);
	}
}