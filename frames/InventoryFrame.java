package frames;

import messages.inventory.InventoryContentAndPresetMessage;
import messages.inventory.InventoryContentMessage;
import messages.inventory.InventoryWeightMessage;
import messages.inventory.KamasUpdateMessage;
import messages.inventory.ObjectAddedMessage;
import messages.inventory.ObjectDeletedMessage;
import messages.inventory.ObjectQuantityMessage;
import messages.inventory.ObjectsAddedMessage;
import messages.inventory.ObjectsDeletedMessage;
import messages.inventory.ObjectsQuantityMessage;
import controller.CharacterState;
import controller.characters.Character;

public class InventoryFrame extends Frame {
	
	public InventoryFrame(Character character) {
		super(character);
	}
	
	protected void process(InventoryContentMessage ICM) {
		this.character.inventory.setObjects(ICM.objects);
		this.character.inventory.setKamas(ICM.kamas);
	}
	
	protected void process(InventoryContentAndPresetMessage ICAPM) {
		this.character.inventory.setObjects(ICAPM.objects);
		this.character.inventory.setKamas(ICAPM.kamas);
	}
	
	protected void process(InventoryWeightMessage IWM) {
		this.character.infos.setWeight(IWM.weight);
		this.character.infos.setWeightMax(IWM.weightMax);
		if(this.character.infos.inventoryIsFull(0.97f)) {
			this.character.updateState(CharacterState.NEED_TO_EMPTY_INVENTORY, true);
			this.character.log.p("Inventory weight maximum almost reached, need to empty.");
		}
	}
	
	protected void process(KamasUpdateMessage KUM) {
		this.character.inventory.setKamas(KUM.kamasTotal);
	}
	
	protected void process(ObjectAddedMessage OAM) {
		this.character.inventory.addObject(OAM.object);
	}
	
	protected void process(ObjectsAddedMessage OAM) {
		this.character.inventory.addObjects(OAM.object);
	}
	
	protected void process(ObjectQuantityMessage OQM) {
		this.character.inventory.updateObjectQuantity(OQM.objectUID, OQM.quantity);
	}
	
	protected void process(ObjectsQuantityMessage OQM) {
		this.character.inventory.updateObjectQuantities(OQM.objectsUIDAndQty);
	}
	
	protected void process(ObjectDeletedMessage ODM) {
		this.character.inventory.removeObject(ODM.objectUID);
	}
	
	protected void process(ObjectsDeletedMessage ODM) {
		this.character.inventory.removeObjects(ODM.objectUID);
	}
}