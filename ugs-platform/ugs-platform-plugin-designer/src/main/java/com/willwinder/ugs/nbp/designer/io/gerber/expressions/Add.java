package com.willwinder.ugs.nbp.designer.io.gerber.expressions;

public record Add(Expression a, Expression b) implements Expression {
    public double eval(double[] params) { return a.eval(params) + b.eval(params); }
}