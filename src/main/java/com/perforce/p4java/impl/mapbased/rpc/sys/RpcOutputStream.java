/**
 *
 */
package com.perforce.p4java.impl.mapbased.rpc.sys;

import com.perforce.p4java.CharsetConverter;
import com.perforce.p4java.CharsetDefs;
import com.perforce.p4java.env.SystemInfo;
import com.perforce.p4java.exception.FileDecoderException;
import com.perforce.p4java.exception.FileEncoderException;
import com.perforce.p4java.exception.NullPointerError;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.impl.generic.client.ClientLineEnding;
import com.perforce.p4java.impl.mapbased.rpc.RpcPropertyDefs;
import com.perforce.p4java.impl.mapbased.rpc.connection.RpcConnection;
import com.perforce.p4java.impl.mapbased.rpc.func.RpcFunctionMapKey;
import com.perforce.p4java.impl.mapbased.rpc.func.helper.MD5Digester;
import com.perforce.p4java.server.P4Charset;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Checksum;
import java.util.zip.Inflater;

/**
 * Provides a Perforce-specific extension to the basic Java OuputStream to allow
 * us to intercept methods and implement our own extensions. The two main aims
 * here are for the GKUNZIP file type to stream unzip on the fly, and to do line
 * end processing for text files where needed; everything else is just currently
 * handled in the superclass without real intervention.
 * <p>
 * Note that for the unzipping we use a contained file output stream rather than
 * ourself, mostly to avoid recursion...
 * <p>
 * Some of the raw GZUNZIP methods and definitions are copied pretty much as-is
 * from the original gkzip stuff.
 * <p>
 * The 10.2 sync / transfer integrity checks are basically implemented here, with
 * help from the RpcInflaterOutputStream class. The way this is done is the MD5 hashing
 * has to be done against the incoming 'raw' bytes (i.e. the normalized server-side
 * form of the file) unless the incoming file type is compressed binary, in which case
 * we have to hash the uncompressed version. The non-binary hashing is done here; the
 * compressed stuff is done in RpcInflaterOutputStream.
 */

public class RpcOutputStream extends FileOutputStream {

	/*
	 * GZIP header magic number.
	 */
	private final static int GZIP_MAGIC = 0x8b1f;

	/*
	 * GZIP File header flags.
	 */
	@SuppressWarnings("unused")
	private final static int FTEXT = 1;    // Extra text
	private final static int FHCRC = 2; // Header CRC
	private final static int FEXTRA = 4; // Extra field
	private final static int FNAME = 8; // File name
	private final static int FCOMMENT = 16; // File comment

	private static final int TRAILER_SIZE = 8; // bytes

	private RpcPerforceFile file = null;
	private RpcPerforceFileType fileType = null;
	private RpcInflaterOutputStream outStream = null;
	private CheckedOutputStream checkedOutStream = null;
	private Inflater inflater = null;
	private RpcCRC32Checksum crc = null;
	private boolean headerRead = false;
	private byte[] footerBytes = null;
	private boolean closed = false;
	private boolean writeUtf8Bom = false;
	private ClientLineEnding lineEnding = null;
	private RpcLineEndFilterOutputStream lineEndStream = null;
	private CharsetConverter converter = null;
	private String serverDigest = null;    // If given, the server-side MD5 digest
	// for this file. Used in the 10.2+ sync (etc.)
	// transfer integrity checks.
	private MD5Digester localDigester = null;    // Also used in the 10.2+ transfer
	// integrity checks.

	private ByteArrayOutputStream tempBufferToStoreCompressedBytes; // Temp Buffer to store de-compressed bytes

	// Temp variables used in "writeConverted"
	private int len = 0;
	private int start = 0;

	public static RpcOutputStream getTmpOutputStream(RpcPerforceFile file) throws IOException {
		return new RpcOutputStream(file, null, false, false, 1);
	}

	public RpcOutputStream(RpcPerforceFile file, RpcConnection rpcConnection, boolean useLocalDigester) throws IOException {
		this(file, rpcConnection.getP4Charset(), rpcConnection.isUnicodeServer(), useLocalDigester, rpcConnection.getFilesysUtf8bom());
	}

	private RpcOutputStream(RpcPerforceFile file, P4Charset p4Charset, boolean isUnicodeServer, boolean useLocalDigester, int filesys_utf8bom) throws IOException {

		super(file);

		if (file == null) {
			throw new NullPointerError("Null RpcPerforceFile passed to RpcOutputStream constructor");
		}

		if (useLocalDigester) {
			this.localDigester = new MD5Digester();
		}

		// Determine charset for converter.  Non unicode servers and UTF8 types does not require conversion.
		Charset converterCharset = null;
		this.fileType = file.getFileType();
		if (p4Charset != null) {
			Charset charset = p4Charset.getCharset();
			if (charset != null && !(this.fileType.equals(RpcPerforceFileType.FST_XUTF8) || this.fileType.equals(RpcPerforceFileType.FST_UTF8))) {
				converterCharset = isUnicodeServer && !charset.equals(CharsetDefs.UTF8) ? charset : null;
			}
		}

		this.closed = false;
		this.file = file;
		this.lineEnding = file.getLineEnding();
		this.writeUtf8Bom = false;

		if (this.fileType != null) {
			switch (this.fileType) {
				case FST_UTF16:
				case FST_XUTF16:
				case FST_UTF16_GUNZIP:
				case FST_XUTF16_GUNZIP:
					converterCharset = CharsetDefs.UTF16;
				case FST_UTF8:
				case FST_XUTF8:
				case FST_UTF8_GUNZIP:
				case FST_XUTF8_GUNZIP:
					// We only write a UTF8 BOM if no charset was set or it is set to a value other
					// than UTF8
					boolean addBOM = (filesys_utf8bom == 1 || (filesys_utf8bom == 2 && SystemInfo.isWindows()));
					this.writeUtf8Bom = (converterCharset == null) && addBOM;
				case FST_UNICODE:
				case FST_XUNICODE:
				case FST_UNICODE_GUNZIP:
				case FST_XUNICODE_GUNZIP:
					if ((converterCharset != null) && (isUnicodeServer || (converterCharset == CharsetDefs.UTF16))) {
						this.converter = new CharsetConverter(CharsetDefs.UTF8, converterCharset);
					}
					this.writeUtf8Bom |= p4Charset != null && p4Charset.isClientBOM();
				case FST_TEXT:
				case FST_XTEXT:
				case FST_TEXT_GUNZIP:
				case FST_XTEXT_GUNZIP:
					if (ClientLineEnding.needsLineEndFiltering(lineEnding)) {
						this.lineEndStream = new RpcLineEndFilterOutputStream(this, this.lineEnding);
					}

					this.inflater = new Inflater(true);
					this.crc = new RpcCRC32Checksum();
					tempBufferToStoreCompressedBytes = new ByteArrayOutputStream();
					this.checkedOutStream = new CheckedOutputStream(tempBufferToStoreCompressedBytes, this.crc);
					this.outStream = new RpcInflaterOutputStream(this.checkedOutStream, this.inflater, this.localDigester);
					this.headerRead = false;
					this.footerBytes = new byte[TRAILER_SIZE];
					break;

				case FST_BINARY_GUNZIP:
				case FST_XBINARY_GUNZIP:
				case FST_SYMLINK_GUNZIP:
				case FST_RESOURCE_GUNZIP:
				case FST_XSYMLINK_GUNZIP:
				case FST_XRESOURCE_GUNZIP:
				case FST_RTEXT_GUNZIP:
				case FST_XRTEXT_GUNZIP:
				case FST_APPLETEXT_GUNZIP:
				case FST_APPLEFILE_GUNZIP:
				case FST_XAPPLETEXT_GUNZIP:
				case FST_XAPPLEFILE_GUNZIP:
				case FST_GUNZIP:
				case FST_XGUNZIP:
					this.inflater = new Inflater(true);
					this.crc = new RpcCRC32Checksum();
					this.checkedOutStream = new CheckedOutputStream(new BufferedOutputStream(this), this.crc);
					this.outStream = new RpcInflaterOutputStream(this.checkedOutStream, this.inflater, this.localDigester);
					this.headerRead = false;
					this.footerBytes = new byte[TRAILER_SIZE];
					break;

				default:
					break;
			}
		} else {
			this.fileType = RpcPerforceFileType.FST_TEXT;
		}
	}

	@Override
	public void close() throws IOException {
		if (!closed) {
			closed = true;
			switch (this.fileType) {
				case FST_UTF16_GUNZIP:
				case FST_XUTF16_GUNZIP:
				case FST_UTF8_GUNZIP:
				case FST_XUTF8_GUNZIP:
				case FST_UNICODE_GUNZIP:
				case FST_XUNICODE_GUNZIP:
				case FST_TEXT_GUNZIP:
				case FST_XTEXT_GUNZIP:
					readTrailer(this.footerBytes);
					this.outStream.close();
					this.checkedOutStream.close();
				case FST_UTF16:
				case FST_XUTF16:
				case FST_UTF8:
				case FST_XUTF8:
				case FST_UNICODE:
				case FST_XUNICODE:
				case FST_TEXT:
				case FST_XTEXT:
					if (this.lineEndStream != null) {
						this.lineEndStream.close();
					}
					break;

				case FST_GUNZIP:
				case FST_XGUNZIP:
				case FST_BINARY_GUNZIP:
				case FST_XBINARY_GUNZIP:
				case FST_SYMLINK_GUNZIP:
				case FST_RESOURCE_GUNZIP:
				case FST_XSYMLINK_GUNZIP:
				case FST_XRESOURCE_GUNZIP:
				case FST_RTEXT_GUNZIP:
				case FST_XRTEXT_GUNZIP:
				case FST_APPLETEXT_GUNZIP:
				case FST_APPLEFILE_GUNZIP:
				case FST_XAPPLETEXT_GUNZIP:
				case FST_XAPPLEFILE_GUNZIP:
					readTrailer(this.footerBytes);
					this.outStream.close();
					this.checkedOutStream.close();
					break;
				default:
					break;
			}
			super.close();
		}
	}

	/**
	 * Specialized write method to write a map containing a byte array
	 * with the key RpcFunctionMapKey.DATA (all other fields are ignored).
	 * This map is assumed to have been constructed as part of the writeFile()
	 * method or something similar.
	 *
	 * @param map map
	 * @return source bytes
	 * @throws FileEncoderException on error
	 * @throws FileDecoderException on error
	 * @throws IOException          on error
	 */
	public long write(Map<String, Object> map) throws IOException, FileDecoderException, FileEncoderException {
		if (map == null) {
			throw new NullPointerError("Null map passed to RpcOutputStream.write(map)");
		}

		try {
			byte[] sourceBytes = (byte[]) map.get(RpcFunctionMapKey.DATA);
			return writeConverted(sourceBytes);
		} catch (ClassCastException exc) {
			throw new P4JavaError("RpcFunctionMapKey.DATA value not byte[] type");
		}
	}

	@Override
	public void write(byte[] sourceBytes, int off, int len) throws IOException {
		if (sourceBytes == null) {
			throw new NullPointerError("Null bytes passed to RpcOutputStream.write()");
		}
		if (off < 0) {
			throw new P4JavaError("Negative offset in RpcOutputStream.write()");
		}
		if (len < 0) {
			throw new P4JavaError("Negative length in RpcOutputStream.write()");
		}
		super.write(sourceBytes, off, len);
	}

	@Override
	public void write(byte[] b) throws IOException {
		if (b == null) {
			throw new NullPointerError("Null bytes passed to RpcOutputStream.write()");
		}
		super.write(b, 0, b.length);
	}

	/**
	 * Write an array of bytes with being aware of encodings, line ending
	 * conversions, and compression. Any 10.2+ sync integrity checks are
	 * done either here or in the unzip's stream, but (definitely) not
	 * in both places.
	 *
	 * @param sourceBytes sourceBytes
	 * @return bytes written
	 * @throws IOException          on error
	 * @throws FileEncoderException on error
	 * @throws FileDecoderException on error
	 */
	public long writeConverted(byte[] sourceBytes) throws IOException, FileDecoderException, FileEncoderException {
		len = sourceBytes.length;
		start = 0;
		int bom = 0;
		boolean isGunZipType = isGunzipFileType(this.fileType);

		if (!isGunZipType) {
			bom = addUtf8BomIfNeeded(sourceBytes);
			// Only strip the BOM for explicit UTF-8 file types (FST_UTF8 / FST_XUTF8) where
			// filesys.utf8bom=0 means "do not write a BOM".  Legacy unicode files
			// (FST_UNICODE / FST_XUNICODE) stored before P4D 2015.2 carry the BOM as part
			// of the stored content.
			if (isUtf8BomManagedType(this.fileType)) {
				sourceBytes = removeUtf8BomIfNeeded(sourceBytes);
			}
			writeUtf8Bom = false;
		}

		if (len <= 0) {
			return 0;
		}

		long bytesWritten = len + bom - start;

		switch (this.fileType) {
			case FST_UNICODE:
			case FST_UTF16:
			case FST_XUTF16:
			case FST_XUNICODE:
				// Convert to local charset if set
				if (this.converter != null) {
					if (this.localDigester != null) {
						this.localDigester.update(sourceBytes);
					}
					// Convert line endings before converting to unicode
					sourceBytes = convertLineEnding(sourceBytes);
					sourceBytes = convertCharset(sourceBytes);
					if (len <= 0) {
						return 0;
					}

					this.write(sourceBytes, start, len);
					bytesWritten = len + bom - start;
					break;
				}

			case FST_TEXT:
			case FST_UTF8:
			case FST_XTEXT:
			case FST_XUTF8:
				if (this.localDigester != null) {
					this.localDigester.update(sourceBytes, start, len);
				}
				if (this.lineEndStream != null) {
					this.lineEndStream.write(sourceBytes, start, len);
				} else {
					this.write(sourceBytes, start, len);
				}
				bytesWritten = len + bom - start;
				break;

			case FST_UNICODE_GUNZIP:
			case FST_UTF16_GUNZIP:
			case FST_XUTF16_GUNZIP:
			case FST_XUNICODE_GUNZIP:
				// Convert to local charset if set
				if (this.converter != null) {
					sourceBytes = decompressSourceBytes(sourceBytes);
					// Convert line endings before converting to unicode
					sourceBytes = convertLineEnding(sourceBytes);
					sourceBytes = convertCharset(sourceBytes);
					if (len <= 0) {
						return 0;
					}
					this.write(sourceBytes, start, len);
					bytesWritten = len + bom - start;
					break;
				}

			case FST_TEXT_GUNZIP:
			case FST_UTF8_GUNZIP:
			case FST_XTEXT_GUNZIP:
			case FST_XUTF8_GUNZIP:
				sourceBytes = decompressSourceBytes(sourceBytes);

				bom = addUtf8BomIfNeeded(sourceBytes);
				if (isUtf8BomManagedType(this.fileType)) {
					sourceBytes = removeUtf8BomIfNeeded(sourceBytes);
				}
				writeUtf8Bom = false;

				if (this.lineEndStream != null) {
					this.lineEndStream.write(sourceBytes, start, len);
				} else {
					this.write(sourceBytes, start, len);
				}
				bytesWritten = len + bom - start;
				break;

			case FST_GUNZIP:
			case FST_XGUNZIP:
			case FST_BINARY_GUNZIP:
			case FST_XBINARY_GUNZIP:
			case FST_SYMLINK_GUNZIP:
			case FST_RESOURCE_GUNZIP:
			case FST_XSYMLINK_GUNZIP:
			case FST_XRESOURCE_GUNZIP:
			case FST_RTEXT_GUNZIP:
			case FST_XRTEXT_GUNZIP:
			case FST_APPLETEXT_GUNZIP:
			case FST_APPLEFILE_GUNZIP:
			case FST_XAPPLETEXT_GUNZIP:
			case FST_XAPPLEFILE_GUNZIP:

				// NOTE: We always copy the last eight bytes of what's passed into
				// us here so we can use it for trailer / footer calculation
				// later, when we don't have direct access to it (we can't retrieve it
				// from the Inflater without a  bit of skulduggery, unfortunately).
				//
				// This is not as straightforward as it looks due to the endless
				// possibilities for cross-packet trailers, etc., so what's here
				// can almost certainly be reworked in a more efficient way
				// in later versions (using FilterOutputStream?) --- HR.

				long bytesWrittenPrior = decompress(sourceBytes, len);
				bytesWritten = this.outStream.getBytesWritten() - bytesWrittenPrior;
				break;

			default:
				if (this.localDigester != null) {
					this.localDigester.update(sourceBytes, 0, len);
				}
				this.write(sourceBytes, 0, len);
				bytesWritten = len + bom - 0;
		}

		return bytesWritten;
	}

	/**
	 * Writes the UTF-8 BOM if writeUtf8Bom is true and sourceBytes does / doesn't already have one.
	 *
	 * @param sourceBytes the source byte array to check
	 * @return the number of BOM bytes written (3 if BOM was added, 0 otherwise)
	 * @throws IOException on error
	 */
	private int addUtf8BomIfNeeded(byte[] sourceBytes) throws IOException {
		if (writeUtf8Bom && !hasUtf8Bom(sourceBytes)) {
			this.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}, 0, 3);
			return 3;
		}
		return 0;
	}

	/**
	 * Removes 1 UTF-8 BOM from sourceBytes if writeUtf8Bom is false and sourceBytes has one/many.
	 * Also updates the len field to reflect the new length.
	 *
	 * Only applies to UTF-8 family file types (FST_UTF8 / FST_XUTF8 and their _GUNZIP siblings),
	 * where a leading BOM is a charset marker the server expects the client to manage. For all
	 * other types (including FST_TEXT / FST_XTEXT used by pre-2015.2 legacy storage of UTF8+BOM
	 * files), a leading BOM is user content and must be preserved.
	 *
	 * @param sourceBytes the source byte array to process
	 * @return the sourceBytes
	 */
	private byte[] removeUtf8BomIfNeeded(byte[] sourceBytes) {
		if (!writeUtf8Bom && hasUtf8Bom(sourceBytes)) {
			byte[] newSourceBytes = new byte[sourceBytes.length - 3];
			System.arraycopy(sourceBytes, 3, newSourceBytes, 0, sourceBytes.length - 3);
			len = newSourceBytes.length;
			return newSourceBytes;
		}
		return sourceBytes;
	}

	/**
	 * Returns true when the first three bytes are BOM
	 *
	 * @param bytes
	 * */
	private boolean hasUtf8Bom(byte[] bytes) {
		return bytes != null && bytes.length >= 3
				&& bytes[0] == (byte) 0xEF
				&& bytes[1] == (byte) 0xBB
				&& bytes[2] == (byte) 0xBF;
	}

	/**
	 * Returns true when the file type is GUNZIP type
	 *
	 * @param fileType
	 * */
	private boolean isGunzipFileType(RpcPerforceFileType fileType) {
		return fileType == RpcPerforceFileType.FST_TEXT_GUNZIP
				|| fileType == RpcPerforceFileType.FST_UTF8_GUNZIP
				|| fileType == RpcPerforceFileType.FST_XTEXT_GUNZIP
				|| fileType == RpcPerforceFileType.FST_XUTF8_GUNZIP
				|| fileType == RpcPerforceFileType.FST_UNICODE_GUNZIP
				|| fileType == RpcPerforceFileType.FST_UTF16_GUNZIP
				|| fileType == RpcPerforceFileType.FST_XUTF16_GUNZIP
				|| fileType == RpcPerforceFileType.FST_XUNICODE_GUNZIP;
	}

	/**
	 * Returns true iff this file type is an explicit UTF-8 type for which BOM
	 * removal is appropriate.
	 * Legacy unicode files stored before P4D 2015.2 carry the BOM as part of the
	 * stored file content and saved as a text file.
	 *
	 * @param fileType the file type to test
	 * @return true for FST_UTF8, FST_XUTF8, FST_UTF8_GUNZIP, FST_XUTF8_GUNZIP
	 */
	private boolean isUtf8BomManagedType(RpcPerforceFileType fileType) {
		return fileType == RpcPerforceFileType.FST_UTF8
				|| fileType == RpcPerforceFileType.FST_XUTF8
				|| fileType == RpcPerforceFileType.FST_UTF8_GUNZIP
				|| fileType == RpcPerforceFileType.FST_XUTF8_GUNZIP;
	}

	/**
	 * Uncompress the source bytes and store them in "tempBufferToStoreCompressedBytes"
	 *
	 * @param sourceBytes
	 * @return
	 * @throws IOException
	 */
	private byte[] decompressSourceBytes(byte[] sourceBytes) throws IOException {
		decompress(sourceBytes, len);
		byte[] uncompressedBytes = tempBufferToStoreCompressedBytes.toByteArray();
		len = uncompressedBytes.length;
		tempBufferToStoreCompressedBytes.reset();
		return uncompressedBytes;
	}

	/**
	 * Extracted the code to method because of redundancy
	 *
	 * @param sourceBytes
	 * @return
	 * @throws FileDecoderException
	 * @throws FileEncoderException
	 */
	private byte[] convertCharset(byte[] sourceBytes) throws FileDecoderException, FileEncoderException {
		ByteBuffer sourceBuffer = ByteBuffer.wrap(sourceBytes);
		ByteBuffer converted = this.converter.convert(sourceBuffer);
		if (converted != null) {
			sourceBytes = converted.array();
			start = converted.position();
			len = converted.limit();
		} else {
			len = 0;
		}
		return sourceBytes;
	}

	/**
	 * Extracted the code to method because of redundancy
	 *
	 * @param sourceBytes
	 * @return
	 * @throws IOException
	 */
	private byte[] convertLineEnding(byte[] sourceBytes) throws IOException {
		if (this.lineEndStream != null) {
			// Use intermediate buffer to hold line ending converted
			// source bytes
			ByteArrayOutputStream out = new ByteArrayOutputStream(RpcPropertyDefs.RPC_DEFAULT_FILE_BUF_SIZE);
			this.lineEndStream.write(out, sourceBytes, start, len);

			sourceBytes = out.toByteArray();
			len = sourceBytes.length;
			start = 0;
		}
		return sourceBytes;
	}

	private long decompress(byte[] sourceBytes, int len) throws IOException {
		long bytesWrittenPrior = this.outStream.getBytesWritten();

		if (!headerRead) {
			int bytesAvailable;
			try (ByteArrayInputStream byteStream = new ByteArrayInputStream(sourceBytes, 0, len)) {
				readHeader(byteStream, new RpcCRC32Checksum());
				headerRead = true;
				bytesAvailable = byteStream.available();
			}
			if (bytesAvailable > 0) {
				this.outStream.write(sourceBytes, (len - bytesAvailable), bytesAvailable);

				if (bytesAvailable >= TRAILER_SIZE) {
					System.arraycopy(sourceBytes, (len - TRAILER_SIZE), this.footerBytes, 0, TRAILER_SIZE);
				} else {
					// Copy what we can; if it turns out the rest was in the next
					// packet, we'll detect that then...

					System.arraycopy(sourceBytes, (len - bytesAvailable), this.footerBytes, 0, bytesAvailable);
				}
			}
		} else {
			this.outStream.write(sourceBytes, 0, len);

			if (len >= TRAILER_SIZE) {
				System.arraycopy(sourceBytes, (len - TRAILER_SIZE), this.footerBytes, 0, TRAILER_SIZE);
			} else {
				// first move the last (TRAILER_SIZE-len) bytes to the
				// beginning of the footer

				System.arraycopy(this.footerBytes, len, this.footerBytes, 0, TRAILER_SIZE - len);

				// Add to what's (hopefully) there already...

				System.arraycopy(sourceBytes, 0, this.footerBytes, TRAILER_SIZE - len, len);
			}
		}
		return bytesWrittenPrior;
	}

	@Override
	public void write(int b) throws IOException {
		super.write(b);
	}

	public RpcPerforceFile getFile() {
		return file;
	}

	private void readHeader(InputStream inStream, RpcCRC32Checksum crc) throws IOException {
		CheckedInputStream in = new CheckedInputStream(inStream, crc);
		crc.reset();
		// Check header magic
		if (readUShort(in) != GZIP_MAGIC) {
			throw new IOException("Not in GZIP format");
		}
		// Check compression method
		if (readUByte(in) != 8) {
			throw new IOException("Unsupported compression method");
		}
		// Read flags
		int flg = readUByte(in);
		// Skip MTIME, XFL, and OS fields
		skipBytes(in, 6);
		// Skip optional extra field
		if ((flg & FEXTRA) == FEXTRA) {
			skipBytes(in, readUShort(in));
		}
		// Skip optional file name
		if ((flg & FNAME) == FNAME) {
			while (readUByte(in) != 0) ;
		}
		// Skip optional file comment
		if ((flg & FCOMMENT) == FCOMMENT) {
			while (readUByte(in) != 0) ;
		}
		// Check optional header CRC
		if ((flg & FHCRC) == FHCRC) {
			int v = (int) crc.getValue() & 0xffff;
			if (readUShort(in) != v) {
				throw new IOException("Corrupt GZIP header");
			}
		}
	}

	private void readTrailer(byte[] bytes) throws IOException {
		InputStream in = new ByteArrayInputStream(bytes);

		// Uses left-to-right evaluation order
		long intIn = readUInt(in);
		long len = readUInt(in);
		Checksum checksum = this.checkedOutStream.getChecksum();

		if ((checksum != null) && (intIn != checksum.getValue())) {
			throw new IOException("Corrupt GZIP trailer (bad CRC value)");
		}

		if (len != (inflater.getBytesWritten() & 0xffffffffL)) {
			throw new IOException("Corrupt GZIP trailer (bad bytes-written size)");
		}
	}

	/*
	 * Reads unsigned integer in Intel byte order.
	 */
	private long readUInt(InputStream in) throws IOException {
		long s = readUShort(in);
		return ((long) readUShort(in) << 16) | s;
	}

	/*
	 * Reads unsigned short in Intel byte order.
	 */
	private int readUShort(InputStream in) throws IOException {
		int b = readUByte(in);
		return (readUByte(in) << 8) | b;
	}

	/*
	 * Reads unsigned byte.
	 */
	private int readUByte(InputStream in) throws IOException {
		int b = in.read();
		if (b == -1) {
			throw new EOFException();
		}
		if (b < -1 || b > 255) {
			throw new IOException(".read() returned value out of range -1..255: " + b);
		}
		return b;
	}

	private void skipBytes(InputStream in, int n) throws IOException {
		byte[] tmpbuf = new byte[128];
		while (n > 0) {
			int len = in.read(tmpbuf, 0, n < tmpbuf.length ? n : tmpbuf.length);
			if (len == -1) {
				throw new EOFException("Unexpected EOF");
			}
			n -= len;
		}
	}

	public String getServerDigest() {
		return serverDigest;
	}

	public void setServerDigest(String serverDigest) {
		this.serverDigest = serverDigest;
	}

	public MD5Digester getLocalDigester() {
		return localDigester;
	}

	public void setLocalDigester(MD5Digester localDigester) {
		this.localDigester = localDigester;
	}
}
