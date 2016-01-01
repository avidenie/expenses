package ro.expectations.expenses.ui.categories;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ro.expectations.expenses.R;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder> {

    private Cursor mCursor;
    final private Context mContext;
    final private View mEmptyView;
    final private OnClickListener mClickListener;

    public CategoriesAdapter(Context context, OnClickListener clickListener, View emptyView) {
        mContext = context;
        mClickListener = clickListener;
        mEmptyView = emptyView;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item_categories, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        // Set the icon background color.
        GradientDrawable bgShape = (GradientDrawable) holder.mIconBackgroundView.getBackground();
        bgShape.setColor(0xFF000000 | ContextCompat.getColor(mContext, R.color.primary));

        // Set the category name.
        String name = mCursor.getString(CategoriesFragment.COLUMN_CATEGORY_NAME);
        holder.mNameView.setText(name);

        // Show or hide the subcategories icon.
        long parentId = mCursor.getLong(CategoriesFragment.COLUMN_CATEGORY_PARENT_ID);
        if (parentId > 0 ) {
            holder.mSubcategoriesIcon.setVisibility(View.GONE);
        } else {
            long children = mCursor.getLong(CategoriesFragment.COLUMN_CATEGORY_CHILDREN);
            if (children > 0) {
                holder.mSubcategoriesIcon.setVisibility(View.VISIBLE);
            } else {
                holder.mSubcategoriesIcon.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final RelativeLayout mIconBackgroundView;
        public final ImageView mIconView;
        public final TextView mNameView;
        public final ImageView mSubcategoriesIcon;

        public ViewHolder(View view) {
            super(view);
            mIconBackgroundView = (RelativeLayout) view.findViewById(R.id.category_icon_background);
            mIconView = (ImageView) view.findViewById(R.id.category_icon);
            mNameView = (TextView) view.findViewById(R.id.category_name);
            mSubcategoriesIcon = (ImageView) view.findViewById(R.id.subcategories_icon);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            mClickListener.onClick(mCursor.getLong(CategoriesFragment.COLUMN_CATEGORY_ID), this);
        }
    }

    public interface OnClickListener {
        void onClick(long categoryId, ViewHolder vh);
    }
}
