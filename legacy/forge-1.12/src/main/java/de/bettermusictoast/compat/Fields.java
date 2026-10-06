package de.bettermusictoast.compat;

import java.lang.reflect.Field;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/**
 * A private field of Minecraft, read by reflection (the newer versions use Mixin accessors).
 * Each field is named twice: its readable name (development) and its name in the released game,
 * which stays the same from 1.9.4 to 1.12.2.
 */
public final class Fields<T> {
	private final Class<?> owner;
	private final String[] names;
	private Field field;
	private boolean missing;

	public Fields(Class<?> owner, String... names) {
		this.owner = owner;
		this.names = names;
	}

	/** Whether the field exists in this Minecraft version. */
	public boolean exists() {
		return resolve() != null;
	}

	@SuppressWarnings("unchecked")
	public T get(Object instance) {
		Field f = resolve();
		if (f == null) {
			return null;
		}
		try {
			return (T) f.get(instance);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public void set(Object instance, T value) {
		try {
			resolve().set(instance, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private Field resolve() {
		if (field == null && !missing) {
			try {
				field = ReflectionHelper.findField(owner, names);
			} catch (RuntimeException e) {
				missing = true;
			}
		}
		return field;
	}
}
