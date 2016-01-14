package ro.expectations.expenses.widget.recyclerview;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

public abstract class SingleSelectionAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    private static final String STATE_KEY_SELECTED_POSITION = "SingleSelectionSelectedPosition";

    public static final int INVALID_POSITION = -1;

    private int mSelectedItemPosition = INVALID_POSITION;

    /**
     * Returns the selected state of the specified position.
     *
     * @param position The item whose selected state to return
     * @return The item's selected state
     */
    public boolean isItemSelected(int position) {
        return position == mSelectedItemPosition;
    }

    /**
     * Returns the position of the currently selected item.
     *
     * @return The position of the currently selected item or {@link #INVALID_POSITION} if nothing is
     * selected
     */
    public int getSelectedItemPosition() {
        return mSelectedItemPosition;
    }

    /**
     * Sets the selected state of the specified position.
     *
     * @param position The item whose selected state is to be set
     * @param selected The new selected state for the item
     */
    public void setItemSelected(int position, boolean selected) {

        if (selected) {
            // First, check if another item was previously selected
            if (mSelectedItemPosition != position) {
                clearSelection();
            }
            mSelectedItemPosition = position;
            notifyItemChanged(position);
        } else {
            // When asked to deselect, only do it if the item was previously selected
            if (mSelectedItemPosition == position) {
                mSelectedItemPosition = INVALID_POSITION;
                notifyItemChanged(position);
            }
        }
    }

    public void clearSelection() {
        int oldPosition = mSelectedItemPosition;
        if (oldPosition != INVALID_POSITION) {
            mSelectedItemPosition = INVALID_POSITION;
            notifyItemChanged(oldPosition);
        }
    }

    public boolean isChoiceMode() {
        return mSelectedItemPosition != INVALID_POSITION;
    }

    public void onSaveInstanceState(Bundle state) {
        state.putInt(STATE_KEY_SELECTED_POSITION, mSelectedItemPosition);
    }

    public void onRestoreInstanceState(Bundle state) {
        mSelectedItemPosition = state.getInt(STATE_KEY_SELECTED_POSITION, INVALID_POSITION);
    }
}
