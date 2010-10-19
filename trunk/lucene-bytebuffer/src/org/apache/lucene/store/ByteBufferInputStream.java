package org.apache.lucene.store;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * A memory-resident {@link IndexInput} implementation.
 * 
 * @version $Id: RAMInputStream.java 632120 2008-02-28 21:13:59Z mikemccand $
 */

class ByteBufferInputStream extends IndexInput implements Cloneable {
  static final int BUFFER_SIZE = RAMOutputStream.BUFFER_SIZE;

  private ByteBufferFile file;
  private long length;

  private ByteBuffer currentBuffer;
  private int currentBufferIndex;
  
  private int bufferPosition;
  private long bufferStart;
  private int bufferLength;

  ByteBufferInputStream(ByteBufferFile f) throws IOException {
    file = f;
    length = file.length;
    if (length/BUFFER_SIZE >= Integer.MAX_VALUE) {
      throw new IOException("Too large RAMFile! "+length); 
    }

    // make sure that we switch to the
    // first needed buffer lazily
    currentBufferIndex = -1;
    currentBuffer = null;
  }

  public void close() {
    // nothing to do here
  }

  public long length() {
    return length;
  }

  public byte readByte() throws IOException {
    if (bufferPosition >= bufferLength) {
      currentBufferIndex++;
      switchCurrentBuffer(true);
    }
    currentBuffer.position(bufferPosition++);
    byte b = currentBuffer.get();
    //return currentBuffer[bufferPosition++];
    return b;
  }

  public void readBytes(byte[] b, int offset, int len) throws IOException {
    while (len > 0) {
      if (bufferPosition >= bufferLength) {
        currentBufferIndex++;
        switchCurrentBuffer(true);
      }

      int remainInBuffer = bufferLength - bufferPosition;
      int bytesToCopy = len < remainInBuffer ? len : remainInBuffer;
      //System.arraycopy(currentBuffer, bufferPosition, b, offset, bytesToCopy);
      //arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
      currentBuffer.position(bufferPosition);
      currentBuffer.get(b, offset, bytesToCopy);
      offset += bytesToCopy;
      len -= bytesToCopy;
      bufferPosition += bytesToCopy;
      currentBuffer.position(bufferPosition);
    }
  }

  private final void switchCurrentBuffer(boolean enforceEOF) throws IOException {
    if (currentBufferIndex >= file.numBuffers()) {
      // end of file reached, no more buffers left
      if (enforceEOF)
        throw new IOException("Read past EOF");
      else {
        // Force EOF if a read takes place at this position
        currentBufferIndex--;
        bufferPosition = BUFFER_SIZE;
        currentBuffer.position(bufferPosition);
      }
    } else {
      currentBuffer = file.getBuffer(currentBufferIndex);
      bufferPosition = 0;
      currentBuffer.position(bufferPosition);
      bufferStart = (long) BUFFER_SIZE * (long) currentBufferIndex;
      long buflen = length - bufferStart;
      bufferLength = buflen > BUFFER_SIZE ? BUFFER_SIZE : (int) buflen;
    }
  }

  public long getFilePointer() {
    return currentBufferIndex < 0 ? 0 : bufferStart + bufferPosition;
  }

  public void seek(long pos) throws IOException {
    if (currentBuffer==null || pos < bufferStart || pos >= bufferStart + BUFFER_SIZE) {
      currentBufferIndex = (int) (pos / BUFFER_SIZE);
      switchCurrentBuffer(false);
    }
    bufferPosition = (int) (pos % BUFFER_SIZE);
    currentBuffer.position(bufferPosition);
  }
}
