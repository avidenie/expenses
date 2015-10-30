package ro.expectations.expenses.ui.backup;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;

import ro.expectations.expenses.R;

public class BackupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.app_bar);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("@@@", "FAB clicked");
            }
        });
        fab.setVisibility(View.VISIBLE);

        ViewStub mainContent = (ViewStub) findViewById(R.id.main_content);
        mainContent.setLayoutResource(R.layout.content_backup);
        mainContent.inflate();
    }
}
