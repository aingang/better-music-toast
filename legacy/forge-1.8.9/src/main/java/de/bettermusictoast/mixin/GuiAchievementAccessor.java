package de.bettermusictoast.mixin;

import net.minecraft.client.gui.achievement.GuiAchievement;
import net.minecraft.stats.Achievement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The "Achievement get!" pop-up, 1.8.9's counterpart of the advancement toasts. */
@Mixin(GuiAchievement.class)
public interface GuiAchievementAccessor {
	@Accessor("theAchievement")
	Achievement bettermusictoast$getAchievement();

	@Accessor("notificationTime")
	long bettermusictoast$getNotificationTime();

	@Accessor("permanentNotification")
	boolean bettermusictoast$isPermanent();
}
