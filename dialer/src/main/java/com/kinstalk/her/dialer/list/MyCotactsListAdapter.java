package com.kinstalk.her.dialer.list;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.kinstalk.her.cmccmode.data.ContactInfo;
import com.kinstalk.her.dialer.R;

import java.util.List;

public class MyCotactsListAdapter extends ArrayAdapter<ContactInfo> {
    private static final String TAG = "MyCotactsListAdapter";
    /**
     * 字母表分组工具
     */
    private SectionIndexer mIndexer;

    public MyCotactsListAdapter(@NonNull Context context, @NonNull List<ContactInfo> objects) {
        super(context, R.layout.my_contact_item, objects);
        Log.i(TAG, "MyCotactsListAdapter: list.size:" + objects.size());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.i(TAG, "getView: positon:" + position);
        ContactInfo contact = getItem(position);
        MyViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.my_contact_item, null);
            // 初始化 ViewHolder 方便重用
            Log.i(TAG, "getView: convertview is null");
            viewHolder = new MyViewHolder();
            viewHolder.nickName = (TextView) convertView.findViewById(R.id.name);
            viewHolder.phoneNumber = (TextView) convertView.findViewById(R.id.phone_number);
            viewHolder.sortKey = (TextView) convertView.findViewById(R.id.sort_key);
            convertView.setTag(viewHolder);
        } else {
            Log.i(TAG, "getView: convertview is not null");
            viewHolder = (MyViewHolder) convertView.getTag();
        }
        Log.i(TAG, "getView: contact:" + contact);
        viewHolder.nickName.setText(contact.getNickname());
        viewHolder.phoneNumber.setText(contact.getContactId());
        int section = mIndexer.getSectionForPosition(position);
        if (position == mIndexer.getPositionForSection(section)) {
            viewHolder.sortKey.setText(getSortKey(contact.getSortKey()));
            viewHolder.sortKey.setVisibility(View.VISIBLE);
        } else {
            viewHolder.sortKey.setVisibility(View.INVISIBLE);
        }
        return convertView;

    }


    /**
     * 获取sort key的首个字符，如果是英文字母就直接返回，否则返回#。
     *
     * @param sortKeyString 数据库中读取出的sort key
     * @return 英文字母或者#
     */
    private String getSortKey(String sortKeyString) {
        String key = sortKeyString.substring(0, 1).toUpperCase();
        if (key.matches("[A-Z]")) {
            return key;
        }
        return "#";
    }


    /**
     * 给当前适配器传入一个分组工具。
     *
     * @param indexer
     */
    public void setIndexer(SectionIndexer indexer) {
        mIndexer = indexer;
    }

    public class MyViewHolder {
        TextView nickName;
        TextView phoneNumber;
        TextView sortKey;
    }
}
