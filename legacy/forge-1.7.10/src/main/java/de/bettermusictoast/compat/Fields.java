package de.bettermusictoast.compat;

import cpw.mods.fml.relauncher.ReflectionHelper;
import java.lang.reflect.Field;

/**
 * A private field of Minecraft, read by reflection (the newer versions use Mixin accessors).
 * Each field is named twice: its readable name (development) and its name in the released game,
 * its so-called SRG name. Fields Minecraft 1.7.10 never named have only the second one.
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
