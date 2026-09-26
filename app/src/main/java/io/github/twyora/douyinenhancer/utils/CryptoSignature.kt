package io.github.twyora.douyinenhancer.utils

import com.highcapable.yukihookapi.hook.log.YLog
import java.io.InputStream
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec

fun verifySha256RsaSignature(dataStream: InputStream, signatureBytes: ByteArray, publicKeyBytes: ByteArray): Boolean = runCatching {
    val publicKey = KeyFactory.getInstance("RSA").generatePublic(
        X509EncodedKeySpec(publicKeyBytes)
    )

    val sig = Signature.getInstance("SHA256withRSA")
    sig.initVerify(publicKey)

    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var read = 0
    while (
        dataStream.read(buffer).also {
            read = it
        } != -1
    ) {
        sig.update(buffer, 0, read)
    }

    sig.verify(signatureBytes)
}.onFailure {
    YLog.error("failed to verify SHA256-RSA signature", it)
}.getOrDefault(false)