package org.enoch.snark.action.command;

public class SendMessageToPlayerCommand extends AbstractCommand {

    public final String herf;
    public final String message;

    public SendMessageToPlayerCommand(String herf, String message) {
        super();
        this.herf = herf;
        this.message = message;
    }

    @Override
    public String toString() {
        return "write message to player";
    }
}
