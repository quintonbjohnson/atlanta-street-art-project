package comquintonj.github.atlantastreetartproject;

import android.content.Context;
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


import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private Context mContext;
    private List<ArtInformation> artList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView nameOfArt;

        public MyViewHolder(View view) {
            super(view);
            nameOfArt = (TextView) view.findViewById(R.id.nameOfArt);
        }
    }


    public MyAdapter(Context mContext, List<ArtInformation> artList) {
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
        ArtInformation pieceOfArt = artList.get(position);
//        holder.nameOfArt.setText(artTitle);
    }

    @Override
    public int getItemCount() {
        return artList.size();
    }
}