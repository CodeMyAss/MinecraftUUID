package com.turt2live.minecraftuuid;

import com.turt2live.minecraftuuid.api.UUIDServiceProvider;
import org.apache.commons.cli.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

public class Main {

    private static boolean append = false;

    public static void main(String[] args) {
        if (args.length <= 0) args = new String[]{"help"};

        Options options = new Options();
        options.addOption(OptionBuilder.withArgName("uuid").hasArg().withDescription("Gets the UUID for the specified name").create("uuid"));
        options.addOption(OptionBuilder.withArgName("name").hasArg().withDescription("Gets the name for the specified UUID").create("name"));
        options.addOption(OptionBuilder.withArgName("file").hasArg().withDescription("Exports the results to a specified file").create("export"));
        options.addOption(OptionBuilder.withArgName("uuid").hasArg().withDescription("Gets the history of a player's UUID").create("history"));
        options.addOption(new Option("file", "Parses the argument (such as UUID or player name) as a file (default false)"));
        options.addOption(new Option("append", "The output file will use append instead of overwrite (default false)"));

        CommandLineParser parser = new BasicParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            String uuid = null;
            String name = null;
            String history = null;
            String export = null;
            boolean isFile = false;

            if (cmd.hasOption("file")) {
                isFile = true;
            }
            if (cmd.hasOption("append")) {
                append = true;
            }

            if (cmd.hasOption("uuid") && cmd.getOptionValue("uuid") != null) {
                name = cmd.getOptionValue("uuid");
            }
            if (cmd.hasOption("name") && cmd.getOptionValue("name") != null) {
                uuid = cmd.getOptionValue("name");
            }
            if (cmd.hasOption("history") && cmd.getOptionValue("history") != null) {
                history = cmd.getOptionValue("history");
            }
            if (cmd.hasOption("export") && cmd.getOptionValue("export") != null) {
                export = cmd.getOptionValue("export");
            }

            if (name == null && uuid == null && history == null) {
                HelpFormatter help = new HelpFormatter();
                help.printHelp("java -jar " + new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName() + " [options]", options);
            } else {
                if (name != null) {
                    if (isFile) {
                        parseUUID(new File(name), export);
                    } else {
                        System.out.println("Parsing " + name + "...");
                        UUID uid = UUIDServiceProvider.getUUID(name);
                        String result = uid == null ? "Unknown" : uid.toString().replace("-", "");
                        System.out.println(name + " = " + result);
                        if (export != null) {
                            export(name, result, export);
                        }
                    }
                }
                if (uuid != null) {
                    if (isFile) {
                        parseName(new File(uuid), export);
                    } else {
                        System.out.println("Parsing " + uuid + "...");
                        try {
                            String result = UUIDServiceProvider.getName(UUID.fromString(UUIDServiceProvider.insertDashes(uuid)));
                            System.out.println(uuid + " = " + result);
                            if (export != null) {
                                export(result, uuid, export);
                            }
                        } catch (IllegalArgumentException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }
                if (history != null) {
                    if (isFile) {
                        parseHistory(new File(history), export);
                    } else {
                        System.out.println("Getting history for " + history + "...");
                        try {
                            Map<String, Date> result = UUIDServiceProvider.getHistory(UUID.fromString(UUIDServiceProvider.insertDashes(history)));
                            try {
                                BufferedWriter writer = null;
                                if (export != null) {
                                    writer = new BufferedWriter(new FileWriter(new File(export), true));
                                }
                                if (result != null && result.size() > 0) {
                                    for (Map.Entry<String, Date> alias : result.entrySet()) {
                                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                                        format.setTimeZone(TimeZone.getTimeZone("UTC"));
                                        String date = format.format(alias.getValue()) + " UTC";

                                        System.out.println(alias.getKey() + " (Last Used: " + date + ")");
                                        if (writer != null) {
                                            writer.write(history + "," + alias.getKey() + "," + date);
                                            writer.newLine();
                                        }
                                    }
                                } else {
                                    System.out.println("No history.");
                                }
                                if (writer != null) {
                                    writer.flush();
                                    writer.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } catch (IllegalArgumentException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }
            }
        } catch (
                ParseException e
                )

        {
            System.err.println("Could not parse command line: " + e.getMessage());
            e.printStackTrace();
        }

    }

    private static void parseHistory(File file, String out) {
        try {
            BufferedWriter writer = null;
            if (out != null) {
                writer = new BufferedWriter(new FileWriter(new File(out), true));
            }

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    System.out.println("Getting history for " + line + "...");
                    Map<String, Date> result = UUIDServiceProvider.getHistory(UUID.fromString(UUIDServiceProvider.insertDashes(line)));
                    if (result != null && result.size() > 0) {
                        for (Map.Entry<String, Date> alias : result.entrySet()) {
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                            format.setTimeZone(TimeZone.getTimeZone("UTC"));
                            String date = format.format(alias.getValue()) + " UTC";

                            System.out.println("\t" + alias.getKey() + " (Last Used: " + date + ")");
                            if (writer != null) {
                                writer.write(line + "," + alias.getKey() + "," + date);
                                writer.newLine();
                            }
                        }
                    } else {
                        System.out.println("No history.");
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                }
            }
            reader.close();

            if (writer != null) {
                writer.flush();
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void parseName(File file, String out) {
        try {
            BufferedWriter writer = null;
            if (out != null) {
                File f = new File(out);
                writer = new BufferedWriter(new FileWriter(f, append));
            }

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Parsing " + line + "...");
                try {
                    String result = UUIDServiceProvider.getName(UUID.fromString(UUIDServiceProvider.insertDashes(line)));
                    System.out.println(line + " = " + result);

                    if (writer != null) {
                        export(result, line, writer);
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                }
            }
            reader.close();

            if (writer != null) {
                writer.flush();
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void parseUUID(File file, String out) {
        try {
            BufferedWriter writer = null;
            if (out != null) {
                File f = new File(out);
                writer = new BufferedWriter(new FileWriter(f, append));
            }

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Parsing " + line + "...");
                UUID uuid = UUIDServiceProvider.getUUID(line);
                String result = uuid == null ? "Unknown" : uuid.toString().replace("-", "");
                System.out.println(line + " = " + result);
                if (writer != null) {
                    export(line, result, writer);
                }
            }
            reader.close();

            if (writer != null) {
                writer.flush();
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void export(String name, String uuid, String file) {
        try {
            File f = new File(file);
            BufferedWriter writer = new BufferedWriter(new FileWriter(f, append));
            export(name, uuid, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void export(String name, String uuid, BufferedWriter stream) throws IOException {
        stream.write(name + "," + uuid);
        stream.newLine();
    }

}
