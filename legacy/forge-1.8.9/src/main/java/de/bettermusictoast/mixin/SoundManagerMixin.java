package de.bettermusictoast.mixin;

import com.google.common.collect.Multimap;
import de.bettermusictoast.compat.SoundManagerAccess;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.audio.SoundPoolEntry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import paulscode.sound.SoundSystem;

// Lets music keep playing while the game is paused, like 1.21.6+ (see MinecraftMixin), and tells
// the mod which file a sound was started with.
@Mixin(SoundManager.class)
public abstract class SoundManagerMixin implements SoundManagerAccess {
	// Its type is a private inner class, so it cannot be a @Shadow; it is read once by reflection.
	@Unique
	private static Field bettermusictoast$soundSystemField;

	@Shadow
	private boolean loaded;

	@Shadow
	private int playTime;

	@Shadow
	@Final
	private Map<String, ISound> playingSounds;

	@Shadow
	private Map<ISound, SoundPoolEntry> playingSoundPoolEntries;

	@Shadow
	@Final
	private Multimap<SoundCategory, String> categorySounds;

	@Shadow
	@Final
	private List<ITickableSound> tickableSounds;

	@Shadow
	@Final
	private Map<String, Integer> playingSoundsStopTime;

	/** Songs that have been heard playing during the current pause. */
	@Unique
	private final Set<String> bettermusictoast$startedMusic = new HashSet<String>();

	@Unique
	private SoundSystem bettermusictoast$soundSystem() {
		if (bettermusictoast$soundSystemField == null) {
			bettermusictoast$soundSystemField = ReflectionHelper.findField(SoundManager.class, "sndSystem", "field_148620_e");
		}
		try {
			return (SoundSystem) bettermusictoast$soundSystemField.get(this);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public SoundPoolEntry bettermusictoast$getEntry(ISound sound) {
		return playingSoundPoolEntries.get(sound);
	}

	// Like SoundEngine.pauseAllExcept(MUSIC, UI) in 1.21.6+. Menu clicks play in MASTER, which
	// is not listed in categorySounds.
	@Override
	public void bettermusictoast$pauseAllButMusic() {
		if (!loaded) {
			return;
		}
		SoundSystem soundSystem = bettermusictoast$soundSystem();
		for (String id : playingSounds.keySet()) {
			if (categorySounds.containsValue(id) && !categorySounds.containsEntry(SoundCategory.MUSIC, id)) {
				soundSystem.pause(id);
			}
		}
		bettermusictoast$startedMusic.clear();
	}

	// Like SoundEngine.tickMusicWhenPaused in 1.21.6+. Without this, a song that ended during the
	// pause would count as playing until the game goes on, and would even start over then.
	@Override
	public void bettermusictoast$cleanUpEndedMusic() {
		if (!loaded) {
			return;
		}
		SoundSystem soundSystem = bettermusictoast$soundSystem();
		Iterator<Map.Entry<String, ISound>> iterator = playingSounds.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, ISound> entry = iterator.next();
			String id = entry.getKey();
			if (!categorySounds.containsEntry(SoundCategory.MUSIC, id)) {
				continue;
			}
			if (soundSystem.playing(id)) {
				bettermusictoast$startedMusic.add(id);
				continue;
			}
			// A song that was just started may not be reported as playing yet.
			Integer stopTime = playingSoundsStopTime.get(id);
			if (!bettermusictoast$startedMusic.contains(id) && (stopTime == null || stopTime > playTime)) {
				continue;
			}
			ISound sound = entry.getValue();
			iterator.remove();
			soundSystem.removeSource(id);
			playingSoundsStopTime.remove(id);
			playingSoundPoolEntries.remove(sound);
			categorySounds.remove(SoundCategory.MUSIC, id);
			if (sound instanceof ITickableSound) {
				tickableSounds.remove(sound);
			}
			bettermusictoast$startedMusic.remove(id);
		}
	}
}
