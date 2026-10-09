package de.bettermusictoast.track;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** Reads the TITLE and ARTIST tags of an Ogg Vorbis file from its comment header, the file's second packet. */
final class OggTags {
	/** Cover art can make the header big; songs without tags should not be read much further. */
	private static final int MAX_BYTES = 1 << 20;

	private OggTags() {
	}

	/** @return {title, artist (or null)}, or null if the file has no title or is no Ogg Vorbis file */
	static String[] read(InputStream stream) {
		try {
			return readHeader(new DataInputStream(stream));
		} catch (IOException e) {
			return null;
		}
	}

	private static String[] readHeader(DataInputStream in) throws IOException {
		ByteArrayOutputStream packet = new ByteArrayOutputStream();
		byte[] header = new byte[27];
		int packets = 0;
		int read = 0;
		while (read < MAX_BYTES) {
			in.readFully(header);
			if (header[0] != 'O' || header[1] != 'g' || header[2] != 'g' || header[3] != 'S') {
				return null;
			}
			byte[] lacing = new byte[header[26] & 0xFF];
			in.readFully(lacing);
			for (byte lace : lacing) {
				byte[] segment = new byte[lace & 0xFF];
				in.readFully(segment);
				read += segment.length;
				if (packets == 1) {
					packet.write(segment, 0, segment.length);
				}
				// A segment shorter than 255 bytes ends a packet.
				if (segment.length < 255 && ++packets == 2) {
					return parse(packet.toByteArray());
				}
			}
		}
		return null;
	}

	private static String[] parse(byte[] packet) {
		ByteBuffer data = ByteBuffer.wrap(packet).order(ByteOrder.LITTLE_ENDIAN);
		byte[] magic = {3, 'v', 'o', 'r', 'b', 'i', 's'};
		for (byte b : magic) {
			if (!data.hasRemaining() || data.get() != b) {
				return null;
			}
		}
		String title = null;
		String artist = null;
		try {
			skip(data, data.getInt()); // vendor
			int count = data.getInt();
			for (int i = 0; i < count; i++) {
				int length = data.getInt();
				if (length < 0 || length > data.remaining()) {
					break;
				}
				byte[] bytes = new byte[length];
				data.get(bytes);
				String comment = decode(bytes);
				int equals = comment.indexOf('=');
				if (equals <= 0) {
					continue;
				}
				String key = comment.substring(0, equals).toUpperCase(Locale.ROOT);
				String value = comment.substring(equals + 1).trim();
				if (value.isEmpty()) {
					continue;
				}
				if (key.equals("TITLE") && title == null) {
					title = value;
				} else if (key.equals("ARTIST") && artist == null) {
					artist = value;
				}
			}
		} catch (RuntimeException e) {
			// A cut-off header keeps what was read so far.
		}
		return title != null ? new String[] {title, artist} : null;
	}

	private static void skip(ByteBuffer data, int length) {
		if (length < 0 || length > data.remaining()) {
			throw new IllegalArgumentException();
		}
		data.position(data.position() + length);
	}

	/** Tags should be UTF-8, but some tools write Latin-1 ("Góða Nótt"). */
	private static String decode(byte[] bytes) {
		try {
			return StandardCharsets.UTF_8.newDecoder()
					.onMalformedInput(CodingErrorAction.REPORT)
					.onUnmappableCharacter(CodingErrorAction.REPORT)
					.decode(ByteBuffer.wrap(bytes)).toString();
		} catch (CharacterCodingException e) {
			return new String(bytes, StandardCharsets.ISO_8859_1);
		}
	}
}
