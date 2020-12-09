package com.example.ocrtest.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ocrtest.Model.IdiomModel;
import com.example.ocrtest.R;
import com.google.android.gms.vision.text.Line;

import java.util.ArrayList;
import java.util.List;

public class IdiomAdapter extends RecyclerView.Adapter<IdiomAdapter.MyViewHolder> implements SectionIndexer {
    List<IdiomModel> idiomModelList;
    private ArrayList<Integer> mSectionPositions;
    OnSuggestionClickListener onSuggestionClickListener;

    @Override
    public Object[] getSections() {
        List<String> sections = new ArrayList<>(26);
        mSectionPositions = new ArrayList<>(26);
        for (int i = 0, size = idiomModelList.size(); i < size; i++) {
            String section = String.valueOf(idiomModelList.get(i).getPribasa().charAt(0)).toUpperCase();
            if (!sections.contains(section)) {
                sections.add(section);
                mSectionPositions.add(i);
            }
        }
        return sections.toArray(new String[0]);
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        return mSectionPositions.get(sectionIndex);
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }



    public IdiomAdapter(List<IdiomModel> idiomModelList, OnSuggestionClickListener onSuggestionClickListener){
        this.idiomModelList = idiomModelList;
        this.onSuggestionClickListener = onSuggestionClickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_idiom,parent,false);
        return new MyViewHolder(view, onSuggestionClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final IdiomModel idiomModel = idiomModelList.get(position);
        holder.idiom.setText(idiomModel.getEjaan());
        holder.deskripsi.setText(idiomModel.getMakna());
    }

    @Override
    public int getItemCount() {
        return idiomModelList.size();
    }

    public void filterList(ArrayList<IdiomModel> filteredList) {
        idiomModelList = filteredList;
        notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView idiom, deskripsi;
        private OnSuggestionClickListener onSuggestionClickListener;
        public MyViewHolder(@NonNull View view, OnSuggestionClickListener onSuggestionClickListener) {
            super(view);
            idiom = view.findViewById(R.id.idiom);
            deskripsi = view.findViewById(R.id.deskripsi);
            this.onSuggestionClickListener = onSuggestionClickListener;
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onSuggestionClickListener.onSuggestionClick(getAdapterPosition());
        }
    }

    public interface OnSuggestionClickListener{
        void onSuggestionClick(int position);
    }
}
