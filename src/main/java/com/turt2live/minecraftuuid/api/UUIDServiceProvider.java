package com.turt2live.minecraftuuid.api;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public final class UUIDServiceProvider {

    public static String insertDashes(String s) {
        if (s.length() != 32) return null;
        return s.substring(0, 8) + "-" + s.substring(8, 12) + "-" + s.substring(12, 16) + "-" + s.substring(16, 20) + "-" + s.substring(20, 32);
    }

    public static String getName(UUID uuid) {
        if (uuid == null) return null;
        try {
            URL url = new URL("http://uuid.turt2live.com/name/" + uuid.toString().replaceAll("-", ""));
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String parsed = "";
            String line;
            while ((line = reader.readLine()) != null) parsed += line;
            reader.close();

            Object o = JSONValue.parse(parsed);
            if (o instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) o;
                Object status = jsonObject.get("status");
                if (status instanceof String && ((String) status).equalsIgnoreCase("ok")) {
                    o = jsonObject.get("name");
                    if (o instanceof String) {
                        return (String) o;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static UUID getUUID(String name) {
        if (name == null) return null;
        try {
            URL url = new URL("http://uuid.turt2live.com/uuid/" + name);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String parsed = "";
            String line;
            while ((line = reader.readLine()) != null) parsed += line;
            reader.close();

            Object o = JSONValue.parse(parsed);
            if (o instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) o;
                o = jsonObject.get("uuid");
                if (o instanceof String) {
                    String s = (String) o;
                    if (!s.equalsIgnoreCase("unknown")) {
                        return UUID.fromString(insertDashes(s));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Dates are in UTC
    public static Map<String, Date> getHistory(UUID uuid) {
        if (uuid == null) return null;
        try {
            URL url = new URL("http://uuid.turt2live.com/history/" + uuid.toString().replaceAll("-", ""));
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String parsed = "";
            String line;
            while ((line = reader.readLine()) != null) parsed += line;
            reader.close();

            Map<String, Date> map = new HashMap<String, Date>();
            Object o = JSONValue.parse(parsed);
            if (o instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) o;
                Object namesObj = jsonObject.get("names");
                if (namesObj instanceof JSONArray) {
                    JSONArray names = (JSONArray) namesObj;
                    for (int i = 0; i < names.size(); i++) {
                        o = names.get(i);
                        if (o instanceof JSONObject) {
                            JSONObject json = (JSONObject) o;

                            Object nameObj = json.get("name");
                            Object dateObj = json.get("last-seen");
                            if (nameObj instanceof String && dateObj instanceof String) {
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                                try {
                                    Date date = format.parse((String) dateObj);
                                    map.put((String) nameObj, date);
                                } catch (ParseException e) {
                                    System.out.println("Could not parse " + dateObj + ": " + e.getMessage());
                                }
                            }
                        }
                    }
                }
            }
            return map;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
