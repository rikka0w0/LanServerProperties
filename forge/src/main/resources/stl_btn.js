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
	getAvailablePort_owner = "net/minecraft/util/HttpUtil";
	getAvailablePort_name = ASMAPI.mapMethod("m_13939_");
	getAvailablePort_desc = "()I";

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
			"ShareToLanScreen.lambda$init$2": {
				"target": {
					"type": "METHOD",
					"class": fullPath_ShareToLanScreen,
					"methodName": "m_96659_",
					"methodDesc": "(Lnet/minecraft/client/gui/components/Button;)V"
				},
				"transformer": patchLambda_ShareToLanScreen
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

	// Not used!
	//toInject.add(new TypeInsnNode(Opcodes.NEW, fullPath_OpenToLanScreenEx));
	//toInject.add(new InsnNode(Opcodes.DUP));
	//toInject.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
	//toInject.add(ASMAPI.buildMethodCall(fullPath_OpenToLanScreenEx, "<init>", "(L" + fullPath_ShareToLanScreen + ";)V", ASMAPI.MethodType.SPECIAL));
	//toInject.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
	//toInject.add(new InsnNode(Opcodes.SWAP));	// Stack: {this, newValue}
	//toInject.add(new FieldInsnNode(Opcodes.PUTFIELD, fullPath_ShareToLanScreen, "lsp_objects", "L"+fullPath_OpenToLanScreenEx+";"));

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

//	void lambda_func(Button not_hooked) {
// +	OpenToLanScreenEx.onOpenToLanClicked();
//		...
// -	... = HttpUtil.getAvailablePort();		// Returns an integer
// +	... = this.lsp_object.getServerPort();	// Returns an integer
//		...
// +	this.lsp_object.onOpenToLanClosed
//		return;
//		...
// +	this.lsp_object.onOpenToLanClosed
//		return;
//	}
function patchLambda_ShareToLanScreen(methodNode) {
	// Redirect
	var getAvailablePort_Call = ASMAPI.findFirstMethodCall(methodNode, ASMAPI.MethodType.STATIC, getAvailablePort_owner, getAvailablePort_name, getAvailablePort_desc);
	var getAvailablePort_Str = getAvailablePort_owner + "." + getAvailablePort_name + getAvailablePort_desc;
	if (getAvailablePort_Call == null) {
		print("[LSP CoreMod] Unable to find injection point \"" + getAvailablePort_Str + "\"");
	} else {
		var toInject = call_OpenToLanScreenEx_VirtualFunction_From_SharedToLanScreen("getServerPort", "()I");
		//toInject.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
		//toInject.add(new FieldInsnNode(Opcodes.GETFIELD, fullPath_ShareToLanScreen, "lsp_objects", "L"+fullPath_OpenToLanScreenEx+";"));
		//toInject.add(ASMAPI.buildMethodCall(fullPath_OpenToLanScreenEx, "getServerPort", "()I", ASMAPI.MethodType.VIRTUAL));
		methodNode.instructions.insertBefore(getAvailablePort_Call, toInject);
		methodNode.instructions.remove(getAvailablePort_Call);

		print("[LSP CoreMod] Redirect \"" + getAvailablePort_Str + "\" to " + fullPath_OpenToLanScreenEx + ".getServerPort()I");
	}

	// At the end of the function
	var retCount = 0;
	var retCall = call_OpenToLanScreenEx_VirtualFunction_From_SharedToLanScreen("onOpenToLanClosed", "()V");
	for (var it = methodNode.instructions.iterator(); it.hasNext();) {
		var instruction = it.next();
		if (instruction.getType() == AbstractInsnNode.INSN && instruction.opcode == Opcodes.RETURN) {
			methodNode.instructions.insertBefore(instruction, retCall);
			retCount++;
		}
	}
	print("[LSP CoreMod] Found " + retCount + " RETURN instruction(s)");
	print("[LSP CoreMod] Patched: ShareToLanScreen_Button_Lambda");

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
