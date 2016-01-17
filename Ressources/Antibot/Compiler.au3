#include <ButtonConstants.au3>
#include <EditConstants.au3>
#include <GUIConstantsEx.au3>
#include <WindowsConstants.au3>
#include <Constants.au3>
#Region ### START Koda GUI section ### Form=
Global $Form1 = GUICreate("Compilateur AIR", 615, 438, 192, 124)
Global $Input1 = GUICtrlCreateInput("", 40, 40, 300, 20)
Global $Button1 = GUICtrlCreateButton("Fichier AS à compiler", 380, 20, 150, 30)
Global $Button2 = GUICtrlCreateButton("Compiler", 380, 60, 150, 30)
Global $debug1 = GUICtrlCreateCheckbox("Debug", 60, 70, 225, 25)
GUICtrlSetState($debug1,$GUI_CHECKED)
Global $Input2 = GUICtrlCreateInput("", 40, 200, 300, 20)
Global $Button3 = GUICtrlCreateButton("Fichier XML à lancer", 380, 180, 150, 30)
Global $Button4 = GUICtrlCreateButton("Lancer", 380, 220, 150, 30)
Global $debug2 = GUICtrlCreateCheckbox("Debug", 60, 230, 225, 25)
GUICtrlSetState($debug2,$GUI_CHECKED)
GUISetState(@SW_SHOW)
#EndRegion ### END Koda GUI section ###

While 1
	$msg = GUIGetMsg()
	Switch $msg
		Case $GUI_EVENT_CLOSE
			Exit
		Case $Button1
			$path1=FileOpenDialog("",@DesktopDir,"(*.as)")
			GUICtrlSetData($Input1,$path1)
		Case $Button2
			If GUICtrlRead($debug1)=$GUI_CHECKED Then
				Run(@ComSpec & ' /k "C:\Progra~2\Flex\bin\amxmlc -debug" ' & $path1)
			Else
				Run(@ComSpec & ' /k "C:\Progra~2\Flex\bin\amxmlc" ' & $path1)
			EndIf
		Case $Button3
			$path2=FileOpenDialog("",@DesktopDir,"(*.xml)")
			GUICtrlSetData($Input2,$path2)
		Case $Button4
			If GUICtrlRead($debug2)=$GUI_CHECKED Then
				Run(@ComSpec & ' /k "C:\Program Files (x86)\Flex\bin\fdb"')
				Sleep(1000)
				Send("run"&"{ENTER}")
				Run("C:\Windows\System32\cmd" & ' /k "C:\Program Files (x86)\AdobeAIRSDK\bin\adl" ' & $path2,"C:\Program Files (x86)\AdobeAIRSDK\bin",@SW_MINIMIZE)
				;Sleep(1000)
				;Send("continue"&"{ENTER}")
			Else
				Run(@ComSpec & ' /k  "C:\Program Files (x86)\AdobeAIRSDK\bin\adl"' & $path2)
			EndIf
	EndSwitch
WEnd
