package ro.expectations.expenses.ui.accounts;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ViewStub;

import ro.expectations.expenses.R;

public class NewAccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ViewStub mainContent = (ViewStub) findViewById(R.id.main_content);
        mainContent.setLayoutResource(R.layout.content_backup);
        mainContent.inflate();
    }
}
