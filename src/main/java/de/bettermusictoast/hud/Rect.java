package de.bettermusictoast.hud;

/** Screen-space rectangle in GUI pixels. */
// Before 1.17 the jar targets Java 8, where Jabel turns records into plain classes.
//? if <1.17
/*@com.github.bsideup.jabel.Desugar*/
public record Rect(int x, int y, int w, int h) {
	public boolean intersects(Rect o) {
		return x < o.x + o.w && o.x < x + w && y < o.y + o.h && o.y < y + h;
	}

	public int bottom() {
		return y + h;
	}
}
