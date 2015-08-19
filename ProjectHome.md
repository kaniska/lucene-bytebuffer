"lucene-bytebuffer" implements Lucene Directory using Direct ByteBuffers.

RAMDirectory uses byte arrays hence subject to java garbage collection. This directory avoids byte[.md](.md) array instead it is backed by direct bytebuffers.

Java Garbage collection has matured a lot but still difficult to get optimal performance in large JVMS. Hence I feel, for objects who's life-cycle can be known beforehand need not be subjected to java GC instead can be tracked "by-hand".

Indexing is typically lives throughout life of JVM hence can be costly in terms of GC which can be easily avoided by keeping it off-heap.

It should be used only when you are indexing large (> 2GB on 64 bit JVM) number of documents in memory. For file-based index MemoryMapped Directory should be used. Refer to Lucene documentation for details.