/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package assembler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Me
 */
public class Assembler {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
      
        Assembler asm = new Assembler();
        String fileName = args[0];
        
        HashMap<String, Integer> symbols = new HashMap<>();
        asm.initSymbolTable(symbols);
        
        // Generate arraylist of instructions from text file
        ArrayList<String> instructions = asm.readFile(fileName);
        // Empty ArrayList to hold binary instructions
        ArrayList<String> binInstructions = new ArrayList<>();
        
        // Scan program and begin making edits
        asm.scanProgram(instructions, binInstructions, symbols, 1);        
        asm.scanProgram(instructions, binInstructions, symbols, 2);
        
        asm.writeFile(fileName, binInstructions);
        
        for (String s : binInstructions){
            System.out.println(s);
        }
        
    }
    
    
    
    private ArrayList<String> readFile(String fileName){
        
        ArrayList<String> lines = new ArrayList<>();
        String line;
        String stripped;
        
        File file = new File(fileName);
        
        try {
            FileReader fr = new FileReader(file);
            BufferedReader reader = new BufferedReader(fr); 
            
            while ((line = reader.readLine()) != null){ 
                if (line.isEmpty()){
                    continue;
                }
                stripped = "";
                for (int i = 0; i < line.length(); i++){
                    // Remove comments
                    if (line.charAt(i) == '/' && line.charAt(i+1) == '/'){
                        break;
                    }
                    // Remove whitespace
                    if (line.charAt(i) != ' '){
                        stripped += line.charAt(i);
                    }

                }
                
                if (!stripped.isEmpty()){
                    lines.add(stripped);    
                    System.out.println(stripped);   
                }
                
            }
            
            reader.close();
            fr.close();
            
        } catch (Exception e){
            System.out.println(e);
            System.err.println("Could not open file.");
        }
        
        

        return lines;
        
    }
    
    private void scanProgram(ArrayList<String> instructions, ArrayList<String> binInstructions, HashMap<String,Integer> symbols, int passCount){
        
        int line = -1;
        
        int n = 16;
        
        for (String instr : instructions){
            
            String binOut = "";
            
            // Label declaration starts with a (
            if (instr.charAt(0) == '('){
                // First time, we index labels
                if (passCount == 1){
                    if (symbols.get(instr.substring(1, instr.length()-1)) == null){
                        // Address is the following line
                        symbols.put(instr.substring(1, instr.length()-1), line+1);
                    }                   
                } 
                
            }
            // A-instruction starts with a @
            else if (instr.charAt(0) == '@'){
                line++;
                
                if (passCount == 1){
                    binInstructions.add("A-instruction");
                }
                // Second time through, we use our symbol table
                else if (passCount == 2){
                    int address;
                    // If it exists in the table, it's a label; get address
                    if (symbols.get(instr.substring(1)) != null){
                        address = symbols.get(instr.substring(1));
                    } 
                    // Otherwise it's a variable; store it
                    else {
                        address = n;
                        // Add to table as variable
                        symbols.put(instr, n);
                        n++;
                    }
                    
                    String binAddress = Integer.toBinaryString(address);
                    
                    String leadingZero = "";
                    for (int i = 0; i < (16-binAddress.length()); i++){
                        leadingZero += "0";
                    }
                    
                    leadingZero += binAddress;

                    binInstructions.set(line, leadingZero);
                    
                }               
                
            }
            // Else it's a C-instruction
            else {
                line++;
                
                if (passCount == 1){
                    // 1 1 1 a c1 c2 c3 c4 c5 c6 d1 d2 d3 j1 j2 j3

                    binOut = "111";

                    // dest = comp ; jump

                    String dest = "";
                    String comp = "";
                    String jump = "";

                    // Parse instruction
                    if (instr.contains("=")){
                        String[] parts = instr.split("=");
                        dest = parts[0];
                        instr = parts[1];
                    }
                    if (instr.contains(";")){
                        String[] parts = instr.split(";");
                        comp = parts[0];
                        jump = parts[1];
                    } else {
                        comp = instr;
                    }

                    // Set 'a' bit
                    if (comp.contains("M")){
                        binOut += "1";
                    }
                    else {
                        binOut += "0";
                    }

                    // Set c1 - c6 bits
                    binOut += binComp(comp);

                    // Set d1 - d3
                    binOut += binDest(dest);

                    // Set j1 - j3
                    binOut += binJump(jump);

                    binInstructions.add(binOut);    
                }
                   
                
            }
            
             
                
        }
        
    }
    
    private ArrayList<String> makeBinary(ArrayList<String> instructions){
        
        ArrayList<String> binInstructions = new ArrayList<>();
        
        HashMap<String, Integer> symbols = new HashMap<>();
        initSymbolTable(symbols);
        
        int line = 0;
        
        for (String instr : instructions){
            
            String binOut = "";
            
            // Label declaration starts with a (
            if (instr.charAt(0) == '('){
                
                if (symbols.get(instr) == null){
                    symbols.put(instr, line+1);
                }
            }
            // A-instruction starts with a @
            else if (instr.charAt(0) == '@'){
                line++;
                
                
            }
            // Else it's a C-instruction
            else {
                line++;
                
                // 1 1 1 a c1 c2 c3 c4 c5 c6 d1 d2 d3 j1 j2 j3
                
                binOut = "111";
                
                // dest = comp ; jump
                
                String dest = "";
                String comp = "";
                String jump = "";
                
                // Parse instruction
                if (instr.contains("=")){
                    String[] parts = instr.split("=");
                    dest = parts[0];
                    instr = parts[1];
                }
                if (instr.contains(";")){
                    String[] parts = instr.split(";");
                    comp = parts[0];
                    jump = parts[1];
                }
                
                // Set 'a' bit
                if (comp.contains("M")){
                    binOut += "1";
                }
                else {
                    binOut += "0";
                }
                
                // Set c1 - c6 bits
                binOut += binComp(comp);
                
                // Set d1 - d3
                binOut += binDest(dest);
                
                // Set j1 - j3
                binOut += binJump(jump);
                
            }
            
            binInstructions.add(binOut);
                        
        }
        
        for (String instr : instructions){
            String binString = "";
            
            // Declaring a label
            if (instr.charAt(0) == '('){
                
                
                System.out.println("label");
                binString = "label";
            }
            // A instruction
            else if (instr.charAt(0) == '@'){
                
                String addr = "";
                   
                // Accessing a label
                if (instr.charAt(1) >= 65 || instr.charAt(1) <= 90){
                    // Convert label to address
                    addr = labelToVal(instr.substring(1));
                    // If it wasn't a predefined symbol:
                    if (addr.equals("")){
                        addr = "22";
                    }
                    
                } 
                // It's already an address
                else {
                    
                    addr = instr.substring(1);
                                   
                    
                }
               
                Integer register = Integer.parseInt(addr);
                binString += Integer.toBinaryString(register);

                String leadingZeros = "";
                for (int i = 0; i < (16-binString.length()); i++){
                    leadingZeros += "0";
                }

                binString = leadingZeros + binString;
                
                
                
            } 
            // C instruction
            // 1 1 1 a c1 c2 c3 c4 c5 c6 d1 d2 d3 j1 j2 j3
            // slide 17
            else {
                // All begin with 111
                binString += "111";
    
                // The line is either x=y or x;y. Split and handle both cases
                // Memory load/store
                if (instr.contains("=")){
                    String[] parts = instr.split("=");
                    String dest = parts[0];
                    String comp = parts[1];
                    
                    // Set 'a' value:
                    if (comp.contains("M")){
                        binString += "1";
                    }
                    else {
                        binString += "0";
                    }
                    
                    // set c1-c6
                    // placeholder
                    binString += binComp(comp);
                    
                    // set d1
                    if (dest.contains("A")){
                        binString += "1";
                    } else {
                        binString += "0";
                    }
                    // set d2
                    if (dest.contains("D")){
                        binString += "1";
                    } else {
                        binString += "0";
                    }
                    // set d3
                    if (dest.contains("M")){
                        binString += "1";
                    } else {
                        binString += "0";
                    }
                    
                    // set j1
                    // set j2
                    // set j3
                    binString += "000";
                    
                }
                // Jump:
                else {
                    String[] parts = instr.split(";");
                    String comp = parts[0];
                    String jmp = parts[1];
                    
                    // Set 'a' value:
                    binString += "0";
                    
                    // set c1-c6
                    binString += binComp(comp);
                                        
                    // set d1 d2 d3
                    binString += "000";
                    
                    // set j1 j2 j3
                    binString += binJump(jmp);
                }
                
                binInstructions.add(binString);
                
            }
            

            
        }
        
        return binInstructions;
    }
    
    private void writeFile(String fileName, ArrayList<String> binInstr){
        
        String[] parts = fileName.split("\\.");
        fileName = parts[0] + ".hack";
        
        try {
            PrintWriter out = new PrintWriter(fileName);
            for (String s : binInstr){
                out.println(s);
            }
            out.close();
        } catch (Exception e){
            System.err.println("Could not write to file.");            
        }

        
    }
    
    private String labelToVal(String label){
        
        if (label.charAt(0) == 'R'){
            String[] split = label.split("R");
            return split[1];
        } else {
            
            switch (label){
                case "SCREEN":
                    return "16384";
                case "KBD":
                    return "24576";
                case "SP":
                    return "0";
                case "LCL":
                    return "1";
                case "ARG":
                    return "2";
                case "THIS":
                    return "3";
                case "THAT":
                    return "4";               
            }
            
            
        }
        
        return "";
        
    }
    
    private String binDest (String dest){
        
        String binString = "";
        
        // set d1
        if (dest.contains("A")){
            binString += "1";
        } else {
            binString += "0";
        }
        // set d2
        if (dest.contains("D")){
            binString += "1";
        } else {
            binString += "0";
        }
        // set d3
        if (dest.contains("M")){
            binString += "1";
        } else {
            binString += "0";
        }
        
        return binString;
    }
    
    private String binComp(String comp){
        
        switch (comp) {
            case "0":
                return "101010";
            case "1":
                return "111111";
            case "-1":
                return "111010";
            case "D":
                return "001100";
            case "A":
            case "M":
                return "110000";
            case "!D":
                return "001101";
            case "!A":
            case "!M":
                return "110001";
            case "-D":
                return "001111";
            case "-A":
            case "-M":
                return "110011";
            case "D+1":
                return "011111";
            case "A+1":
            case "M+1":
                return "110111";
            case "D-1":
                return "001110";
            case "A-1":
            case "M-1":
                return "110010";
            case "D+A":
            case "D+M":
                return "000010";
            case "D-A":
            case "D-M":
                return "010011";
            case "A-D":
            case "M-D":
                return "000111";
            case "D&A":
            case "D&M":
                return "000000";
            case "D|A":
            case "D|M":
                return "010101";
            default:
                return "000000";
        }
        
        
    }
    
    private String binJump (String jump){
        
        switch(jump){
            case "JGT":
                return "001";
            case "JEQ":
                return "010";
            case "JGE":
                return "011";
            case "JLT":
                return "100";
            case "JNE":
                return "101";
            case "JLE":
                return "110";
            case "JMP":
                return "111";
            default:
                return "000";
        }
        
    }
    
    private void initSymbolTable(HashMap<String, Integer> table){
        
        table.put("R0", 0);
        table.put("R1", 1);
        table.put("R2", 2);
        table.put("R3", 3);
        table.put("R4", 4);
        table.put("R5", 5);
        table.put("R6", 6);
        table.put("R7", 7);
        table.put("R8", 8);
        table.put("R9", 9);
        table.put("R10", 10);
        table.put("R11", 11);
        table.put("R12", 12);
        table.put("R13", 13);
        table.put("R14", 14);
        table.put("R15", 15);
        table.put("SCREEN", 16384);
        table.put("KBD", 24576);
        table.put("SP", 0);
        table.put("LCL", 1);
        table.put("ARG", 2);
        table.put("THIS", 3);
        table.put("THAT", 4);      
                
    }
}
