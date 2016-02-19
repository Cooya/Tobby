package game.server;

public enum ServerStatusEnum {
	STATUS_UNKNOWN(0),
    OFFLINE(1),
    STARTING(2),
    ONLINE(3),
    NOJOIN(4),
    SAVING(5),
    STOPING(6),
    FULL(7);
	
	@SuppressWarnings("unused")
	private int value;

    private ServerStatusEnum(int value) {
    	this.value = value;
    }
}