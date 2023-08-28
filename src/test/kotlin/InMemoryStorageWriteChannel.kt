import com.google.cloud.RestorableState
import com.google.cloud.WriteChannel
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.WritableByteChannel

class InMemoryStorageWriteChannel(private val writableByteChannel: WritableByteChannel) : WriteChannel {

    companion object {
        fun inMemoryChannel(): WriteChannel {
            return InMemoryStorageWriteChannel(Channels.newChannel(ByteArrayOutputStream()))
        }
    }

    override fun close() {
        writableByteChannel.close()
    }

    override fun isOpen(): Boolean {
        return writableByteChannel.isOpen
    }

    override fun write(src: ByteBuffer): Int {
        return writableByteChannel.write(src)
    }

    override fun capture(): RestorableState<WriteChannel> {
        throw NotImplementedError()
    }

    override fun setChunkSize(chunkSize: Int) {
        throw NotImplementedError()
    }
}