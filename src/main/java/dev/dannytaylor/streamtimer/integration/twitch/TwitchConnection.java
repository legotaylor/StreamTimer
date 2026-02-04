/*
    StreamTimer
    Contributor(s): dannytaylor
    Github: https://github.com/legotaylor/StreamTimer
    Licence: LGPL-3.0
*/

package dev.dannytaylor.streamtimer.integration.twitch;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.*;
import com.github.twitch4j.common.enums.SubscriptionPlan;
import dev.dannytaylor.streamtimer.StreamTimerMain;
import dev.dannytaylor.streamtimer.config.StreamTimerConfig;
import dev.dannytaylor.streamtimer.integration.AuthConfig;
import dev.dannytaylor.streamtimer.timer.TimerUtils;

public class TwitchConnection {
    private TwitchClient client;
    private String username;

    public void toggleConnected() {
        if (!this.hasClient()) this.connect();
        else this.disconnect();
    }

    private void setButtons() {
        if (TwitchIntegration.connectButton != null) {
            TwitchIntegration.connectButton.setText(this.hasClient() ? "Disconnect" : "Connect");
            TwitchIntegration.connectButton.setEnabled(true);
        }
    }

    public void connect() {
        new Thread(() -> {
            if (!this.hasClient()) {
                System.out.println("[Stream Timer/Twitch Integration] Creating Client...");
                OAuth2Credential credential = null;
                try {
                    credential = new TwitchAuthenticator(AuthConfig.instance.twitchId.value(), AuthConfig.decrypt(AuthConfig.instance.twitchSecret.value()), AuthConfig.instance.twitchPort.value()).authenticate();
                } catch (Exception error) {
                    System.err.println("Failed to create twitch auth: " + error);
                }
                if (credential != null) {
                    this.client = TwitchClientBuilder.builder().withEnableChat(true).withChatAccount(credential).build();
                    this.registerEvents();
                    if (AuthConfig.instance.twitchChannel.value().isEmpty()) {
                        AuthConfig.instance.twitchChannel.setValue(credential.getUserName(), true);
                        TwitchIntegration.channel.setText(credential.getUserName());
                    }
                    this.join(this.username = AuthConfig.instance.twitchChannel.value()); // This could be changed in future to allow for configurable channels (e.g. multi-stream subbathons, connected via chatbot/mod account, etc.)
                }
            }
            this.setButtons();
        }, "TwitchIntegrationConnectionThread").start();
    }

    public void disconnect() {
        new Thread(() -> {
            if (this.hasClient()) {
                if (this.isConnected()) this.leave(this.username);
                System.out.println("[Stream Timer/Twitch Integration] Removing Client...");
                this.client.close();
                this.client = null;
            }
            this.setButtons();
        }, "TwitchIntegrationConnectionThread").start();
    }

    private void join(String channelName) {
        this.client.getChat().joinChannel(channelName);
        System.out.println("[Stream Timer/Twitch Integration] Joined " + channelName + "!");
    }

    private void leave(String channelName) {
        this.client.getChat().leaveChannel(channelName);
        System.out.println("[Stream Timer/Twitch Integration] Left " + channelName + "!");
    }

    public boolean hasClient() {
        return this.client != null;
    }

    public boolean isConnected() {
        return this.hasClient() && this.client.getChat().isChannelJoined(this.username);
    }

    private void registerEvents() {
        this.registerCommands();
        this.registerFollow();
        this.registerBits();
        this.registerSubs();
    }

    private void registerCommands() {
        this.client.getEventManager().onEvent(ChannelMessageEvent.class, event -> {
            if (StreamTimerConfig.instance.twitchTimes.commandEnabled.value().containsAny(event.getPermissions())) {
                String[] message = event.getMessage().split(" ");
                if (StreamTimerConfig.containsIgnoresCase(StreamTimerConfig.instance.twitchTimes.addCommand.value(), message[0])) {
                    String value = message[1];
                    boolean add = true;

                    if (value.charAt(0) == '-') {
                        add = false;
                        value = value.substring(1);
                    } else if (value.charAt(0) == '+') value = value.substring(1);

                    while (!Character.isDigit(value.charAt(0))) {
                        value = value.substring(1); // It's probably a money symbol, such as $.
                        if (value.isEmpty()) value = "0.00"; // fallback to 0
                    }

                    try {
                        addTime(Float.parseFloat(value), StreamTimerConfig.instance.twitchTimes.money.value(), StreamTimerConfig.instance.twitchTimes.moneySeconds.value(), "money command");
                    } catch (Exception error) {
                        System.err.println("[Stream Timer/Twitch Integration] Failed to parse money command: " + error);
                    }
                } else if (StreamTimerConfig.containsIgnoresCase(StreamTimerConfig.instance.twitchTimes.setCommand.value(), message[0])) {
                    String value = message[1];
                    long seconds = 0;
                    if (value.contains(":")) {
                        String[] parts = value.split(":");
                        if (parts.length == 3) seconds = TimerUtils.getSecondsFromString(parts[0], parts[1], parts[2], false);
                        else if (parts.length == 2) seconds = TimerUtils.getSecondsFromString("0", parts[0], parts[1], false);
                        else if (parts.length == 1) seconds = TimerUtils.getSecondsFromString("0", "0", parts[0], false);
                    } else seconds = TimerUtils.getSecondsFromString("0", "0", value, false);
                    TimerUtils.setTimer(seconds, false, true);
                } else if (StreamTimerConfig.containsIgnoresCase(StreamTimerConfig.instance.twitchTimes.toggleCommand.value(), message[0])) {
                    TimerUtils.toggleTimer();
                }
            }
        });
    }

    private void registerFollow() {
        this.client.getEventManager().onEvent(FollowEvent.class, event -> {
            if (StreamTimerConfig.instance.twitchTimes.followEnabled.value()) addTime(1, 1, StreamTimerConfig.instance.twitchTimes.followSeconds.value(), "follow");
        });
    }

    private void registerBits() {
        this.client.getEventManager().onEvent(CheerEvent.class, event -> {
            if (StreamTimerConfig.instance.twitchTimes.bitsEnabled.value()) addTime(event.getBits(), StreamTimerConfig.instance.twitchTimes.bits.value(), StreamTimerConfig.instance.twitchTimes.bitsSeconds.value(), "bits");
        });
    }

    private void registerSubs() {
        this.client.getEventManager().onEvent(GiftSubscriptionsEvent.class, event -> {
            SubscriptionPlan subPlan = event.getTier();
            System.out.println("[GiftSubscriptionsEvent] " + subPlan.ordinalName());
            if (subPlan.equals(SubscriptionPlan.TWITCH_PRIME) || subPlan.equals(SubscriptionPlan.TIER1)) onSub(1, event.getCount(), event.getTier().ordinalName() + " x" + event.getCount() + " Gifted Sub"); // You can't gift prime subs so we don't really have to check
            else if (subPlan.equals(SubscriptionPlan.TIER2)) onSub(2, event.getCount(), event.getTier().ordinalName() + " x" + event.getCount() + " Gifted Sub");
            else if (subPlan.equals(SubscriptionPlan.TIER3)) onSub(3, event.getCount(), event.getTier().ordinalName() + " x" + event.getCount() + " Gifted Sub");
        });
        this.client.getEventManager().onEvent(ExtendSubscriptionEvent.class, event -> {
            SubscriptionPlan subPlan = event.getSubPlan();
            System.out.println("[ExtendSubscriptionEvent] " + subPlan.ordinalName());
            if (subPlan.equals(SubscriptionPlan.TWITCH_PRIME) || subPlan.equals(SubscriptionPlan.TIER1)) onSub(1, 1, subPlan.ordinalName() + " Resub");
            else if (subPlan.equals(SubscriptionPlan.TIER2)) onSub(2, 1, subPlan.ordinalName() + " Resub");
            else if (subPlan.equals(SubscriptionPlan.TIER3)) onSub(3, 1, subPlan.ordinalName() + " Resub");
        });
        this.client.getEventManager().onEvent(SubscriptionEvent.class, event -> {
            // Is there a better way to do this?
            // Does this function as intended?
            // Why did the event get fired by the total amount gifted ever...
            if (!event.getGifted() && event.getMonths() == 0 && event.getSubStreak() == 0) {
                SubscriptionPlan subPlan = event.getSubPlan();
                System.out.println("[SubscriptionEvent] " + subPlan.ordinalName());
                if (subPlan.equals(SubscriptionPlan.TWITCH_PRIME) || subPlan.equals(SubscriptionPlan.TIER1)) onSub(1, 1, subPlan.ordinalName() + " Sub");
                else if (subPlan.equals(SubscriptionPlan.TIER2)) onSub(2, 1, subPlan.ordinalName() + " Sub");
                else if (subPlan.equals(SubscriptionPlan.TIER3)) onSub(3, 1, subPlan.ordinalName() + " Sub");
            }
        });
    }

    private void onSub(int tier, int amount, String name) {
        if (tier == 1 && StreamTimerConfig.instance.twitchTimes.tierOneEnabled.value()) addTime(amount, 1, StreamTimerConfig.instance.twitchTimes.tierOneSeconds.value(), name + " Sub");
        else if (tier == 2 && StreamTimerConfig.instance.twitchTimes.tierTwoEnabled.value()) addTime(amount, 1, StreamTimerConfig.instance.twitchTimes.tierTwoSeconds.value(), name + " Sub");
        else if (tier == 3 && StreamTimerConfig.instance.twitchTimes.tierThreeEnabled.value()) addTime(amount, 1, StreamTimerConfig.instance.twitchTimes.tierThreeSeconds.value(), name + " Sub");
    }

    private void addTime(float value, float equValue, int equSeconds, String type) {
        long secondsToAdd = (long) (value / equValue) * equSeconds;
        if (!StreamTimerMain.timer.isFinished()) {
            TimerUtils.setTimer(secondsToAdd, true, true);
            String message = "Added " + TimerUtils.getTime(secondsToAdd * 1000L) + " to the timer"; // we probably could send this to the twitch chat
            String extendedMessage = message + " via " + type + "! (" + value + ")";
            StreamTimerMain.gui.messageText.setText(extendedMessage);
            System.out.println("[Stream Timer/Twitch Integration] " + extendedMessage);
        } else {
            String log = "Did not add time as timer has finished (" + value + " " + type + ")";
            StreamTimerMain.gui.messageText.setText(log);
            System.out.println("[Stream Timer/Twitch Integration] " + log);
        }
    }
}
