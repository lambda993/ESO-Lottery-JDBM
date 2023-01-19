package com.lambda.core.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import com.lambda.core.Settings;

public class FileManager {

	private boolean detectedErrors;

	private Map<String, Double> partecipants;

	private HistoryManager history;

	public FileManager() {

		history = new HistoryManager();

		partecipants = new HashMap<String, Double>();

		checkData();

		readData();

	}

	private void checkData() {

		Connection conn = SQLManager.connectToDatabase();

		if (conn == null) {

			JOptionPane.showMessageDialog(null, "Could not access the database!",
					"Database Error", JOptionPane.ERROR_MESSAGE);

			detectedErrors = true;

		} else {

			String[] tables = { "settings", "players" };
			String[] columns = { "settings name", "settings value", "players name", "players tickets" };
			String[] dataTypes = { "varchar(255)", "float(53)", "varchar(255)", "float(53)" };
			String[] primaryKeys = { "name", "name" };

			SQLManager.generateTables(conn, tables, columns, dataTypes, primaryKeys);

		}

	}

	private void getSettings(Connection connection) {

		String[] settingsParameters = { Settings.guildCutName, Settings.ticketCostName,
				Settings.firstWinnerCutName, Settings.secondWinnerCutName, Settings.donationsName };

		double[] orderedValues = SQLManager.retrieveSettingsValuesFromDB(connection, "settings",
		 settingsParameters, "name", "value");

		Settings.guildCut = (orderedValues[0] == -1 ? Settings.guildCut : orderedValues[0]);
		Settings.ticketCost = (orderedValues[1] == -1 ? Settings.ticketCost : orderedValues[1]);
		Settings.firstWinnerCut = (orderedValues[2] == -1 ? Settings.firstWinnerCut : orderedValues[2]);
		Settings.secondWinnerCut = (orderedValues[3] == -1 ? Settings.secondWinnerCut : orderedValues[3]);
		Settings.donations = (orderedValues[4] == -1 ? Settings.donations : orderedValues[4]);

	}

	private void readData() {

		Connection conn = SQLManager.connectToDatabase();

		getSettings(conn);

		SQLManager.getPlayers(conn, "players", "name", "tickets", this);

	}

	public void addPartecipant(String name, double tickets) {

		if (partecipants.containsKey(name)) {

			double current = partecipants.get(name);
			partecipants.put(name, current + tickets);

		} else {

			partecipants.put(name, tickets);

		}

	}

	public void editPartecipant(String name, double tickets) {

		if (partecipants.containsKey(name)) {

			partecipants.remove(name);
			partecipants.put(name, tickets);

		}

	}

	public void saveData() {

		Connection conn = SQLManager.connectToDatabase();
		String[] settings = {Settings.guildCutName, Settings.ticketCostName, Settings.firstWinnerCutName,
		Settings.secondWinnerCutName, Settings.donationsName};
		double[] settingVals = {Settings.guildCut, Settings.ticketCost, Settings.firstWinnerCut,
		Settings.secondWinnerCut, Settings.donations};

		SQLManager.saveSettings(conn, "settings", "name", "value", settings, settingVals);

		SQLManager.savePlayers(conn, partecipants, "players", "name", "tickets");

	}

	public boolean hasErrors() {

		return detectedErrors;

	}

	public Map<String, Double> getPartecipants() {

		return partecipants;

	}

	public void addPartecipantsFromFile(File file) {

		String fileData = "";
		boolean skipped = false;

		try {

			Scanner sc = new Scanner(file);
			sc.useDelimiter("\\Z");

			fileData = sc.next();

			sc.close();

		} catch (FileNotFoundException e) {

			JOptionPane.showMessageDialog(null, String.format("Could not read file %s.", file.getName()),
					"File Not Found", JOptionPane.ERROR_MESSAGE);

			detectedErrors = true;

		}

		StringTokenizer st = new StringTokenizer(fileData);

		while (st.hasMoreTokens()) {

			String username = st.nextToken();
			String tickets = st.nextToken();

			if (username != null && noSpecialCharacters(username) && tickets != null) {

				if (isNumber(tickets)) {

					double ticket = Double.valueOf(tickets);
					addPartecipant(username, ticket);
					history.addAdditionOperation(username, ticket);

				} else {

					skipped = true;

				}

			} else {

				skipped = true;

			}

		}

		if (skipped) {

			JOptionPane.showMessageDialog(null, "Invalid line format detected, some entries have been skipped!",
					"Entry Skip Warning", JOptionPane.WARNING_MESSAGE);

		}

	}

	private boolean noSpecialCharacters(String s) {

		for(int i = 0; i < s.length(); i++){

			if(isSpecialCharacter(s.charAt(i))){

				return false;

			}

		}

		return true;
	}

	private boolean isSpecialCharacter(char c) {

		boolean NAN = c < '0' || c > '9';
		boolean NLC = c < 'a' || c > 'z';
		boolean NUC = c < 'A' || c > 'Z';

		return NAN && NLC && NUC;

	}

	private boolean isNumber(String number) {

		for (int i = 0; i < number.length(); i++) {

			if (!(number.charAt(i) >= '0' && number.charAt(i) <= '9')) {

				return false;

			}

		}

		return true;

	}

	public HistoryManager getHistoryManager() {

		return history;

	}

}
