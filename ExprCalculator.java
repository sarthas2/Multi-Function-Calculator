package eecs40;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

public class ExprCalculator implements CalculatorInterface {
    String output = "0";
    String errorCode = "";
    String equation = "";
    String tempCheck = "";

    @Override
    public void acceptInput(String s) {
        // Make sure that there are no leading or trailing whitespaces (could result in error)
        s = s.trim();
        tempCheck = tempCheck.trim();
        output = output.trim();

        if (output.endsWith(".0")) {
            output = output.substring(0, output.length() - 2);
        }

        tempCheck = output + s;

        if (s.equals("Backspace") || s.equals("BS")) { // Backspace
            output = output.substring(0, output.length() - 1);
            errorCode = "";
        } else if (s.equals("C")) { //Clear input
            output = "0";
            errorCode = "";
        } else if ((tempCheck.contains("/0") && !tempCheck.contains("/0.")) && tempCheck.contains("=")) { // div by 0 error
            output = tempCheck.substring(0, tempCheck.length() - 1);
            errorCode = "NaN";
        } else if ((s.equals(".") && output.endsWith(".")) || (tempCheck.contains(".."))) { // ignore multiple consequent decimal points
            // do nothing
            if (errorCode.isEmpty()) {
                output = output + s;
                errorCode = "Error";
            }
        } else if (tempCheck.contains(".0.")) { // Random formatting error
            tempCheck = tempCheck.replace(".0.", ".");
            output = tempCheck;
        } else if ( // consecutive operator error handling
                (s.equals("+") && (output.endsWith("+") || output.endsWith("*") || output.endsWith("/") || output.endsWith("=")))
                        || (s.equals("*") && (output.endsWith("+") || output.endsWith("*") || output.endsWith("/") || output.endsWith("=")))
                        || (s.equals("/") && (output.endsWith("+") || output.endsWith("*") || output.endsWith("/") || output.endsWith("=")))
                        || (s.equals("-") && (output.endsWith("+") || output.endsWith("*") || output.endsWith("/") || output.endsWith("=")))
                        || (output.endsWith(".") && (s.equals("+") || s.equals("*") || s.equals("/") || s.equals("=")))
        ) {
            errorCode = "Error";
        } else if (s.equals("-")) { // deal with negative inputs
            if (output.endsWith("+") || output.endsWith("*") || output.endsWith("-") || output.endsWith("/")) {
                output = output + "n";
            } else if (output.equals("0") || output.length() == 1) {
                output = output + "+n";
            } else {
                output = output + "-";
            }
        } else if (s.equals("=")) { // calculate answer
            if (errorCode.equals("")) {
                if ((
                        !output.contains("+") && !output.contains("*") && !output.contains("-") && !output.contains("/") && !output.contains("^") &&
                                !output.contains("cos") && !output.contains("sin") && !output.contains("tan") && !output.contains("log") && !output.contains("fac") &&
                                !output.contains("sqrt") && !output.contains("ln") && !output.contains("mod"))
                ) {
                    output = output;
                } else {
                    calculate();
                }
            }
        } else if (output.equals("0")) { // initial calculation input
            if ((s.equals("+"))) {
                output = "0";
            } else if (s.startsWith("(")) {
                output = s;
            } else {
                output = "0" + s;
            }
        } else {   // default case: append input to calculation
            output = output + s;
            errorCode = "";
        }
    }

    private int getWeight(String in) { // Order of Operations Weights
        // Order: % -> ^ -> * / -> + -
        int weight = -1;
        if (in.contains("+") || in.contains("-")) {
            weight = 1;
        } else if (in.contains("*") || in.contains("/")) {
            weight = 2;
        } else if (in.contains("^")) {
            weight = 3;
        } else if (in.contains("cos") || in.contains("sin") || in.contains("tan") || in.contains("log") || in.contains("fac") || in.contains("ln") || in.contains("sqrt")) {
            weight = 4;
        } else if (in.contains("mod")) {
            weight = 5;
        } else {
            weight = -1;
        }
        return weight;
    }

    private void calculate() { // Tokenize answer into postfix form
        //output = output;
        if (output.startsWith("0")) {
            output = output.substring(1);
        }
        int openCount = 0;
        int closeCount = 0;

        for (int i = 0; i < output.length(); i++) {
            char c = output.charAt(i);

            if (Character.compare(c, '(') == 0) {
                openCount++;
            } else if (Character.compare(c, ')') == 0) {
                closeCount++;
            }
            if (i < output.length() - 1) {
                char c1 = output.charAt(i + 1);
                if (!Character.isDigit(output.charAt(i)) && !Character.isDigit(output.charAt(i + 1))) { // Check for repeating operators
                    if (
                            Character.compare(c, ')') == 0 ||
                                    (!Character.isDigit(c) && Character.compare(c, '.') != 0 && Character.isAlphabetic(c1))
                    ) {
                        continue;
                    }
                    if (Character.compare(c1, '(') == 0) {
                        if (Character.isAlphabetic(c)) {
                            continue;
                        }
                    } else if (Character.compare(c, '(') == 0) {
                        if (Character.isAlphabetic(c1)) {
                            continue;
                        }
                    } else {
                        errorCode = "Error";
                        return;
                    }
                }
            }
        }

        if (openCount != closeCount) { // Check for Missing Parenthesis
            output = output;
            errorCode = "Error: Missing Parenthesis";
            return;
        }

        /*
         * Postfix Algorithm:
         *
         * Parse Through Equation
         * If Number, add to output equation (num)
         * If opening Parenthesis, put on of stack
         * If closing Parenthesis, add all operands to (num) from stack until you reach an opening parenthesis
         * If Operator, add to stack but add all operators from the stack that have a higher weight than the current one
         * Repeat above, until the end of the equation
         * At the end add all remaining operators to (num)
         *
         * */

        Stack<String> answerTokens = new Stack<String>(); // Postfix conversion
        String num = "";
        for (int i = 0; i < output.length(); i++) {
            char c = output.charAt(i);
            String exprOp = "";
            if (
                    Character.isDigit(c) || (Character.compare(c, '-') == 0 && (i == 0 || (!Character.isDigit(output.charAt(i - 1)) && ((Character.compare(output.charAt(i - 1), ')') == 1) || (Character.compare(output.charAt(i - 1), ')') == 1)))))
                            || Character.compare(c, '.') == 0
            ) {
                num += (c + "");
            }
            else if (Character.compare(c, '(') == 0) {
                answerTokens.push(c + "");
            }
            else if (Character.compare(c, ')') == 0) {
                String toAdd = answerTokens.peek();
                if (toAdd.equals("(")) {
                    answerTokens.pop();
                }
                toAdd = answerTokens.peek();
                while (!toAdd.equals("(")) {
                    if (!answerTokens.empty()) {
                        toAdd = answerTokens.peek();
                        if (!toAdd.equals("(")) {
                            toAdd = answerTokens.pop();
                            num += (" " + toAdd);
                        }
                        else if (toAdd.equals("(")) {
                            answerTokens.pop();
                        }
                    }
                    else
                        break;
                }
            }
            else {
                if (Character.compare(output.charAt(i), 'l') == 0 && Character.compare(output.charAt(i + 1), 'n') == 0) {
                    exprOp = "ln";
                    i += 1;
                }
                else if (
                        (Character.compare(output.charAt(i), 'l') == 0 && Character.compare(output.charAt(i + 1), 'o') == 0 && Character.compare(output.charAt(i + 2), 'g') == 0) ||
                                (Character.compare(output.charAt(i), 's') == 0 && Character.compare(output.charAt(i + 1), 'i') == 0 && Character.compare(output.charAt(i + 2), 'n') == 0) ||
                                (Character.compare(output.charAt(i), 'c') == 0 && Character.compare(output.charAt(i + 1), 'o') == 0 && Character.compare(output.charAt(i + 2), 's') == 0) ||
                                (Character.compare(output.charAt(i), 't') == 0 && Character.compare(output.charAt(i + 1), 'a') == 0 && Character.compare(output.charAt(i + 2), 'n') == 0) ||
                                (Character.compare(output.charAt(i), 'f') == 0 && Character.compare(output.charAt(i + 1), 'a') == 0 && Character.compare(output.charAt(i + 2), 'c') == 0) ||
                                (Character.compare(output.charAt(i), 'm') == 0 && Character.compare(output.charAt(i + 1), 'o') == 0 && Character.compare(output.charAt(i + 2), 'd') == 0)
                ) {
                    exprOp = "" + output.charAt(i) + output.charAt(i + 1) + output.charAt(i + 2);
                    i += 2;
                }
                else if ((Character.compare(output.charAt(i), 's') == 0 && Character.compare(output.charAt(i + 1), 'q') == 0 && Character.compare(output.charAt(i + 2), 'r') == 0 && Character.compare(output.charAt(i + 3), 't') == 0)) {
                    exprOp = "sqrt";
                    i += 3;
                }

                if (answerTokens.empty())
                    if (exprOp.isEmpty()) {
                        answerTokens.push(c + " ");
                    } else {
                        answerTokens.push(exprOp + " ");
                    }

                else {
                    String toAdd = answerTokens.peek();
                    if (exprOp.isEmpty()) {
                        exprOp = c + "";
                    }
                    if (!toAdd.equals("(")) {
                        while ((getWeight(exprOp) <= getWeight(toAdd)) && !toAdd.equals("(")) {
                            if (!answerTokens.empty()) {
                                toAdd = answerTokens.peek();
                                if ((getWeight(exprOp) <= getWeight(toAdd)) && !toAdd.equals("(")) {
                                    toAdd = answerTokens.pop();
                                    num += (" " + toAdd);
                                }
                            }
                            else
                                break;
                        }
                    }
                    answerTokens.push(" " + exprOp);
                }
                num += " ";
            }
        }

        String toAdd;

        if (!answerTokens.empty())
            toAdd = answerTokens.peek();

        while (!answerTokens.empty()) {
            if (!answerTokens.empty()) {
                toAdd = answerTokens.pop();
                if (!toAdd.equals("("))
                    num += (" " + toAdd);
            }
        }

        equation = num; // Set Equation equal to postfix output
        output = solve(equation);
    }

    private String solve(String input) {
        String answer = "";
        ArrayList<Double> ops = new ArrayList<Double>();
        ArrayList<String> answerTokens = new ArrayList<String>(Arrays.asList(input.split(" ")));
        while (answerTokens.contains(""))
            answerTokens.remove("");
        for (int i = 0; i < answerTokens.size(); i++) {
            String temp = answerTokens.get(i);
            switch (temp) { // solve from left to right and simplifying postfix equation
                case "+":
                    ops.add(Double.parseDouble(answerTokens.get(i - 2)));
                    ops.add(Double.parseDouble(answerTokens.get(i - 1)));
                    answerTokens.set(i, (ops.get(ops.size() - 2) + ops.get(ops.size() - 1)) + "");
                    answerTokens.remove(i - 1);
                    i -= 1;
                    answerTokens.remove(i - 1);
                    i -= 1;
                    i -= 1;
                    break;
                case "-":
                    ops.add(Double.parseDouble(answerTokens.get(i - 2)));
                    ops.add(Double.parseDouble(answerTokens.get(i - 1)));
                    answerTokens.set(i, (ops.get(ops.size() - 2) - ops.get(ops.size() - 1)) + "");
                    answerTokens.remove(i - 1);
                    i -= 1;
                    answerTokens.remove(i - 1);
                    i -= 1;
                    i -= 1;
                    break;
                case "*":
                    ops.add(Double.parseDouble(answerTokens.get(i - 2)));
                    ops.add(Double.parseDouble(answerTokens.get(i - 1)));
                    answerTokens.set(i, (ops.get(ops.size() - 2) * ops.get(ops.size() - 1)) + "");
                    answerTokens.remove(i - 1);
                    i -= 1;
                    answerTokens.remove(i - 1);
                    i -= 1;
                    i -= 1;
                    break;
                case "/":
                    ops.add(Double.parseDouble(answerTokens.get(i - 2)));
                    ops.add(Double.parseDouble(answerTokens.get(i - 1)));
                    answerTokens.set(i, (ops.get(ops.size() - 2) / ops.get(ops.size() - 1)) + "");
                    answerTokens.remove(i - 1);
                    i -= 1;
                    answerTokens.remove(i - 1);
                    i -= 1;
                    i -= 1;
                    break;
                case "^":
                    ops.add(Double.parseDouble(answerTokens.get(i - 2)));
                    ops.add(Double.parseDouble(answerTokens.get(i - 1)));
                    answerTokens.set(i, (Math.pow(ops.get(ops.size() - 2), ops.get(ops.size() - 1))) + "");
                    answerTokens.remove(i - 1);
                    i -= 1;
                    answerTokens.remove(i - 1);
                    i -= 1;
                    i -= 1;
                    break;
                case "mod":
                    ops.add(Double.parseDouble(answerTokens.get(i - 2)));
                    ops.add(Double.parseDouble(answerTokens.get(i - 1)));
                    answerTokens.set(i, (ops.get(ops.size() - 2) % ops.get(ops.size() - 1)) + "");
                    answerTokens.remove(i - 1);
                    i -= 1;
                    answerTokens.remove(i - 1);
                    i -= 1;
                    i -= 1;
                    break;
                case "sin":
                    ops.add(Double.parseDouble(answerTokens.get(i - 1)));
                    answerTokens.set(i, (ops.get(ops.size() - 1)) + "");
                    answerTokens.remove(i - 1);
                    i = -1;
                    break;
                case "cos":
                    ops.add(Double.parseDouble(answerTokens.get(i - 1)));
                    answerTokens.set(i, (Math.cos(ops.get(ops.size() - 1))) + "");
                    answerTokens.remove(i - 1);
                    i = -1;
                    break;
                case "tan":
                    ops.add(Double.parseDouble(answerTokens.get(i - 1)));
                    answerTokens.set(i, (Math.tan(ops.get(ops.size() - 1))) + "");
                    answerTokens.remove(i - 1);
                    i = -1;
                    break;
                case "ln":
                    ops.add(Double.parseDouble(answerTokens.get(i - 1)));
                    answerTokens.set(i, (Math.log(ops.get(ops.size() - 1))) + "");
                    answerTokens.remove(i - 1);
                    i = -1;
                    break;
                case "log":
                    ops.add(Double.parseDouble(answerTokens.get(i - 1)));
                    answerTokens.set(i, (Math.log10(ops.get(ops.size() - 1))) + "");
                    answerTokens.remove(i - 1);
                    i = -1;
                    break;
                case "sqrt":
                    ops.add(Double.parseDouble(answerTokens.get(i - 1)));
                    answerTokens.set(i, (Math.sqrt(ops.get(ops.size() - 1))) + "");
                    answerTokens.remove(i - 1);
                    i = -1;
                    break;
                case "fac":
                    ops.add(Double.parseDouble(answerTokens.get(i - 1)));
                    answerTokens.set(i, (factorial(ops.get(ops.size() - 1))) + "");
                    answerTokens.remove(i - 1);
                    i = -1;
                    break;
            }
        }
        return answerTokens.get(0); // return the calculated answer
    }

    private double factorial(double x) {
        double total = 1;
        if (x > 0) {
            for (int i = 1; i <= x; i++) {
                total = total * i;
            }
        }
        return total;
    }

    @Override
    public String getDisplayString() {
        if (!errorCode.isEmpty()) {
            return errorCode;
        }
        if (output.contains("n")) { // display negative number error handling
            String customOut = output.replaceAll("n", "-");
            if (customOut.startsWith("0+-")) {
                return ("-" + customOut.substring(3));
            }
        }
        if (output.startsWith("0") && output.length() > 1 && !output.startsWith("0.")) { // display formatting for initial calculation
            return output.substring(1);
        }
        return output; // default case: return output
    }
}