package {
	import flash.utils.*;
	import flash.display.Sprite;
	import flash.display.Loader;
	import flash.display.Shape;
	import flash.net.Socket;
	import flash.net.ServerSocket;
	import flash.net.URLRequest;
	import flash.system.ApplicationDomain;
	import flash.system.LoaderContext;
	import flash.events.Event;
	import flash.events.ProgressEvent;
	import flash.events.ServerSocketConnectEvent;
	
	public class Bypass extends Sprite {
		private var interval:uint;
		private var packetSize:uint;
		private var hashFunctionsArray:Array = new Array();

		public function Bypass() : void {
			loadClient();
			runServer();
		}

		private function loadClient() : void {
			trace("Loading client...");
			var loader:Loader = new Loader();
			var lc:LoaderContext = new LoaderContext(false, ApplicationDomain.currentDomain);
            lc.allowCodeImport = true;
			lc.allowLoadBytesCodeExecution = true;
			loader.load(new URLRequest("DofusInvoker.swf"), lc);
			addChild(loader);
		}
		
		private function runServer() : void {
			var server:ServerSocket = new ServerSocket();
			server.bind(5554, "127.0.0.1");
			server.addEventListener(ServerSocketConnectEvent.CONNECT, clientConnectionHandler);
			server.listen();
		}

		private function clientConnectionHandler(e:ServerSocketConnectEvent) : void {
			trace("New client connected.");
			e.socket.addEventListener(Event.CLOSE, clientDisconnectionHandler);
			e.socket.addEventListener(ProgressEvent.SOCKET_DATA, dataReceptionHandler);
		}

		private function clientDisconnectionHandler(e:Event) : void {
			trace("Client disconnected.");
			e.target.removeEventListener(Event.CLOSE, clientDisconnectionHandler);
			e.target.removeEventListener(ProgressEvent.SOCKET_DATA, dataReceptionHandler);
		}

		private function dataReceptionHandler(e:ProgressEvent) : void {
			var socket:Socket = Socket(e.target);
			trace(socket.bytesAvailable + " bytes received from client.");
			if(this.packetSize != -1) {
				if(socket.bytesAvailable >= this.packetSize) {
					trace("Packet size : " + this.packetSize + " bytes.");
					processData(socket, this.packetSize);
					this.packetSize = -1;
					trace(socket.bytesAvailable + " available bytes.");
				}
				else
					return;
			}
			while(socket.bytesAvailable > 4) {
				this.packetSize = socket.readInt();
				if(socket.bytesAvailable >= this.packetSize) {
					trace("Packet size : " + this.packetSize + " bytes.");
					processData(socket, this.packetSize);
					this.packetSize = -1;
				}
				else
					break;
			}
			trace("");
        }
		
        private function processData(socket:Socket, packetSize:int) : void {
        	var hashFunctionId:int;
        	var id:int = socket.readByte(); // identifiant de la requête
        	if(id == 1) { // demande de simulation d'une authentification à partir du client officiel
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
        	}
        	else if(id == 3) { // demande d'utilisation de la fonction de hachage sur un paquet
        		var msg:ByteArray = new ByteArray();
        		hashFunctionId = socket.readByte();
        		trace("Hashing message for client with id = " + hashFunctionId + ".");
        		socket.readBytes(msg, 0, packetSize - 2);
        		hashFunctionsArray[hashFunctionId].call(null, msg);
        		msg.position = 0;
        		socket.writeShort(msg.length);
        		socket.writeBytes(msg, 0);
        		socket.flush();
        	}
        	else if(id == 4) { // demande d'action pour rester "éveillé"
        		trace("Stimulating official client with a fake authentification.")
        		sendLoginValidationAction("toto", "tata");
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
			var AuthentificationFrame:Class = getDefinitionByName("com.ankamagames.dofus.logic.connection.frames.AuthentificationFrame") as Class;
			if(worker.contains(AuthentificationFrame)) {
				clearInterval(interval);
				sendLoginValidationAction(username, password);
			}
		}

		private function getHashFunction() : Function {
			var NetworkMessage:Class = getDefinitionByName("com.ankamagames.jerakine.network.NetworkMessage") as Class;
			return NetworkMessage.HASH_FUNCTION;
		}

		private function sendLoginValidationAction(username:String, password:String) : void {
			var Kernel:Class = getDefinitionByName("com.ankamagames.dofus.kernel.Kernel") as Class;
			var LoginValidationAction:Class = getDefinitionByName("com.ankamagames.dofus.logic.connection.actions.LoginValidationAction") as Class;
			Kernel["getWorker"]().process(LoginValidationAction["create"](username, password, false, 0));
			trace("LoginValidationAction sent.");
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

		private function sendOpenMainMenuAction() : void {
			var Kernel:Class = getDefinitionByName("com.ankamagames.dofus.kernel.Kernel") as Class;
			var OpenMainMenuAction:Class = getDefinitionByName("com.ankamagames.dofus.logic.game.common.actions.OpenMainMenuAction") as Class;
			Kernel["getWorker"]().process(OpenMainMenuAction["create"]());
			trace("OpenMainMenuAction sent.");
		}

		private function sendLeaveDialogAction() : void {
			/*
			var Kernel:Class = getDefinitionByName("com.ankamagames.dofus.kernel.Kernel") as Class;
			var LeaveDialogAction:Class = getDefinitionByName("com.ankamagames.dofus.logic.game.common.actions.GameContextQuitAction") as Class;
			Kernel["getWorker"]().process(LeaveDialogAction["create"]());
			*/
			var KernelEventsManager:Class = getDefinitionByName("com.ankamagames.berilia.managers.KernelEventsManager") as Class;
			var HookList:Class = getDefinitionByName("com.ankamagames.dofus.misc.lists.HookList") as Class;
			KernelEventsManager["getInstance"]().processCallback(HookList["CloseContextMenu"]);
			trace("LeaveDialogAction sent.");
		}
	}
}