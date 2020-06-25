package readability;

import java.io.File;
import java.util.regex.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {

        double[] counts = getCountFromText(args[0]);

        System.out.println("Words: " + (long)counts[1]);
        System.out.println("Sentences: " + (long)counts[2]);
        System.out.println("Characters: " + (long)counts[0]);
        System.out.println("Syllables: " + (long)counts[3]);
        System.out.println("Polysyllables: " + (long)counts[4]);

        generateScore(counts);
    }

    private static void generateScore(double[] input) {

        long chars = (long)input[0];
        long words = (long)input[1];
        long sents = (long)input[2];
        long sylbs = (long)input[3];
        long psylbs = (long)input[4];
        double valL = input[5];
        double valS = input[6];

        Scanner in = new Scanner(System.in);
        System.out.print("Enter the score you want to calculate (ARI, FK, SMOG, CL, all): ");
        String choice = in.next();
        System.out.println();

        
        if ("ARI".equals(choice)) {
            generateARIScore(chars, words, sents);
        } else if ("FK".equals(choice)) {
            generateFKScore(words, sents, sylbs);
        } else if ("SMOG".equals(choice)) {
            generateSMOGScore(sents, psylbs);
        } else if ("CL".equals(choice)) {
            generateCLScore(valL, valS);
        } else if ("all".equals(choice)) {
            double avgAge = 0;
            avgAge += generateARIScore(chars, words, sents);
            avgAge += generateFKScore(words, sents, sylbs);
            avgAge += generateSMOGScore(sents, psylbs);
            avgAge += generateCLScore(valL, valS);
            System.out.println("\nThis text should be understood in average by "
                    + String.format("%1.2f", avgAge / 4) + " year olds.");
        }
        in.close();

    }

    private static int getAgeLevel(int score) {

        int ageLevel;
        switch (score) {
            case 1:
                ageLevel = 5;
                break;
            case 2:
                ageLevel = 6;
                break;
            case 3:
                ageLevel = 7;
                break;
            case 4:
                ageLevel = 9;
                break;
            case 5:
                ageLevel = 10;
                break;
            case 6:
                ageLevel = 11;
                break;
            case 7:
                ageLevel = 12;
                break;
            case 8:
                ageLevel = 13;
                break;
            case 9:
                ageLevel = 14;
                break;
            case 10:
                ageLevel = 15;
                break;
            case 11:
                ageLevel = 17;
                break;
            case 12:
                ageLevel = 18;
                break;
            case 13:
                ageLevel = 19;
                break;
            case 14:
                ageLevel = 24;
                break;
            default:
                ageLevel = -1;
        }

        return ageLevel;
    }

    private static int generateARIScore(long chars, long words, long sents) {

        double score = 4.71D * ((double) chars / (double) words) + 0.5D * ((double) words / (double) sents) - 21.43D;
        int age = getAgeLevel((int) Math.ceil(score));
        // System.out.println((int) Math.ceil(score));
        System.out.println(
                "Automated Readability Index: " + String.format("%1.2f", score) + " (about " + age + " year olds).");

        return age;
    }

    private static int generateFKScore(long words, long sents, long sylbs) {

        double score = (0.39 * (double) words / (double) sents) + (11.8 * (double) sylbs / (double) words) - 15.59;
        int age = getAgeLevel((int) Math.ceil(score));
        // System.out.println((int) Math.ceil(score));
        System.out.println("Flesch–Kincaid readability tests: " + String.format("%1.2f", score) + " (about " + age
                + " year olds).");

        return age;
    }

    private static int generateSMOGScore(long sents, long psylbs) {
        double score = 1.043D * Math.sqrt((double) psylbs * (30 / (double) sents)) + 3.1291D;
        int age = getAgeLevel((int) Math.ceil(score));
        // System.out.println((int) Math.ceil(score));
        System.out.println("Simple Measure of Gobbledygook: " + String.format("%1.2f", score) + " (about " + age + " year olds).");

        return age;
    }

    private static int generateCLScore(double valL, double valS) {
        double score = 0.0588 * valL - 0.296 * valS - 15.8;
        // System.out.println((int) Math.ceil(score));
        int age = getAgeLevel((int) Math.ceil(score));

        System.out.println("Coleman–Liau index: " + String.format("%1.2f", score) + " (about " + age + " year olds).");

        return age;
    }

    private static double[] getCountFromText(String filePath) {
        File file = new File(filePath);
        long chars = 0L, words = 0L, sents = 1L, sylbs = 0, psylbs = 0;
        List<String> text = new ArrayList<String>();
        double[] lsVal = new double[2];
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNext()) {
                String line = scanner.nextLine().toLowerCase();
                chars += charCount(line);
                words += wordCount(line);
                sents += sentsCount(line);
                sylbs += sylbsCount(line);
                psylbs += psylbsCount(line);
                text.add(line);
            }
            scanner.close();
            lsVal = cliCount(text);
        } catch (Exception e) {
            System.out.println(e.getClass().getSimpleName());
        }

        return new double[] { (double)chars, (double)words, (double)sents, 
            (double)sylbs, (double)psylbs, lsVal[0], lsVal[1] };
    }

    private static long charCount(String input) {
        Pattern pattern = Pattern.compile("\\S");
        return pattern.matcher(input).results().count();
    }

    private static long wordCount(String input) {
        Pattern pattern = Pattern.compile("[a-zA-Z0-9,]+");
        return pattern.matcher(input).results().count();
    }

    private static long sentsCount(String input) {
        Pattern pattern = Pattern.compile("\\w\\w+[\\.\\!\\?]\\s");
        return pattern.matcher(input).results().count();
    }

    private static long sylbsCount(String input) {

        Pattern pattern = Pattern.compile("([aiouy][aeiouy]*|e[aeiouy]*\\B)");
        Pattern wordPattern = Pattern.compile("[a-zA-Z]+");
        long count = 0L;
        String[] words = input.split(" ");

        for (String word : words) {
            long temp = pattern.matcher(word).results().count();
            if (temp != 0) {
                count += temp;
            } else if (wordPattern.matcher(word).results().count() == 1L) {
                count += 1L;
            }
        }

        return count;
    }

    private static long psylbsCount(String input) {
        Pattern pattern = Pattern.compile("([aiouy][aeiouy]*|e[aeiouy]*\\B)");
        long count = 0L;
        String[] words = input.split(" ");

        for (String word : words) {
            long temp = pattern.matcher(word).results().count();
            if (temp > 2) {
                count++;
            }
        }

        return count;
    }

    private static double[] cliCount(List<String> input) { // Coleman–Liau index

        Pattern charP = Pattern.compile("\\S");
        Pattern wordP = Pattern.compile("[a-zA-Z0-9,]+");
        Pattern sentsP = Pattern.compile("\\w\\w+[\\.\\!\\?]\\s");

        long countC = 0L, countW = 0L, countS = 0L;
        double valL = 0L, valS = 0L;
        for (int i = 0; i < input.size(); i++) {

            countC += charP.matcher(input.get(i)).results().count();
            countW += wordP.matcher(input.get(i)).results().count();
            countS += sentsP.matcher(input.get(i)).results().count();


        }
        valL = (double)countC / countW * 100;

        valS = ((double)countS + 1) / (double)countW * 100;
        // System.out.println(valL);
        // System.out.println(valS);
        return new double[] { valL, valS };
    }

}