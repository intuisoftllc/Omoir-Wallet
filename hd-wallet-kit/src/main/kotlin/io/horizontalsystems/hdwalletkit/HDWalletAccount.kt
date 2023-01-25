package io.horizontalsystems.hdwalletkit

import io.horizontalsystems.hdwalletkit.HDWallet.Chain

class HDWalletAccount(
    accountPrivateKey: HDKey
) {
    private val hdKeychain: HDKeychain = HDKeychain(accountPrivateKey)

    fun privateKey(index: Int, chain: Chain): HDKey {
        return hdKeychain.getKeyByPath("${chain.ordinal}/$index")
    }

    fun fullPublicKeyPath(index: Int, chain: Chain): String =
        hdKeychain.getKeyByPath("${chain.ordinal}/$index").toString()

    fun privateKey(path: String): HDKey {
        return hdKeychain.getKeyByPath(path)
    }

    fun publicKey(index: Int, chain: Chain): HDPublicKey {
        return HDPublicKey(key = privateKey(index, chain))
    }

    fun masterPublicKey(purpose: HDWallet.Purpose, mainNet: Boolean, passphraseWallet: Boolean) =
        hdKeychain.getKeyByPath(if(passphraseWallet) "m" else "m/${purpose.value}'/${if(mainNet) 0 else 1}'/0'")
            .serializePublic(
                HDExtendedKey.getVersion(
                    purpose.value,
                    !mainNet
                ).value
            )

    fun publicKeys(indices: IntRange, chain: Chain): List<HDPublicKey> {
        val parentPrivateKey = privateKey("${chain.ordinal}")
        return hdKeychain.deriveNonHardenedChildKeys(parentPrivateKey, indices).map {
            HDPublicKey(it)
        }
    }
}
