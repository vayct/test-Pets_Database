package com.example.android.pets;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import com.example.android.pets.data.PetContract.PetEntry;


/**
 * {@link PetCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of pet data as its data source. This adapter knows
 * how to create list items for each row of pet data in the {@link Cursor}.
 */
public class PetCursorAdapter extends RecyclerView.Adapter<PetCursorAdapter.PetViewHolder> {

    private Cursor mCursor;
    private Context mContext;

    final private ListItemClickListener mOnClickListener;

    public interface ListItemClickListener{
        void onClickListener(int index);
    }

    public PetCursorAdapter(Context context, Cursor cursor, ListItemClickListener listener) {
        mCursor = cursor;
        mContext = context;
        mOnClickListener = listener;

    }





    @Override
    public PetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(layoutIdForListItem, parent, false);

        return new PetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PetViewHolder holder, int position) {

        if(!mCursor.moveToPosition(position))
            return;

        // Update the view holder with the information needed to display
        String name = mCursor.getString(mCursor.getColumnIndex(PetEntry.COLUMN_PET_NAME));
        String summary = mCursor.getString(mCursor.getColumnIndex(PetEntry.COLUMN_PET_BREED));


        long id = mCursor.getLong(mCursor.getColumnIndexOrThrow(PetEntry._ID));


        if (TextUtils.isEmpty(summary)) {
             summary = mContext.getString(R.string.unknown_breed);
        }

        // Display the guest name
        holder.nameTextView.setText(name);
        // Display the party count
        holder.summaryTextView.setText(summary);
        // TODO (7) Set the tag of the itemview in the holder to the id
        holder.itemView.setTag(id);

    }

    @Override
    public int getItemCount() {
        if(mCursor != null)
            return mCursor.getCount();
        return 0;
    }





    public void swapCursor(Cursor cursor) {
        if (mCursor != null) mCursor.close();
        mCursor = cursor;
        if (cursor != null) {
            // Force the RecyclerView to refresh
            this.notifyDataSetChanged();
        }
    }






    class PetViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        // Will display the guest name
        TextView nameTextView;
        // Will display the party size number
        TextView summaryTextView;


        public PetViewHolder(View itemView) {
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.name);
            summaryTextView = (TextView) itemView.findViewById(R.id.summary);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition =getAdapterPosition();
            mOnClickListener.onClickListener(clickedPosition);
        }
    }
}
