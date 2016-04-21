package {
	import flash.utils.*;
	import flash.display.Sprite;
	import flash.display.Loader;
	import flash.net.Socket;
	import flash.net.ServerSocket;
	import flash.net.URLRequest;
	import flash.system.ApplicationDomain;
	import flash.system.LoaderContext;
	import flash.events.Event;
	import flash.events.ProgressEvent;
	import flash.events.ServerSocketConnectEvent;
	
	public class Antibot extends Sprite {
		private var server:ServerSocket;
		private var client:Socket;
		private var packetSize:int = -1;
		private var interval:uint;
		private var unloadTimeout:uint;
		private var injected:Boolean = false;
		private var hashFunctionsArray:Array = new Array();
		private var clients:Array;
		private var loader:Loader;
		//private var clientIsLoaded:Boolean = false;

		public function Antibot() : void {
			loadClient();
			runServer();
		}

		private function loadClient() : void {
			trace("Loading client...");
			loader = new Loader();
			var lc:LoaderContext = new LoaderContext(false, ApplicationDomain.currentDomain);
            lc.allowCodeImport = true;
			lc.allowLoadBytesCodeExecution = true;
			loader.load(new URLRequest("DofusInvoker.swf"), lc); // chargement du client officiel
			addChild(loader);
			//clientIsLoaded = true;
		}

		/*
		private function unloadClient() : void {
			trace("Unloading client...");
			loader.unloadAndStop();
			removeChild(loader);
			loader = null;
			clientIsLoaded = false;
		}
		*/
		
		private function runServer() : void {
			clients = new Array();
			server = new ServerSocket();
			server.bind(5554, "127.0.0.1");
			server.addEventListener(ServerSocketConnectEvent.CONNECT, clientConnectionHandler);
			server.listen();
		}

		private function clientConnectionHandler(e:ServerSocketConnectEvent) : void {
			trace("New client connected.");
			client = e.socket;
			clients.push(client);
			client.writeBoolean(this.injected);
			client.flush();
			this.injected = true;
			client.addEventListener(Event.CLOSE, clientDisconnectionHandler);
			client.addEventListener(ProgressEvent.SOCKET_DATA, dataReceptionHandler);
		}

		private function clientDisconnectionHandler(e:Event) : void {
			trace("Client disconnected.");
			clients.splice(clients.indexOf(e.target), 1);
			e.target.removeEventListener(Event.CLOSE, clientDisconnectionHandler);
			e.target.removeEventListener(ProgressEvent.SOCKET_DATA, dataReceptionHandler);
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
        		//if(!clientIsLoaded)
        		//loadClient();
        		trace("Simulating authentification on official client.");
        		var username:String = socket.readUTF();
        		var password:String = socket.readUTF();
				interval = setInterval(loginAttempt, 500, username, password);
        	}
        	else if(id == 2) { // demande de récupération de la fonction de hachage
        		hashFunctionId = socket.readByte();
        		trace("Retrieving hash function for client with id = " + hashFunctionId + ".");
        		hashFunctionsArray[hashFunctionId] = getHashFunction();
        		sendResetGameAction();
        		//unloadTimeout = setTimeout(unloadClient, 30000);
        	}
        	else if(id == 3) { // demande d'utilisation de la fonction de hachage sur un paquet
        		var msg:ByteArray = new ByteArray();
        		hashFunctionId = socket.readByte();
        		trace("Hashing message for client with id = " + hashFunctionId + ".");
        		socket.readBytes(msg, 0);
        		hashFunctionsArray[hashFunctionId].call(null, msg);
        		msg.position = 0;
        		socket.writeShort(msg.length);
        		socket.writeBytes(msg, 0);
        		socket.flush();
        	}
        	else
        		trace("Invalid packet id.");
        }

		private function loginAttempt(username:String, password:String) : void {
			var Kernel:Class = getDefinitionByName("com.ankamagames.dofus.kernel.Kernel") as Class;
			if(Kernel == null)
				return;
			var worker:Object = Kernel["getWorker"]();
			if(worker == null)
				return;
			var LoginValidationAction:Class = getDefinitionByName("com.ankamagames.dofus.logic.connection.actions.LoginValidationAction") as Class;
			var AuthentificationFrame:Class = getDefinitionByName("com.ankamagames.dofus.logic.connection.frames.AuthentificationFrame") as Class;
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

		private function sendResetGameAction() : void {
			var Kernel:Class = getDefinitionByName("com.ankamagames.dofus.kernel.Kernel") as Class;
			var ResetGameAction:Class = getDefinitionByName("com.ankamagames.dofus.logic.common.actions.ResetGameAction") as Class;
			Kernel["getWorker"]().process(ResetGameAction["create"]());
			trace("ResetGameAction sent.");
		}

		private function sendQuitGameAction() : void {
			var Kernel:Class = getDefinitionByName("com.ankamagames.dofus.kernel.Kernel") as Class;
			var QuitGameAction:Class = getDefinitionByName("com.ankamagames.dofus.logic.common.actions.QuitGameAction") as Class;
			Kernel["getWorker"]().process(QuitGameAction["create"]());
			trace("QuitGameAction sent.");
		}
	}
}