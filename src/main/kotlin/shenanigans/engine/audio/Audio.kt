package shenanigans.engine.audio

import shenanigans.engine.term.Logger
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.LineEvent
import javax.sound.sampled.LineListener

class AudioClip(private val stream: AudioInputStream) : AutoCloseable {
    private val clip = AudioSystem.getClip().apply {
        open(stream)
        addLineListener(ClipLogger)
    }

    fun play() {
        clip.framePosition = 0
        clip.start()
    }

    override fun close() {
        clip.close()
        stream.close()
    }

    companion object {
        fun fromFile(name: String): AudioClip {
            val inputStream = this::class.java.getResourceAsStream(name)
            val audioStream = AudioSystem.getAudioInputStream(inputStream)
            return AudioClip(audioStream)
        }

        init {
            AudioSystem.getAudioFileTypes().forEach {
                Logger.log("audio", "supported audio file type: " + it.extension)
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