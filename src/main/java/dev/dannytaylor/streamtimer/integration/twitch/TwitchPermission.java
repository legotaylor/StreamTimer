/*
    StreamTimer
    Contributor(s): dannytaylor
    Github: https://github.com/legotaylor/StreamTimer
    Licence: LGPL-3.0
*/

package dev.dannytaylor.streamtimer.integration.twitch;

import com.github.twitch4j.common.enums.CommandPermission;
import org.quiltmc.config.api.values.ComplexConfigValue;
import org.quiltmc.config.api.values.ConfigSerializableObject;

import java.util.List;
import java.util.Set;

public enum TwitchPermission implements ConfigSerializableObject<String> {
    EVERYONE("Everyone", CommandPermission.EVERYONE, CommandPermission.VIP, CommandPermission.MODERATOR, CommandPermission.BROADCASTER),
    SUBSCRIBER("Subscribers, VIPS, Moderators, Broadcaster", CommandPermission.SUBSCRIBER, CommandPermission.VIP, CommandPermission.MODERATOR, CommandPermission.BROADCASTER),
    VIP("VIPS, Moderators, Broadcaster", CommandPermission.VIP, CommandPermission.MODERATOR, CommandPermission.BROADCASTER),
    MODERATOR("Moderators, Broadcaster", CommandPermission.MODERATOR, CommandPermission.BROADCASTER),
    BROADCASTER("Broadcaster", CommandPermission.BROADCASTER),
    NONE("Disabled");

    private final List<CommandPermission> permissions;
    public final String label;

    TwitchPermission(String label, CommandPermission... permissions) {
        this.label = label;
        this.permissions = List.of(permissions);
    }

    public boolean hasPermission(CommandPermission permission) {
        return this.permissions.contains(permission);
    }

    public boolean containsAny(Set<CommandPermission> permissions) {
        for (CommandPermission permission : permissions) {
            if (this.permissions.contains(permission)) return true;
        }
        return false;
    }

    public List<CommandPermission> getPermissions() {
        return this.permissions;
    }

    @Override
    public ConfigSerializableObject<String> convertFrom(String representation) {
        return valueOf(representation);
    }

    @Override
    public String getRepresentation() {
        return this.name();
    }

    @Override
    public ComplexConfigValue copy() {
        return this;
    }
}
