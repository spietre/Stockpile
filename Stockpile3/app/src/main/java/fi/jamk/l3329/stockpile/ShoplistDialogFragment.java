package fi.jamk.l3329.stockpile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by peter on 6.11.2017.
 */

public class ShoplistDialogFragment extends DialogFragment implements AdapterView.OnItemSelectedListener {


    private String currency;
    private String unit;
    ShopListDialogListener mListener;

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item

        Spinner spinner = (Spinner) parent;
        if(spinner.getId() == R.id.spinner_amount)
        {
            unit = parent.getItemAtPosition(position).toString();
        }
        else if(spinner.getId() == R.id.spinner_price)
        {
            currency = parent.getItemAtPosition(position).toString();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /* The activity that creates an instance of this dialog fragment must
         * implement this interface in order to receive event callbacks. */
    public interface ShopListDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog, String shopListName, float price, float amount,String currency, String unit);
        public void onDialogNegativeClick(DialogFragment dialog);
    }




    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.new_item_dialog, null);

        Spinner spin_currency = (Spinner)dialogView.findViewById(R.id.spinner_price);
        Spinner spin_units = (Spinner) dialogView.findViewById(R.id.spinner_amount);

        spin_currency.setOnItemSelectedListener(this);
        spin_units.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        final List<String> currencies = new ArrayList<String>();
        currencies.add("€");
        currencies.add("$");
        final List<String> units = new ArrayList<String>();
        units.add("kg");
        units.add("g");
        units.add("l");
        units.add("ml");
        units.add("pcs");

        // Creating adapter for spinner
        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<String>(dialogView.getContext(), android.R.layout.simple_spinner_item, currencies);
        ArrayAdapter<String> unitsAdapter = new ArrayAdapter<String>(dialogView.getContext(), android.R.layout.simple_spinner_item, units);

        // Drop down layout style - list view with radio button
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spin_currency.setAdapter(currencyAdapter);
        spin_units.setAdapter(unitsAdapter);

        builder.setView(dialogView)
                .setTitle(R.string.dialog_title)
                .setPositiveButton(R.string.dialog_add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //findFileToDownload an item name

                        EditText name = (EditText) dialogView.findViewById(R.id.name_item);
                        EditText price1 = (EditText) dialogView.findViewById(R.id.price_item);
                        EditText amount1 = (EditText) dialogView.findViewById(R.id.amount_item);

                        String shopListName = name.getText().toString().isEmpty() ? "New Item": name.getText().toString();
                        float price = Float.parseFloat(price1.getText().toString().isEmpty() ? "0" : price1.getText().toString());
                        float amount = Float.parseFloat(amount1.getText().toString().isEmpty() ? "0" : amount1.getText().toString());
                        // Send the positive button event back to the host activity
                        mListener.onDialogPositiveClick(ShoplistDialogFragment.this,shopListName, price, amount, currency, unit);
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogNegativeClick(ShoplistDialogFragment.this);
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
            mListener = (ShopListDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString() + " must implement ShopListDialogListener");
        }
    }
}
