package org.wordpress.android.ui.stats.refresh.sections

import android.support.v7.widget.RecyclerView.Adapter
import android.view.ViewGroup
import org.wordpress.android.ui.stats.refresh.sections.viewholders.BlockItemViewHolder.BarChartViewHolder
import org.wordpress.android.ui.stats.refresh.sections.viewholders.BlockItemViewHolder.ColumnsViewHolder
import org.wordpress.android.ui.stats.refresh.sections.viewholders.BlockItemViewHolder.EmptyViewHolder
import org.wordpress.android.ui.stats.refresh.sections.viewholders.BlockItemViewHolder.ExpandableItemViewHolder
import org.wordpress.android.ui.stats.refresh.sections.viewholders.BlockItemViewHolder.InformationViewHolder
import org.wordpress.android.ui.stats.refresh.sections.viewholders.BlockItemViewHolder.ItemViewHolder
import org.wordpress.android.ui.stats.refresh.sections.viewholders.BlockItemViewHolder.LabelViewHolder
import org.wordpress.android.ui.stats.refresh.sections.viewholders.BlockItemViewHolder.LinkViewHolder
import org.wordpress.android.ui.stats.refresh.sections.viewholders.BlockItemViewHolder.ListItemViewHolder
import org.wordpress.android.ui.stats.refresh.sections.viewholders.BlockItemViewHolder.TabsViewHolder
import org.wordpress.android.ui.stats.refresh.sections.viewholders.BlockItemViewHolder.TextViewHolder
import org.wordpress.android.ui.stats.refresh.sections.viewholders.BlockItemViewHolder.TitleViewHolder
import org.wordpress.android.ui.stats.refresh.sections.viewholders.BlockItemViewHolder.UserItemViewHolder
import org.wordpress.android.ui.stats.refresh.sections.BlockListItem.BarChartItem
import org.wordpress.android.ui.stats.refresh.sections.BlockListItem.Columns
import org.wordpress.android.ui.stats.refresh.sections.BlockListItem.ExpandableItem
import org.wordpress.android.ui.stats.refresh.sections.BlockListItem.Information
import org.wordpress.android.ui.stats.refresh.sections.BlockListItem.Item
import org.wordpress.android.ui.stats.refresh.sections.BlockListItem.Label
import org.wordpress.android.ui.stats.refresh.sections.BlockListItem.Link
import org.wordpress.android.ui.stats.refresh.sections.BlockListItem.ListItem
import org.wordpress.android.ui.stats.refresh.sections.BlockListItem.TabsItem
import org.wordpress.android.ui.stats.refresh.sections.BlockListItem.Text
import org.wordpress.android.ui.stats.refresh.sections.BlockListItem.Title
import org.wordpress.android.ui.stats.refresh.sections.BlockListItem.Type.BAR_CHART
import org.wordpress.android.ui.stats.refresh.sections.BlockListItem.Type.COLUMNS
import org.wordpress.android.ui.stats.refresh.sections.BlockListItem.Type.EMPTY
import org.wordpress.android.ui.stats.refresh.sections.BlockListItem.Type.EXPANDABLE_ITEM
import org.wordpress.android.ui.stats.refresh.sections.BlockListItem.Type.INFO
import org.wordpress.android.ui.stats.refresh.sections.BlockListItem.Type.ITEM
import org.wordpress.android.ui.stats.refresh.sections.BlockListItem.Type.LABEL
import org.wordpress.android.ui.stats.refresh.sections.BlockListItem.Type.LINK
import org.wordpress.android.ui.stats.refresh.sections.BlockListItem.Type.LIST_ITEM
import org.wordpress.android.ui.stats.refresh.sections.BlockListItem.Type.TABS
import org.wordpress.android.ui.stats.refresh.sections.BlockListItem.Type.TEXT
import org.wordpress.android.ui.stats.refresh.sections.BlockListItem.Type.TITLE
import org.wordpress.android.ui.stats.refresh.sections.BlockListItem.Type.USER_ITEM
import org.wordpress.android.ui.stats.refresh.sections.BlockListItem.Type.values
import org.wordpress.android.ui.stats.refresh.sections.BlockListItem.UserItem
import org.wordpress.android.ui.stats.refresh.sections.viewholders.BlockItemViewHolder
import org.wordpress.android.util.image.ImageManager

class BlockListAdapter(val imageManager: ImageManager) : Adapter<BlockItemViewHolder>() {
    private var items: List<BlockListItem> = listOf()
    fun update(newItems: List<BlockListItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockItemViewHolder {
        return when (values()[viewType]) {
            TITLE -> TitleViewHolder(parent)
            ITEM -> ItemViewHolder(parent, imageManager)
            USER_ITEM -> UserItemViewHolder(parent, imageManager)
            LIST_ITEM -> ListItemViewHolder(parent)
            EMPTY -> EmptyViewHolder(parent)
            TEXT -> TextViewHolder(parent)
            COLUMNS -> ColumnsViewHolder(parent)
            LINK -> LinkViewHolder(parent)
            BAR_CHART -> BarChartViewHolder(parent)
            TABS -> TabsViewHolder(parent, imageManager)
            INFO -> InformationViewHolder(parent)
            LABEL -> LabelViewHolder(parent)
            EXPANDABLE_ITEM -> ExpandableItemViewHolder(parent, imageManager)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].type.ordinal
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: BlockItemViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is TitleViewHolder -> holder.bind(item as Title)
            is ItemViewHolder -> holder.bind(item as Item)
            is UserItemViewHolder -> holder.bind(item as UserItem)
            is ListItemViewHolder -> holder.bind(item as ListItem)
            is TextViewHolder -> holder.bind(item as Text)
            is ColumnsViewHolder -> holder.bind(item as Columns)
            is LinkViewHolder -> holder.bind(item as Link)
            is BarChartViewHolder -> holder.bind(item as BarChartItem)
            is TabsViewHolder -> holder.bind(item as TabsItem)
            is InformationViewHolder -> holder.bind(item as Information)
            is LabelViewHolder -> holder.bind(item as Label)
            is ExpandableItemViewHolder -> holder.bind(item as ExpandableItem)
        }
    }
}
