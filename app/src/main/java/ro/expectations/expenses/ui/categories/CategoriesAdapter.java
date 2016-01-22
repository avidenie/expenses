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
import ro.expectations.expenses.helper.ListHelper;
import ro.expectations.expenses.widget.recyclerview.MultipleSelectionAdapter;

public class CategoriesAdapter extends MultipleSelectionAdapter<CategoriesAdapter.ViewHolder> {

    private Cursor mCursor;
    final private Context mContext;

    public CategoriesAdapter(Context context) {
        mContext = context;
        setHasStableIds(true);
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

        // Set the row background
        ListHelper.setItemBackground(mContext, holder.itemView, isItemSelected(position),
                holder.mCategoryIconBackground, holder.mSelectedIconBackground);

        // Set the icon background color
        GradientDrawable bgShape = (GradientDrawable) holder.mCategoryIconBackground.getBackground();
        bgShape.setColor(0xFF000000 | ContextCompat.getColor(mContext, R.color.primary));

        // Set the category name
        String name = mCursor.getString(CategoriesFragment.COLUMN_CATEGORY_NAME);
        holder.mNameView.setText(name);

        // Show or hide the subcategories icon
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

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(CategoriesFragment.COLUMN_CATEGORY_ID);
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final RelativeLayout mCategoryIconBackground;
        public final ImageView mCategoryIcon;
        public final RelativeLayout mSelectedIconBackground;
        public final ImageView mSelectedIcon;
        public final TextView mNameView;
        public final ImageView mSubcategoriesIcon;

        public ViewHolder(View view) {
            super(view);
            mCategoryIconBackground = (RelativeLayout) view.findViewById(R.id.category_icon_background);
            mCategoryIcon = (ImageView) view.findViewById(R.id.category_icon);
            mSelectedIconBackground = (RelativeLayout) view.findViewById(R.id.selected_icon_background);
            mSelectedIcon = (ImageView) view.findViewById(R.id.selected_icon);
            mNameView = (TextView) view.findViewById(R.id.category_name);
            mSubcategoriesIcon = (ImageView) view.findViewById(R.id.subcategories_icon);
        }
    }
}
