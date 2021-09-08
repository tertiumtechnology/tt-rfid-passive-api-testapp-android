package com.tertiumtechnology.testapp.util.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tertiumtechnology.api.rfidpassiveapilib.Tag;
import com.tertiumtechnology.testapp.R;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

public class InventoryTagsListAdapter extends RecyclerView.Adapter<InventoryTagsListAdapter.TagViewHolder> {

    public interface OnTagClickListener {
        void onTagClick(Tag tag);
    }

    public class TagViewHolder extends RecyclerView.ViewHolder {
        private AppCompatTextView tagView;

        public TagViewHolder(View itemView) {
            super(itemView);
            tagView = itemView.findViewById(R.id.tag);
        }

        public void bind(final Tag tag, final OnTagClickListener tagClickListener, final int position) {
            this.tagView.setText(tag.toString());

            this.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    notifyItemChanged(tagSelected);
                    tagSelected = position;
                    notifyItemChanged(tagSelected);

                    tagClickListener.onTagClick(tag);
                }
            });
        }
    }

    private int tagSelected = -1;
    private List<Tag> tags;
    private OnTagClickListener tagClickListener;

    public InventoryTagsListAdapter(OnTagClickListener tagClickListener) {
        this.tagClickListener = tagClickListener;
        this.tags = new ArrayList<>();
    }

    public void addTag(Tag tag) {
        if (!tags.contains(tag)) {
            tags.add(tag);
            notifyItemInserted(tags.size() - 1);
        }
    }

    public void clear() {
        int size = tags.size();
        tags.clear();
        tagSelected = -1;
        notifyItemRangeRemoved(0, size);
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

    @Override
    public void onBindViewHolder(TagViewHolder holder, int position) {
        holder.bind(tags.get(position), tagClickListener, position);

        holder.itemView.setSelected(tagSelected == position);
    }

    @Override
    public TagViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View tagView = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_list_item, parent, false);

        return new TagViewHolder(tagView);
    }
}