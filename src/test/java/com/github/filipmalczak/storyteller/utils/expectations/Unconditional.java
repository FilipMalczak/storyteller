package com.github.filipmalczak.storyteller.utils.expectations;

class Unconditional {
    public static class Success<E, T> implements Condition<E, T> {

        @Override
        public boolean isSatisfied(E expected, T tracked) {
            return true;
        }

        @Override
        public String describe() {
            return "success";
        }

        @Override
        public Condition<E, T> and(Condition<E, T> another) {
            return another;
        }

        @Override
        public Condition<E, T> or(Condition<E, T> another) {
            return this;
        }

        @Override
        public Condition<E, T> not() {
            return new Failure<>();
        }

        @Override
        public boolean isUnconditionalSuccess() {
            return true;
        }

        @Override
        public boolean isUnconditionalFailure() {
            return false;
        }
    }

    public static class Failure<E, T> implements Condition<E, T> {

        @Override
        public boolean isSatisfied(E expected, T tracked) {
            return false;
        }

        @Override
        public String describe() {
            return "failure";
        }

        @Override
        public Condition<E, T> and(Condition<E, T> another) {
            return this;
        }

        @Override
        public Condition<E, T> or(Condition<E, T> another) {
            return another;
        }

        @Override
        public Condition<E, T> not() {
            return new Success<>();
        }

        @Override
        public boolean isUnconditionalSuccess() {
            return false;
        }

        @Override
        public boolean isUnconditionalFailure() {
            return true;
        }
    }
}
