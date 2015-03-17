package com.example.android.animationsdemo;

/**
 * Created by lfredericks on 3/17/2015.
 */
import android.R;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by IntelliJ IDEA.
 * User: Jim
 * Date: 3/26/13
 * Time: 1:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class SelectionActivity extends ListActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent != null) {
            String title = intent.getStringExtra(CameraActivity.ACTIVITY_TITLE_EXTRA);
            String[] selections = intent.getStringArrayExtra(CameraActivity.SELECTIONS_EXTRA);

            if (title != null)
                setTitle(title);

            if(selections != null) {
                ArrayAdapter<String> adapter =
                        new ArrayAdapter<String>(this, R.layout.simple_list_item_1, selections);
                setListAdapter(adapter);
            }

        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Intent resultIntent = new Intent();
        resultIntent.putExtra(CameraActivity.SELECTED_INDEX_EXTRA, position);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}