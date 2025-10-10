package functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdentityFunctionTest {

    @org.junit.jupiter.api.Test
    void apply_1equals1() {
        for (int i = 0; i < 1000; i++) {
            IdentityFunction x = new IdentityFunction();

            double result = x.apply(1.0);

            assertEquals(1.0, result);
        }
    }

    @Test
    void apply_2equals2() {

            IdentityFunction x = new IdentityFunction();

            double result = x.apply(2.3);

            assertEquals(2.3, result);
    }


    @Test
    void apply_equals() {

        IdentityFunction x = new IdentityFunction();

        double result = x.apply(0.001);

        assertEquals(0.001, result);
    }
}
