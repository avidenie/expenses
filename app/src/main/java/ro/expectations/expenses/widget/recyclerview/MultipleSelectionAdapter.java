package ro.expectations.expenses.widget.recyclerview;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;

public abstract class MultipleSelectionAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    private static final String STATE_KEY_SELECTED_ITEMS = "MultipleSelectionSelectedItems";
    private static final String STATE_KEY_SELECTED_COUNT = "MultipleSelectionSelectedCount";

    private SelectedItems mSelectedItems = new SelectedItems();
    private int mSelectedItemsCount;

    /**
     * Returns the number of items currently selected.
     *
     * <p>To determine the specific items that are currently selected, use
     * the <code>getSelectedItemPositions</code> method.
     *
     * @return The number of items currently selected
     */
    public int getSelectedItemCount() {
        return mSelectedItemsCount;
    }

    /**
     * Returns the selected state of the specified position.
     *
     * @param position The item whose selected state to return
     * @return The item's selected state
     */
    public boolean isItemSelected(int position) {
        return mSelectedItems.get(position);
    }

    /**
     * Returns the set of selected items in the list.
     *
     * @return A SparseBooleanArray which will return the selected status for each position in
     * the list.
     */
    public SparseBooleanArray getSelectedItemPositions() {
        return mSelectedItems;
    }

    /**
     * Sets the selected state of the specified position.
     *
     * @param position The item whose selected state is to be set
     * @param selected The new selected state for the item
     */
    public void setItemSelected(int position, boolean selected) {
        boolean oldValue = mSelectedItems.get(position);
        mSelectedItems.put(position, selected);

        if (oldValue != selected) {
            if (selected) {
                mSelectedItemsCount++;
            } else {
                mSelectedItemsCount--;
            }
            notifyItemChanged(position);
        }
    }

    public void clearSelectedItems() {
        mSelectedItems.clear();
        mSelectedItemsCount = 0;
    }

    public boolean isChoiceMode() {
        return mSelectedItemsCount > 0;
    }

    public void onSaveInstanceState(Bundle state) {
        state.putParcelable(STATE_KEY_SELECTED_ITEMS, mSelectedItems);
        state.putInt(STATE_KEY_SELECTED_COUNT, mSelectedItemsCount);
    }

    public void onRestoreInstanceState(Bundle state) {
        mSelectedItems = state.getParcelable(STATE_KEY_SELECTED_ITEMS);
        if (mSelectedItems == null) {
            mSelectedItems = new SelectedItems();
        }
        mSelectedItemsCount = state.getInt(STATE_KEY_SELECTED_COUNT, 0);
    }

    private static class SelectedItems extends SparseBooleanArray implements Parcelable {

        private static final int FALSE = 0;
        private static final int TRUE = 1;

        public SelectedItems() {
            super();
        }

        private SelectedItems(Parcel in) {
            final int size = in.readInt();
            if (size > 0) {
                for (int i = 0; i < size; i++) {
                    final int key = in.readInt();
                    final boolean value = (in.readInt() == TRUE);
                    put(key, value);
                }
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            final int size = size();
            parcel.writeInt(size);

            for (int i = 0; i < size; i++) {
                parcel.writeInt(keyAt(i));
                parcel.writeInt(valueAt(i) ? TRUE : FALSE);
            }
        }

        public static final Parcelable.Creator<SelectedItems> CREATOR =
                new Parcelable.Creator<SelectedItems>() {

                @Override
                public SelectedItems createFromParcel(Parcel in) {
                    return new SelectedItems(in);
                }

                @Override
                public SelectedItems[] newArray(int size) {
                    return new SelectedItems[size];
                }
        };
    }
}
