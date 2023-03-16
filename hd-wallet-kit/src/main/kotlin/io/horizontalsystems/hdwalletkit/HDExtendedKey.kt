package io.horizontalsystems.hdwalletkit

import io.horizontalsystems.hdwalletkit.HDWallet.Purpose
import java.math.BigInteger
import kotlin.experimental.and

class HDExtendedKey(
    val key: HDKey,
    private val version: HDExtendedKeyVersion
) {
    constructor(serialized: String) : this(key(serialized), version(serialized))

    constructor(seed: ByteArray, purpose: Purpose) : this(
        HDKeyDerivation.createRootKey(seed),
        when (purpose) {
            Purpose.BIP44 -> HDExtendedKeyVersion.xprv
            Purpose.BIP49 -> HDExtendedKeyVersion.yprv
            Purpose.BIP84 -> HDExtendedKeyVersion.zprv
        }
    )

    constructor(seed: ByteArray, derivationPath: String, purpose: Purpose) : this(
        HDKeychain(HDKeyDerivation.createRootKey(seed)).getKeyByPath(derivationPath),
        when (purpose) {
            Purpose.BIP44 -> HDExtendedKeyVersion.xprv
            Purpose.BIP49 -> HDExtendedKeyVersion.yprv
            Purpose.BIP84 -> HDExtendedKeyVersion.zprv
        }
    )

    val derivedType: DerivedType
        get() = DerivedType.initFrom(key.depth)

    val info: KeyInfo
        get() = KeyInfo(
            purpose = version.purpose,
            coinType = version.extendedKeyCoinType,
            derivedType = DerivedType.initFrom(key.depth),
            isPublic = version.isPublic
        )

    fun serializePublic() = key.serializePublic(version.pubKey.value)

    fun serializePrivate() = key.serializePrivate(version.privKey.value)

    fun serialize() = if (key.hasPrivKey()) serializePrivate() else serializePublic()

    companion object {
        private const val length = 82

        fun getVersion(purpose: Int, testNet: Boolean) : HDExtendedKeyVersion {
            return when {
                purpose == Purpose.BIP44.value && !testNet -> HDExtendedKeyVersion.xpub
                purpose == Purpose.BIP49.value && !testNet -> HDExtendedKeyVersion.ypub
                purpose == Purpose.BIP84.value && !testNet -> HDExtendedKeyVersion.zpub
                purpose == Purpose.BIP44.value && testNet -> HDExtendedKeyVersion.tpub
                purpose == Purpose.BIP49.value && testNet -> HDExtendedKeyVersion.upub
                purpose == Purpose.BIP84.value && testNet -> HDExtendedKeyVersion.vpub
                else -> HDExtendedKeyVersion.xpub
            }
        }

        private fun key(serialized: String): HDKey {
            val version = version(serialized)

            val data = Base58.decode(serialized)
            if (data.size != length) {
                throw ParsingError.WrongKeyLength
            }

            val depth = data[4] and 0xff.toByte()

            var parentFingerprint = data[5].toInt() and 0x000000FF
            parentFingerprint = parentFingerprint shl 8
            parentFingerprint = parentFingerprint or (data[6].toInt() and 0x000000FF)
            parentFingerprint = parentFingerprint shl 8
            parentFingerprint = parentFingerprint or (data[7].toInt() and 0x000000FF)
            parentFingerprint = parentFingerprint shl 8
            parentFingerprint = parentFingerprint or (data[8].toInt() and 0x000000FF)

            var sequence = data[9].toInt() and 0x000000FF
            sequence = sequence shl 8
            sequence = sequence or (data[10].toInt() and 0x000000FF)
            sequence = sequence shl 8
            sequence = sequence or (data[11].toInt() and 0x000000FF)
            sequence = sequence shl 8
            sequence = sequence or (data[12].toInt() and 0x000000FF)

            val hardened = sequence and HDKey.HARDENED_FLAG != 0
            val childNumber = sequence and 0x7FFFFFFF

            val derivedType = DerivedType.initFrom(depth.toInt())
            if (derivedType == DerivedType.Bip32) {
                throw ParsingError.WrongDerivedType
            }

            validateChecksum(data)

            val bytes: ByteArray = data.copyOfRange(0, data.size - 4)
            val chainCode: ByteArray = bytes.copyOfRange(13, 13 + 32)
            val pubOrPrv: ByteArray = bytes.copyOfRange(13 + 32, bytes.size)

            return if (!version.isPublic) {
                HDKey(
                    BigInteger(1, pubOrPrv),
                    chainCode,
                    null,
                    parentFingerprint,
                    depth.toInt(),
                    childNumber,
                    hardened
                )
            } else {
                HDKey(
                    pubOrPrv,
                    chainCode,
                    null,
                    parentFingerprint,
                    depth.toInt(),
                    childNumber,
                    hardened
                )
            }
        }

        @Throws
        fun version(serialized: String): HDExtendedKeyVersion {
            val prefix = serialized.take(4)
            return HDExtendedKeyVersion.initFrom(prefix) ?: throw ParsingError.WrongVersion
        }

        @Throws
        fun validate(serialized: String, isPublic: Boolean) {
            val raw = Base58.decode(serialized)
            if (raw.size != length) {
                throw ParsingError.WrongKeyLength
            }

            val version = version(serialized)
            if (version.isPublic != isPublic) {
                throw ParsingError.WrongVersion
            }

            validateChecksum(raw)
        }

        fun validateChecksum(extendedKey: ByteArray) {
            val bytes = extendedKey.copyOfRange(0, extendedKey.size - 4)
            val checksum = extendedKey.copyOfRange(extendedKey.size - 4, extendedKey.size)
            val hash = Utils.doubleDigest(bytes).copyOfRange(0, 4)
            if (!hash.contentEquals(checksum)) {
                throw ParsingError.InvalidChecksum
            }
        }
    }

    enum class DerivedType {
        Bip32,
        Master,
        Account;

        companion object {
            //master key depth == 0, account depth = "m/purpose'/coin_type'/account'" = 3, all others is custom
            fun initFrom(depth: Int) =
                when (depth) {
                    0 -> Master
                    3 -> Account
                    else -> Bip32
                }
        }
    }

    data class KeyInfo(
        val purpose: Purpose,
        val coinType: ExtendedKeyCoinType,
        val derivedType: DerivedType,
        val isPublic: Boolean
    )

    sealed class ParsingError(message: String) : Throwable(message) {
        object WrongVersion : ParsingError("Wrong Version")
        object WrongKeyLength : ParsingError("Wrong Key Length")
        object WrongDerivedType : ParsingError("Wrong Derived Type")
        object InvalidChecksum : ParsingError("Invalid Checksum")
    }
}
