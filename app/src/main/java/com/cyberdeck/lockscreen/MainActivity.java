package com.cyberdeck.lockscreen;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);

        // Start the lock screen service
        Intent serviceIntent = new Intent(this, LockScreenService.class);
        startService(serviceIntent);

        statusText.setText("[STATUS: ACTIVE]");
        statusText.setTextColor(0xFF00FF00);

        Toast.makeText(this, "CyberDeck Lock Screen enabled", Toast.LENGTH_LONG).show();
    }
}
