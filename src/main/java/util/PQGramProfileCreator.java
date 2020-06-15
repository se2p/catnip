package util;

public abstract class PQGramProfileCreator {
    private static int p = 2;
    private static int q = 3;



    public static void setP(int p) {
        PQGramProfileCreator.p = p;
    }

    public static void setQ(int q) {
        PQGramProfileCreator.q = q;
    }

    public static int getP() {
        return p;
    }

    public static int getQ() {
        return q;
    }
}
