import java.util.*;
import java.util.stream.Collectors;

class ParseMolecule {

    public static Map<String, Integer> getAtoms(String formula) {
        // Your code here!
        if (formula == null || formula.isEmpty()) {
            return new HashMap<>();
        }

        Deque<Element> stack = new ArrayDeque<>();
        stack.addFirst(new Element());

        Token token = parseNextToken(formula);
        while (token != null) {
            if (token.isOpen()) {
                Element element = stack.removeFirst();
                element.check(token.element);
                stack.getFirst().current = add(stack.getFirst().current, multiply(element.current, element.multiplier));
            } else if (token.isClose()) {
                stack.addFirst(new Element(token.multiplier, token.element));
            } else {
                stack.getFirst().current = add(stack.getFirst().current, of(token.element, token.multiplier));
            }

            token = parseNextToken(token.head);
        }

        if (stack.size() != 1) {
            throw new IllegalArgumentException(formula);
        }

        return stack.getFirst().current;
    }

    private static Map<String, Integer> of(String k, Integer v) {
        HashMap<String, Integer> map = new HashMap<>();
        map.put(k, v);
        return map;
    }

    private static Map<String, Integer> multiply(Map<String, Integer> map, int multiplier) {
        if (map.isEmpty() || multiplier == 1) {
            return map;
        }
        return map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() * multiplier));
    }

    private static Map<String, Integer> add(Map<String, Integer> left, Map<String, Integer> right) {
        Set<String> keys = new HashSet<>(left.keySet());
        keys.addAll(right.keySet());
        return keys.stream()
                .collect(Collectors.toMap(k -> k, k -> left.getOrDefault(k, 0) + right.getOrDefault(k, 0)));
    }

    private static class Element {
        private int multiplier = 1;
        private String bracket;
        private Map<String, Integer> current = new HashMap<>();

        public Element() {
        }

        public Element(int multiplier, String bracket) {
            this.multiplier = multiplier;
            this.bracket = bracket;
        }

        void check(String bracket) {
            switch (bracket) {
                case "(":
                    if (!")".equals(this.bracket)) {
                        throw new IllegalArgumentException(bracket);
                    }
                    break;
                case "[":
                    if (!"]".equals(this.bracket)) {
                        throw new IllegalArgumentException(bracket);
                    }
                    break;
                case "{":
                    if (!"}".equals(this.bracket)) {
                        throw new IllegalArgumentException(bracket);
                    }
                    break;
                default:
                    throw new IllegalArgumentException(bracket);
            }
        }
    }

    private static class Token {
        private final int multiplier;
        private final String element;
        private final String head;

        private Token(int multiplier, String element, String head) {
            this.multiplier = multiplier;
            this.element = element;
            this.head = head;
        }

        public boolean isOpen() {
            return "(".equals(element) || "[".equals(element) || "{".equals(element);
        }

        public boolean isClose() {
            return ")".equals(element) || "]".equals(element) || "}".equals(element);
        }
    }

    private static Token parseNextToken(String formula) {
        if (formula == null || formula.isEmpty()) {
            return null;
        }
        int length = formula.length();
        char last = formula.charAt(length - 1);
        if (last == '(' || last == '[' || last == '{') {
            return new Token(1, String.valueOf(last), formula.substring(0, length - 1));
        }
        if (last == ')' || last == ']' || last == '}') {
            return new Token(1, String.valueOf(last), formula.substring(0, length - 1));
        }
        if (last >= 'A' && last <= 'Z') {
            return new Token(1, String.valueOf(last), formula.substring(0, length - 1));
        }
        if (last >= 'a' && last <= 'z') {
            if (length - 2 < 0) {
                throw new IllegalArgumentException(formula);
            }
            if (formula.charAt(length - 2) < 'A' || formula.charAt(length - 2) > 'Z') {
                throw new IllegalArgumentException(formula);
            }
            return new Token(1, formula.substring(length - 2, length), formula.substring(0, length - 2));
        }
        int i = 0;
        while (last >= '0' && last <= '9') {
            last = formula.charAt(length - 1 - (++i));
        }
        int multiplier = Integer.parseInt(formula.substring(length - i, length));
        last = formula.charAt(length - 1 - i);
        if (last == ')' || last == ']' || last == '}') {
            return new Token(multiplier, String.valueOf(last), formula.substring(0, length - 1 - i));
        }
        if (last >= 'A' && last <= 'Z') {
            return new Token(multiplier, String.valueOf(last), formula.substring(0, length - 1 - i));
        }
        if (last >= 'a' && last <= 'z') {
            if (length - 2 - i < 0) {
                throw new IllegalArgumentException(formula);
            }
            if (formula.charAt(length - 2 - i) < 'A' || formula.charAt(length - 2 - i) > 'Z') {
                throw new IllegalArgumentException(formula);
            }
            return new Token(multiplier, formula.substring(length - 2 - i, length - i), formula.substring(0, length - 2 - i));
        }
        throw new IllegalStateException(formula);
    }
}