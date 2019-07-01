package com.example.mcommnew.user;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.example.mcommnew.R;

public class UsernameDialog extends AppCompatDialogFragment {

    private EditText mUsernameEdit;
    private UsernameDialogListener mUsernameDialogListener;

    @NonNull
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
                        String newUsername = mUsernameEdit.getText().toString();
                        mUsernameDialogListener.changeUsername(newUsername);
                    }
                });

        mUsernameEdit = view.findViewById(R.id.changeUsernameEdit);
        return builder.create();
    }

    public void setUsernameDialogListener(UsernameDialogListener usernameDialogListener)
    {
        mUsernameDialogListener = usernameDialogListener;
    }
}
