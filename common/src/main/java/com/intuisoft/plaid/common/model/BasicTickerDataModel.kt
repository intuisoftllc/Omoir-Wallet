package com.intuisoft.plaid.common.model

import com.intuisoft.plaid.common.repositories.LocalStoreRepository

data class BasicTickerDataModel(
    var price: Double,
    var marketCap: Double,
    var totalVolume: Double,
    var circulatingSupply: Double,
    val maxSupply: Double
) {
    companion object {
        fun consume(data: CoinInfoDataModel, localStoreRepository: LocalStoreRepository): BasicTickerDataModel {
            return BasicTickerDataModel(
                price = data.marketData.currentPrice.getPrice(localStoreRepository.getLocalCurrency()),
                marketCap = data.marketData.marketCap.getPrice(localStoreRepository.getLocalCurrency()),
                totalVolume = data.marketData.totalVolume.getPrice(localStoreRepository.getLocalCurrency()),
                circulatingSupply = data.marketData.circulatingSupply,
                maxSupply = data.marketData.maxSupply
            )
        }
    }
}