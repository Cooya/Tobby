package main;

public class Main {
	private static final boolean MODE = true; // sniffer = false
	public static final String DLL_LOCATION = "Ressources/DLLInjector/No.Ankama.dll";
	public static final int BUFFER_DEFAULT_SIZE = 8192;
	public static final String AUTH_SERVER_IP = "213.248.126.39";
	public static final int SERVER_PORT = 5555;
	
	public static void main(String[] args) {
		if(MODE) {
			Emulation.runASLauncher();
			new Instance("maxlebgdu9", "represente", 11);
			new Instance("maxlebgdu93", "represente", 11);
			//new Instance("maxlebgdu94", "represente", 11);
			//new Instance("maxlebgdu96", "represente", 11);
			//new Instance("maxlebgdu97", "represente", 11);
			//new Instance("maxlebgdu98", "represente", 11);
			//new Instance("maxlebgdu99", "represente", 11);
			//new Instance("maxlebgdu100", "represente", 11);
		}
		else
			new Sniffer();
	}
}