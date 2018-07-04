package ru.russianmediagroup.rusrad.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import org.jetbrains.anko.find
import ru.russianmediagroup.rusrad.R
import ru.russianmediagroup.rusrad.network.models.Menu

/**
 * @author by Arthur Korchagin on 16.04.18
 */

typealias OnMenuClickListener = (Menu) -> Unit

class MoreMenuAdapter(private val items: List<Menu>, private val listener: OnMenuClickListener) : RecyclerView.Adapter<MoreMenuAdapter.MoreMenuHolder>() {

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: MoreMenuHolder, position: Int) = holder.bind(items[position])


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            MoreMenuHolder(LayoutInflater.from(parent.context).inflate(R.layout.li_more_menu, parent, false) as ViewGroup, listener)


    class MoreMenuHolder(item: ViewGroup, listener: OnMenuClickListener) : RecyclerView.ViewHolder(item) {

        private var menuItem: Menu? = null

        private val liMoreMenuRoot: ViewGroup = item.find(R.id.liMoreMenuRoot)
        private val liMoreMenuTv: TextView = item.find(R.id.liMoreMenuTv)
        private val liMoreMenuIv: ImageView = item.find(R.id.liMoreMenuIv)
        private val context = liMoreMenuRoot.context

        init {
            liMoreMenuRoot.setOnClickListener { menuItem?.let(listener) }
        }

        fun bind(menu: Menu) {
            menuItem = menu
            liMoreMenuTv.text = menu.name

            Glide.with(context)
                    .load(menu.icon.url)
                    .asBitmap()
                    .placeholder(R.drawable.bg_placeholder)
                    .animate(R.anim.fade_in)
                    .into(liMoreMenuIv)


        }
    }
}