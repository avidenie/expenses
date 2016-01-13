package ro.expectations.expenses.ui.payees;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ro.expectations.expenses.R;
import ro.expectations.expenses.provider.ExpensesContract;

public class PayeesAdapter extends RecyclerView.Adapter<PayeesAdapter.ViewHolder> {

    private Cursor mCursor;
    final private Context mContext;

    public PayeesAdapter(Context context) {
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item_payees, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        // Set the payee name
        String name = mCursor.getString(mCursor.getColumnIndex(ExpensesContract.Payees.NAME));
        holder.mPayeeName.setText(name);
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
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView mPayeeName;

        public ViewHolder(View itemView) {
            super(itemView);
            mPayeeName = (TextView) itemView.findViewById(R.id.payee_name);
        }
    }
}
