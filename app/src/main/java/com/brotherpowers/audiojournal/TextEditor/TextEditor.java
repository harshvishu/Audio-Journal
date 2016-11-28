package com.brotherpowers.audiojournal.TextEditor;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;

import com.brotherpowers.audiojournal.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TextEditor extends AppCompatActivity {

    @BindView(R.id.text_view)
    AppCompatEditText markDownTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_editor);
        ButterKnife.bind(this);


        String text = "https://www.google.com hello";

        Editable.Factory factory = new Editable.Factory();
        factory.newEditable(text).append(text);

        markDownTextView.setEditableFactory(factory);



    }
}
