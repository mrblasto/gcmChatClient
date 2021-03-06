package com.mrblasto.gcmchatclient.database;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import com.mrblasto.gcmchatclient.R;

/**
 * Created by jeffreyfried on 4/25/15.
 */
public class ChatContactCursorAdapter extends CursorAdapter {
    public ChatContactCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.chatcontact_listitem, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView tvName = (TextView) view.findViewById(R.id.text_view_vame);
        TextView tvCount = (TextView) view.findViewById(R.id.text_view_count);
        // Extract properties from cursor
        String body = cursor.getString(cursor.getColumnIndexOrThrow(DataProvider.COL_NAME));
        int count = cursor.getInt(cursor.getColumnIndexOrThrow(DataProvider.COL_COUNT));
        // Populate fields with extracted properties
        tvName.setText(body);
        tvCount.setText(String.valueOf(count));
    }
}
