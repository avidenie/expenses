package ro.expectations.expenses.widget.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public class ItemClickHelper {

    private final RecyclerView mRecyclerView;

    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mOnItemClickListener != null) {
                RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(view);
                mOnItemClickListener.onItemClick(mRecyclerView, view, holder.getAdapterPosition());
            }
        }
    };

    private View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            if (mOnItemLongClickListener != null) {
                RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(view);
                return mOnItemLongClickListener.onItemLongClick(mRecyclerView, view, holder.getAdapterPosition());
            }
            return false;
        }
    };

    private RecyclerView.OnChildAttachStateChangeListener mAttachListener
            = new RecyclerView.OnChildAttachStateChangeListener() {
        @Override
        public void onChildViewAttachedToWindow(View view) {
            if (mOnItemClickListener != null) {
                view.setOnClickListener(mOnClickListener);
            }
            if (mOnItemLongClickListener != null) {
                view.setOnLongClickListener(mOnLongClickListener);
            }
        }

        @Override
        public void onChildViewDetachedFromWindow(View view) {

        }
    };

    public ItemClickHelper(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        mRecyclerView.addOnChildAttachStateChangeListener(mAttachListener);
    }

    public ItemClickHelper setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
        return this;
    }

    public ItemClickHelper setOnItemLongClickListener(OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
        return this;
    }

    public void detach() {
        mRecyclerView.removeOnChildAttachStateChangeListener(mAttachListener);
    }

    public interface OnItemClickListener {
        void onItemClick(RecyclerView parent, View view, int position);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(RecyclerView parent, View view, int position);
    }
}
