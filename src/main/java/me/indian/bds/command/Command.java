package me.indian.bds.command;

public abstract class Command {

    private final String name;
    private final String description;
    private final boolean isOp;

    public Command(final String name, final String description) {
        this(name, description, false);
    }

    public Command(final String name, final String description, final boolean isOp) {
        this.name = "!" + name;
        this.description = description;
        this.isOp = isOp;
    }

    public abstract boolean onExecute(String player, String[] args);

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public boolean isOp() {
        return this.isOp;
    }
}