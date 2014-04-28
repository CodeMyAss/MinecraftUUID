package com.turt2live.minecraftuuid.thread;

import com.turt2live.minecraftuuid.api.UUIDServiceProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThreadTest implements Runnable {

    private static ConcurrentLinkedQueue<String> QUEUE = new ConcurrentLinkedQueue<String>();
    private static AtomicBoolean DONE = new AtomicBoolean(false);
    private static List<String> names = new ArrayList<String>(1000000);

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            new Thread(new ThreadTest()).start();
        }

        new Thread(new Runnable() {

            @Override
            public void run() {
                File[] files = new File(System.getProperty("user.dir")).listFiles();
                if (files != null && files.length > 0) {
                    try {
                        scan(files);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                DONE.set(true);
            }
        }).start();
    }

    private static void scan(File[] files) throws Exception {
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.isDirectory()) {
                    scan(file.listFiles());
                } else {
                    parse(file);
                }
            }
        }
    }

    private static void parse(File file) throws Exception {
        if (file.getName().endsWith(".csv")) {
            System.out.println(file.getAbsolutePath());
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                char[] chars = line.toCharArray();
                String name = "", uuid = "";
                boolean onUuid = true;
                for (int i = 0; i < chars.length; i++) {
                    char c = chars[i];
                    if (c != '"') {
                        if (c == ',') {
                            onUuid = !onUuid;
                        } else {
                            if (onUuid) {
                                uuid += c;
                            } else {
                                name += c;
                            }
                        }
                    }
                }
                QUEUE.add(name);
            }
            reader.close();
        }
    }

    @Override
    public void run() {
        while (!DONE.get()) {
            while (QUEUE.size() > 0) {
                String name = QUEUE.poll();
                UUID uuid = UUIDServiceProvider.getUUID(name);
                System.out.println("[" + QUEUE.size() + "] " + name + " = " + uuid);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
