package com.example.android.googlebook;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by cheah on 21/10/16.
 */

public class MyArrayAdapter extends ArrayAdapter<BookInfo> {
    private Context context;
    public MyArrayAdapter(Context context, int resource, ArrayList<BookInfo> objects) {
        super(context, resource, objects);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_layout, parent, false);;
        }

        final BookInfo info = this.getItem(position);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = info.getInfoLink();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                context.startActivity(i);
            }
        });

        TextView txtItemNo = (TextView)convertView.findViewById(R.id.txtItemNo);
        TextView txtTitle = (TextView)convertView.findViewById(R.id.txtTitle);
        TextView txtAuthors = (TextView)convertView.findViewById(R.id.txtAuthors);
        TextView txtDate = (TextView)convertView.findViewById(R.id.txtDate);

        txtItemNo.setText(position+1+")");
        txtTitle.setText(checktext(info.getTitle()));
        txtAuthors.setText(checktext(info.getAuthors()));
        txtDate.setText(checktext(info.getPublishedDate()));
        return convertView;
    }
    private String checktext(String str){
        if(!str.isEmpty()){
            return str;
        }
        return context.getString(R.string.unknown);
    }
}
