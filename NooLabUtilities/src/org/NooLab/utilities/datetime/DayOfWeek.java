package org.NooLab.utilities.datetime;

import java.util.*;


public class DayOfWeek {
    /**
     * A demo implementation of 'Zeller's Congruence' for the Gregorian Calendar
     * See http://en.wikipedia.org/wiki/Zeller's_congruence
     *
     * @param args (Not used)
     */
    final static String[] DAYS_OF_WEEK = {
            "Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday",
            "Friday"
        };

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.print("Enter the date in dd/mm/yyyy form: ");

        String[] atoms = input.nextLine().split("/");
        int q = Integer.parseInt(atoms[0]);
        int m = Integer.parseInt(atoms[1]);
        int y = Integer.parseInt(atoms[2]);

        if (m < 3) {
            m += 12;
            y -= 1;
        }

        int k = y % 100;
        int j = y / 100;

        int day = ((q + (((m + 1) * 26) / 10) + k + (k / 4) + (j / 4)) +
            (5 * j)) % 7;

        System.out.println("That date was a " + DAYS_OF_WEEK[day] + ".");
    }
}

