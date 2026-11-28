package io.github.twyora.douyinenhancer.utils.file

import java.io.File
import kotlin.concurrent.Volatile
import org.apache.tika.Tika
import org.apache.tika.detect.DefaultDetector
import org.apache.tika.mime.MimeTypes

class MimeTypeDetector(classLoader: ClassLoader?) {
    data class MimeTypeInfo(val mimeType: String, val dotExtensions: List<String>?)

    private val mimeTypes by lazy {
        MimeTypes.getDefaultMimeTypes(classLoader)
    }

    private val tika by lazy {
        Tika(DefaultDetector(mimeTypes))
    }

    fun detect(file: File): Result<MimeTypeInfo> = runCatching {
        val mediaType = tika.detect(file)
        val mimeType = mimeTypes.forName(mediaType)

        MimeTypeInfo(
            mediaType,
            mimeType.extensions.toList().takeIf {
                it.isNotEmpty()
            }
        )
    }

    fun detect(filePath: String) = detect(File(filePath))

    companion object {
        @Volatile
        lateinit var default: MimeTypeDetector
            private set

        fun init(classLoader: ClassLoader?) {
            default = MimeTypeDetector(classLoader)
        }
    }
}
