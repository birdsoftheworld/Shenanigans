package shenanigans.engine.audio

import shenanigans.engine.term.Logger
import javax.sound.sampled.*

class AudioClip(private val stream: AudioInputStream) : AutoCloseable {
    private fun makeJClip(): Clip = AudioSystem.getClip().apply {
        open(stream)
        addLineListener(ClipCloser)
        addLineListener(ClipLogger)
    }

    fun play() {
        makeJClip().start()
    }

    override fun close() {
        stream.close()
    }

    companion object {
        fun fromFile(name: String): AudioClip {
            val inputStream = this::class.java.getResourceAsStream(name)
            val audioStream = AudioSystem.getAudioInputStream(inputStream)
            return AudioClip(audioStream)
        }
    }
}

internal object ClipCloser : LineListener {
    override fun update(event: LineEvent) {
        when (event.type) {
            LineEvent.Type.STOP -> {
                event.line.close()
            }
        }
    }
}

internal object ClipLogger : LineListener {
    override fun update(event: LineEvent) {
        when (event.type) {
            LineEvent.Type.OPEN -> {
                Logger.log("audio", "opened audio line")
            }
            LineEvent.Type.START -> {
                Logger.log("audio", "started playing clip")
            }
            LineEvent.Type.STOP -> {
                Logger.log("audio", "stopped playing clip")
            }
            LineEvent.Type.CLOSE -> {
                Logger.log("audio", "closed audio line")
            }
        }
    }
}