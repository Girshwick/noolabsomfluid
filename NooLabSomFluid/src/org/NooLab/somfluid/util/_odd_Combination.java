package org.NooLab.somfluid.util;

import java.io.*;

public class _odd_Combination {
	static boolean __DEBUG = true;

	int m_combination[];
	int m_guesses[];

	int m_numNumbers;

	// //////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////
	_odd_Combination(int numNumbers) {
		if (numNumbers < 0)
			numNumbers = 4; // Default.

		m_numNumbers = numNumbers;

		m_combination = new int[m_numNumbers];
		m_guesses = new int[m_numNumbers];
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////
	//
	// API methods.
	//
	// /////
	public void pickCombination() {
		for (int i = 0; i < m_numNumbers; i++) {
			m_combination[i] = (int) ((Math.random() * 9) + 1);
		}
	}

	public void getUsersInput() {
		for (int i = 0; i < m_numNumbers; i++) {
			pickNumber(i);
		}
	}

	public boolean checkCombination() {
		int numCorrectNumbers = getNumCorrectNumbers();
		int numCorrectPositions = getNumCorrectPositions();

		System.out.println("The number of correct numbers is:   "
				+ numCorrectNumbers);
		System.out.println("The number of correct positions is: "
				+ numCorrectPositions);

		return (numCorrectPositions == m_numNumbers);
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper methods
	//
	// /////
	private void pickNumber(int index) {
		BufferedReader keyboard = new BufferedReader(new InputStreamReader(
				System.in));

		System.out.print("Enter guess " + (index + 1) + ": ");

		try {
			boolean ok = false;

			do {
				String line = keyboard.readLine();

				m_guesses[index] = Integer.parseInt(line);

				if (m_guesses[index] < 0 || m_guesses[index] > 9) {
					System.out.println("Enter guess " + (index + 1) + ":");
				} else if (m_guesses[index] == 0) {
					System.exit(0);
				} else
					ok = true;

			} while (!ok);
		} catch (IOException e1) {
			System.out.println("IOException occurred. Aborting.");
			System.exit(0); // TODO: Ask user for proper input.
		} catch (NumberFormatException e2) {
			System.out.println("Number Format Exception occurred. Aborting.");
			System.exit(0); // TODO: Ask user for proper input.

		}
	}

	private int getNumCorrectNumbers() {
		int num = 0;

		int[] g = (int[]) m_guesses.clone();

		for (int i = 0; i < m_numNumbers; i++) {
			for (int j = 0; j < m_numNumbers; j++) {
				if (g[j] == m_combination[i]) {
					num++;
					g[j] = -1;
					break;
				}
			}
		}
		return num;
	}

	private int getNumCorrectPositions() {
		int num = 0;

		for (int i = 0; i < m_numNumbers; i++) {
			if (m_guesses[i] == m_combination[i])
				num++;
		}
		return num;
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Main method - gets the ball rolling.
	//
	// /////
	public static void main(String[] args) {
		_odd_Combination c = new _odd_Combination(4);

		c.pickCombination();

		do {
			c.getUsersInput();

		} while (!c.checkCombination());
	}
}
