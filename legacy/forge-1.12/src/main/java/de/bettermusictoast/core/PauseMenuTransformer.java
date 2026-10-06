package de.bettermusictoast.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Pausing the game stops the music too before 1.21.6. Since then music keeps playing in the pause
 * menu and the settings; this brings that behaviour here: the call to SoundHandler.pauseSounds() in
 * Minecraft.displayInGameMenu() goes to {@code SoundAccess.pauseAllButMusic} instead. While paused,
 * the mod keeps the music going (next song, cleanup) from its client tick, see BetterMusicToast.
 */
public final class PauseMenuTransformer implements IClassTransformer {
	private static final Logger LOGGER = LogManager.getLogger("bettermusictoast");
	private static final String HOOK_OWNER = "de/bettermusictoast/compat/SoundAccess";

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (basicClass == null || !"net.minecraft.client.Minecraft".equals(transformedName)) {
			return basicClass;
		}
		ClassNode node = new ClassNode();
		new ClassReader(basicClass).accept(node, 0);
		int changed = 0;
		for (MethodNode method : node.methods) {
			// Development name / the name in the released game.
			if (!"()V".equals(method.desc) || !("displayInGameMenu".equals(method.name) || "func_71385_j".equals(method.name))) {
				continue;
			}
			for (AbstractInsnNode insn : method.instructions.toArray()) {
				if (insn.getOpcode() != Opcodes.INVOKEVIRTUAL) {
					continue;
				}
				MethodInsnNode call = (MethodInsnNode) insn;
				if ("net/minecraft/client/audio/SoundHandler".equals(call.owner) && "()V".equals(call.desc)
						&& ("pauseSounds".equals(call.name) || "func_147689_b".equals(call.name))) {
					// Same stack use: the SoundHandler becomes the hook's only argument.
					method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK_OWNER, "pauseAllButMusic",
							"(Lnet/minecraft/client/audio/SoundHandler;)V", false));
					changed++;
				}
			}
		}
		if (changed == 0) {
			LOGGER.warn("Could not find the pause menu's sound pause; music will pause with the game");
			return basicClass;
		}
		ClassWriter writer = new ClassWriter(0);
		node.accept(writer);
		return writer.toByteArray();
	}
}
