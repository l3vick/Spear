package adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.spear.android.R;

import java.util.List;

import fragments.album.AlbumFragment;
import fragments.album.AlbumView;
import objects.CardImage;

/**
 * Created by Pablo on 20/5/17.
 */

public class AlbumAdapter2  extends RecyclerView.Adapter<AlbumAdapter2.MyViewHolder> {

    private Context mContext;
    private List<CardImage> albumList;
    private AlbumFragment albumFragment;
    private AlbumView.OnImageClick onImageClick;




    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView image;
        public CardImage card;




        public MyViewHolder(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.image);
            image.setOnClickListener(this);


            //ratingBar.setOnTouchListener(this);

        }


        @Override
        public void onClick(View view) {
            if (view instanceof ImageView){
                Toast.makeText(mContext, ""+card.getUsername()+" "+ card.getUrlString(), Toast.LENGTH_SHORT).show();
                onImageClick.onSuccess(card);

            }

        }
    }


    public AlbumAdapter2(AlbumFragment albumFragment, Context mContext, List<CardImage> albumList, AlbumView.OnImageClick onImageClick) {
        this.albumFragment = albumFragment;
        this.mContext = mContext;
        this.albumList = albumList;
        this.onImageClick = onImageClick;
    }

    @Override
    public AlbumAdapter2.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.album_card2, parent, false);

        return new AlbumAdapter2.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        CardImage album = albumList.get(position);
        holder.card= album;
        Glide.with(mContext).load(album.getUrlString()).into(holder.image);
    }


    @Override
    public int getItemCount() {
        return albumList.size();
    }
}