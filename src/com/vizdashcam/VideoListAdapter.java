package com.vizdashcam;

import android.content.Context;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.Vector;

public class VideoListAdapter extends ArrayAdapter<VideoItem> {

    public static final String TAG = "VideoListFragmentAdapt";
    public static final int NORMAL_TYPE = 0;
    public static final int WITH_DATE_TYPE = 1;
    LayoutInflater inflater;
    Vector<VideoItem> directoryEntries = null;
    SparseBooleanArray selectedItemsIds;

    public VideoListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        inflater = LayoutInflater.from(getContext());
    }

    public VideoListAdapter(Context context, int resource,
                            Vector<VideoItem> items) {
        super(context, resource, items);
        inflater = LayoutInflater.from(getContext());
        directoryEntries = items;
        selectedItemsIds = new SparseBooleanArray();
    }

    @Override
    public int getItemViewType(int position) {
        VideoItem item = getItem(position);
        return item.getShowDate() ? WITH_DATE_TYPE : NORMAL_TYPE;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        int type = getItemViewType(position);
        VideoItem videoItem = getItem(position);

        if (convertView == null) {
            if (videoItem != null) {
                if (type == WITH_DATE_TYPE)
                    convertView = inflater.inflate(R.layout.row_item_withdate,
                            parent, false);
                else
                    convertView = inflater.inflate(R.layout.row_item, parent,
                            false);
            }
        }

        if (videoItem != null) {

            TextView tvTitle = (TextView) convertView
                    .findViewById(R.id.tv_title);
            TextView tvSize = (TextView) convertView.findViewById(R.id.tv_size);
            TextView tvLength = (TextView) convertView
                    .findViewById(R.id.tv_length);
            LinearLayout llShock = (LinearLayout) convertView
                    .findViewById(R.id.ll_shock);
            ImageView ivThumbnail = (ImageView) convertView
                    .findViewById(R.id.siv_tumbnail);

            if (tvTitle != null) {
                tvTitle.setText(videoItem.getName());
            }
            if (tvSize != null) {
                tvSize.setText(videoItem.getSize() + "MB");
            }
            if (tvLength != null) {
                tvLength.setText(videoItem.getDuration() + " min");
            }

            if (llShock != null && videoItem.isMarked()) {
                llShock.setVisibility(View.VISIBLE);
            } else {
                llShock.setVisibility(View.INVISIBLE);
            }

            if (ivThumbnail != null) {
                VideoPreview.getFileIcon(videoItem.getFile(), ivThumbnail);
            }

            if (type == WITH_DATE_TYPE) {
                TextView tvDate = (TextView) convertView
                        .findViewById(R.id.tv_date);

                DateTime date = videoItem.getDate();
                int year = date.getYear();
                int month = date.getMonthOfYear();
                int day = date.getDayOfMonth();

                tvDate.setText(month + " / " + day + " / " + year);
            }

        }

        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        groupEntriesByDate();
        super.notifyDataSetChanged();
    }

    private void groupEntriesByDate() {
        if (directoryEntries.size() > 0) {
            Collections.sort(directoryEntries,
                    VideoItem.VideoDateComparatorDesc);

            if (directoryEntries.size() > 1) {
                VideoItem firstVid = directoryEntries.get(0);
                VideoItem secondVid = directoryEntries.get(1);
                String firstVidName = firstVid.getName();
                String secondVidName = secondVid.getName();
                try {

                    if (VideoItem.isMarked(firstVidName)) {
                        firstVidName = firstVidName.substring(0,
                                firstVidName.lastIndexOf("_"));
                    } else {
                        firstVidName = firstVidName.substring(0,
                                firstVidName.lastIndexOf("."));
                    }

                    if (VideoItem.isMarked(secondVidName)) {
                        secondVidName = secondVidName.substring(0,
                                secondVidName.lastIndexOf("_"));
                    } else {
                        secondVidName = secondVidName.substring(0,
                                secondVidName.lastIndexOf("."));
                    }

                    if (firstVidName.equals(secondVidName)) {
                        if (firstVid.getSize() > secondVid.getSize())
                            directoryEntries.remove(1);
                        else
                            directoryEntries.remove(0);
                    }
                } catch (IndexOutOfBoundsException e) {
                    Log.e(TAG,
                            "IndexOutOfBoundsException when removing first duplicate: "
                                    + e.getMessage());
                }
            }

            directoryEntries.get(0).setShowDate(true);
            for (int i = 0; i < directoryEntries.size() - 1; i++) {
                VideoItem first = directoryEntries.get(i);
                VideoItem second = directoryEntries.get(i + 1);
                second.setShowDate(false);

                DateTime date1 = first.getDate();
                DateTime date2 = second.getDate();
                int month1 = date1.getMonthOfYear();
                int day1 = date1.getDayOfMonth();
                int year1 = date1.getYear();
                int month2 = date2.getMonthOfYear();
                int day2 = date2.getDayOfMonth();
                int year2 = date2.getYear();

                if (year1 > year2) {
                    second.setShowDate(true);

                } else if (year1 == year2 && month1 > month2) {
                    second.setShowDate(true);

                } else if (year1 == year2 && month1 == month2 && day1 > day2) {
                    second.setShowDate(true);
                }
            }
        }
    }

    @Override
    public void remove(VideoItem object) {
        super.remove(object);
        if (directoryEntries != null) {
            directoryEntries.remove(object);
            notifyDataSetChanged();
            object.getFile().delete();
        }
    }

    public void removeSelection() {
        selectedItemsIds.clear();
        notifyDataSetChanged();
    }

    public Vector<VideoItem> getDataset() {
        return directoryEntries;
    }

    public void toggleSelection(int position) {
        selectView(position, !selectedItemsIds.get(position));
    }

    public void selectView(int position, boolean value) {
        if (value)
            selectedItemsIds.put(position, value);
        else
            selectedItemsIds.delete(position);
        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return selectedItemsIds.size();
    }

    public SparseBooleanArray getSelectedIds() {
        return selectedItemsIds;
    }
}