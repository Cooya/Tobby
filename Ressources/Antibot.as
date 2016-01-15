package {
	import flash.display.Sprite;
	import flash.utils.*;
	import flash.net.Socket;
	import flash.net.URLRequest;
	import flash.system.LoaderContext;
	import flash.display.Loader;
	import flash.events.Event;
	import flash.events.ProgressEvent;
	import flash.desktop.NativeApplication;
	import flash.system.ApplicationDomain;
	
	public class Antibot extends Sprite {
		private var socket:Socket;
		private var packetSize:int = -1;
		private var publicKey:Vector.<int> = new Vector.<int>();
		private var gameServerTicket:String = null;
		private var result:ByteArray = null;

		public function Antibot() : void {
			socket = new Socket();
			socket.connect("127.0.0.1", 5555);
			trace("Client : connecté au serveur.");
			socket.addEventListener(ProgressEvent.SOCKET_DATA, process);
		}

		private function process(e:Event) : void {
			if(packetSize == -1 && socket.bytesAvailable >= 4) {
				packetSize = socket.readInt();
				trace("Client : taille du paquet à recevoir, " + packetSize + " octets.")
			}
			trace("Client : " + socket.bytesAvailable + " octets recus.");
			if(socket.bytesAvailable == packetSize)
				processData(socket);	
        }
		
        private function processData(socket:Socket) : void {
        	var len:int = socket.readUnsignedShort();
            var i:int = 0;
            while (i < len) {
                this.publicKey.push(socket.readByte());
                i++;
            };
			this.gameServerTicket = socket.readUTF();
			socket.close();
			trace("Client : données reçues traitées.");
			loadDofus();
        }

		private function loadDofus() : void {
			var ldr:Loader = new Loader();
			var lc:LoaderContext = new LoaderContext(false, ApplicationDomain.currentDomain);
            lc.allowCodeImport = true;
			lc.allowLoadBytesCodeExecution = true;
			ldr.contentLoaderInfo.addEventListener(Event.COMPLETE, dofusLoaded);
			ldr.load(new URLRequest("DofusInvoker.swf"), lc);
			addChild(ldr);
		}
		
		private function dofusLoaded(e:Event) : void {
			trace("Client : client Dofus chargé.");
			var AM:Object = getDefinitionByName("com.ankamagames.dofus.logic.connection.managers.AuthentificationManager").getInstance();
			var SSDEM:Class = getDefinitionByName("com.ankamagames.dofus.network.messages.connection.SelectedServerDataMessage") as Class;
			var CH:Object = getDefinitionByName("com.ankamagames.dofus.kernel.net.ConnectionsHandler");

			AM.setPublicKey(this.publicKey);

			var msg:Object = new SSDEM();
			msg.initSelectedServerDataMessage(11, "127.0.0.1", 5555, true, this.gameServerTicket);
         	CH.getConnection().send(msg);
		}
    }
}