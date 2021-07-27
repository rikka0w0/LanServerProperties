function initializeCoreMod() {
	Opcodes = Java.type("org.objectweb.asm.Opcodes");
	AbstractInsnNode = Java.type("org.objectweb.asm.tree.AbstractInsnNode");
	ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');

	OpenToLanScreenEx = "rikka/lanserverproperties/OpenToLanScreenEx";
	getAvailablePort_owner = "net/minecraft/util/HttpUtil";
	getAvailablePort_name = ASMAPI.mapMethod("m_13939_");
	getAvailablePort_desc = "()I";

	return {
			"ShareToLanScreen.lambda$init$2": {
				"target": {
					"type": "METHOD",
					"class": "net.minecraft.client.gui.screens.ShareToLanScreen",
					"methodName": "m_96659_",
					"methodDesc": "(Lnet/minecraft/client/gui/components/Button;)V"
				},
				"transformer": patchLambda_ShareToLanScreen
			}
	}
}

function patchLambda_ShareToLanScreen(methodNode) {
	// At the beginning of the function
	ASMAPI.appendMethodCall(methodNode, ASMAPI.buildMethodCall(OpenToLanScreenEx, "onOpenToLanClicked", "()V", ASMAPI.MethodType.STATIC));
					
	// Redirect
	var getAvailablePort_Call = ASMAPI.findFirstMethodCall(methodNode, ASMAPI.MethodType.STATIC, getAvailablePort_owner, getAvailablePort_name, getAvailablePort_desc);
	var getAvailablePort_Str = getAvailablePort_owner + "." + getAvailablePort_name + getAvailablePort_desc;
	if (getAvailablePort_Call == null) {
		print("[LSP CoreMod] Unable to find injection point \"" + getAvailablePort_Str + "\"");
	} else {
		getAvailablePort_Call.owner = OpenToLanScreenEx;
		getAvailablePort_Call.name = "getServerPort";
		print("[LSP CoreMod] Redirect \"" + getAvailablePort_Str + "\" to " + OpenToLanScreenEx + ".getServerPort()I");
	}

	// At the end of the function
	var retCount = 0;
	var retCall = ASMAPI.buildMethodCall(OpenToLanScreenEx, "onOpenToLanClosed", "()V", ASMAPI.MethodType.STATIC);
	for (var it = methodNode.instructions.iterator(); it.hasNext();) {
		var instruction = it.next();
		if (instruction.getType() == AbstractInsnNode.INSN && instruction.opcode == Opcodes.RETURN) {
			methodNode.instructions.insertBefore(instruction, retCall);
			retCount++;
		}
	}
	print("[LSP CoreMod] Found " + retCount + " RETURN instruction(s)");

	return methodNode;
}