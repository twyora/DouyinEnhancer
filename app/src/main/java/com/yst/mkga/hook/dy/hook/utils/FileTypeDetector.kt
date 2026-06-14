package com.yst.mkga.hook.dy.hook.utils

import java.io.File

object FileTypeDetector {
    data class FileTypeInfo(
        val mimeType: String,
        val extensions: List<String>,
        val description: String
    )

    object MimeTypes {
        // Images
        val JPEG = FileTypeInfo("image/jpeg", listOf("jpg", "jpeg"), "JPEG Image")
        val PNG = FileTypeInfo("image/png", listOf("png"), "PNG Image")
        val GIF = FileTypeInfo("image/gif", listOf("gif"), "GIF Image")
        val BMP = FileTypeInfo("image/bmp", listOf("bmp"), "BMP Image")
        val TIFF_LE = FileTypeInfo("image/tiff", listOf("tif", "tiff"), "TIFF Image (LE)")
        val TIFF_BE = FileTypeInfo("image/tiff", listOf("tif", "tiff"), "TIFF Image (BE)")
        val ICO = FileTypeInfo("image/x-icon", listOf("ico"), "Icon Image")
        val PSD = FileTypeInfo("image/vnd.adobe.photoshop", listOf("psd"), "Photoshop Document")
        val WEBP = FileTypeInfo("image/webp", listOf("webp"), "WebP Image")
        val HEIC = FileTypeInfo("image/heic", listOf("heic", "heif"), "HEIF Image")
        val HEIF = FileTypeInfo("image/heif", listOf("heif"), "HEIF Image (MIF1)")
        val AVIF = FileTypeInfo("image/avif", listOf("avif"), "AVIF Image")
        val ANI = FileTypeInfo("image/x-ani", listOf("ani"), "Animated Cursor")

        // Audio
        val MP3_ID3 = FileTypeInfo("audio/mpeg", listOf("mp3", "mpga"), "MPEG Audio (ID3)")
        val MP3_RAW = FileTypeInfo("audio/mpeg", listOf("mp3", "mp2"), "MPEG Audio (Raw Frame)")
        val FLAC = FileTypeInfo("audio/flac", listOf("flac"), "FLAC Audio")
        val OGG = FileTypeInfo("audio/ogg", listOf("ogg", "oga", "opus"), "Ogg Audio")
        val AMR = FileTypeInfo("audio/amr", listOf("amr"), "AMR Audio")
        val MIDI = FileTypeInfo("audio/midi", listOf("mid", "midi"), "MIDI Audio")
        val WAV = FileTypeInfo("audio/wav", listOf("wav", "wave"), "WAV Audio")
        val M4A = FileTypeInfo("audio/mp4", listOf("m4a", "m4b"), "MPEG-4 Audio")

        // Video
        val MP4 = FileTypeInfo("video/mp4", listOf("mp4", "m4v", "f4v"), "MPEG-4 Video")
        val QUICKTIME = FileTypeInfo("video/quicktime", listOf("mov", "qt"), "QuickTime Video")
        val V_3GPP = FileTypeInfo("video/3gpp", listOf("3gp", "3gpp"), "3GPP Video")
        val V_3GPP2 = FileTypeInfo("video/3gpp2", listOf("3g2", "3gpp2"), "3GPP2 Video")
        val AVI = FileTypeInfo("video/avi", listOf("avi"), "AVI Video")
        val FLV = FileTypeInfo("video/x-flv", listOf("flv"), "Flash Video")
        val MPEG_PS = FileTypeInfo("video/mpeg", listOf("mpg", "mpeg"), "MPEG-PS Video")
        val MATROSKA = FileTypeInfo("video/x-matroska", listOf("mkv", "webm"), "Matroska/WebM Media")
        val REALMEDIA = FileTypeInfo("application/vnd.rn-realmedia", listOf("rm", "rmvb"), "RealMedia Video")

        // Documents & Archives
        val PDF = FileTypeInfo("application/pdf", listOf("pdf"), "PDF Document")
        val RTF = FileTypeInfo("application/rtf", listOf("rtf"), "Rich Text Format")
        val ZIP = FileTypeInfo("application/zip", listOf("zip", "apk", "docx", "xlsx", "jar"), "Zip Archive")
        val ZIP_EMPTY = FileTypeInfo("application/zip", listOf("zip"), "Zip Archive (Empty)")
        val RAR = FileTypeInfo("application/vnd.rar", listOf("rar"), "RAR Archive")
        val RAR5 = FileTypeInfo("application/vnd.rar", listOf("rar"), "RAR5 Archive")
        val SEVEN_Z = FileTypeInfo("application/x-7z-compressed", listOf("7z"), "7-Zip Archive")
        val GZIP = FileTypeInfo("application/gzip", listOf("gz", "gzip"), "Gzip Archive")
        val BZIP2 = FileTypeInfo("application/x-bzip2", listOf("bz2"), "Bzip2 Archive")

        // Fonts & Executables & Others
        val TTF = FileTypeInfo("font/ttf", listOf("ttf"), "TrueType Font")
        val OTF = FileTypeInfo("font/otf", listOf("otf"), "OpenType Font")
        val WOFF = FileTypeInfo("font/woff", listOf("woff"), "Web Open Font Format")
        val WOFF2 = FileTypeInfo("font/woff2", listOf("woff2"), "Web Open Font Format 2")
        val SWF = FileTypeInfo("application/x-shockwave-flash", listOf("swf"), "Flash SWF")
        val WASM = FileTypeInfo("application/wasm", listOf("wasm"), "WebAssembly Binary")
        val JAVA_CLASS = FileTypeInfo("application/java-vm", listOf("class"), "Java Class File")
        val SQLITE = FileTypeInfo("application/x-sqlite3", listOf("db", "sqlite"), "SQLite Database")
        val OLE2 = FileTypeInfo("application/x-ole-storage", listOf("doc", "xls", "ppt"), "OLE2 Compound Document")
        val DEX = FileTypeInfo("application/vnd.android.dex", listOf("dex"), "Android DEX")
        val ODEX = FileTypeInfo("application/vnd.android.odex", listOf("odex"), "Android ODEX")

        // Fallback
        val OCTET_STREAM = FileTypeInfo("application/octet-stream", listOf("bin"), "Binary Data")
    }

    interface DetectionRule {
        fun match(bytes: ByteArray): FileTypeInfo?
    }

    class ExactMatchRule(
        private val offset: Int,
        private val magic: ByteArray,
        val info: FileTypeInfo
    ) : DetectionRule {
        override fun match(bytes: ByteArray): FileTypeInfo? {
            if (bytes.size < offset + magic.size) {
                return null
            }
            for (i in magic.indices) {
                if (bytes[offset + i] != magic[i]) {
                    return null
                }
            }
            return info
        }
    }

    data class MaskedMatchRule(
        val offset: Int,
        val magic: ByteArray,
        val mask: ByteArray,
        val info: FileTypeInfo
    ) : DetectionRule {
        override fun match(bytes: ByteArray): FileTypeInfo? {
            if (bytes.size < offset + magic.size) {
                return null
            }
            for (i in magic.indices) {
                if ((bytes[offset + i].toInt() and mask[i].toInt()) != (magic[i].toInt() and mask[i].toInt())) {
                    return null
                }
            }
            return info
        }
    }

    class FtypContainerRule : DetectionRule {
        override fun match(bytes: ByteArray): FileTypeInfo? {
            if (bytes.size < 12) {
                return null
            }
            if (String(bytes, 4, 4, Charsets.US_ASCII) != "ftyp") {
                return null
            }

            return when (val brand = String(bytes, 8, 4, Charsets.US_ASCII)) {
                "heic", "hevc", "hevx", "heim", "heis" -> MimeTypes.HEIC
                "mif1" -> MimeTypes.HEIF
                "avif", "avis" -> MimeTypes.AVIF
                "M4A ", "M4B ", "mp4a" -> MimeTypes.M4A
                "qt  " -> MimeTypes.QUICKTIME
                else -> {
                    if (brand.startsWith("3gp") || brand.startsWith("3gr") || brand.startsWith("3gs")) {
                        MimeTypes.V_3GPP
                    } else if (brand.startsWith("3g2")) {
                        MimeTypes.V_3GPP2
                    } else {
                        MimeTypes.MP4
                    }
                }
            }
        }
    }

    class RiffContainerRule : DetectionRule {
        override fun match(bytes: ByteArray): FileTypeInfo? {
            if (bytes.size < 12) {
                return null
            }
            if (String(bytes, 0, 4, Charsets.US_ASCII) != "RIFF") {
                return null
            }

            val subType = String(bytes, 8, 4, Charsets.US_ASCII)
            return when (subType) {
                "WAVE" -> MimeTypes.WAV
                "WEBP" -> MimeTypes.WEBP
                "AVI " -> MimeTypes.AVI
                "ACON" -> MimeTypes.ANI
                else -> null
            }
        }
    }

    private val DEFAULT_RULES: List<DetectionRule> = buildList {
        add(FtypContainerRule())
        add(RiffContainerRule())

        // --- Images ---
        add(
            ExactMatchRule(
                0,
                byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte()),
                FileTypeInfo("image/jpeg", listOf("jpg", "jpeg"), "JPEG Image")
            )
        )
        add(
            ExactMatchRule(
                0,
                byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A),
                FileTypeInfo("image/png", listOf("png"), "PNG Image")
            )
        )
        add(
            ExactMatchRule(
                0,
                "GIF8".toByteArray(Charsets.US_ASCII),
                FileTypeInfo("image/gif", listOf("gif"), "GIF Image")
            )
        )
        add(
            ExactMatchRule(
                0,
                "BM".toByteArray(Charsets.US_ASCII),
                FileTypeInfo("image/bmp", listOf("bmp"), "BMP Image")
            )
        )

        // --- Audio ---
        add(
            ExactMatchRule(
                0,
                "ID3".toByteArray(Charsets.US_ASCII),
                FileTypeInfo("audio/mpeg", listOf("mp3", "mpga"), "MPEG Audio (ID3)")
            )
        )
        add(
            ExactMatchRule(
                0,
                "fLaC".toByteArray(Charsets.US_ASCII),
                FileTypeInfo("audio/flac", listOf("flac"), "FLAC Audio")
            )
        )
        add(
            ExactMatchRule(
                0,
                "OggS".toByteArray(Charsets.US_ASCII),
                FileTypeInfo("audio/ogg", listOf("ogg", "oga"), "Ogg Audio")
            )
        )
        add(
            MaskedMatchRule(
                0,
                byteArrayOf(0xFF.toByte(), 0xE0.toByte()),
                byteArrayOf(0xFF.toByte(), 0xE0.toByte()),
                FileTypeInfo("audio/mpeg", listOf("mp3"), "MPEG Audio (Raw Frame)")
            )
        )

        // --- Video ---
        add(
            ExactMatchRule(
                0,
                "FLV".toByteArray(Charsets.US_ASCII),
                FileTypeInfo("video/x-flv", listOf("flv"), "Flash Video")
            )
        )
        add(
            ExactMatchRule(
                0,
                byteArrayOf(0x1A.toByte(), 0x45.toByte(), 0xDF.toByte(), 0xA3.toByte()),
                FileTypeInfo("video/x-matroska", listOf("mkv", "webm"), "Matroska/WebM")
            )
        )

        // --- Documents & Archives ---
        add(
            ExactMatchRule(
                0,
                "%PDF".toByteArray(Charsets.US_ASCII),
                FileTypeInfo("application/pdf", listOf("pdf"), "PDF Document")
            )
        )
        add(
            ExactMatchRule(
                0,
                byteArrayOf(0x50, 0x4B, 0x03, 0x04),
                FileTypeInfo("application/zip", listOf("zip", "apk", "docx", "xlsx"), "Zip Archive")
            )
        )
        add(
            ExactMatchRule(
                0,
                "Rar!\u001A\u0007\u0000".toByteArray(Charsets.US_ASCII),
                FileTypeInfo("application/vnd.rar", listOf("rar"), "RAR Archive")
            )
        )
        add(
            ExactMatchRule(
                0,
                byteArrayOf(0x37, 0x7A, 0xBC.toByte(), 0xAF.toByte(), 0x27, 0x1C),
                FileTypeInfo("application/x-7z-compressed", listOf("7z"), "7-Zip Archive")
            )
        )

        // --- Android Specific ---
        add(
            ExactMatchRule(
                0,
                "dex\n".toByteArray(Charsets.US_ASCII),
                FileTypeInfo("application/vnd.android.dex", listOf("dex"), "Android DEX")
            )
        )
        add(
            ExactMatchRule(
                0,
                "dey\n".toByteArray(Charsets.US_ASCII),
                FileTypeInfo("application/vnd.android.odex", listOf("odex"), "Android ODEX")
            )
        )
    }

    fun detect(bytes: ByteArray): FileTypeInfo {
        if (bytes.isEmpty()) {
            return MimeTypes.OCTET_STREAM
        }
        for (matcher in DEFAULT_RULES) {
            val result = matcher.match(bytes)
            if (result != null) {
                return result
            }
        }
        return MimeTypes.OCTET_STREAM
    }

    fun detect(file: File, headerSize: Int = 32): FileTypeInfo {
        if (!file.exists() || !file.isFile || file.length() == 0L) {
            return MimeTypes.OCTET_STREAM
        }
        return file.inputStream().use { fis ->
            val buffer = ByteArray(headerSize)
            val bytesRead = fis.read(buffer)
            if (bytesRead > 0) {
                detect(
                    if (bytesRead < buffer.size) {
                        buffer.copyOf(bytesRead)
                    } else {
                        buffer
                    }
                )
            } else {
                MimeTypes.OCTET_STREAM
            }
        }
    }


    fun detect(filePath: String, headerSize: Int = 32): FileTypeInfo = detect(File(filePath))
}