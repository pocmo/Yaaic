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
package org.yaaic.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import org.yaaic.R;
import org.yaaic.Yaaic;
import org.yaaic.activity.AddServerActivity;
import org.yaaic.activity.YaaicActivity;
import org.yaaic.adapter.ServersAdapter;
import org.yaaic.db.Database;
import org.yaaic.irc.IRCBinder;
import org.yaaic.listener.ServerListener;
import org.yaaic.model.Broadcast;
import org.yaaic.model.Extra;
import org.yaaic.model.Server;
import org.yaaic.model.Status;
import org.yaaic.receiver.ServerReceiver;

/**
 * Fragment showing a list of configured servers.
 */
public class OverviewFragment extends Fragment implements ServerListener, ServersAdapter.ClickListener, View.OnClickListener {
    public static final String TRANSACTION_TAG = "fragment_overview";

    private ServersAdapter adapter;
    private YaaicActivity activity;
    private BroadcastReceiver receiver;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof YaaicActivity)) {
            throw new IllegalArgumentException("Activity has to implement YaaicActivity interface");
        }

        this.activity = (YaaicActivity) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity.setToolbarTitle(getString(R.string.app_name));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_servers, container, false);

        adapter = new ServersAdapter(this);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        ImageButton button = (ImageButton) view.findViewById(R.id.fab);
        button.setOnClickListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        receiver = new ServerReceiver(this);
        getActivity().registerReceiver(receiver, new IntentFilter(Broadcast.SERVER_UPDATE));

        adapter.loadServers();
    }

    @Override
    public void onPause() {
        super.onPause();

        getActivity().unregisterReceiver(receiver);
    }

    @Override
    public void onClick(View view) {
        final Context context = view.getContext();

        Intent intent = new Intent(context, AddServerActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onServerSelected(Server server) {
        activity.onServerSelected(server);
    }

    @Override
    public void onConnectToServer(Server server) {
        IRCBinder binder = activity.getBinder();

        if (binder != null && server.getStatus() == Status.DISCONNECTED) {
            binder.connect(server);
            server.setStatus(Status.CONNECTING);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDisconnectFromServer(Server server) {
        IRCBinder binder = activity.getBinder();

        if (binder != null) {
            server.clearConversations();
            server.setStatus(Status.DISCONNECTED);
            server.setMayReconnect(false);
            binder.getService().getConnection(server.getId()).quitServer();
        }
    }

    @Override
    public void onEditServer(Server server) {
        if (server.getStatus() != Status.DISCONNECTED) {
            Toast.makeText(getActivity(), getResources().getString(R.string.disconnect_before_editing), Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(getActivity(), AddServerActivity.class);
            intent.putExtra(Extra.SERVER, server.getId());
            startActivityForResult(intent, 0);
        }

    }

    @Override
    public void onDeleteServer(Server server) {
        IRCBinder binder = activity.getBinder();

        if (binder != null) {
            binder.getService().getConnection(server.getId()).quitServer();

            Database db = new Database(getActivity());
            db.removeServerById(server.getId());
            db.close();

            Yaaic.getInstance().removeServerById(server.getId());
            adapter.loadServers();
        }
    }

    @Override
    public void onStatusUpdate() {
        adapter.loadServers();
    }
}
