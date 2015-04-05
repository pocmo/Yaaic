/*
Yaaic - Yet Another Android IRC Client

Copyright 2009-2013 Sebastian Kaspari

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
package org.yaaic.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.yaaic.R;
import org.yaaic.Yaaic;
import org.yaaic.menu.ServerPopupMenu;
import org.yaaic.model.Server;

import java.util.List;

/**
 * RecyclerView adapter for server cards.
 */
public class ServersAdapter extends RecyclerView.Adapter<ServersAdapter.ViewHolder> {
    public interface ClickListener {
        void onServerSelected(Server server);
        void onConnectToServer(Server server);
        void onDisconnectFromServer(Server server);
        void onEditServer(Server server);
        void onDeleteServer(Server server);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView titleView;
        public final TextView hostView;
        public final ImageView connectionView;
        public final View menuView;
        public final ServerPopupMenu popupMenu;

        public ViewHolder(View view, ClickListener listener) {
            super(view);

            titleView = (TextView) view.findViewById(R.id.title);
            hostView = (TextView) view.findViewById(R.id.host);
            connectionView = (ImageView) view.findViewById(R.id.connection);
            menuView = view.findViewById(R.id.menu);

            popupMenu = new ServerPopupMenu(
                view.getContext(), view.findViewById(R.id.menu),
                listener
            );
        }
    }

    private List<Server> servers;
    private ClickListener listener;

    public ServersAdapter(ClickListener listener) {
        this.listener = listener;

        loadServers();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.item_server, parent, false);

        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Server server = servers.get(position);

        int colorResource = server.isConnected() ? R.color.connected : R.color.disconnected;
        int color = holder.itemView.getContext().getResources().getColor(colorResource);

        holder.titleView.setText(server.getTitle());
        holder.titleView.setTextColor(color);
        holder.connectionView.setImageResource(
                server.isConnected()
                ? R.drawable.ic_navigation_server_connected
                : R.drawable.ic_navigation_server_disconnected
        );
        holder.hostView.setText(String.format("%s @ %s : %d",
                server.getIdentity().getNickname(),
                server.getHost(),
                server.getPort()
        ));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onServerSelected(server);
            }
        });

        holder.popupMenu.updateServer(server);
    }

    /**
     * Load servers from database
     *
     * Delegate call to yaaic instance
     */
    public void loadServers() {
        servers = Yaaic.getInstance().getServersAsArrayList();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return servers.size();
    }
}
