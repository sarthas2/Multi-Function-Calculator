package eecs40;

public class Calculator implements CalculatorInterface{
    private String output = "0";

    @Override
    public void acceptInput(String s){

        if(output.equals("NaN") && !s.equals("C")){ // clear NaN input lock
            output = "NaN";
        }

        else if(output.equals("Error") && !s.equals("C")){ // clear Error input lock
            output = "Error";
        }

        else if(s.equals("0") && output.endsWith("/")){ // div by 0 error
            output = "NaN";
        }

        else if (s.equals("C")){ //Clear input
            output = "0";
        }

        else if( // consecutive operator error handling
                (s.equals("+") && (output.endsWith("+") || output.endsWith("*") || output.endsWith("/") || output.endsWith("=")) )
                        || (s.equals("*") && (output.endsWith("+") || output.endsWith("*") || output.endsWith("/") || output.endsWith("=")))
                        || (s.equals("/") && (output.endsWith("+") || output.endsWith("*") || output.endsWith("/") || output.endsWith("=")))
                        || (s.equals("-") && (output.endsWith("+") || output.endsWith("*") || output.endsWith("/") || output.endsWith("=")))
                        || (output.endsWith(".") && (s.equals("+") || s.equals("*") || s.equals("/") || s.equals("=")))
        ){
            output = "Error";
        }

        else if(s.equals("-")){ // deal with negative inputs
            if(output.endsWith("+") || output.endsWith("*") || output.endsWith("-") || output.endsWith("/")){
                output = output + "n";
            }
            else if (output.equals("0") || output.length()  == 1){
                output = output  + "+n";
            }
            else{
                output = output  + "-";
            }
        }

        else if(s.equals("=")){ // calculate answer
            if((!output.contains("+") && !output.contains("*") && !output.contains("-") && !output.contains("/"))){
                output = output;
            }
            else {
                calculate();
            }
        }

        else if (output.contains("c")){ // error handling for using the answer of last calculation as input
            if((!s.contains("+") && !s.contains("*") && !s.contains("-") && !s.contains("/"))){
                if(!s.equals("=")){
                    output = s;
                }
                else {
                    output = output.substring(0, output.length() - 1);
                }
            }
            else {
                output = output.substring(0, output.length() - 1) + s;
            }
        }

        else if(s.equals(".") && output.endsWith(".")){ // ignore multiple consequent decimal points
            // do nothing
            output = output;
        }

        else if(output.equals("0")) { // initial calculation input
            if((s.equals("+"))){
                output = "0";
            }
            else {
                output = "0" + s;
            }
        }

        else{   // default case: append input to calculation
            output = output + s;
        }
    }

    private void calculate(){
        String[] div;
        String[] mult;
        String[] add;
        String[] sub;

        //Method: Split up input based on order of operations (/ -> * -> - -> +)
        add = output.split("\\+"); // split for addition
        for(int a = 0; a < add.length; a++){
            sub = add[a].split("-"); // split for subtraction
            for(int s = 0; s < sub.length; s++){
                if(sub[s].startsWith("n")){
                    sub[s] = "-" + (sub[s].substring(1));
                }
                mult = sub[s].split("\\*"); // split for multiplication
                for (int m = 0; m < mult.length; m++){
                    if(mult[m].startsWith("n")){
                        mult[m] = "-" + (mult[m].substring(1));
                    }
                    div = mult[m].split("/"); // split for division
                    for (int d = 0; d < div.length; d++){
                        // division loop
                        if(div[d].startsWith("n")){
                            div[d] = "-" + (div[d].substring(1));
                        }

                        float tempD;
                        if(d != 0) { // calculate division
                            tempD = Float.parseFloat(div[0]) / Float.parseFloat(div[d]);
                            div[0] = Float.toString(tempD);
                        }
                    }
                    // mult loop
                    mult[m] = div[0];

                    float tempM;
                    if(m != 0) { // calculate multiplication
                        tempM = Float.parseFloat(mult[0]) * Float.parseFloat(mult[m]);
                        mult[0] = Float.toString(tempM);
                    }
                }
                // sub loop
                sub[s] = mult[0];

                float tempS;
                if(s != 0) { // calculate subtraction
                    tempS = Float.parseFloat(sub[0]) - Float.parseFloat(sub[s]);
                    sub[0] = Float.toString(tempS);
                }
            }
            // add loop
            add[a] = sub[0];

            float tempA;
            if(a != 0) { // calculate addition
                tempA = Float.parseFloat(add[0]) +  Float.parseFloat(add[a]);
                add[0] = Float.toString(tempA);
            }
        }

        output = add[0] + "c"; // set output to calculation result
    }

    @Override
    public String getDisplayString(){
        if (output.contains("n")){ // display negative number error handling
            String customOut = output.replaceAll("n", "-");
            if(customOut.startsWith("0+-")) {
                return ("-" + customOut.substring(3));
            }
        }
        if(output.equals(".0c")){ // deal with edge case of displaying ".0" as answer
            return "0";
        }
        else if (output.endsWith("c")){ // display formatting for calculation result
            return (output.substring(0, output.length() - 1));
        }
        else if(output.startsWith("0") && output.length() > 1){ // display formatting for initial calculation
            return output.substring(1);
        }
        return output; // default case: return output
    }
}
