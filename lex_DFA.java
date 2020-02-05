import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class lex_DFA
{
    public static void print_set(Set<Integer> s)
    {
        StringBuilder msg = new StringBuilder(" ");
        for (int el: s)
            msg.append(el + " ");
        System.out.println(msg.toString());
    }

    public static class NFA
    {
        Character epsilon_symb = '®';
        Set<Integer> states;
        Set<Integer> current_states;
        int startState;
        Set<Integer> finalStates;
        Map<Integer, Map<Character, Set<Integer>>> transitionFunction;
        Map<Integer, Set<Integer>> epsilon_moves;

        private NFA()
        {
            states = new HashSet<>();
            startState = -1;
            current_states = new HashSet<>();
            finalStates = new HashSet<>();
            transitionFunction = new HashMap<>();
            epsilon_moves = new HashMap<>();
        }

        private NFA(Scanner fileScanner)
        {
            int numberOfStates = fileScanner.nextInt();
            states = new HashSet<>(numberOfStates);
            for (int i = 1; i <= numberOfStates; ++i)
                states.add(fileScanner.nextInt());

            startState = fileScanner.nextInt();
            current_states = new HashSet<>();
            current_states.add(startState);

            int numberOfFinalStates = fileScanner.nextInt();
            finalStates = new HashSet<>(numberOfFinalStates);
            for (int i = 0; i < numberOfFinalStates; ++i)
                finalStates.add(fileScanner.nextInt());

            transitionFunction = new HashMap<>(numberOfStates);
            for (Integer state: states)
                transitionFunction.put(state, new HashMap<>());

            epsilon_moves = new HashMap<>(numberOfStates);
            for (Integer state: states)
                epsilon_moves.put(state, new HashSet<>());

            while (fileScanner.hasNext())
            {
                int from = fileScanner.nextInt();
                Character via = fileScanner.next().charAt(0);
                if (via == epsilon_symb)
                {
                    epsilon_moves.get(from).add(fileScanner.nextInt());
                } else {
                    if (!transitionFunction.get(from).keySet().contains(via))
                    {
                        transitionFunction.get(from).put(via, new HashSet<>());
                    }
                    transitionFunction.get(from).get(via).add(fileScanner.nextInt());
                }
            }
        }

        private static Scanner getScanner(String pathname) throws FileNotFoundException {
            File file = new File(pathname);

            if (!file.exists())
                System.out.format("File '%s' does not exist.%n", pathname);

            if (!file.canRead())
                System.out.format("Cannot read file '%s'.%n", pathname);

            return new Scanner(file);
        }

        private NFA load_from_file(String filename)
        {
            NFA res = new NFA();
            try
            {
                Scanner filescanner = getScanner(filename);
                res = new NFA(filescanner);
                filescanner.close();
                return res;
            }
            catch (FileNotFoundException ee)
            {
                System.out.println(" An error occured: file not found!");
            }
            return res;
        }

        private void save_to_file(String filename)
        {
            try
            {
                PrintWriter writer = new PrintWriter(filename, "UTF-8");
                StringBuilder out = new StringBuilder("");
                out.append(states.size() + "\n");
                for (int state: states) out.append(state + " ");
                out.append("\n" + startState + "\n");
                out.append(finalStates.size() + "\n");
                for (int fstate: finalStates) out.append(fstate + " ");
                for (int from: transitionFunction.keySet())
                {
                    if (transitionFunction.get(from) != null)
                        for (char via: transitionFunction.get(from).keySet())
                            if (transitionFunction.get(from).get(via) != null)
                                for (int to: transitionFunction.get(from).get(via))
                                    out.append("\n " + from + " " + via + " " + to);
                }
                for (int from: epsilon_moves.keySet())
                {
                    if (epsilon_moves.get(from) != null)
                        for (int to: epsilon_moves.get(from))
                            out.append("\n " + from + " " + epsilon_symb + " " + to);
                }
                writer.println(out.toString());
                writer.close();
            }
            catch (IOException ee)
            {
                System.out.println(" Some error occured!");
            }
        }

        private NFA(NFA other)
        {
            int numberOfStates = other.states.size();
            states = new HashSet<>(other.states);
            for (int state: other.states)
                states.add(state);

            startState = other.startState;
            current_states = new HashSet<>(other.current_states);

            int numberOfFinalStates = other.finalStates.size();
            finalStates = new HashSet<>(numberOfFinalStates);
            for (int state: other.finalStates)
                finalStates.add(state);

            transitionFunction = new HashMap<>(numberOfStates);
            for (Integer state: states)
                transitionFunction.put(state, new HashMap<>());

            epsilon_moves = new HashMap<>(numberOfStates);
            for (Integer state: states)
                epsilon_moves.put(state, new HashSet<>());

            for (int from: states)
            {
                if (other.epsilon_moves.get(from) != null)
                    for (int to: other.epsilon_moves.get(from))
                        epsilon_moves.get(from).add(to);
                if (other.transitionFunction.get(from) != null)
                    for (char via: other.transitionFunction.get(from).keySet())
                    {
                        if (other.transitionFunction.get(from).get(via) != null)
                            transitionFunction.get(from).put(via, new HashSet<>(other.transitionFunction.get(from).get(via)));
                    }
            }
        }

        private void reset()
        {
            current_states = new HashSet<>();
            current_states.add(startState);
        }

        private Set<Integer> epsilon_closure(Set<Integer> some_states)
        {
            Set<Integer> res = new HashSet<>(some_states);
            Map<Integer, Boolean> visited = new HashMap<>();
            for (int state: states) visited.put(state, false);
            Set<Integer> states_to_add = new HashSet<>();
            Stack<Integer> states_to_process = new Stack<>();
            for (int state: some_states) states_to_process.add(state);
            int current_state;
            while (!states_to_process.isEmpty())
            {
                current_state = states_to_process.pop();
                visited.put(current_state, true);
                if (epsilon_moves.get(current_state) != null) {
                    res.addAll(epsilon_moves.get(current_state));
                    for (int state : epsilon_moves.get(current_state))
                        if (!visited.get(state)) states_to_process.add(state);
                }
            }
            return res;
        }

        private boolean read_symb(Character c)
        {
            boolean res = false;
            current_states = epsilon_closure(current_states);
            Set<Integer> next_states = new HashSet<>();
            for (int from: current_states)
            {
                if (transitionFunction.get(from) != null)
                    if (transitionFunction.get(from).get(c) != null)
                    {
                        next_states.addAll(transitionFunction.get(from).get(c));
                        res = true;
                    }
            }
            if (res) {
                next_states = epsilon_closure(next_states);
                current_states = next_states;
            }
            return res;
        }

        private void print_NFA()
        {
            for (int from: transitionFunction.keySet())
            {
                for (char via: transitionFunction.get(from).keySet())
                    for (int to: transitionFunction.get(from).get(via))
                        System.out.println(" " + from + " " + via + " " + to);
            }
            for (int from: epsilon_moves.keySet())
            {
                for (int to: epsilon_moves.get(from))
                    System.out.println(" " + from + " " + epsilon_symb + " " + to);
            }
        }

        private int get_max_state_number()
        {
            int max = -1;
            for (int state: states)
                if (state > max) max = state;
            return max;
        }

        private boolean is_in_terminal_state()
        {
            boolean res = false;
            Iterator<Integer> fstate = finalStates.iterator();
            while (fstate.hasNext() & !res)
            {
                if (current_states.contains(fstate.next()))
                    res = true;
            }
            return res;
        }

        private NFA iterate_k_times(int k)
        {
            NFA res = new NFA(this);
            if (k <= 1) return res;
            for (int i = 1; i < k; ++i)
                res = res.merge_NFA_AND(this);
            return res;
        }

        private NFA merge_NFA_OR(NFA other)
        {
            NFA res = new NFA();
            res.startState = 1;
            res.current_states.add(startState);
            res.states.add(1);
            int this_max = get_max_state_number(), other_max = other.get_max_state_number();
            for (int state: states) res.states.add(state + 1);
            for (int state: other.states) res.states.add(state + this_max + 1);
            for (int from: states)
            {
                if (epsilon_moves.get(from) != null)
                {
                    res.epsilon_moves.put(from+1, new HashSet<>());
                    for (int to: epsilon_moves.get(from))
                        res.epsilon_moves.get(from+1).add(to+1);
                }
                if (transitionFunction.get(from) != null)
                {
                    res.transitionFunction.put(from+1, new HashMap<>());
                    for (char via: transitionFunction.get(from).keySet())
                    {
                        {
                            res.transitionFunction.get(from+1).put(via, new HashSet<>());
                            for (int to: transitionFunction.get(from).get(via))
                                res.transitionFunction.get(from+1).get(via).add(to+1);
                        }
                    }
                }
            }
            for (int from: other.states)
            {
                if (other.epsilon_moves.get(from) != null)
                {
                    res.epsilon_moves.put(from+this_max+1, new HashSet<>());
                    for (int to: other.epsilon_moves.get(from))
                        res.epsilon_moves.get(from+this_max+1).add(to+this_max+1);
                }
                if (other.transitionFunction.get(from) != null)
                {
                    res.transitionFunction.put(from + this_max +1, new HashMap<>());
                    for (char via: other.transitionFunction.get(from).keySet())
                    {
                        {
                            res.transitionFunction.get(from+this_max+1).put(via, new HashSet<>());
                            for (int to: other.transitionFunction.get(from).get(via))
                                res.transitionFunction.get(from+this_max+1).get(via).add(to+this_max+1);
                        }
                    }
                }
            }
            res.epsilon_moves.put(res.startState, new HashSet<>());
            res.epsilon_moves.get(res.startState).add(startState+1);
            res.epsilon_moves.get(res.startState).add(other.startState+this_max+1);
            int new_finals = other_max+this_max+2;
            res.states.add(new_finals);
            res.finalStates.add(new_finals);
            for (int state: finalStates)
            {
                if (epsilon_moves.get(state) == null)
                    res.epsilon_moves.put(state+1, new HashSet<>());
                res.epsilon_moves.get(state+1).add(new_finals);
            }
            for (int state: other.finalStates)
            {
                if (other.epsilon_moves.get(state) == null)
                    res.epsilon_moves.put(state+this_max+1, new HashSet<>());
                res.epsilon_moves.get(state+this_max+1).add(new_finals);
            }
            res.reset();
            return res;
        }

        private NFA merge_NFA_AND(NFA other)
        {
            NFA res = new NFA(this);
            int this_max = get_max_state_number();
            res.states.addAll(states);
            res.startState = startState;
            for (int state: other.states) res.states.add(state + this_max);
            for (int state: other.finalStates) res.finalStates.add(state + this_max);
            res.transitionFunction = new HashMap<>(transitionFunction);
            res.epsilon_moves = new HashMap<>(epsilon_moves);
            for (int from: other.states)
            {
                if (other.epsilon_moves.get(from) != null)
                {
                    res.epsilon_moves.put(from+this_max, new HashSet<>());
                    for (int to: other.epsilon_moves.get(from))
                        res.epsilon_moves.get(from+this_max).add(to+this_max);
                }
                if (other.transitionFunction.get(from) != null)
                {
                    res.transitionFunction.put(from + this_max, new HashMap<>());
                    for (char via: other.transitionFunction.get(from).keySet())
                    {
                        {
                            res.transitionFunction.get(from+this_max).put(via, new HashSet<>());
                            for (int to: other.transitionFunction.get(from).get(via))
                                res.transitionFunction.get(from+this_max).get(via).add(to+this_max);
                        }
                    }
                }
            }
            for (int from: finalStates)
            {
                if (res.epsilon_moves.get(from) == null)
                    res.epsilon_moves.put(from, new HashSet<>());
                res.epsilon_moves.get(from).add(other.startState + this_max);
            }
            res.reset();
            return res;
        }

        private NFA NFA_star()
        {
            NFA res = new NFA();
            int new_start = 1 + get_max_state_number();
            res.states.addAll(states);
            res.states.add(new_start);
            res.startState = new_start;
            res.current_states.add(res.startState);
            res.finalStates = new HashSet<>();
            res.finalStates.add(res.startState);
            res.transitionFunction = new HashMap<>(transitionFunction);
            res.epsilon_moves = new HashMap<>(epsilon_moves);
            res.epsilon_moves.put(new_start, new HashSet<>());
            res.epsilon_moves.get(new_start).add(startState);
            for (int from: finalStates)
            {
                if (epsilon_moves.get(from) == null)
                    res.epsilon_moves.put(from, new HashSet<>());
                res.epsilon_moves.get(from).add(new_start);
            }
            res.reset();
            return res;
        }

        private NFA NFA_zero_one()
        {
            NFA res = new NFA(this);
            int this_max = get_max_state_number();
            res.states.add(this_max+1);
            res.startState = this_max+1;
            res.epsilon_moves.put(this_max+1, new HashSet<>());
            res.epsilon_moves.get(this_max).add(startState);
            for (int state: finalStates) res.epsilon_moves.get(this_max+1).add(state);
            res.reset();
            return res;
        }
    }

    public static NFA NFA_from_characters(char[] characters)
    {
        NFA res = new NFA();
        res.states.add(1);
        res.states.add(2);
        res.startState = 1;
        res.current_states.add(res.startState);
        res.finalStates.add(2);
        res.transitionFunction.put(1, new HashMap<>());
        for (char c: characters)
        {
            if (c == res.epsilon_symb)
            {
                res.epsilon_moves.put(1, new HashSet<>());
                res.epsilon_moves.get(1).add(2);
            }
            else res.transitionFunction.get(1).put(c, new HashSet<>(res.finalStates));
        }
        res.reset();
        return res;
    }

    public static NFA NFA_from_word(String word)
    {
        char[] c = word.toCharArray();
        NFA res = new NFA();
        res.states.add(1);
        res.startState = 1;
        res.current_states.add(res.startState);
        Integer i = 1;
        for (char symb: c)
        {
            res.states.add(i+1);
            res.transitionFunction.put(i, new HashMap<>());
            res.transitionFunction.get(i).put(symb, new HashSet<>());
            res.transitionFunction.get(i).get(symb).add(i+1);
            i++;
        }
        res.finalStates.add(i);
        res.reset();
        return res;
    }

    public static void test_NFA_on_String(NFA nfa, String line)
    {
        nfa.reset();
        int i = 0;
        while (nfa.read_symb(line.charAt(i)))
        {
            i++;
            if (i >= line.length()) break;
        }
        System.out.println(" Last read character index - " + i);
        System.out.println(" Last read character - " + line.charAt(i-1));
        if (i+1 < line.length()) System.out.println(" Next character - " + line.charAt(i));
        System.out.println(" Longest read  - " + line.substring(0, i));
        System.out.println(" Original line - " + line);
    }

    public static int get_longest_lexem(NFA nfa, String line)
    {
        nfa.reset();
        int i = 0, last_terminal = -1;
        while (nfa.read_symb(line.charAt(i)))
        {
            if (nfa.is_in_terminal_state()) last_terminal = i;
            i++;
        }
        System.out.println(" Last terminal state at index - " + last_terminal);
        if (last_terminal > -1)
        {
            System.out.println(" Longest lexem - " + line.substring(0, last_terminal+1));
            System.out.println(" Original line - " + line);
        }
        return last_terminal;
    }

    public static NFA numbers_fa()
    {
        char[] bin_dig = {'0','1'};
        char[] oct_dig = {'0','1','2','3','4','5','6','7'};
        char[] non_zero_dig = {'1','2','3','4','5','6','7','8','9'};
        char[] digit = {'0','1','2','3','4','5','6','7','8','9'};
        char[] hex_dig = {'0','1','2','3','4','5','6','7','8','9','a','A','b','B','c','C','d','D','e','E','f','F'};
        NFA bin_digs = NFA_from_characters(bin_dig);
        NFA bin_digs_star = bin_digs.merge_NFA_AND(bin_digs.NFA_star());
        NFA oct_digs = NFA_from_characters(oct_dig);
        NFA oct_digs_star = oct_digs.merge_NFA_AND(oct_digs.NFA_star());
        NFA hex_digs = NFA_from_characters(hex_dig);
        NFA hex_digs_star = hex_digs.merge_NFA_AND(hex_digs.NFA_star());
        NFA digits = NFA_from_characters(digit);
        NFA digs_star = digits.NFA_star();
        NFA digs = digits.merge_NFA_AND(digs_star);

        // decimal literal
        NFA zero = NFA_from_word("0");
        NFA zero_star = zero.merge_NFA_AND(zero.NFA_star());
        NFA dec_lit = zero.merge_NFA_OR(NFA_from_characters(non_zero_dig).merge_NFA_AND(digs));

        // binary literal
        NFA bin_lit = NFA_from_word("0b").merge_NFA_AND(bin_digs_star).merge_NFA_OR(NFA_from_word("0B").merge_NFA_AND(bin_digs_star));

        // octal literal
        NFA oct_lit = NFA_from_word("0o").merge_NFA_AND(oct_digs_star).merge_NFA_OR(NFA_from_word("0O").merge_NFA_AND(oct_digs_star));

        // hex literal
        NFA hex_lit = NFA_from_word("0x").merge_NFA_AND(hex_digs_star).merge_NFA_OR(NFA_from_word("0X").merge_NFA_AND(hex_digs_star));

        NFA int_literal = dec_lit.merge_NFA_OR(bin_lit.merge_NFA_OR(oct_lit.merge_NFA_OR(hex_lit)));
        
        // float literal
        NFA mantissa = (digs.merge_NFA_AND(NFA_from_word(".").merge_NFA_AND(digs_star))).merge_NFA_OR(NFA_from_word(".").merge_NFA_AND(digs));
        char[] e = {'+','-'};
        NFA exponent0 = NFA_from_characters(e).NFA_zero_one().merge_NFA_AND(digs);
        NFA exponent = NFA_from_word("e").merge_NFA_AND(exponent0);
        exponent = exponent.merge_NFA_OR(NFA_from_word("E").merge_NFA_AND(exponent0));
        NFA float_literal = mantissa.merge_NFA_AND(exponent.NFA_zero_one()).merge_NFA_OR(digs.merge_NFA_AND(exponent));

        //imaginary literal
        char[] img = {'j','J'};
        NFA img_ind = NFA_from_characters(img);
        NFA img_literal = digs.merge_NFA_AND(img_ind).merge_NFA_OR(float_literal.merge_NFA_AND(img_ind));

        NFA res = int_literal.merge_NFA_OR(float_literal).merge_NFA_OR(img_literal);
        res.save_to_file("number_fa.txt");
        return res;
    }

    public static NFA bool_fa()
    {
        NFA res = NFA_from_word("True").merge_NFA_OR(NFA_from_word("False"));
        res.save_to_file("bool_fa.txt");
        return res;
    }

    public static NFA string_fa()
    {
        char[] bin_dig = {'0','1'};
        char[] oct_dig = {'0','1','2','3','4','5','6','7'};
        char[] non_zero_dig = {'1','2','3','4','5','6','7','8','9'};
        char[] digit = {'0','1','2','3','4','5','6','7','8','9'};
        char[] hex_dig = {'0','1','2','3','4','5','6','7','8','9','a','A','b','B','c','C','d','D','e','E','f','F'};
        NFA bin_digs = NFA_from_characters(bin_dig);
        NFA bin_digs_star = bin_digs.merge_NFA_AND(bin_digs.NFA_star());
        NFA oct_digs = NFA_from_characters(oct_dig);
        NFA oct_digs_star = oct_digs.merge_NFA_AND(oct_digs.NFA_star());
        NFA hex_digs = NFA_from_characters(hex_dig);
        NFA hex_digs_star = hex_digs.merge_NFA_AND(hex_digs.NFA_star());
        NFA digits = NFA_from_characters(digit);

        char[] graphics = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u',
                'v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U'
                ,'V','W','X','Y','Z','0','1','2','3','4','5','6','7','8','9','_','|','(',')','[',']','{','}','+','-'
                ,'*','/','%','!','&','|','~','^','<','=','>',',','.',':',';','$','?','#','@'};
        char[] spaces = {' ', '\t', '\n'};
        char[] delims = {'\'', '\"'};
        NFA graphic = NFA_from_characters(graphics);
        NFA white_space = NFA_from_characters(spaces);
        NFA text_chars = graphic.merge_NFA_OR(white_space.merge_NFA_OR(NFA_from_characters(delims)));
        NFA
           esc_a = NFA_from_word("\\").merge_NFA_AND(text_chars).merge_NFA_OR(
                   NFA_from_word("0o").merge_NFA_AND(oct_digs.iterate_k_times(3)).merge_NFA_OR(
                           NFA_from_word("0h").merge_NFA_AND(hex_digs.iterate_k_times(2))
                   ));
        NFA  esc_u = esc_a.merge_NFA_OR(
                NFA_from_word("\\u").merge_NFA_AND(hex_digs.iterate_k_times(4)).merge_NFA_OR(
                        NFA_from_word("\\U").merge_NFA_AND(hex_digs.iterate_k_times(8))
                ));
        char[] raw = {'r', 'R'}, bytes = {'b', 'B'}, e1 = {' ', '\t', '\''}, e2 = {' ', '\t', '\"'};
        NFA raw_opt = NFA_from_characters(raw), bytes_opt = NFA_from_characters(bytes);
        NFA single_quoted_str =
                (NFA_from_word("\"").merge_NFA_AND(
                   graphic.merge_NFA_OR(esc_u.merge_NFA_OR(NFA_from_characters(e1))).NFA_star()
           ).merge_NFA_AND(NFA_from_word("\""))).merge_NFA_OR(
                        NFA_from_word("\'").merge_NFA_AND(
                                graphic.merge_NFA_OR(esc_u.merge_NFA_OR(NFA_from_characters(e1))).NFA_star()
                        ).merge_NFA_AND(NFA_from_word("\'")));
        NFA tripple_quoted_str =
                (NFA_from_word("\"\"\"").merge_NFA_AND(text_chars.merge_NFA_OR(esc_u).NFA_star()).merge_NFA_AND(
                        NFA_from_word("\"\"\""))).merge_NFA_OR(
                        NFA_from_word("\'\'\'").merge_NFA_AND(text_chars.merge_NFA_OR(esc_u).NFA_star()).merge_NFA_AND(
                                NFA_from_word("\'\'\'")));
        NFA str_literal = raw_opt.NFA_zero_one().merge_NFA_AND(single_quoted_str.merge_NFA_OR(tripple_quoted_str));
        NFA res = bytes_opt.NFA_zero_one().merge_NFA_AND(str_literal);
        res.save_to_file("string_fa.txt");
        return res;
    }

    public static void main(String[] args)
    {
        
        NFA bool = new NFA(), str = new NFA(), num = new NFA();
        bool = bool.load_from_file("bool_fa.txt");
        str = str.load_from_file("string_fa.txt");
        num = num.load_from_file("number_fa.txt");
        // up to this point, we have the three nfa loaded
        // suppose we have some string variable "line", where we want to find the longest possible lexem for each type
        // then we call our nfa's one by one using  get_longest_lexem(nfa, line);
        
        /*
        test_NFA_on_String(bool, "True1");
        test_NFA_on_String(str, "\'Askjnd \\uAhe2 *&%(@$ \'");
        String num_test = "0.123324e-29384 woierj";
        //String test = "0b100123432m";
        //String test = "0o12335259";
        test_NFA_on_String(num, test);
        //*/
    }
}
