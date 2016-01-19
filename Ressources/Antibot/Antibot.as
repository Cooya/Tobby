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
	import flash.desktop.NativeApplication;
	import flash.system.ApplicationDomain;
	import flash.desktop.NativeProcess;
	
	public class Antibot extends Sprite {
		private var server:ServerSocket;
		private var packetSize:int = -1;
		private var interval:uint;
		private var injected:Boolean = false;

		public function Antibot() : void {
			loadDofus();
		}

		private function loadDofus() : void {
			var ldr:Loader = new Loader();
			var lc:LoaderContext = new LoaderContext(false, ApplicationDomain.currentDomain);
            lc.allowCodeImport = true;
			lc.allowLoadBytesCodeExecution = true;
			ldr.contentLoaderInfo.addEventListener(Event.COMPLETE, runServer);
			ldr.load(new URLRequest("DofusInvoker.swf"), lc);
			addChild(ldr);
		}
		
		private function runServer(e:Event) : void {
			server = new ServerSocket();
			server.bind(5554, "127.0.0.1");
			server.addEventListener(ServerSocketConnectEvent.CONNECT, clientConnectionHandler);
			server.listen();
		}

		private function clientConnectionHandler(e:ServerSocketConnectEvent) : void {
			var socket:Socket = e.socket;
			socket.writeBoolean(this.injected);
			socket.flush();
			this.injected = true;
			socket.addEventListener(ProgressEvent.SOCKET_DATA, dataReceptionHandler);
			socket.addEventListener(Event.CLOSE, clientDeconnectionHandler);
		}

		private function clientDeconnectionHandler(e:Event) : void {
			trace("test");


			var Kernel:Class = getDefinitionByName("com.ankamagames.dofus.kernel.Kernel") as Class;
			var ResetGameAction:Class = getDefinitionByName("com.ankamagames.jerakine.handlers.messages.Action.ResetGameAction") as Class;

			var rga:Object = ResetGameAction["create"]("");
			var worker:Object = Kernel["getWorker"]();

			worker.process(rga);
		}

		private function dataReceptionHandler(e:ProgressEvent) : void {
			var socket:Socket = Socket(e.target);
			if(packetSize == -1 && socket.bytesAvailable >= 4)
				packetSize = socket.readInt();
			if(socket.bytesAvailable == packetSize) {
				packetSize = -1;
				processData(socket);	
			}
        }
		
        private function processData(socket:Socket) : void {
        	var username:String = socket.readUTF();
        	var password:String = socket.readUTF();
			socket.close();
			login(username, password);
        }

		private function login(username:String, password:String) : void {
			//var SelectedServerDataMessage:Class = getDefinitionByName("com.ankamagames.dofus.network.messages.connection.SelectedServerDataMessage") as Class;
			//var ServerControlFrame:Class = getDefinitionByName("com.ankamagames.dofus.logic.common.frames.ServerControlFrame") as Class;
			//var Frame:Class = getDefinitionByName("com.ankamagames.jerakine.messages.Frame") as Class;
			var LoginValidationAction:Class = getDefinitionByName("com.ankamagames.dofus.logic.connection.actions.LoginValidationAction") as Class;
			var Kernel:Class = getDefinitionByName("com.ankamagames.dofus.kernel.Kernel") as Class;
			var AuthentificationFrame:Class = getDefinitionByName("com.ankamagames.dofus.logic.connection.frames.AuthentificationFrame") as Class;
			
			var worker:Object = Kernel["getWorker"]();
			interval = setInterval(checkAM, 1000, worker, AuthentificationFrame, LoginValidationAction, username, password);
		}

		private function checkAM(worker:Object, AuthentificationFrame:Class, LoginValidationAction:Class, username:String, password:String) : void {
			if(worker.contains(AuthentificationFrame)) {
				clearInterval(interval);
				var lva:Object = LoginValidationAction["create"](username, password, false, 0);
				worker.process(lva);
				trace("LoginValidationAction sent.");
			}
		}
    }
}