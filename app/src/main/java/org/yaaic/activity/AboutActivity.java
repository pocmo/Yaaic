/*
Yaaic - Yet Another Android IRC Client

Copyright 2009-2015 Sebastian Kaspari

This file is part of Yaaic.

Yaaic is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Yaaic is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Yaaic.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.yaaic.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import org.yaaic.R;

/**
 * "About" dialog activity.
 */
public class AboutActivity extends Activity {
    private static final String TAG = "Yaaic/AboutActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.about);

        initializeVersionView();
        initializeIrcView();
    }

    private void initializeVersionView() {
        try {
            TextView versionView = (TextView) findViewById(R.id.version);
            versionView.setText(
                getPackageManager().getPackageInfo(getPackageName(), 0).versionName
            );
        } catch (PackageManager.NameNotFoundException e) {
            throw new AssertionError("Should not happen: Can't read application info of myself");
        }
    }

    private void initializeIrcView() {
        TextView ircLinkView = (TextView) findViewById(R.id.about_irclink);
        ircLinkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AboutActivity.this, AddServerActivity.class);
                intent.setData(Uri.parse(getString(R.string.app_irc)));
                startActivity(intent);
            }
        });
    }
}
