package roleplay.currentmap;

import utilities.BooleanByteWrapper;
import utilities.ByteArray;

public class FightOptionsInformations {
    public boolean isSecret = false;
    public boolean isRestrictedToPartyOnly = false;
    public boolean isClosed = false;
    public boolean isAskingForHelp = false;

	public FightOptionsInformations(ByteArray buffer) {
        int nb = buffer.readByte();
        this.isSecret = BooleanByteWrapper.getFlag(nb, 0);
        this.isRestrictedToPartyOnly = BooleanByteWrapper.getFlag(nb, 1);
        this.isClosed = BooleanByteWrapper.getFlag(nb, 2);
        this.isAskingForHelp = BooleanByteWrapper.getFlag(nb, 3);
	}
}