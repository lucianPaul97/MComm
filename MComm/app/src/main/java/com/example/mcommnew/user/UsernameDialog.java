package com.example.mcommnew.user;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.example.mcommnew.R;

public class UsernameDialog extends AppCompatDialogFragment {

    private EditText usernameEdit;
    private UsernameDialogListener usernameDialogListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.change_username_dialog, null);

        builder.setView(view)
                .setTitle("Change username")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String newUsername = usernameEdit.getText().toString();
                        usernameDialogListener.changeUsername(newUsername);
                    }
                });

        usernameEdit = view.findViewById(R.id.changeUsernameEdit);
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        usernameDialogListener = (UsernameDialogListener) context;

    }
}
