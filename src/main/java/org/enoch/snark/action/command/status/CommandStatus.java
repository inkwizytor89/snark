package org.enoch.snark.action.command.status;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommandStatus {
    private ExecutionStatus status;
    private ExecutionIssue issue;
    private String reason;
    private int failed;

    public void failed() {
        failed++;
    }

    public void setSuccess() {
        setStatus(ExecutionStatus.SUCCESS);
        setIssue(ExecutionIssue.NO_ISSUE);
    }

    public void setFailed(ExecutionIssue executionIssue) {
        setStatus(ExecutionStatus.FAILED);
        setIssue(executionIssue);
    }
}
