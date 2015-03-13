package org.yaaic.activity;

import java.util.ArrayList;

import org.yaaic.R;
import org.yaaic.model.Extra;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class AddAliasActivity extends Activity implements OnClickListener, OnItemClickListener, TextWatcher
{
    private EditText aliasInput;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> aliases;
    private Button addButton;
    private Button okButton;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.aliasadd);

        aliasInput = (EditText) findViewById(R.id.alias);
        aliasInput.addTextChangedListener(this);

        adapter = new ArrayAdapter<String>(this, R.layout.aliasitem);

        ListView list = (ListView) findViewById(R.id.aliases);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);

        addButton = (Button) findViewById(R.id.add);
        addButton.setOnClickListener(this);
        ((Button) findViewById(R.id.cancel)).setOnClickListener(this);

        okButton = (Button) findViewById(R.id.ok);
        okButton.setOnClickListener(this);
        okButton.setEnabled(false);

        aliases = getIntent().getExtras().getStringArrayList(Extra.ALIASES);

        for (String alias : aliases) {
            adapter.add(alias);
        }
    }

    /**
     * On Click
     */
    @Override
    public void onClick(View v)
    {
        switch (v.getId()) {
            case R.id.add:
                String alias = aliasInput.getText().toString().trim();
                aliases.add(alias);
                adapter.add(alias);
                aliasInput.setText("");
                okButton.setEnabled(true);
                break;

            case R.id.cancel:
                setResult(RESULT_CANCELED);
                finish();
                break;

            case R.id.ok:
                // Get list and return as result
                Intent intent = new Intent();
                intent.putExtra(Extra.ALIASES, aliases);
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }

    /**
     * On item clicked
     */
    @Override
    public void onItemClick(AdapterView<?> list, View item, int position, long id)
    {
        final String alias = adapter.getItem(position);

        String[] items = { getResources().getString(R.string.action_remove) };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(alias);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0: // Remove
                        adapter.remove(alias);
                        aliases.remove(alias);
                        okButton.setEnabled(true);
                        break;
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * On text changed
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {
        addButton.setEnabled(aliasInput.getText().length() > 0);
    }

    /**
     * After text changed
     */
    @Override
    public void afterTextChanged(Editable s)
    {
        // Do nothing.
    }

    /**
     * Before text changed
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after)
    {
        // Do nothing.
    }
}
