package com.turt2live.minecraftuuid.thread;

import com.turt2live.minecraftuuid.api.UUIDServiceProvider;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ThreadTest implements Runnable {

    private static ConcurrentLinkedQueue<String> QUEUE = new ConcurrentLinkedQueue<String>();

    public static void main(String[] args) {
        final FileDialog dialog = new FileDialog((Frame) null, "Open", FileDialog.LOAD);
        dialog.setVisible(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    while (QUEUE.size() < 100000) {
                        try {
                            BufferedReader reader = new BufferedReader(new FileReader(new File(dialog.getDirectory(), dialog.getFile())));
                            String line;
                            while ((line = reader.readLine()) != null) {
                                System.out.println("Queued " + line);
                                QUEUE.add(line);
                            }
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {

                    }
                }
            }
        }).start();

        for (int i = 0; i < 10; i++) {
            new Thread(new ThreadTest()).start();
        }
    }

    @Override
    public void run() {
        while (true) {
            if (QUEUE.size() > 0) {
                String name = QUEUE.poll();
                UUID uuid = UUIDServiceProvider.getUUID(name);
                String uid = uuid == null ? "Unknown" : uuid.toString().replace("-", "");
                System.out.println(name + " = " + uid);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }
    }
}
