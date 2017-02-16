package com.brotherpowers.audiojournal.TextEditor;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.brotherpowers.audiojournal.Model.DataEntry;
import com.brotherpowers.audiojournal.Model.TextNoteJSON;
import com.brotherpowers.audiojournal.R;
import com.google.gson.Gson;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.mthli.knife.KnifeText;
import io.realm.Realm;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link TextEditorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TextEditorFragment extends Fragment {


    public TextEditorFragment() {
        // Required empty public constructor
    }

    private final Gson gson = new Gson();

    private long entry_id;
    private String title;
    private String note;

    @BindView(R.id.input_title)
    TextView _title;

    @BindView(R.id.input_note)
    KnifeText _note;

    /**
     * @param entry_id {@link DataEntry#id}
     */
    public static TextEditorFragment newInstance(long entry_id) {
        TextEditorFragment fragment = new TextEditorFragment();
        Bundle args = new Bundle();
        args.putLong("id", entry_id);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        entry_id = getArguments().getLong("id", -1);

        final Realm realm = Realm.getDefaultInstance();

        if (savedInstanceState == null) {
            final DataEntry entry = realm.where(DataEntry.class).equalTo("id", entry_id).findFirst();
            assert entry != null;

            TextNoteJSON noteJSON = gson.fromJson(entry.text_note, TextNoteJSON.class);

            title = noteJSON.title;
            note = noteJSON.note;
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_text_editor, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (!TextUtils.isEmpty(title)) {
            _title.setText(title);
        }
        if (!TextUtils.isEmpty(note)) {
            _note.fromHtml(note);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_text_editor, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_text_note:
                final Realm realm = Realm.getDefaultInstance();

                TextNoteJSON noteJSON = new TextNoteJSON(_title.getText().toString(), _note.toHtml());

                final DataEntry entry = realm.where(DataEntry.class).equalTo("id", entry_id).findFirst();
                assert entry != null;

                // Persist the data
                realm.executeTransaction(r -> entry.text_note = gson.toJson(noteJSON));

                // Finish the activity
                getActivity().finish();
                break;
        }
        return true;
    }

    @OnClick(R.id.bold)
    void boldText() {
        _note.bold(!_note.contains(KnifeText.FORMAT_BOLD));
    }

    @OnClick(R.id.italic)
    void italicText() {
        _note.italic(!_note.contains(KnifeText.FORMAT_ITALIC));
    }

    @OnClick(R.id.underline)
    void underlineText() {
        _note.underline(!_note.contains(KnifeText.FORMAT_UNDERLINED));
    }

    @OnClick(R.id.strikethrough)
    void strikeThruText() {
        _note.strikethrough(!_note.contains(KnifeText.FORMAT_STRIKETHROUGH));
    }

    @OnClick(R.id.bullet)
    void bulletText() {
        _note.bullet(!_note.contains(KnifeText.FORMAT_BULLET));
    }

    @OnClick(R.id.quote)
    void quoteText() {
        _note.quote(!_note.contains(KnifeText.FORMAT_QUOTE));
    }

    @OnClick(R.id.link)
    void linkText() {
        final int start = _note.getSelectionStart();
        final int end = _note.getSelectionEnd();

        EditText editText = new EditText(getContext());
        editText.setHint(R.string.link_dialog_title);

        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.link_dialog_hint)
                .setView(editText)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String link = editText.getText().toString().trim();
                    if (TextUtils.isEmpty(link)) {
                        return;
                    }
                    _note.link(link, start, end);

                })
                .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(true)
                .create();
        alertDialog.show();
    }

    @OnClick(R.id.clear)
    void clearText() {
        _note.clearFormats();
    }
}
