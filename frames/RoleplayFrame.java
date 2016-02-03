package frames;

import main.CharacterController;
import main.NetworkInterface;
import messages.EmptyMessage;
import messages.Message;
import messages.currentmap.CurrentMapMessage;
import messages.currentmap.MapInformationsRequestMessage;
import messages.gamestarting.ChannelEnablingMessage;
import messages.gamestarting.ClientKeyMessage;
import messages.gamestarting.PrismsListRegisterMessage;
import roleplay.InterClientKeyManager;
import roleplay.currentmap.EntityDispositionInformations;
import roleplay.currentmap.MapComplementaryInformationsDataMessage;

public class RoleplayFrame extends Frame {
	
	public RoleplayFrame(NetworkInterface net, CharacterController CC) {
		super(net, CC);
	}
	
	public void processMessage(Message msg) {
		switch(msg.getId()) {
			case 6471 :
				InterClientKeyManager ICKM = InterClientKeyManager.getInstance();
				ICKM.getKey();
				EmptyMessage EM1 = new EmptyMessage("FriendsGetListMessage");
				EmptyMessage EM2 = new EmptyMessage("IgnoredGetListMessage");
				EmptyMessage EM3 = new EmptyMessage("SpouseGetInformationsMessage");
				EmptyMessage EM4 = new EmptyMessage("GameContextCreateRequestMessage");
				//EmptyMessage EM5 = new EmptyMessage("ObjectAveragePricesGetMessage");
				EmptyMessage EM6 = new EmptyMessage("QuestListRequestMessage");
				PrismsListRegisterMessage PLRM = new PrismsListRegisterMessage();
				PLRM.serialize();
				ChannelEnablingMessage CEM = new ChannelEnablingMessage();
				CEM.serialize();
				ClientKeyMessage CKM = new ClientKeyMessage();
				CKM.serialize(ICKM);
				net.sendMessage(EM1);
				net.sendMessage(EM2);
				net.sendMessage(EM3);
				net.sendMessage(CKM);
				net.sendMessage(EM4);
				//net.sendMessage(EM5);
				net.sendMessage(EM6);
				net.sendMessage(PLRM);
				net.sendMessage(CEM);
				break;
			case 220 :
				CurrentMapMessage CMM = new CurrentMapMessage(msg);
				CC.setCurrentMap(CMM.getMapId());
				MapInformationsRequestMessage MIRM = new MapInformationsRequestMessage();
				MIRM.serialize(CC.getCurrentMapId());
				net.sendMessage(MIRM);
				break;
			case 226 :
				MapComplementaryInformationsDataMessage MCIDM = new MapComplementaryInformationsDataMessage(msg);
				double characterId = CC.getCharacterId();
				String characterName = MCIDM.getCharacterName(characterId);
				if(characterName != null)
					CC.setCharacterName(characterName);
				else
					throw new Error("Invalid character id.");
				EntityDispositionInformations dispo = MCIDM.getCharacterDisposition(characterId);
				if(dispo != null) {
					CC.setCurrentCellId(dispo.cellId);
					CC.setCurrentDirection(dispo.direction);
				}
				else
					throw new Error("Invalid character id.");
				
				CC.makeCharacterAccessible(); // on peut de nouveau bouger
				
				break;
		}
	}
}