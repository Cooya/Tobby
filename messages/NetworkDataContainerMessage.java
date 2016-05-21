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
		// création de l'objet de décompression avec passage du tableau d'octets à décompresser
		Inflater inflater = new Inflater();
		inflater.setInput(array.bytes());
		
		// vidage du tableau à recompléter et création du buffer utilisé lors de la décompression
		array.flushArray();
		byte[] buffer = new byte[ByteArray.BUFFER_DEFAULT_SIZE];
		int bufferSize = 0;
		
		// boucle de décompression
		while(!inflater.finished()) {
			try {
				// on récupère progressivement les octets décompressés dans le buffer
				bufferSize = inflater.inflate(buffer);
			} catch(Exception e) {
				e.printStackTrace();
			}
			// que l'on écrit dans le tableau d'octets initial
			array.writeBytes(buffer, bufferSize);
		}
		
		// on retourne au début du tableau
		array.setPos(0);
	}
}