package com.github.filipmalczak.storyteller.utils.expectations.condition;

import lombok.Value;

@Value
class IfCondition<E, T> implements Condition<E, T> {
    Condition<E, T> conditional;
    Condition<E, T> ifTrue;
    Condition<E, T> ifFalse;

    @Override
    public boolean isSatisfied(E expected, T tracked) {
        return conditional.isSatisfied(expected, tracked) ? ifTrue.isSatisfied(expected, tracked) : ifFalse.isSatisfied(expected, tracked);
    }

    @Override
    public String describe() {
        String out = "(if {"+conditional.describe()+"} then {"+ifTrue.describe()+"}";
        if (!(ifFalse instanceof Unconditional.Success))
            out += " else {"+ifFalse.describe()+"}";
        out += ")";
        return out;
    }
}
