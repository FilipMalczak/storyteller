package com.github.filipmalczak.storyteller.utils.expectations.condition;

import com.github.filipmalczak.storyteller.utils.expectations.Describable;

import java.util.ArrayList;
import java.util.function.BiPredicate;

public interface Condition<E, T> extends Describable {
    boolean isSatisfied(E expected, T tracked);

    static <E, T> Condition<E, T> of(BiPredicate<E, T> predicate, String desc) {
        return new Condition<>() {
            @Override
            public boolean isSatisfied(E expected, T tracked) {
                return predicate.test(expected, tracked);
            }

            @Override
            public String describe() {
                return desc;
            }
        };
    }

    default Condition<E, T> and(Condition<E, T> another){
        if (another.isUnconditionalFailure())
            return another;
        if (another.isUnconditionalSuccess())
            return this;
        if (another instanceof AndCondition and){
            var parts = new ArrayList<Condition<E, T>>();
            parts.add(this);
            parts.addAll(and.getParts());
            return new AndCondition<>(parts);
        }
        return new AndCondition<>(this, another);
    }

    default Condition<E, T> or(Condition<E, T> another){
        if (another.isUnconditionalFailure())
            return this;
        if (another.isUnconditionalSuccess())
            return another;
        if (another instanceof OrCondition or){
            var parts = new ArrayList<Condition<E, T>>();
            parts.add(this);
            parts.addAll(or.getParts());
            return new OrCondition<>(parts);
        }
        return new OrCondition<>(this, another);
    }

    default Condition<E, T> not(){
        return new NotCondition<>(this);
    }

    default boolean isUnconditionalSuccess(){
        return this instanceof Unconditional.Success;
    }

    default boolean isUnconditionalFailure(){
        return this instanceof Unconditional.Failure;
    }

    static <E, T> Condition<E, T> not(Condition<E, T> condition){
        return condition.not();
    }

    static <E, T> Condition<E, T> success(){
        return new Unconditional.Success<>();
    }

    static <E, T> Condition<E, T> failure(){
        return new Unconditional.Failure<>();
    }

    static <E, T> Condition<E, T> ifThenElse(Condition<E, T> conditional, Condition<E, T> ifTrue, Condition<E, T> ifFalse){
        //if true then x else y => x
        if (conditional.isUnconditionalSuccess())
            return ifTrue;
        //if false then x else y => y
        if (conditional.isUnconditionalFailure())
            return ifFalse;
        //if x then true else y => if not(x) then y [else true]
        if (ifTrue.isUnconditionalSuccess()){
            return ifThen(conditional.not(), ifFalse);
        }
        //if x then false else y => not(x) and y
        if (ifTrue.isUnconditionalFailure())
            return conditional.not().and(ifFalse);
        // if x then y else false => x and y
        if (ifFalse.isUnconditionalFailure())
            return conditional.and(ifTrue);
        return new IfCondition<>(conditional, ifTrue, ifFalse);
    }

    static <E, T> Condition<E, T> ifThen(Condition<E, T> conditional, Condition<E, T> ifTrue){
        return ifThenElse(conditional, ifTrue, success());
    }
}
