package {
	import flash.display.Sprite;
	import flash.utils.*;
	import flash.net.Socket;
	import flash.net.ServerSocket;
	import flash.net.URLRequest;
	import flash.system.LoaderContext;
	import flash.display.Loader;
	import flash.events.Event;
	import flash.events.ProgressEvent;
	import flash.events.ServerSocketConnectEvent;
	//import flash.events.IEventDispatcher;
	//import flash.events.KeyboardEvent;
	import flash.system.ApplicationDomain;
	//import flash.ui.Keyboard;
	
	public class Antibot extends Sprite /* implements IEventDispatcher */ {
		private var server:ServerSocket;
		private var client:Socket;
		private var packetSize:int = -1;
		private var interval:uint;
		private var injected:Boolean = false;
		private var hashFunctionsArray:Array = new Array();

		public function Antibot() : void {
			var ldr:Loader = new Loader();
			var lc:LoaderContext = new LoaderContext(false, ApplicationDomain.currentDomain);
            lc.allowCodeImport = true;
			lc.allowLoadBytesCodeExecution = true;
			ldr.contentLoaderInfo.addEventListener(Event.COMPLETE, runServer);
			ldr.load(new URLRequest("DofusInvoker.swf"), lc); // chargement du client officiel
			addChild(ldr);
		}
		
		private function runServer(e:Event) : void {
			server = new ServerSocket();
			server.bind(5554, "127.0.0.1");
			server.addEventListener(ServerSocketConnectEvent.CONNECT, clientConnectionHandler);
			server.listen();
		}

		private function clientConnectionHandler(e:ServerSocketConnectEvent) : void {
			client = e.socket;
			client.writeBoolean(this.injected);
			client.flush();
			this.injected = true;
			client.addEventListener(ProgressEvent.SOCKET_DATA, dataReceptionHandler);
		}

		private function dataReceptionHandler(e:ProgressEvent) : void {
			var socket:Socket = Socket(e.target);
			trace(socket.bytesAvailable + " bytes received from client.");
			if(packetSize == -1 && socket.bytesAvailable >= 4)
				packetSize = socket.readInt();
			if(socket.bytesAvailable == packetSize) {
				packetSize = -1;
				processData(socket);	
			}
        }
		
        private function processData(socket:Socket) : void {
        	var hashFunctionId:int;
        	var id:int = socket.readByte();
        	if(id == 1) { // demande de simulation d'une authentification à partir du client officiel
        		var username:String = socket.readUTF();
        		var password:String = socket.readUTF();
				login(username, password);
        	}
        	else if(id == 2) { // demande de récupération de la fonction de hachage
        		hashFunctionId = socket.readByte();
        		hashFunctionsArray[hashFunctionId] = getHashFunction();
        	}
        	else if(id == 3) { // demande d'utilisation de la fonction de hachage sur un paquet
        		var msg:ByteArray = new ByteArray();
        		hashFunctionId = socket.readByte();
        		socket.readBytes(msg, 0);
        		hashFunctionsArray[hashFunctionId].call(null, msg);
        		msg.position = 0;
        		socket.writeBytes(msg, 0);
        		socket.flush();
        	}
        	else
        		trace("Invalid packet id.");
        }

		private function login(username:String, password:String) : void {
			var LoginValidationAction:Class = getDefinitionByName("com.ankamagames.dofus.logic.connection.actions.LoginValidationAction") as Class;
			var Kernel:Class = getDefinitionByName("com.ankamagames.dofus.kernel.Kernel") as Class;
			var AuthentificationFrame:Class = getDefinitionByName("com.ankamagames.dofus.logic.connection.frames.AuthentificationFrame") as Class;
			
			var worker:Object = Kernel["getWorker"]();
			interval = setInterval(checkAM, 500, worker, AuthentificationFrame, LoginValidationAction, username, password);
		}

		private function checkAM(worker:Object, AuthentificationFrame:Class, LoginValidationAction:Class, username:String, password:String) : void {
			if(worker.contains(AuthentificationFrame)) {
				clearInterval(interval);
				var lva:Object = LoginValidationAction["create"](username, password, false, 0);
				worker.process(lva);
				trace("LoginValidationAction sent.");
			}
		}

		private function getHashFunction() : Function {
			var NetworkMessage:Class = getDefinitionByName("com.ankamagames.jerakine.network.NetworkMessage") as Class;
			return NetworkMessage.HASH_FUNCTION;
		}

		/*
		private function sendResetGameAction() : void {
			var Kernel:Class = getDefinitionByName("com.ankamagames.dofus.kernel.Kernel") as Class;
			var ResetGameAction:Class = getDefinitionByName("com.ankamagames.dofus.logic.common.actions.ResetGameAction") as Class;

			Kernel["getWorker"]().process(new ResetGameAction());
			trace("ResetGameAction sent.");

			var keyCode:uint = Keyboard.ENTER;
        	var e:KeyboardEvent = new KeyboardEvent(KeyboardEvent.KEY_DOWN, true, false, 0, keyCode);   
        	dispatchEvent(e);
		}
		*/
    }
}