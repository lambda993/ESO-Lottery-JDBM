package com.lambda.core.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class SQLManager {

    private static final String DBdriver = "org.mariadb.jdbc.Driver";

    private static final String DBurl = "jdbc:mariadb://localhost:3306/lottery";

    private static final String username = "root";

    private static final String password = "";

    public static Connection connectToDatabase() {

        try {

            Class.forName(DBdriver);

            Connection conn = DriverManager.getConnection(DBurl, username, password);
            System.out.println("Connection to database success!");

            return conn;

        } catch (Exception e) {

            e.printStackTrace();

        }

        return null;
    }

    public static void generateTables(Connection connection, String[] tables, String[] columns,
            String[] columnDataType, String[] primaryKeys) {

        if (tables.length != primaryKeys.length) {

            System.err.println("Invalid table/primary key amount");
            return;

        }

        if (columnDataType.length != columns.length) {

            System.err.println("Specified data type don't match the number of columns");
            return;

        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < tables.length; i++) {

            sb.append("CREATE TABLE IF NOT EXISTS " + tables[i] + "(");

            for (int j = 0; j < columns.length; j++) {

                StringTokenizer st = new StringTokenizer(columns[j]);
                String p1 = st.nextToken();
                String p2 = st.nextToken();

                if (p1.equals(tables[i])) {

                    sb.append(p2 + " " + columnDataType[j] + ", ");

                }

            }

            sb.append("PRIMARY KEY(" + primaryKeys[i] + "))");
            execQuery(connection, sb.toString());
            sb.setLength(0);

        }

    }

    private static void execQuery(Connection connection, String query) {

        try {

            PreparedStatement createTable = connection.prepareStatement(query);
            createTable.executeUpdate();

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            System.out.println("Table creation has been completed.");

        }

    }

    public static double[] retrieveSettingsValuesFromDB(Connection connection, String table,
            String[] settingsParameters, String key, String value) {

        double[] result = new double[settingsParameters.length];

        for (int i = 0; i < result.length; i++) {

            result[i] = -1;

        }

        try {

            String query = "SELECT * FROM " + table;
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet queryResultSet = statement.executeQuery();

            while (queryResultSet.next()) {

                for (int i = 0; i < settingsParameters.length; i++) {

                    if (queryResultSet.getString(key).equals(settingsParameters[i])) {

                        result[i] = queryResultSet.getDouble(value);

                    }

                }

            }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return result;

    }

    public static void getPlayers(Connection connection, String table, String key, String value,
            FileManager manager) {

        try {

            String query = "SELECT * FROM " + table;
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet result = statement.executeQuery();

            while (result.next()) {

                String playerName = result.getString(key);
                double playerTickets = result.getDouble(value);

                manager.addPartecipant(playerName, playerTickets);

            }

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    public static void savePlayers(Connection conn, Map<String, Double> partecipants, String table,
            String key, String value) {

        Set<String> players = partecipants.keySet();

        try {

            for (String s : players) {

                String savePlayerQuery = String.format("INSERT INTO players (%s, %s) " +
                        "VALUES('%s', %f) ON DUPLICATE KEY UPDATE %s=VALUES(%s), %s=VALUES(%s)",
                        key, value, s, partecipants.get(s), key, key, value, value);

                PreparedStatement statement = conn.prepareStatement(savePlayerQuery);
                statement.executeUpdate();

            }

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    public static void saveSettings(Connection conn, String table, String key, String value,
            String[] keys, double[] values) {

        try {

            for (int i = 0; i < keys.length; i++) {

                String setting = String.format("INSERT INTO %s(%s, %s) VALUES('%s', %f) " +
                        "ON DUPLICATE KEY UPDATE %s=VALUES(%s), %s=VALUES(%s)",
                        table, key, value, keys[i], values[i], key, key, value, value);

                PreparedStatement stat = conn.prepareStatement(setting);
                stat.executeUpdate();

            }

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    public static void clearTable(String table) {

        try {

            String query = "DELETE FROM " + table;
            PreparedStatement statement = connectToDatabase().prepareStatement(query);
            statement.executeUpdate();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    public static void deleteEntry(String table, String key, String name) {

        try {

            String query = String.format("DELETE FROM %s WHERE %s='%s'", table, key, name);
            PreparedStatement statement = connectToDatabase().prepareStatement(query);
            statement.executeUpdate();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

}
