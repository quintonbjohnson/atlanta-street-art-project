package comquintonj.github.atlantastreetartproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private Context mContext;
    private List<StorageReference> artList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView pictureOfArt;

        public MyViewHolder(View view) {
            super(view);
            pictureOfArt = (ImageView) view.findViewById(R.id.artPicture);
        }
    }


    public MyAdapter(Context mContext, List<StorageReference> artList) {
        this.mContext = mContext;
        this.artList = artList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        StorageReference pieceOfArt = artList.get(position);
        Glide.with(mContext)
                .using(new FirebaseImageLoader())
                .load(pieceOfArt)
                .into(holder.pictureOfArt);
    }

    @Override
    public int getItemCount() {
        return artList.size();
    }
}