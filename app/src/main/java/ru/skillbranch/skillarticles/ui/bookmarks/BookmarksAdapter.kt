package ru.skillbranch.skillarticles.ui.bookmarks

import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.skillbranch.skillarticles.data.models.ArticleItemData
import ru.skillbranch.skillarticles.ui.custom.ArticleItemView

class BookmarksAdapter(
        private val listener: (ArticleItemData) -> Unit,
        private val bookmarkListener: (String, Boolean) -> Unit
) : PagedListAdapter<ArticleItemData, BookmarkVH>(BookmarkDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkVH {
        val view = ArticleItemView(parent.context)
        return BookmarkVH(view)
    }

    override fun onBindViewHolder(holder: BookmarkVH, position: Int) {
        holder.bind(
                getItem(position),
                listener,
                bookmarkListener
        )
    }
}

class BookmarkDiffCallback : DiffUtil.ItemCallback<ArticleItemData>() {
    override fun areItemsTheSame(oldItem: ArticleItemData, newItem: ArticleItemData): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: ArticleItemData, newItem: ArticleItemData): Boolean = oldItem == newItem
}

class BookmarkVH(private val containerView: View) : RecyclerView.ViewHolder(containerView) {
    fun bind(
            item: ArticleItemData?,
            listener: (ArticleItemData) -> Unit,
            bookmarkListener: (String, Boolean) -> Unit
    ) {
        // If use placeholder, item may be null
        (containerView as ArticleItemView).bind(item!!) { id, isBookmark ->
            bookmarkListener.invoke(id, isBookmark)
        }
        itemView.setOnClickListener { listener(item) }
    }
}