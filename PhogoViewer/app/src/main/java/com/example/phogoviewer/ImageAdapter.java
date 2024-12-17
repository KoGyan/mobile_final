package com.example.phogoviewer;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private List<Bitmap> imageList;
    private OnItemClickListener listener;

    // 클릭 이벤트 인터페이스
    public interface OnItemClickListener {
        void onItemClick(Bitmap bitmap);
    }

    public ImageAdapter(List<Bitmap> imageList, OnItemClickListener listener) {
        this.imageList = imageList;
        this.listener = listener;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        Bitmap bitmap = imageList.get(position);
        holder.imageView.setImageBitmap(bitmap);

        // 이미지 클릭 이벤트 전달
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(bitmap);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewItem);
        }
    }
}
