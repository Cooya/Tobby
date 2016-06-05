package gamedata.enums;

public abstract class IdentificationFailureReasonEnum {
	public static final int BAD_VERSION = 1;
    public static final int WRONG_CREDENTIALS = 2;
    public static final int BANNED = 3;
    public static final int KICKED = 4;
    public static final int IN_MAINTENANCE = 5;
    public static final int TOO_MANY_ON_IP = 6;
    public static final int TIME_OUT = 7;
    public static final int BAD_IPRANGE = 8;
    public static final int CREDENTIALS_RESET = 9;
    public static final int EMAIL_UNVALIDATED = 10;
    public static final int OTP_TIMEOUT = 11;
    public static final int LOCKED = 12;
    public static final int SERVICE_UNAVAILABLE = 53;
    public static final int EXTERNAL_ACCOUNT_LINK_REFUSED = 61;
    public static final int EXTERNAL_ACCOUNT_ALREADY_LINKED = 62;
    public static final int UNKNOWN_AUTH_ERROR = 99;
    public static final int SPARE = 100;
}