package com.example.mylawyer.ads

import android.content.Context
import android.util.Log
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData

object BannerAds {
    fun initializeBanner(bannerAdView: BannerAdView, context: Context, adUnitId: String = "demo-banner-yandex") {
        // Установка adUnitId
        bannerAdView.setAdUnitId(adUnitId)
        // Установка размера sticky-баннера
        bannerAdView.setAdSize(BannerAdSize.stickySize(context, 320))

        // Создание запроса на рекламу
        val adRequest = AdRequest.Builder().build()
        bannerAdView.loadAd(adRequest)

        // Установка слушателя событий баннера
        bannerAdView.setBannerAdEventListener(object : BannerAdEventListener {
            override fun onAdClicked() {
            }

            override fun onAdFailedToLoad(error: AdRequestError) {
                Log.d("AdsLog", "Ошибка загрузки Yandex Ads: ${error.description}")
            }

            override fun onAdLoaded() {
            }

            override fun onImpression(impressionData: ImpressionData?) {
            }

            override fun onLeftApplication() {
            }

            override fun onReturnedToApplication() {
            }
        })
    }
}