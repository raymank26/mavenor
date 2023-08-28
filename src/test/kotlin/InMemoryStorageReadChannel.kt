import com.google.cloud.ReadChannel
import com.google.cloud.RestorableState
import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel

class InMemoryStorageReadChannel(private val readableByteChannel: ReadableByteChannel) : ReadChannel {

    companion object {
        fun inMemoryReadChannel(byteArray: ByteArray): InMemoryStorageReadChannel {
            return InMemoryStorageReadChannel(Channels.newChannel(ByteArrayInputStream(byteArray)))
        }
    }

    override fun close() {
        readableByteChannel.close()
    }

    override fun isOpen(): Boolean {
        return readableByteChannel.isOpen
    }

    override fun read(dst: ByteBuffer): Int {
        return readableByteChannel.read(dst)
    }

    override fun capture(): RestorableState<ReadChannel> {
        throw NotImplementedError()
    }

    override fun seek(position: Long) {
        throw NotImplementedError()
    }

    override fun setChunkSize(chunkSize: Int) {
        throw NotImplementedError()
    }
}