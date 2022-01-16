package com.hyapp.achat.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hyapp.achat.R
import com.hyapp.achat.databinding.ItemPeopleBinding
import com.hyapp.achat.model.entity.Contact
import com.hyapp.achat.model.entity.User
import com.hyapp.achat.model.entity.UserConsts
import com.hyapp.achat.model.entity.utils.PersonUtils
import com.hyapp.achat.view.ChatActivity.Companion.start

class UsersAdapter(private val context: Context) : ListAdapter<User, UsersAdapter.Holder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<User> = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(
                    oldItem: User, newItem: User): Boolean {
                return oldItem.uid == newItem.uid
            }

            override fun areContentsTheSame(
                    oldItem: User, newItem: User): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun submitList(list: MutableList<User>?) {
        if (list != null)
            super.submitList(ArrayList(list))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding: ItemPeopleBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.item_people, parent, false)
        binding.lifecycleOwner = context as LifecycleOwner
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class Holder(private val binding: ItemPeopleBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        fun bind(user: User?) {
            binding.people = user
            binding.executePendingBindings()
            val avatars: List<String> = user!!.avatars
            binding.avatar.setImageURI(if (avatars.isNotEmpty()) avatars[0] else null)
            when (user.gender) {
                UserConsts.GENDER_MALE -> binding.genderCircle.setBackgroundResource(R.drawable.gender_circle_people_male_bg)
                UserConsts.GENDER_FEMALE -> binding.genderCircle.setBackgroundResource(R.drawable.gender_circle_people_female_bg)
            }
            val pair = PersonUtils.rankInt2rankStrResAndColor(user.rank)
            binding.rank.setText(pair.first)
            binding.rank.setTextColor(pair.second)
        }

        override fun onClick(v: View) {
            val user = getItem(adapterPosition)
            start(context, Contact(user!!))
        }

        init {
            itemView.setOnClickListener(this)
        }
    }
}