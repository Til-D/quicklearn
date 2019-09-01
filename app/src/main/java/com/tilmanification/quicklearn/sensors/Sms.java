package com.tilmanification.quicklearn.sensors;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

class Sms {

    static final String	TAG				= Sms.class.getSimpleName();

    public int					id;
    public String				address;
    public int					person;
    public int					read;											// '0' = unread, '1' = read
    public long					time;
    public int					type;
    public String				protocol;
    public String				body;

    public String				event;
    public boolean				isOutgoing;
    public String				fullDescription	= "";

    public static Sms parseFrom(Context context) {
        Uri uriSMSURI = Uri.parse("content://sms");
        Cursor cursor = context.getContentResolver().query(uriSMSURI, null, null, null, null);
        cursor.moveToNext();

        Sms sms = new Sms();

        sms.id = getInt(cursor, "_id");
        sms.address = get(cursor, "address");
        sms.person = getInt(cursor, "person");
        sms.read = getInt(cursor, "read");
        sms.time = getLong(cursor, "date");
        sms.type = getInt(cursor, "type");
        sms.protocol = get(cursor, "protocol");
        sms.body = get(cursor, "body");

        if (sms.protocol == null) {
            sms.event = "sent";
            sms.isOutgoing = true;
        } else {
            sms.isOutgoing = false;
            if (sms.read == 0) {
                sms.event = "received";
            } else if (sms.read == 1) {
                sms.event = "read";
            }
        }

        StringBuilder b = new StringBuilder();
        for (int i = 0; i < cursor.getColumnCount(); i++) {
            String columnName = cursor.getColumnName(i);
            b.append(i + " --> " + columnName + ": " + get(cursor, columnName) + "\n");
        }
        sms.fullDescription = b.toString();

        return sms;
    }

    private static String get(Cursor c, String columnName) {
        try {
            return c.getString(c.getColumnIndexOrThrow(columnName));
        } catch (Exception e) {
            return columnName + " not found";
        }
    }

    private static int getInt(Cursor c, String columnName) {
        try {
            return c.getInt(c.getColumnIndexOrThrow(columnName));
        } catch (Exception e) {
            return -1;
        }
    }

    private static long getLong(Cursor c, String columnName) {
        try {
            return c.getLong(c.getColumnIndexOrThrow(columnName));
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public String toString() {
        return "SMS (" + id + ") " + event + ", address: " + address + ": " + body;
    }
}
