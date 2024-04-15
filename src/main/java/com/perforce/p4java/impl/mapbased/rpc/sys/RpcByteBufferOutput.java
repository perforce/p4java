/**
 *
 */
package com.perforce.p4java.impl.mapbased.rpc.sys;

import com.perforce.p4java.exception.FileDecoderException;
import com.perforce.p4java.exception.FileEncoderException;
import com.perforce.p4java.exception.NullPointerError;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.impl.mapbased.rpc.func.RpcFunctionMapKey;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.util.Map;

/**
 * Provides a Perforce-specific extension to the basic Java ByteBuffer to allow
 * us to intercept methods and implement our own extensions.
 */

public class RpcByteBufferOutput {
	private int capacity;

	private int bufLimit = 0;

	private ByteBuffer byteBuffer;

	public static RpcByteBufferOutput getBufferOutputStream(String[] cmdArguments) throws IOException {
		int capacity = 0;

		for (String str : cmdArguments) {
			if (str.contains("--size")) {
				capacity = Integer.parseInt(str.split("=")[1]);
			}
		}
		return new RpcByteBufferOutput( capacity );
	}

	private RpcByteBufferOutput(int capacity) {
		this.capacity = capacity;

		if (capacity > 0) {
			this.byteBuffer = ByteBuffer.allocate(capacity);
			this.bufLimit = byteBuffer.limit();
		}
	}

	public void write(byte[] sourceBytes, int off, int len) throws IOException {
		if (sourceBytes == null) {
			throw new NullPointerError("Null bytes passed to RpcByteBufferOutput.write()");
		}
		if (off < 0) {
			throw new P4JavaError("Negative offset in RpcByteBufferOutput.write()");
		}
		if (len < 0) {
			throw new P4JavaError("Negative length in RpcByteBufferOutput.write()");
		}

		int remainingBytes = this.byteBuffer.capacity() - this.byteBuffer.position();
		if( remainingBytes < len ) {
			len = remainingBytes;
		}

		if (this.byteBuffer.limit() == 0) {
			this.byteBuffer.limit(this.bufLimit);
		}

		this.byteBuffer.put(sourceBytes, off, len);
	}

	public void write(byte[] b) throws IOException {
		if (b == null) {
			throw new NullPointerError("Null bytes passed to RpcByteBufferOutput.write()");
		}

		int len = b.length;

		int remainingBytes = this.byteBuffer.capacity() - this.byteBuffer.position();
		if( remainingBytes < len ) {
			len = remainingBytes;
		}

		if ( this.byteBuffer.limit() == 0) {
			this.byteBuffer.limit(bufLimit);
		}

		this.byteBuffer.put(b, 0, len);
	}

	public void write(byte b) throws IOException {
		try {
			this.byteBuffer.put(b);
		}
		catch (BufferOverflowException e) {
			System.out.println("Byte Buffer OverFlow Exception throws : " + e);
		}
		catch (ReadOnlyBufferException e) {
			System.out.println("Byte Buffer ReadOnly Buffer Exception throws : " + e);
		}
	}

	public long write(Map<String, Object> map) throws IOException, FileDecoderException, FileEncoderException {
		if (map == null) {
			throw new NullPointerError("Null map passed to RpcOutputStream.write(map)");
		}

		try {
			byte[] sourceBytes = (byte[]) map.get(RpcFunctionMapKey.DATA);

			int len = sourceBytes.length;
			int bom = 0;

			if (len <= 0) {
				return 0;
			}

			this.write(sourceBytes, 0, len);
			int bytesWritten = (len + bom - 0);

			return bytesWritten;
		}
		catch (ClassCastException exc) {
			throw new P4JavaError("RpcFunctionMapKey.DATA value not byte[] type");
		}
	}

	public ByteBuffer getByteBuffer() {
		this.byteBuffer.flip(); // Rewinds this buffer. The position is set to zero and the mark is discarded.
		return this.byteBuffer;
	}

	public int getCapacity() {
		return this.capacity;
	}

	public int getLimit() {
		return this.bufLimit;
	}

	public int getPosition() {
		return this.byteBuffer.position();
	}
}
