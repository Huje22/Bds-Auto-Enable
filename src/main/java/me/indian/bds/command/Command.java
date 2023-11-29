package me.indian.bds.command;

public abstract class Command {

    private final String name;
    private final String description;

    public Command(final String name, final String description) {
        this.name = "!" + name;
        this.description = description;
    }

    public abstract boolean onExecute(String player, String[] args, boolean isOp);

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    @Override
    public String toString() {
        return "Command(name=" + this.name +
                " description=" + this.description +
                ")";
    }
}