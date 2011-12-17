/*
Yaaic - Yet Another Android IRC Client

Copyright 2011 Michael Kowalchuk

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

package org.yaaic.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ModeParser
{
    public static class InvalidModeStringException extends Exception
    {
        private static final long serialVersionUID = -386634658872128990L;
    }

    public static class ChannelModeReply
    {
        private final String channelName;
        private final Map<Character, String> channelModes;

        public ChannelModeReply(String channelName, Map<Character, String> effectiveModes) {
            this.channelName = channelName;
            this.channelModes = effectiveModes;
        }
        public String getChannelName() {
            return channelName;
        }
        public Map<Character, String> getChannelModes() {
            return channelModes;
        }
    }

    /**
     * Parses a numeric 324 MODE reply, which is a list of the current modes
     * that apply to a channel.
     */
    public static ChannelModeReply parseChannelModeReply(String reply) throws InvalidModeStringException
    {
        String[] parts = reply.split(" ");
        if( parts.length < 3 ) {
            throw new InvalidModeStringException();
        }

        String channelName = parts[1];
        String modeString = parts[2];
        ArrayList<String> modeParams = new ArrayList<String>();
        for(int i = 3; i < parts.length; ++i) {
            modeParams.add(parts[i]);
        }

        List<ModeChange> modeChanges = ModeParser.parseModeChanges(modeString, modeParams);
        Map<Character,String> effectiveModes = new HashMap<Character,String>();

        for(ModeChange modeChange : modeChanges) {
            if( modeChange.getAction() == ModeChange.ACTION.REMOVING_MODE ) {
                effectiveModes.remove(modeChange.getMode());
            }
            else if( modeChange.getAction() == ModeChange.ACTION.ADDING_MODE ) {
                effectiveModes.put(modeChange.getMode(), modeChange.getParam());
            }
        }

        return new ChannelModeReply(channelName, effectiveModes);
    }


    /**
     * A single mode change, e.g., +k key
     */
    public static class ModeChange
    {
        private final ACTION action;
        private final char mode;
        private final String param;

        public enum ACTION {
            ADDING_MODE,
            REMOVING_MODE
        };

        public ModeChange(ACTION action, char mode, String param) {
            this.action = action;
            this.mode = mode;
            this.param = param;
        }
        public ModeChange(ACTION action, char mode) {
            this(action, mode, "");
        }
        public ACTION getAction() {
            return action;
        }
        public char getMode() {
            return mode;
        }
        public String getParam() {
            return param;
        }

    };

    /**
     * Combine a mode string (e.g., +sn-k), and a list of mode parameters.
     */
    public static List<ModeChange> parseModeChanges(String modeString, List<String> parameters) throws InvalidModeStringException
    {
        LinkedList<ModeChange> modes = new LinkedList<ModeChange>();

        ModeChange.ACTION action = null;
        Iterator<String> paramsIterator = parameters.iterator();
        for(int i = 0; i < modeString.length(); ++i) {
            char atPos = modeString.charAt(i);

            if( atPos == '+' ) {
                action = ModeChange.ACTION.ADDING_MODE;
            }
            else if( atPos == '-' ) {
                action = ModeChange.ACTION.REMOVING_MODE;
            }
            else {
                if( action == null ) {
                    throw new InvalidModeStringException();
                }

                if( "ovklb".contains(""+atPos) ) {
                    if( !paramsIterator.hasNext() ) {
                        throw new InvalidModeStringException();
                    }
                    modes.add( new ModeChange(action, atPos, paramsIterator.next() ) );
                }
                else {
                    modes.add( new ModeChange(action, atPos) );
                }
            }
        }
        return modes;
    }
}
