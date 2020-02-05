import java.io.*;
import java.util.Scanner;
import java.util.*;

import lex_DFA.*;

public class Main
{
    static lex_DFA.NFA bool = new lex_DFA.NFA(), str = new lex_DFA.NFA(), num = new lex_DFA.NFA(), chr = new lex_DFA.NFA(),
            delim = new lex_DFA.NFA(), key = new lex_DFA.NFA(), oper = new lex_DFA.NFA(), ident = new lex_DFA.NFA();
    static Map<lex_DFA.NFA, String> fa_lexname = new HashMap<>();

    public static void create_fa()
    {
        bool.load_from_file("bool_fa.txt");
        str.load_from_file("string_fa.txt");
        num.load_from_file("number_fa.txt");
        chr.load_from_file("char_fa.txt");
        delim.load_from_file("delim_fa.txt");
        key.load_from_file("keyword_fa.txt");
        oper.load_from_file("operator_fa.txt");
        ident.load_from_file("identifier_fa.txt");
        fa_lexname.put(bool, "boolean");
        fa_lexname.put(str, "string");
        fa_lexname.put(chr, "character");
        fa_lexname.put(num, "number");
        fa_lexname.put(delim, "delimiter");
        fa_lexname.put(key, "keyword");
        fa_lexname.put(oper, "operator");
        fa_lexname.put(ident, "identifier");
    }

    public static String analyze(String line)
    {
        StringBuilder res = new StringBuilder("");
        String newline = line;
        int symbs_read = 0;
        Set<String> lex;
        while (symbs_read < line.length())
        {
            lex = new HashSet<>();
            int maxread = 0;
            for (lex_DFA.NFA fa: fa_lexname.keySet())
            {
                int faread = lex_DFA.get_longest_lexem(fa, newline);
                if (faread > maxread)
                {
                    maxread = faread;
                    lex = new HashSet<>();
                }
                if (maxread == faread) lex.add(fa_lexname.get(fa));
            }
            StringBuilder lex_name = new StringBuilder(" < ");
            for (String lx: lex) lex_name.append(lx + " / ");
            lex_name.append("> ");
            newline = line.substring(symbs_read+maxread,line.length());
            res.append(line.substring(symbs_read, symbs_read+maxread) + lex_name);
            symbs_read += maxread;
        }
        return res.toString();
    }
    /*
    public static void read()
    {
        Analyzer anal = new Analyzer();
        boolean gotten = false;
        String path = "";
        while (!gotten) {
            Scanner in = new Scanner(System.in);
            System.out.println("Введите путь к файлу: ");
            path = in.nextLine();

            try {
                BufferedReader br = new BufferedReader(new FileReader(path));
                BufferedWriter bw = new BufferedWriter(new FileWriter("return.txt"));
                String s;
                while ((s = br.readLine()) != null) {
                    String line = anal.Analyze(s);
                    bw.write(line);
                    bw.newLine();
                }
                gotten = true;
                bw.close();
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
    //*/

    public static void read()
    {
        create_fa();
        boolean gotten = false;
        String path = "";
        while (!gotten) {
            Scanner in = new Scanner(System.in);
            System.out.println("Введите путь к файлу: ");
            path = in.nextLine();

            try {
                BufferedReader br = new BufferedReader(new FileReader(path));
                BufferedWriter bw = new BufferedWriter(new FileWriter("return.txt"));
                String s;
                while ((s = br.readLine()) != null) {
                    String line = analyze(s);
                    bw.write(line);
                    bw.newLine();
                }
                gotten = true;
                bw.close();
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public static void main(String[] args)
    {
        read();
    }
}
