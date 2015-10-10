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

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.yaaic.R;
import org.yaaic.Yaaic;
import org.yaaic.fragment.ConversationFragment;
import org.yaaic.fragment.OverviewFragment;
import org.yaaic.fragment.SettingsFragment;
import org.yaaic.irc.IRCBinder;
import org.yaaic.irc.IRCService;
import org.yaaic.listener.ServerListener;
import org.yaaic.model.Broadcast;
import org.yaaic.model.Extra;
import org.yaaic.model.Server;
import org.yaaic.model.Status;
import org.yaaic.receiver.ServerReceiver;

import java.util.List;

/**
 * The main activity of Yaaic. We'll add, remove and replace fragments here.
 */
public class MainActivity extends AppCompatActivity implements YaaicActivity, ServiceConnection, ServerListener {
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;
    private DrawerLayout drawer;
    private IRCBinder binder;
    private ServerReceiver receiver;
    private LinearLayout serverContainer;
    private View drawerEmptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initializeToolbar();
        initializeDrawer();

        if (savedInstanceState == null) {
            onOverview(null);
        }
    }

    public void initializeToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void initializeDrawer() {
        drawer = (DrawerLayout) findViewById(R.id.drawer);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, 0, 0);

        drawer.setDrawerListener(toggle);

        serverContainer = (LinearLayout) findViewById(R.id.server_container);

        drawerEmptyView = findViewById(R.id.drawer_empty_servers);
        drawerEmptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddServerActivity.class);
                startActivity(intent);

                drawer.closeDrawers();
            }
        });
    }

    public void updateDrawerServerList() {
        List<Server> servers = Yaaic.getInstance().getServers();
        drawerEmptyView.setVisibility(servers.size() > 0 ? View.GONE : View.VISIBLE);

        serverContainer.removeAllViews();

        for (final Server server : servers) {
            TextView serverView = (TextView) getLayoutInflater().inflate(R.layout.item_drawer_server, drawer, false);
            serverView.setText(server.getTitle());

            serverView.setCompoundDrawablesWithIntrinsicBounds(
                    getDrawable(server.isConnected()
                        ? R.drawable.ic_navigation_server_connected
                        : R.drawable.ic_navigation_server_disconnected),
                    null,
                    null,
                    null
            );

            int colorResource = server.isConnected() ? R.color.connected : R.color.disconnected;
            serverView.setTextColor(ContextCompat.getColor(this, colorResource));

            serverView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onServerSelected(server);

                    drawer.closeDrawers();
                }
            });

            serverContainer.addView(serverView, 0);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        toggle.syncState();
    }

    @Override
    protected void onResume() {
        super.onResume();

        receiver = new ServerReceiver(this);
        registerReceiver(receiver, new IntentFilter(Broadcast.SERVER_UPDATE));

        Intent intent = new Intent(this, IRCService.class);
        intent.setAction(IRCService.ACTION_BACKGROUND);
        startService(intent);

        bindService(intent, this, 0);

        updateDrawerServerList();
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(receiver);

        if (binder != null && binder.getService() != null) {
            binder.getService().checkServiceStatus();
        }

        unbindService(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }

        return false;
    }

    @Override
    public void onServerSelected(Server server) {
        Bundle arguments = new Bundle();

        if (server.getStatus() == Status.DISCONNECTED && !server.mayReconnect()) {
            server.setStatus(Status.PRE_CONNECTING);

            arguments.putBoolean(Extra.CONNECT, true);
        }

        arguments.putInt(Extra.SERVER_ID, server.getId());

        ConversationFragment fragment = new ConversationFragment();
        fragment.setArguments(arguments);

        switchToFragment(fragment, ConversationFragment.TRANSACTION_TAG + "-" + server.getId());
    }

    public void onOverview(View view) {
        switchToFragment(new OverviewFragment(), OverviewFragment.TRANSACTION_TAG);
    }

    public void onSettings(View view) {
        switchToFragment(new SettingsFragment(), SettingsFragment.TRANSACTION_TAG);
    }

    private void switchToFragment(Fragment fragment, String tag) {
        drawer.closeDrawers();

        FragmentManager fragmentManager = getSupportFragmentManager();

        if (fragmentManager.findFragmentByTag(tag) != null) {
            // We are already showing this fragment
            return;
        }

        fragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left)
                .replace(R.id.container, fragment, tag)
                .commit();
    }

    public void onAbout(View view) {
        drawer.closeDrawers();
        startActivity(new Intent(this, AboutActivity.class));
    }

    @Override
    public IRCBinder getBinder() {
        return binder;
    }

    @Override
    public Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    public void setToolbarTitle(String title) {
        if (toolbar != null) {
            toolbar.setTitle(title);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        binder = (IRCBinder) service;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        binder = null;
    }

    @Override
    public void onStatusUpdate() {
        updateDrawerServerList();
    }
}
