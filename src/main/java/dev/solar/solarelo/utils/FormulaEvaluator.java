package dev.solar.solarelo.utils;

import dev.solar.solarelo.SolarElo;

public class FormulaEvaluator {

    public static double evaluateFormula(SolarElo plugin, String formula, int killerElo, int victimElo, double killerKD, double victimKD, int killerStreak, int victimStreak, int kFactor) {
        String expr = formula
                .replace("{killer_elo}", String.valueOf(killerElo))
                .replace("killer_elo", String.valueOf(killerElo))
                .replace("{victim_elo}", String.valueOf(victimElo))
                .replace("victim_elo", String.valueOf(victimElo))
                .replace("{killer_kd}", String.valueOf(killerKD))
                .replace("killer_kd", String.valueOf(killerKD))
                .replace("{victim_kd}", String.valueOf(victimKD))
                .replace("victim_kd", String.valueOf(victimKD))
                .replace("{killer_streak}", String.valueOf(killerStreak))
                .replace("killer_streak", String.valueOf(killerStreak))
                .replace("{victim_streak}", String.valueOf(victimStreak))
                .replace("victim_streak", String.valueOf(victimStreak))
                .replace("{k_factor}", String.valueOf(kFactor))
                .replace("k_factor", String.valueOf(kFactor));

        try {
            return evaluateMathExpression(expr);
        } catch (Exception e) {
            plugin.getLogger().warning("Lỗi phân tích cú pháp công thức tùy chỉnh: " + formula + " -> " + expr + ". Lỗi: " + e.getMessage());
            return 0;
        }
    }

    private static double evaluateMathExpression(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Kí tự không hợp lệ: " + (char)ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor();
                    else if (eat('/')) {
                        double divisor = parseFactor();
                        if (divisor == 0) return 0;
                        x /= divisor;
                    }
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();

                double x;
                int startPos = this.pos;
                if (eat('(')) {
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Kí tự không hợp lệ: " + (char)ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor());

                return x;
            }
        }.parse();
    }
}
