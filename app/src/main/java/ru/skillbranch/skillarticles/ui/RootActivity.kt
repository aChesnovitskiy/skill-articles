package ru.skillbranch.skillarticles.ui

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_root.*
import kotlinx.android.synthetic.main.layout_bottombar.*
import kotlinx.android.synthetic.main.layout_submenu.*
import kotlinx.android.synthetic.main.search_view_layout.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.extensions.dpToPx
import ru.skillbranch.skillarticles.extensions.setMarginOptionally
import ru.skillbranch.skillarticles.ui.base.BaseActivity
import ru.skillbranch.skillarticles.ui.custom.SearchSpan
import ru.skillbranch.skillarticles.viewmodels.ArticleState
import ru.skillbranch.skillarticles.viewmodels.ArticleViewModel
import ru.skillbranch.skillarticles.viewmodels.base.Notify
import ru.skillbranch.skillarticles.viewmodels.base.ViewModelFactory

class RootActivity : BaseActivity<ArticleViewModel>(), IArticleView {
    override val layout: Int = R.layout.activity_root
    override lateinit var viewModel: ArticleViewModel
    private var isSearchMode: Boolean = false
    private var searchQuery: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupViewModel()
    }

    override fun setupViews() {
        setupToolbar()
        setupBottombar()
        setupSubmenu()
    }

    private fun setupViewModel() {
        val vmFactory =
            ViewModelFactory("0")
        viewModel = ViewModelProvider(this, vmFactory).get(ArticleViewModel::class.java)

        // Subscribe on article state
        viewModel.observeState(this) {
            renderUI(it)
//            setupToolbar() TODO delete if this is will not be need in further. It cause error while search (maybe add check of search mode to logo set up)
        }

        // Subscribe on notifications
        viewModel.observeNotifications(this) {
            renderNotifications(it)
        }
    }

    override fun renderSearchResult(searchResult: List<Pair<Int, Int>>) {
        val content = tv_text_content.text as Spannable
        val bgColor = Color.RED // TODO
        val fgColor = Color.WHITE // TODO

        searchResult.forEach{(start, end) ->
            content.setSpan(
                SearchSpan(bgColor, fgColor),
                start,
                end,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    override fun renderSearchPosition(searchPosition: Int) {
        TODO("Not yet implemented")
    }

    override fun clearSearchResult() {
        TODO("Not yet implemented")
    }

    override fun showSearchBar() {
        bottombar.setSearchState(true)
        scrollView.setMarginOptionally(bottom = dpToIntPx(56))
    }

    override fun hideSearchBar() {
        bottombar.setSearchState(false)
        scrollView.setMarginOptionally(bottom = dpToIntPx(0))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)

        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView

        if (isSearchMode) {
            searchItem.expandActionView()
            with (searchView) {
                setQuery(searchQuery ?: "", true)
                clearFocus()
            }
        }

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                Log.d("My_RootActivity", "Search is open")
                viewModel.handleSearchMode(true)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                Log.d("My_RootActivity", "Search is close")
                viewModel.handleSearchMode(false)
                invalidateOptionsMenu()
                return true
            }
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.handleSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.handleSearch(newText)
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Set up logo
        val logo = if (toolbar.childCount > 2) toolbar.getChildAt(2) as ImageView else null
        logo?.scaleType = ImageView.ScaleType.CENTER_CROP
        // Check toolbar imports
        (logo?.layoutParams as? Toolbar.LayoutParams)?.let {
            it.width = dpToIntPx(40)
            it.height = dpToIntPx(40)
            it.marginEnd = dpToIntPx(16)
            logo.layoutParams = it
        }
    }

    private fun setupBottombar() {
        btn_like.setOnClickListener { viewModel.handleLike() }
        btn_bookmark.setOnClickListener { viewModel.handleBookmark() }
        btn_share.setOnClickListener { viewModel.handleShare() }
        btn_settings.setOnClickListener { viewModel.handleToggleMenu() }

        btn_result_up.setOnClickListener {
            if (search_view.hasFocus()) search_view.clearFocus()
            viewModel.handleUpResult()
        }

        btn_result_down.setOnClickListener {
            if (search_view.hasFocus()) search_view.clearFocus()
            viewModel.handleDownResult()
        }

        btn_search_close.setOnClickListener{
            viewModel.handleSearchMode(false)
            invalidateOptionsMenu()
        }
    }

    private fun setupSubmenu() {
        btn_text_up.setOnClickListener { viewModel.handleUpText() }
        btn_text_down.setOnClickListener { viewModel.handleDownText() }
        switch_mode.setOnClickListener { viewModel.handleNightMode() }
    }

    private fun renderUI(data: ArticleState) {
        if (data.isSearch) showSearchBar() else hideSearchBar()

        if(data.searchResults.isNotEmpty()) renderSearchResult(data.searchResults)

        // Bind personal article data
        btn_like.isChecked = data.isLike
        btn_bookmark.isChecked = data.isBookmark

        // Bind submenu state
        btn_settings.isChecked = data.isShowMenu
        if (data.isShowMenu) submenu.open() else submenu.close()

        // Bind submenu views
        switch_mode.isChecked = data.isDarkMode
        delegate.localNightMode =
            if (data.isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        if (data.isBigText) {
            tv_text_content.textSize = 18f
            btn_text_up.isChecked = true
            btn_text_down.isChecked = false
        } else {
            tv_text_content.textSize = 14f
            btn_text_up.isChecked = false
            btn_text_down.isChecked = true
        }

        // Bind content
        tv_text_content.text =
            if (data.isLoadingContent) "Loading..." else data.content.first() as String

        // Bind toolbar
        toolbar.title = data.title ?: "Skill Articles"
        toolbar.subtitle = data.category ?: "Loading..."
        if (data.categoryIcon != null) toolbar.logo = getDrawable(data.categoryIcon as Int)
    }

    override fun renderNotifications(notify: Notify) {
        val snackbar = Snackbar.make(coordinator_container, notify.message, Snackbar.LENGTH_LONG)
            .apply { anchorView = bottombar }

        when (notify) {
            is Notify.TextMessage -> { /* Nothing */
            }
            is Notify.ActionMessage -> {
                snackbar.setActionTextColor(getColor(R.color.color_accent_dark))
                snackbar.setAction(notify.actionLabel) {
                    notify.actionHandler.invoke()
                }
            }
            is Notify.ErrorMessage -> {
                with(snackbar) {
                    setBackgroundTint(getColor(R.color.design_default_color_error))
                    setTextColor(getColor(android.R.color.white))
                    setActionTextColor(getColor(android.R.color.white))
                    setAction(notify.errLabel) {
                        notify.errHandler?.invoke()
                    }
                }
            }
        }

        snackbar.show()
    }
}
