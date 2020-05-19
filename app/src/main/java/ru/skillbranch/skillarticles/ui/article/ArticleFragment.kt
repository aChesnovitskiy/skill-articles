package ru.skillbranch.skillarticles.ui.article

import android.os.Bundle
import android.view.WindowManager
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions.circleCropTransform
import kotlinx.android.synthetic.main.fragment_article.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.ui.IArticleView
import ru.skillbranch.skillarticles.ui.base.BaseFragment
import ru.skillbranch.skillarticles.ui.base.Binding
import ru.skillbranch.skillarticles.ui.base.*
import ru.skillbranch.skillarticles.ui.base.ToolbarBuilder
import ru.skillbranch.skillarticles.viewmodels.ArticleViewModel
import ru.skillbranch.skillarticles.viewmodels.base.ViewModelFactory

class ArticleFragment : BaseFragment<ArticleViewModel>(), IArticleView {
    private val args: ArticleFragmentArgs by navArgs()

    override val viewModel: ArticleViewModel by viewModels {
        ViewModelFactory(owner = this, params = args.articleId)
    }

    override val layout: Int = R.layout.fragment_article
    override val binding: Binding: ArticleBinding by lazy
    { ArticleBinding() }

    override val prepareToolbar: (ToolbarBuilder.() -> Unit)? = {
        this.setTitle(args.title)
                .setSubtitle(args.category)
                .setLogo(args.categoryIcon)
                .addMenuItem(
                        MenuItemHolder(
                                "search",
                                R.id.action_search,
                                R.drawable.ic_search_24dp,
                                R.layout.search_view_layout
                        )
                )
    }

    override val prepareBottombar: (BottombarBuilder.() -> Unit)? = {
        this.addView(R.layout.layout_submenu)
                .addView(R.layout.layout_bottombar)
                .setVisibility(false)
    }

    private val bottombar
        get() = root.bottombar
    private val submenu
        get() = root.submenu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun setupViews() {
        // Window resize options
        root.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        setupBottombar()
        setupSubmenu()

        // Init views
        val avatarSize = root.dpToIntPx(40)
        val cornerRadius = root.dpToIntPx(8)

        Glide.with(root)
                .load(args.authorAvatar)
                .apply(circleCropTransform())
                .override(avatarSize)
                .into(iv_author_avatar)
    }
}