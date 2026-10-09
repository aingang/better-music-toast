package de.bettermusictoast.hud;

final class Rect {
	final int x;
	final int y;
	final int width;
	final int height;

	Rect(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	int bottom() {
		return y + height;
	}

	boolean intersects(Rect other) {
		return x < other.x + other.width && other.x < x + width && y < other.y + other.height && other.y < y + height;
	}
}
