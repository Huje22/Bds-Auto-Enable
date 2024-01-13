package me.indian.bds.util;

import java.util.List;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.RichPresence;

public class UnUsed {

    private UnUsed(){}


    public String getActivity(final Member member) {
        final List<Activity> activities = member.getActivities();
        final StringBuilder activityMessage = new StringBuilder();
        int counter = 0;

        if (activities.isEmpty()) return "";

        for (final Activity activity : activities) {
            final RichPresence richPresence = activity.asRichPresence();
            final String detal = (richPresence == null ? "" : (richPresence.getDetails() == null ? "" : " &e=&3 " + richPresence.getDetails()));
            final String url = (activity.getUrl() == null ? "" : activity.getUrl());

            switch (activity.getType()) {
                case LISTENING ->
                        activityMessage.append("&a").append(activity.getName()).append(":&1 ").append(detal.replaceAll(" &e=&3 ", "")).append(counter < activities.size() - 1 ? "&r\n" : "");
                case STREAMING ->
                        activityMessage.append(activity.getName()).append(" ").append(url).append(detal).append(counter < activities.size() - 1 ? "&r\n" : "");
                case CUSTOM_STATUS -> {
                    if (!activity.getName().equalsIgnoreCase("Custom Status")) {
                        activityMessage
                                /* .append(activity.getEmoji() == null ? "" : activity.getEmoji().getName()) */
                                .append(" ").append(activity.getName()).append(detal).append(counter < activities.size() - 1 ? "&r\n" : "");
                    }
                }
//                case COMPETING ->  {}
//                case WATCHING -> {}
                case PLAYING ->
                        activityMessage.append("Gra w:&1 ").append(activity.getName()).append(detal).append(counter < activities.size() - 1 ? "&r\n" : "");
                default ->
                        activityMessage.append(activity.getName()).append("  ").append(detal).append(counter < activities.size() - 1 ? "&r\n" : "");
            }
            counter++;
        }
        return activityMessage + "\n";
    }
}
