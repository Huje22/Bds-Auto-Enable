package me.indian.bds.command;

public abstract class Command {

    private final String name, description, usage;

    public Command(final String name, final String description, final String usage) {
        this.name = "!" + name;
        this.description = description;
        this.usage = usage;
    }

    public Command(final String name, final String description) {
        this(name, description, "");
    }

    public abstract boolean onExecute(String player, String[] args, boolean isOp);

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getUsage(){
        return this.usage;
    }

    @Override
    public String toString() {
        return "Command(name=" + this.name +
                " description=" + this.description +
                ")";
    }
}
