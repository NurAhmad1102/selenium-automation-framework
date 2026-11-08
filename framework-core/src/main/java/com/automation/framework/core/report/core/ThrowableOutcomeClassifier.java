package com.automation.framework.core.report.core;

import com.automation.framework.core.report.internal.model.OutcomeCategory;
import com.automation.framework.core.report.internal.model.OutcomeStatus;
import com.automation.framework.core.report.internal.model.TestOutcome;

public final class ThrowableOutcomeClassifier {

    private ThrowableOutcomeClassifier() {
    }

    public static TestOutcome classify(Throwable throwable) {
        if (throwable == null) {
            return new TestOutcome(OutcomeStatus.SKIP, OutcomeCategory.LIFECYCLE, "No reason provided");
        }

        Throwable current = throwable;
        while (current != null) {
            TestOutcome outcome = classifySingle(current);
            if (outcome != null) {
                return outcome;
            }
            Throwable cause = current.getCause();
            current = cause != current ? cause : null;
        }

        return new TestOutcome(OutcomeStatus.FAIL, OutcomeCategory.LIFECYCLE, throwable.getMessage());
    }

    public static boolean isInfrastructureSkip(Throwable throwable) {
        TestOutcome outcome = classify(throwable);
        return outcome.getStatus() == OutcomeStatus.SKIP || outcome.getStatus() == OutcomeStatus.BROKEN;
    }

    private static TestOutcome classifySingle(Throwable throwable) {
        String className = throwable.getClass().getName();
        String message = throwable.getMessage();
        String lowerMessage = message != null ? message.toLowerCase() : "";

        if ("org.testng.SkipException".equals(className)) {
            return new TestOutcome(OutcomeStatus.SKIP, OutcomeCategory.LIFECYCLE, message);
        }

        if (className.endsWith("AssertionError") || throwable instanceof AssertionError) {
            return new TestOutcome(OutcomeStatus.FAIL, OutcomeCategory.ASSERTION, message);
        }

        if (className.contains("SessionNotCreatedException") ||
                className.contains("NoSuchSessionException") ||
                lowerMessage.contains("sessionnotcreatedexception") ||
                lowerMessage.contains("session not created") ||
                lowerMessage.contains("nosuchsessionexception") ||
                lowerMessage.contains("no such session") ||
                lowerMessage.contains("err_connection_timed_out") ||
                lowerMessage.contains("err_connection_refused") ||
                lowerMessage.contains("connection timed out")) {
            return new TestOutcome(OutcomeStatus.BROKEN, OutcomeCategory.INFRASTRUCTURE, message);
        }

        if (className.contains("NoSuchElementException") ||
                className.contains("TimeoutException") ||
                className.contains("StaleElementReferenceException") ||
                className.contains("ElementClickInterceptedException")) {
            return new TestOutcome(OutcomeStatus.FAIL, OutcomeCategory.ELEMENT, message);
        }

        if (lowerMessage.contains("dependency") ||
                lowerMessage.contains("depends on") ||
                lowerMessage.contains("precondition") ||
                lowerMessage.contains("prerequisite")) {
            return new TestOutcome(OutcomeStatus.BLOCKED, OutcomeCategory.DEPENDENCY, message);
        }

        return null;
    }
}
