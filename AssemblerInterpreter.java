import java.util.HashMap;
import java.util.regex.Pattern;

public class AssemblerInterpreter {
    static boolean isNum(String sentence) {
        for (int i = 0; i < sentence.length(); i++)
            if (sentence.charAt(i) != 45 && (sentence.charAt(i) < 48 || sentence.charAt(i) > 57))
                return false;
        return true;
    }

    static String[] cleaner(String input) {
        String firstPart[] = input.replaceAll(";.+\n", "\n")
                                  .replaceAll("\n+", "\n")
                                  .split("\n");

        String secondPart[] = new String[firstPart.length];
        for (int i = 0; i < firstPart.length; i++)
            secondPart[i] = firstPart[i].trim();

        return secondPart;
    }

    static String instruction(String input) {
        return input.split(" ")[0];
    }

    static String[] parameters(String input) {
        String firstPart[] = input.replaceFirst(instruction(input), "").trim().split(",");
        String secondPart[] = new String[firstPart.length];
        for (int i = 0; i < firstPart.length; i++)
            secondPart[i] = firstPart[i].trim();
        return secondPart;
    }

    public static String interpret(final String input) {
        HashMap<String, Integer> labels = new HashMap<>();
        HashMap<String, Integer> vars = new HashMap<>();
        String program[] = cleaner(input);
        int lastComparison = -1;
        String message = "";
        String jumps="";

        for (int i = 0; i < program.length; i++)
            if (program[i].endsWith(":"))
                labels.put(instruction(program[i]).replaceAll(":", ""), i);

        for (int i = 0; i < program.length; i++) {
            String instruction = instruction(program[i]).toLowerCase();

            if (instruction.compareTo("end") == 0)
                return message;

            if (instruction.compareTo("msg") == 0) {
                boolean isString = false;
                String out = program[i].replaceFirst(instruction("msg"), "").trim();

                while (!out.isEmpty()) {
                    char letter = out.charAt(0);
                    if (letter == 39) {
                        isString = !isString;
                        if (!isString)
                            out = out.replaceFirst(",", "");
                        out = out.replaceFirst("'", "");
                        continue;
                    }

                    if (isString){
                        message += letter;
                        out = out.replaceFirst(Pattern.quote(String.valueOf(letter)), "");
                    }
                  
                    else {
                        String variable = out.trim().split(",")[0].trim();
                        message += isNum(variable) ? Integer.parseInt(variable) : vars.get(variable);
                        out = out.replaceFirst(variable, "").replaceFirst(",", "").trim();
                    }
                }
                continue;
            }

            String params[] = parameters(program[i]);
            switch (instruction) {
                // Setter block
                case "mov":
                    vars.put(params[0], isNum(params[1]) ? Integer.parseInt(params[1]) : vars.get(params[1]));
                    break;
                case "inc":
                    int incremento = vars.get(params[0]) + 1;
                    vars.put(params[0], incremento);
                    break;
                case "dec":
                    int decremento = vars.get(params[0]) - 1;
                    vars.put(params[0], decremento);
                    break;
                case "add":
                    vars.put(params[0], (isNum(params[0]) ? Integer.parseInt(params[0]) : vars.get(params[0]))
                                      + (isNum(params[1]) ? Integer.parseInt(params[1]) : vars.get(params[1])));
                    break;
                case "sub":
                    vars.put(params[0], (isNum(params[0]) ? Integer.parseInt(params[0]) : vars.get(params[0]))
                                      - (isNum(params[1])? Integer.parseInt(params[1])  : vars.get(params[1])));
                    break;
                case "div":
                    vars.put(params[0], (isNum(params[0]) ? Integer.parseInt(params[0]) : vars.get(params[0]))
                                      / (isNum(params[1]) ? Integer.parseInt(params[1]) : vars.get(params[1])));
                    break;
                case "mul":
                    vars.put(params[0], (isNum(params[0]) ? Integer.parseInt(params[0]) : vars.get(params[0]))
                                      * (isNum(params[1]) ? Integer.parseInt(params[1]) : vars.get(params[1])));
                    break;
                
                // Simple jumps block
                case "call":
                    jumps=i+" "+jumps;
                case "jmp":
                    i = labels.get(params[0]) - 1;
                    break;
                case "ret":
                    i = Integer.parseInt(jumps.split(" ")[0]);
                    jumps=jumps.replaceFirst(i+" ", "");
                    break;
                
                // Comparison jumps block
                case "cmp":
                    lastComparison = i;
                    break;
                case "je": {
                    String parts[] = parameters(program[lastComparison]);
                    if ((isNum(parts[0]) ? Integer.parseInt(parts[0]) : vars.get(parts[0])) 
                     == (isNum(parts[1]) ? Integer.parseInt(parts[1]) : vars.get(parts[1])))
                            i = labels.get(params[0]) - 1;
                }
                    break;
                case "jne": {
                    String parts[] = parameters(program[lastComparison]);
                    if ((isNum(parts[0]) ? Integer.parseInt(parts[0]) : vars.get(parts[0])) 
                     != (isNum(parts[1]) ? Integer.parseInt(parts[1]) : vars.get(parts[1])))
                            i = labels.get(params[0]) - 1;
                }
                    break;
                case "jg": {
                    String parts[] = parameters(program[lastComparison]);
                    if ((isNum(parts[0]) ? Integer.parseInt(parts[0]) : vars.get(parts[0])) 
                      > (isNum(parts[1]) ? Integer.parseInt(parts[1]) : vars.get(parts[1])))
                            i = labels.get(params[0]) - 1;
                }
                    break;
                case "jge": {
                    String parts[] = parameters(program[lastComparison]);
                    if ((isNum(parts[0]) ? Integer.parseInt(parts[0]) : vars.get(parts[0])) 
                     >= (isNum(parts[1]) ? Integer.parseInt(parts[1]) : vars.get(parts[1])))
                            i = labels.get(params[0]) - 1;
                }
                    break;
                case "jl": {
                    String parts[] = parameters(program[lastComparison]);
                    if ((isNum(parts[0]) ? Integer.parseInt(parts[0]) : vars.get(parts[0])) 
                      < (isNum(parts[1]) ? Integer.parseInt(parts[1]) : vars.get(parts[1])))
                            i = labels.get(params[0]) - 1;
                }
                    break;
                case "jle": {
                    String parts[] = parameters(program[lastComparison]);
                    if ((isNum(parts[0]) ? Integer.parseInt(parts[0]) : vars.get(parts[0])) 
                     <= (isNum(parts[1]) ? Integer.parseInt(parts[1]) : vars.get(parts[1])))
                            i = labels.get(params[0]) - 1;
                }
                break;
            }
        }
      
        return null;
    }
}
