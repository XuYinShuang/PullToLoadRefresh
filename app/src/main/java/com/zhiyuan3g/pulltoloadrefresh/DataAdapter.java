package com.zhiyuan3g.pulltoloadrefresh;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Administrator on 2016/7/28.
 */
public class DataAdapter extends BaseAdapter {

    private List<String>list;
    private Context context;

    public DataAdapter(List<String> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TextView textView=new TextView(context);
        textView.setText(list.get(position));
        textView.setTextSize(18);
        textView.setTextColor(Color.RED);
        return textView;
    }
}
