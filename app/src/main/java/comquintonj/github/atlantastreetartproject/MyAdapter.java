package comquintonj.github.atlantastreetartproject;

import android.content.Context;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private Context mContext;
    private HashMap<String, Pair<String, String>> pathAndDataMap;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView pictureOfArt;
        public TextView userSubmitted;

        public MyViewHolder(View view) {
            super(view);
            pictureOfArt = (ImageView) view.findViewById(R.id.artPicture);
            userSubmitted = (TextView) view.findViewById(R.id.user_submitted);
        }
    }

    public MyAdapter(Context mContext,
                     HashMap<String, Pair<String, String>> pathAndDataMap) {
        this.mContext = mContext;
        this.pathAndDataMap = pathAndDataMap;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        // Initiate Firebase storage reference to retrieve images
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        // Get a list of image paths from the key set
        ArrayList<String> imagePaths = new ArrayList<>(pathAndDataMap.keySet());

        // Grab the file paths and store them as StorageReferences
        ArrayList<StorageReference> images = new ArrayList<>();
        for (String pathName : imagePaths) {
            StorageReference pathReference = storageRef.child(pathName);
            images.add(pathReference);
        };

        // Retrieve images from references
        StorageReference pieceOfArt = images.get(position);
        Glide.with(mContext)
                .using(new FirebaseImageLoader())
                .load(pieceOfArt)
                .into(holder.pictureOfArt);

        // Using same position, set proper artist name
        String submitter = pathAndDataMap.get(imagePaths.get(position)).first;
        holder.userSubmitted.setText(submitter);

    }

    @Override
    public int getItemCount() {
        return pathAndDataMap.size();
    }
}