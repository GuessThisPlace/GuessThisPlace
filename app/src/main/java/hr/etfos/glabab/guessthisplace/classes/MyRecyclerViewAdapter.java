package hr.etfos.glabab.guessthisplace.classes;

import android.util.Log;
import android.view.ViewGroup;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

import hr.etfos.glabab.guessthisplace.R;
import hr.etfos.glabab.guessthisplace.activities.SingleGuessActivity;


public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.MyViewHolder> {

    List<ImageItem> data = Collections.emptyList();
    private LayoutInflater inflater;
    View.OnClickListener oNL;

    public MyRecyclerViewAdapter(Context context, List<ImageItem> data, View.OnClickListener oNL) {
        inflater = LayoutInflater.from(context);
        this.data = data;
        this.oNL = oNL;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.row_layout, viewGroup, false);
        MyViewHolder holder = new MyViewHolder(view, oNL);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder myViewHolder, int i) {
        final MyViewHolder tempViewHolder = myViewHolder;
        ImageItem current = data.get(i);
        final Uri uri = Uri.parse(current.imageUrlThumb);
        final Context context = myViewHolder.image.getContext();
        Picasso.with(context)
                .load(uri)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(myViewHolder.image, new Callback() {
                    @Override
                    public void onSuccess() {
                        myViewHolder.setONL();
                    }

                    @Override
                    public void onError() {
                        //Try again online if cache failed
                        Picasso.with(context)
                                .load(uri)
                                .error(R.drawable.error)
                                .into(tempViewHolder.image, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        myViewHolder.setONL();
                                    }

                                    @Override
                                    public void onError() {

                                    }
                                });
                    }
                });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        View item;
        View.OnClickListener onClickListen;
        public MyViewHolder(View itemView, View.OnClickListener oNL) {
            super(itemView);
            item = itemView;
            image = (ImageView) itemView.findViewById(R.id.imageView2);
            onClickListen = oNL;

        }

        public void setONL() {
            itemView.setOnClickListener(onClickListen);
        }
        public View.OnClickListener getONL()
        {
            return onClickListen;
        }

    }


}


