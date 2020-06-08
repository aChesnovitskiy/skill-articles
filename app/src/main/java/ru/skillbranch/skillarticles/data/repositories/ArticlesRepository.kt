package ru.skillbranch.skillarticles.data.repositories

import androidx.lifecycle.LiveData
import ru.skillbranch.skillarticles.data.LocalDataHolder
import ru.skillbranch.skillarticles.data.models.ArticleItemData

object ArticlesRepository {
    fun loadArticles(): LiveData<List<ArticleItemData>?> = LocalDataHolder.findArticles()
}