function initializeCoreMod() {
	Opcodes = Java.type("org.objectweb.asm.Opcodes");
	AbstractInsnNode = Java.type("org.objectweb.asm.tree.AbstractInsnNode");
	InsnList = Java.type("org.objectweb.asm.tree.InsnList");
	LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
	FieldNode = Java.type("org.objectweb.asm.tree.FieldNode");
	InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
	VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
	FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");
	JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
	TypeInsnNode = Java.type("org.objectweb.asm.tree.TypeInsnNode");
	ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');

	fullPath_LanServerProperties = "rikka/lanserverproperties/LanServerProperties";
	fullPath_ShareToLanScreen = "net/minecraft/client/gui/screens/ShareToLanScreen";
	fullPath_OpenToLanScreenEx = "rikka/lanserverproperties/OpenToLanScreenEx";

	UUIDFixer = "rikka/lanserverproperties/UUIDFixer"

	return {
		"ShareToLanScreen.<addFields>": {
			"target": {
				"type": "CLASS",
				"name": fullPath_ShareToLanScreen
			},
			"transformer": addFields_ShareToLanScreen
		},
		"ShareToLanScreen.<init>": {
			"target": {
				"type": "METHOD",
				"class": fullPath_ShareToLanScreen,
				"methodName": "<init>",
				"methodDesc": "(Lnet/minecraft/client/gui/screens/Screen;)V"
			},
			"transformer": patchInit_ShareToLanScreen
		},
		"LanServerProperties.getLSPData": {
			"target": {
				"type": "METHOD",
				"class": fullPath_LanServerProperties,
				"methodName": "getLSPData",
				"methodDesc": "(L" + fullPath_ShareToLanScreen + ";)L" + fullPath_OpenToLanScreenEx + ";"
			},
			"transformer": impl_getLSPData
		},
		"ShareToLanScreen.startButton.OnClick$Lambda": {
			"target": {
				"type": "METHOD",
				"class": fullPath_ShareToLanScreen,
				"methodName": "m_257074_",
				"methodDesc": "(Lnet/minecraft/client/server/IntegratedServer;Lnet/minecraft/client/gui/components/Button;)V"
			},
			"transformer": patch_startButton_OnClick
		},
		"ShareToLanScreen.portEditBox.OnChange$Lambda": {
			"target": {
				"type": "METHOD",
				"class": fullPath_ShareToLanScreen,
				"methodName": "m_257075_",
				"methodDesc": "(Lnet/minecraft/client/gui/components/Button;Ljava/lang/String;)V"
			},
			"transformer": patch_portEditBox_OnChange
		},
		"ShareToLanScreen.tryParsePort": {
			"target": {
				"type": "METHOD",
				"class": fullPath_ShareToLanScreen,
				"methodName": "tryParsePort",
				"methodDesc": "(Ljava/lang/String;)Lnet/minecraft/network/chat/Component;"
			},
			"transformer": redirect_tryParsePort
		},
		"ShareToLanScreen.render": {
			"target": {
				"type": "METHOD",
				"class": fullPath_ShareToLanScreen,
				"methodName": "render",
				"methodDesc": "(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V"
			},
			"transformer": bypass_render_PortText
		},
		"Player.createPlayerUUID_String": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.world.entity.player.Player",
				"methodName": "m_36283_",
				"methodDesc": "(Ljava/lang/String;)Ljava/util/UUID;"
			},
			"transformer": patchMethod_Player_createPlayerUUID
		}
	}
}

function addFields_ShareToLanScreen(classNode) {
	classNode.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "lsp_objects", "L"+fullPath_OpenToLanScreenEx+";", null, null));
	print("[LSP CoreMod] Add field(s) to ShareToLanScreen!");

	return classNode;
}

//
//	public ShareToLanScreen() {
//		...
// +	this.lsp_objects = LanServerProperties.attachLSPData(this);
//	}
function patchInit_ShareToLanScreen(methodNode) {
	// Append to the end of the constructor
	var lastReturnInst = ASMAPI.findFirstInstruction(methodNode, Opcodes.RETURN);
	var toInject = new InsnList();
	toInject.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
	toInject.add(new InsnNode(Opcodes.DUP));
	toInject.add(ASMAPI.buildMethodCall(fullPath_LanServerProperties, "attachLSPData", "(L" + fullPath_ShareToLanScreen + ";)L" + fullPath_OpenToLanScreenEx + ";", ASMAPI.MethodType.STATIC));
	toInject.add(new FieldInsnNode(Opcodes.PUTFIELD, fullPath_ShareToLanScreen, "lsp_objects", "L"+fullPath_OpenToLanScreenEx+";"));

	methodNode.instructions.insertBefore(lastReturnInst, toInject);

	print("[LSP CoreMod] Patched: ShareToLanScreen.<init>()!");

	return methodNode;
}

//	private static OpenToLanScreenEx getLSPData(ShareToLanScreen screen) {
// +	return screen.lsp_objects;
//		throw new RuntimeException("Coremod implementation failed!");
//	}
function impl_getLSPData(methodNode) {
	var toInject = new InsnList();
	toInject.add(new VarInsnNode(Opcodes.ALOAD, 0)); // the first parameter
	toInject.add(new FieldInsnNode(Opcodes.GETFIELD, fullPath_ShareToLanScreen, "lsp_objects", "L"+fullPath_OpenToLanScreenEx+";"));
	toInject.add(new InsnNode(Opcodes.ARETURN));
	methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), toInject);

	print("[LSP CoreMod] Implemented: LanServerProperties.getLSPData()!");

	return methodNode;
}

function call_OpenToLanScreenEx_VirtualFunction_From_SharedToLanScreen(func_name, func_signature) {
	var toInject = new InsnList();
	toInject.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
	toInject.add(new FieldInsnNode(Opcodes.GETFIELD, fullPath_ShareToLanScreen, "lsp_objects", "L"+fullPath_OpenToLanScreenEx+";"));
	toInject.add(ASMAPI.buildMethodCall(fullPath_OpenToLanScreenEx, func_name, func_signature, ASMAPI.MethodType.VIRTUAL));
	return toInject;
}

function call_OpenToLanScreenEx_callback_on_Return(methodNode, func_name) {
	// At the end of the function
	var retCount = 0;
	var retCall = call_OpenToLanScreenEx_VirtualFunction_From_SharedToLanScreen(func_name, "()V");
	for (var it = methodNode.instructions.iterator(); it.hasNext();) {
		var instruction = it.next();
		if (instruction.getType() == AbstractInsnNode.INSN && instruction.opcode == Opcodes.RETURN) {
			methodNode.instructions.insertBefore(instruction, retCall);
			retCount++;
		}
	}
	return retCount;
}

//	void lambda_func(IntegratedServer integratedserver_implicit, Button startButton_arg) {
//		...
// +	this.lsp_object.onOpenToLanClosed();
//		return;
//	}
function patch_startButton_OnClick(methodNode) {
	var retCount = call_OpenToLanScreenEx_callback_on_Return(methodNode, "onOpenToLanClosed");
	print("[LSP CoreMod] Found " + retCount + " RETURN instruction(s)");
	print("[LSP CoreMod] Patched: ShareToLanScreen.startButton.OnClick$Lambda");
	return methodNode;
}

//	void lambda_func(Button startButton_implicit, String newValue_arg) {
//		...
// +	this.lsp_object.onPortEditBoxChanged();
//		return;
//	}
function patch_portEditBox_OnChange(methodNode) {
	var retCount = call_OpenToLanScreenEx_callback_on_Return(methodNode, "onPortEditBoxChanged");
	print("[LSP CoreMod] Found " + retCount + " RETURN instruction(s)");
	print("[LSP CoreMod] Patched: ShareToLanScreen.portEditBox.OnChange$Lambda");
	return methodNode;
}

//	private Component tryParsePort(String p_259426_) {
//	...
// -	this.port = HttpUtil.getAvailablePort();
// +	this.port = this.lsp_objects.getDefaultPort();
//	...
//	}
function redirect_tryParsePort(methodNode) {
	var cnt = 0;
	for (var it = methodNode.instructions.iterator(); it.hasNext();) {
		var instruction = it.next();

		if (instruction.getOpcode() == Opcodes.INVOKESTATIC
		  && instruction.name.equals(ASMAPI.mapMethod("m_13939_"))
		  && instruction.desc.equals("()I")) {
			it.remove();
			for (var it2 = call_OpenToLanScreenEx_VirtualFunction_From_SharedToLanScreen("getDefaultPort", "()I").iterator(); it2.hasNext();) {
				it.add(it2.next());
			}
			cnt++;
		}
	}
	print("[LSP CoreMod] Redirected " + cnt + " HttpUtil.getAvailablePort() in ShareToLanScreen::tryParsePort, expect 2.");
	return methodNode;
}

function bypass_render_PortText(methodNode) {
	print("[LSP CoreMod] Attempting to bypass drawCenteredString(p_96652_, this.font, PORT_INFO_TEXT, this.width / 2, 142, 16777215); in ShareToLanScreen::render.");

	var delete_begin = -1;

	for (var i = 0; i < methodNode.instructions.size(); i++) {
		var node = methodNode.instructions.get(i);
		if (delete_begin < 0) {
			if (node.getOpcode() == Opcodes.GETSTATIC
			  && node.name.equals(ASMAPI.mapField("f_257007_"))
			  && node.desc.equals("Lnet/minecraft/network/chat/Component;")) {
				// We found GETSTATIC net/minecraft/client/gui/screens/ShareToLanScreen.f_257007_ Lnet/minecraft/network/chat/Component;

				var ALOAD_PoseStack = methodNode.instructions.get(i - 3);
				if (ALOAD_PoseStack.getOpcode() == Opcodes.ALOAD && ALOAD_PoseStack.var == 1) {
					delete_begin = i - 3;
				}
			}
		} else {
			// We have found where to start deleting, looking for the end
			if (node.getOpcode() == Opcodes.INVOKESTATIC
			  && node.name.equals(ASMAPI.mapMethod("m_93215_"))
			  && node.desc.equals("(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V")) {
				// Delete the entire function call
				for (var it = methodNode.instructions.iterator(delete_begin); it.hasNext();) {
					var instruction = it.next();

					if (instruction == node) {
						it.remove();
						print("[LSP CoreMod] Removed " + (i - delete_begin + 1) + " instructions.");
						return methodNode;
					} else {
						it.remove();
					}
				}

				// We can never reach here!
			}
		}
	}

	if (delete_begin < 0) {
		print("[LSP CoreMod] Cannot find where to start, abort.");
	} else {
		print("[LSP CoreMod] Found where to start but not where to stop, abort.");
	}
	
	return methodNode;
}

//	public static UUID createPlayerUUID(String playerName) {
// +	UUID local_1 = UUIDFixer.hookEntry(playerName);
// +	if (local_1 != null)
// +		return local_1;
//		return ....;
//	}
function patchMethod_Player_createPlayerUUID(methodNode) {
	var toInject = new InsnList();
	var originalInstructionsLabel = new LabelNode();
	toInject.add(new VarInsnNode(Opcodes.ALOAD, 0)); // playerName
	toInject.add(ASMAPI.buildMethodCall(UUIDFixer, "hookEntry", "(Ljava/lang/String;)Ljava/util/UUID;", ASMAPI.MethodType.STATIC));
	toInject.add(new VarInsnNode(Opcodes.ASTORE, 1));	// Our new UUID, if any
	toInject.add(new VarInsnNode(Opcodes.ALOAD, 1));	// Our new UUID, if any
	toInject.add(new JumpInsnNode(Opcodes.IFNULL, originalInstructionsLabel));
	toInject.add(new VarInsnNode(Opcodes.ALOAD, 1));	// Our new UUID, if any
	toInject.add(new InsnNode(Opcodes.ARETURN));
	toInject.add(originalInstructionsLabel);
	methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), toInject);

	print("[LSP CoreMod] Patched: Player.createPlayerUUID(String)");

	return methodNode;
}
