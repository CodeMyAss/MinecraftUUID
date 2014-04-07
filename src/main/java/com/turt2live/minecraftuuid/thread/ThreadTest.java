package com.turt2live.minecraftuuid.thread;

import com.turt2live.minecraftuuid.api.UUIDServiceProvider;

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ThreadTest implements Runnable {

    private static ConcurrentLinkedQueue<String> QUEUE = new ConcurrentLinkedQueue<String>();

    public static void main(String[] args) {
        final char[] valid = "abcdefghijklmnopqrstuvwxyz1234567890_".toCharArray();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 3; i < 17; i++) {
                    String start = generate(i);
                    QUEUE.add(start);
                    System.out.println(start);
                    while ((start = next(start)) != null) {
                        QUEUE.add(start);
                        System.out.println(start);
                    }
                    while (QUEUE.size() > 100000) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }

            private String generate(int i) {
                String s = "";
                for (int x = 0; x < i; x++) {
                    s += valid[0];
                }
                return s;
            }

            private String next(String s) {
                char[] chars = s.toCharArray();
                boolean made = false;
                for (int i = chars.length - 1; i >= 0; i--) {
                    if (chars[i] != valid[valid.length - 1]) {
                        chars[i] = valid[indexOf(valid, chars[i]) + 1];
                        made = true;
                        for (int o = chars.length - 1; o > i; o--) {
                            chars[o] = valid[0];
                        }
                        break;
                    }
                }
                if (!made) return null;
                return new String(chars);
            }

            private int indexOf(char[] chars, char c) {
                for (int i = 0; i < chars.length; i++) {
                    if (chars[i] == c) {
                        return i;
                    }
                }
                return -1;
            }
        }).start();

        for (int i = 0; i < 5; i++) {
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
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
    }
}
