package com.talv.icytower.firebase;

import java.util.regex.Pattern;

public class AuthVerifier {

    private static final Pattern emailPattern = Pattern.compile("^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$");
    private static final Pattern userPattern = Pattern.compile("^[A-Za-z]\\w{3,29}$");
    private static final Pattern passPattern = Pattern.compile("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{5,20}$");


    public static boolean isValidEmailAddress(String email) {
        return patternMatches(email, emailPattern);
    }

    public static boolean isValidUsername(String username) {
        return patternMatches(username, userPattern);
    }

    public static boolean isValidPassword(String password) {
        return patternMatches(password, passPattern);
    }

    public static boolean patternMatches(String str, Pattern pattern) {
        return pattern.matcher(str).matches();
    }
}
