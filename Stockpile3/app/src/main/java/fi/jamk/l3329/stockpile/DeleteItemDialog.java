package fi.jamk.l3329.stockpile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by peter on 9.11.2017.
 */

public class DeleteItemDialog extends DialogFragment {

    LongClickItemListener mListener;
    private int position;

    interface LongClickItemListener {
        void onDialogPositiveClick(DialogFragment dialogFragment , int position);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        position = getArguments().getInt("position");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.delete_item_dialog, null);


        builder.setView(dialogView)
                .setTitle("Delete?")
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogPositiveClick(DeleteItemDialog.this , position);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        return builder.create();
    }

    // Override the Fragment.onAttach() method to instantiate the mListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the ShopListDialogListener so we can send events to the host
            mListener = (LongClickItemListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString() + " must implement LongClickItemListener");
        }
    }

}
