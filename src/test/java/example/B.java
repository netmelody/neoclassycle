package example;

public final class B extends BofA {
    class M implements example.p.A {
        @Override
        public A getA() {
            return null;
        }
    }

    @Override
    public A getA() {
        return new A();
    }
}
