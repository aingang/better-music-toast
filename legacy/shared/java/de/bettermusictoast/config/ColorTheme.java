package de.bettermusictoast.config;

/**
 * Colour themes for the panel. Apart from the classic look, each pairs a light background
 * with a darker border taken from a two-colour combination in Wada Sanzō's
 * "A Dictionary of Color Combinations" (1933-34), sampled from the printed swatches.
 * Same colours as in the newer Minecraft versions.
 */
public enum ColorTheme {
	CLASSIC(0xE9D8B0, 0x4A2E16, 0xF8EED4, 0xC8AE80, 0x3B2410, 0x7A5432, 0x6B4626),
	MIRUAI("094", 0xEDCC9D, 0x0A433F,
			new ColorName("海松藍", "Miruai", "Dusky Green"),
			new ColorName("白茶", "Shiracha", "Ivory Buff")),
	KOKUSHI("050", 0xE1C78A, 0x441835,
			new ColorName("黒紫", "Kokushi", "Dusky Madder Violet"),
			new ColorName("白茶", "Shiracha", "Ivory Buff")),
	KAMENOZOKI("016", 0xB3D0DC, 0x6D191F,
			new ColorName("紅海老茶", "Beniebicha", "Vandyke Red"),
			new ColorName("瓶覗", "Kamenozoki", "Pale King's Blue")),
	FUKAGAWA("119", 0xB1C8CC, 0x1D273D,
			new ColorName("深縹", "Kokihanada", "Dark Tyrian Blue"),
			new ColorName("深川鼠", "Fukagawa-nezumi", "Light Glaucous Blue")),
	/** The book's raw sienna is darkened slightly so text on the pale yellow stays legible. */
	HIWADA("003", 0xE7D99E, 0x8E3F12,
			new ColorName("檜皮色", "Hiwada-iro", "Raw Sienna"),
			new ColorName("淡卵色", "Usutamago-iro", "Pale Lemon Yellow"));

	/** A colour as named in the book: kanji, reading and the English name. */
	public static final class ColorName {
		public final String kanji;
		public final String romaji;
		public final String english;

		ColorName(String kanji, String romaji, String english) {
			this.kanji = kanji;
			this.romaji = romaji;
			this.english = english;
		}
	}

	public final int fill;
	public final int border;
	public final int highlight;
	public final int shade;
	public final int title;
	public final int artist;
	public final int icon;
	/** Combination number in the book, or null for the classic theme. */
	public final String bookNumber;
	public final ColorName borderName;
	public final ColorName fillName;

	ColorTheme(int fill, int border, int highlight, int shade, int title, int artist, int icon) {
		this(fill, border, highlight, shade, title, artist, icon, null, null, null);
	}

	/** Derives the bevel, text and icon colours the same way the classic palette is built. */
	ColorTheme(String bookNumber, int fill, int border, ColorName borderName, ColorName fillName) {
		this(fill, border,
				mix(fill, 0xFFFFFF, 0.45f),
				mix(fill, border, 0.22f),
				mix(border, 0x000000, 0.15f),
				mix(border, fill, 0.38f),
				mix(border, fill, 0.12f),
				bookNumber, borderName, fillName);
	}

	ColorTheme(int fill, int border, int highlight, int shade, int title, int artist, int icon,
			String bookNumber, ColorName borderName, ColorName fillName) {
		this.fill = fill;
		this.border = border;
		this.highlight = highlight;
		this.shade = shade;
		this.title = title;
		this.artist = artist;
		this.icon = icon;
		this.bookNumber = bookNumber;
		this.borderName = borderName;
		this.fillName = fillName;
	}

	public String translationKey() {
		return "bettermusictoast.colorTheme." + name().toLowerCase();
	}

	public static int mix(int from, int to, float t) {
		int result = 0;
		for (int shift = 0; shift <= 16; shift += 8) {
			int a = (from >> shift) & 0xFF;
			int b = (to >> shift) & 0xFF;
			result |= Math.round(a + (b - a) * t) << shift;
		}
		return result;
	}
}
