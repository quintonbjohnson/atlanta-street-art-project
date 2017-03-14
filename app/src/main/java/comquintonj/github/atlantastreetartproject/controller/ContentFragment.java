package comquintonj.github.atlantastreetartproject.controller;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import comquintonj.github.atlantastreetartproject.R;

/*
 * A class that represents the fragment used for each tab. It sets up the cards list.
 */
public class ContentFragment extends Fragment {

    public View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.content_explore, container, false);
        return rootView;
    }

    public void addRecyclerView(RecyclerView recyclerView) {
        ((ViewGroup)recyclerView.getParent()).removeView(recyclerView);
        LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.cardLayout);
        layout.addView(recyclerView);
    }

}