package ru.skillbranch.skillarticles.data.repositories

import androidx.lifecycle.LiveData
import ru.skillbranch.skillarticles.data.*

object ArticleRepository {
    private val local = LocalDataHolder
    private val network = NetworkDataHolder

    fun loadArticleContent(articleId: String): LiveData<List<Any>?> =
        network.loadArticleContent(articleId) //5s delay from network

    fun getArticle(articleId: String): LiveData<ArticleData?> =
        local.findArticle(articleId) //2s delay from db

    fun loadArticlePersonalInfo(articleId: String): LiveData<ArticlePersonalInfo?> =
        local.findArticlePersonalInfo(articleId) //1s delay from db

    fun getAppSettings(): LiveData<AppSettings> = local.getAppSettings() //from preferences

    fun updateSettings(appSettings: AppSettings) {
        local.updateAppSettings(appSettings)
    }

    fun updateArticlePersonalInfo(info: ArticlePersonalInfo) {
        local.updateArticlePersonalInfo(info)
    }
}