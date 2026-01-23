package com.willwinder.ugs.nbp.designer.io.gerber.expressions;

public record Constant(double value) implements Expression {
    public double eval(double[] params) { return value; }
}