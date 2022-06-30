package com.builderlinebr.qr_codechecker;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class AttentionDialogFragment extends DialogFragment implements View.OnClickListener {

    String url;
    public static int resultDialog = -1;

    public AttentionDialogFragment(String text) {
        resultDialog = -1;
        url = text;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(requireActivity()).inflate(R.layout.fragment_attention_dialog, null);
        view.findViewById(R.id.button_save).setOnClickListener(this);
        view.findViewById(R.id.button_go_over).setOnClickListener(this);
        view.findViewById(R.id.button_close).setOnClickListener(this);

        ((TextView)view.findViewById(R.id.message_id)).setText(url);


        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view);
        builder.setCustomTitle(null);

        return builder.create();
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        resultDialog = view.getId();
        getDialog().cancel();

    }
}
