package com.hyapp.achat.view.component.sticker;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.hyapp.achat.R;
import com.hyapp.achat.view.component.emojiview.sticker.Sticker;
import com.hyapp.achat.view.component.emojiview.sticker.StickerCategory;

public class a18Stickers implements StickerCategory<Integer> {
    @NonNull
    @Override
    public Sticker<Integer>[] getStickers() {
        return new Sticker[]{
                new Sticker<>(R.drawable.sticker),
                new Sticker<>(R.drawable.sticker1),
                new Sticker<>(R.drawable.sticker2),
                new Sticker<>(R.drawable.sticker3),
                new Sticker<>(R.drawable.sticker4),
                new Sticker<>(R.drawable.sticker5),
                new Sticker<>(R.drawable.sticker6),
                new Sticker<>(R.drawable.sticker7),
                new Sticker<>(R.drawable.sticker8),
                new Sticker<>(R.drawable.sticker9),
                new Sticker<>(R.drawable.sticker10),
                new Sticker<>(R.drawable.sticker11),
                new Sticker<>(R.drawable.sticker12),
                new Sticker<>(R.drawable.sticker13),
                new Sticker<>(R.drawable.sticker14),
                new Sticker<>(R.drawable.sticker15)
        };
    }

    @Override
    public Integer getCategoryData() {
        return R.drawable.sticker;
    }

    @Override
    public boolean useCustomView() {
        return false;
    }

    @Override
    public View getView(ViewGroup viewGroup) {
        return null;
    }

    @Override
    public void bindView(View view) {}

    @Override
    public View getEmptyView(ViewGroup viewGroup) {
        return null;
    }
}