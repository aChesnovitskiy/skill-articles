package ru.skillbranch.skillarticles.viewmodels.article

import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.skillbranch.skillarticles.data.models.ArticleData
import ru.skillbranch.skillarticles.data.models.ArticlePersonalInfo
import ru.skillbranch.skillarticles.data.models.CommentItemData
import ru.skillbranch.skillarticles.data.repositories.ArticleRepository
import ru.skillbranch.skillarticles.data.repositories.CommentsDataFactory
import ru.skillbranch.skillarticles.data.repositories.MarkdownElement
import ru.skillbranch.skillarticles.extensions.data.toAppSettings
import ru.skillbranch.skillarticles.extensions.data.toArticlePersonalInfo
import ru.skillbranch.skillarticles.extensions.format
import ru.skillbranch.skillarticles.extensions.indexesOf
import ru.skillbranch.skillarticles.data.repositories.clearContent
import ru.skillbranch.skillarticles.viewmodels.base.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.NavigationCommand
import ru.skillbranch.skillarticles.viewmodels.base.Notify
import java.util.concurrent.Executors

class ArticleViewModel(
        handle: SavedStateHandle,
        private val articleId: String
) : BaseViewModel<ArticleState>(
        handle,
        ArticleState()
), IArticleViewModel {
    private val repository = ArticleRepository
    private var clearContent: String? = null

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    private val listConfig by lazy {
        PagedList.Config.Builder()
                .setEnablePlaceholders(true)
                .setPageSize(5)
                .build()
    }

    private val listData: LiveData<PagedList<CommentItemData>> = Transformations.switchMap(getArticleData()) {
        buildPagedList(repository.allComments(articleId, it?.commentCount ?: 0))
    }

    init {
        // Subscribe on mutable data (article data)
        subscribeOnDataSource(getArticleData()) { article, state ->
            article ?: return@subscribeOnDataSource null
            state.copy(
                    shareLink = article.shareLink,
                    title = article.title,
                    author = article.author,
                    category = article.category,
                    categoryIcon = article.categoryIcon,
                    date = article.date.format()
            )
        }

        // Subscribe on mutable data (article content)
        subscribeOnDataSource(getArticleContent()) { content, state ->
            content ?: return@subscribeOnDataSource null
            state.copy(
                    isLoadingContent = false,
                    content = content
            )
        }

        // Subscribe on mutable data (personal article info)
        subscribeOnDataSource(getArticlePersonalInfo()) { info, state ->
            info ?: return@subscribeOnDataSource null
            state.copy(
                    isBookmark = info.isBookmark,
                    isLike = info.isLike
            )
        }

        // Subscribe on mutable data (app settings)
        subscribeOnDataSource(repository.getAppSettings()) { settings, state ->
            state.copy(
                    isDarkMode = settings.isDarkMode,
                    isBigText = settings.isBigText
            )
        }

        subscribeOnDataSource(repository.isAuth()) { auth, state ->
            state.copy(isAuth = auth)
        }
    }

    // Load article text from network
    override fun getArticleContent(): LiveData<List<MarkdownElement>?> =
            repository.loadArticleContent(articleId)

    // Load data from db
    override fun getArticleData(): LiveData<ArticleData?> = repository.getArticle(articleId)

    // Load data from db
    override fun getArticlePersonalInfo(): LiveData<ArticlePersonalInfo?> =
            repository.loadArticlePersonalInfo(articleId)

    // App settings
    override fun handleNightMode() {
        val settings = currentState.toAppSettings()
        repository.updateSettings(settings.copy(isDarkMode = !settings.isDarkMode))
    }

    override fun handleUpText() {
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = true))
    }

    override fun handleDownText() {
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = false))
    }

    // Personal article info
    override fun handleBookmark() {
        val info = currentState.toArticlePersonalInfo()
        repository.updateArticlePersonalInfo(info.copy(isBookmark = !info.isBookmark))

        val msg = if (currentState.isBookmark) "Add to bookmarks" else "Remove from bookmarks"
        notify(Notify.TextMessage(msg))
    }

    override fun handleLike() {
        val isLiked = currentState.isLike
        val toggleLike = {
            val info = currentState.toArticlePersonalInfo()
            repository.updateArticlePersonalInfo(info.copy(isLike = !info.isLike))
        }

        toggleLike()

        val msg = if (!isLiked) {
            Notify.TextMessage("Mark is liked")
        } else {
            Notify.ActionMessage("Don`t like it anymore", "No, still like it", toggleLike)
        }

        notify(msg)
    }

    // Not implemented yet
    override fun handleShare() {
        notify(Notify.ErrorMessage("Share is not implemented", "OK", null))
    }

    // Session state
    override fun handleToggleMenu() {
        updateState { it.copy(isShowMenu = !it.isShowMenu) }
    }

    override fun handleSearchMode(isSearch: Boolean) {
        updateState { it.copy(isSearch = isSearch, isShowMenu = false, searchPosition = 0) }
    }

    override fun handleSearch(query: String?) {
        query ?: return
        if (clearContent == null && currentState.content.isNotEmpty()) clearContent =
                currentState.content.clearContent()

        val result = clearContent
                .indexesOf(query)
                .map { it to it + query.length }

        updateState { it.copy(searchQuery = query, searchResults = result, searchPosition = 0) }
    }

    // FIXME buttons don't work
    override fun handleUpResult() {
        updateState { it.copy(searchPosition = it.searchPosition.dec()) }
    }

    override fun handleDownResult() {
        updateState { it.copy(searchPosition = it.searchPosition.inc()) }
    }

    override fun handleCopyCode() {
        notify(Notify.TextMessage("Code was copied to clipboard"))
    }

    override fun handleSendComment(comment: String) {
        updateState { it.copy(commentInitial = comment) }
        saveState()
        Log.e("ArticleViewModel", "Comment send: ${currentState.commentInitial}")
        if (!currentState.isAuth) navigate(NavigationCommand.StartLogin())
        else {
            viewModelScope.launch {
                repository.sendComment(articleId, comment, currentState.answerToSlug)
                withContext(Dispatchers.Main) {
                    updateState { it.copy(answerTo = null, answerToSlug = null) }
                }
            }
        }
    }

    fun observeList(
            owner: LifecycleOwner,
            onChange: (list: PagedList<CommentItemData>) -> Unit
    ) {
        listData.observe(owner, Observer { onChange(it) })
    }

    private fun buildPagedList(dataFactory: CommentsDataFactory): LiveData<PagedList<CommentItemData>> {
        return LivePagedListBuilder<String, CommentItemData>(dataFactory, listConfig)
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .build()
    }

    fun handleCommentFocus(hasFocus: Boolean) {
        updateState { it.copy(isShowBottomBar = !hasFocus) }
    }

    fun handleClearComment() {
        updateState { it.copy(commentInitial = null, answerTo = null, answerToSlug = null) }
    }

    fun handleReplyTo(slug: String, name: String) {
        updateState { it.copy(answerTo = "Reply to $name", answerToSlug = slug) }
    }

    // TODO delete
//    fun handleChangeComment(comment: String) {
//        updateState { it.copy(commentInitial = comment) }
//        Log.e("ArticleViewModel", "Comment: $comment")
//    }
}

data class ArticleState(
        val isAuth: Boolean = false,
        val isLoadingContent: Boolean = true,
        val isLoadingReviews: Boolean = true,
        val isLike: Boolean = false,
        val isBookmark: Boolean = false,
        val isShowMenu: Boolean = false,
        val isBigText: Boolean = false,
        val isDarkMode: Boolean = false,
        val isSearch: Boolean = false,
        val searchQuery: String? = null,
        val searchResults: List<Pair<Int, Int>> = emptyList(),
        val searchPosition: Int = 0,
        val shareLink: String? = null,
        val title: String? = null,
        val category: String? = null,
        val categoryIcon: Any? = null,
        val date: String? = null,
        val author: Any? = null,
        val poster: String? = null,
        val content: List<MarkdownElement> = emptyList(),
        val commentInitial: String? = null,
        val commentsCount: Int = 0,
        val answerTo: String? = null,
        val answerToSlug: String? = null,
        val isShowBottomBar: Boolean = true
) : IViewModelState {
    override fun save(outState: SavedStateHandle) {
        // TODO save state
        outState.set("commentInitial", commentInitial)
        outState.set("isSearch", isSearch)
        outState.set("searchQuery", searchQuery)
        outState.set("searchResults", searchResults)
        outState.set("searchPosition", searchPosition)
        Log.e("ArticleViewModel", "Saved comment: $commentInitial")
    }

    override fun restore(savedState: SavedStateHandle): ArticleState {
        // TODO restore state
        return copy(
                commentInitial = savedState["commentInitial"],
                isSearch = savedState["isSearch"] ?: false,
                searchQuery = savedState["searchQuery"],
                searchResults = savedState["searchResults"] ?: emptyList(),
                searchPosition = savedState["searchPosition"] ?: 0
        ).also {
            Log.e("ArticleViewModel", "Restored comment: ${it.commentInitial}")
        }
    }
}

