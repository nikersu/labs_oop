package operations;

import functions.MathFunction;

public abstract class SteppingDifferentialOperator implements DifferentialOperator<MathFunction> {
    protected double step;
    public SteppingDifferentialOperator(double step) {
        if (step <= 0 || Double.isNaN(step) || Double.isInfinite(step)) {
            throw new IllegalArgumentException("Incorrect differentiation step");
        }
        this.step = step;
    }
    public double getStep() {
        return step;
    }
    public void setStep(double step) {
        if (step <= 0 || Double.isNaN(step) || Double.isInfinite(step)) {
            throw new IllegalArgumentException("Incorrect differentiation step");
        }
        this.step = step;
    }
}