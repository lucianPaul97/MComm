package com.example.mcomm.user;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.example.mcomm.R;

public class CustomDialog extends AppCompatDialogFragment {

    private EditText mNameEdit;
    private CustomDialogListener mCustomDialogListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_dialog, null);
        mNameEdit = view.findViewById(R.id.NameEdit);

        String intent = getArguments().getString("intent");
        if (intent.equals("changeUsername")) {
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
                            String newUsername = mNameEdit.getText().toString();
                            mCustomDialogListener.changeUsername(newUsername);
                        }
                    });
        }
        else
        {
            builder.setView(view)
                    .setTitle("Create group")
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String groupName = mNameEdit.getText().toString();
                            mCustomDialogListener.createGroup(groupName);
                        }
                    });
            mNameEdit.setHint("Group name");
        }
        return builder.create();
    }

    public void setUsernameDialogListener(CustomDialogListener customDialogListener)
    {
        mCustomDialogListener = customDialogListener;
    }
}
