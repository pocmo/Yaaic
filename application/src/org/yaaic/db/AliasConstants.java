package org.yaaic.db;

import android.provider.BaseColumns;

public class AliasConstants implements BaseColumns {

    public static final String TABLE_NAME = "aliases";
    
    // fields
    public static final String ALIAS = "alias";
    public static final String IDENTITY = "identity";
    
    public static final String[] ALL = {
        _ID,
        ALIAS,
        IDENTITY,
    };
    
}
