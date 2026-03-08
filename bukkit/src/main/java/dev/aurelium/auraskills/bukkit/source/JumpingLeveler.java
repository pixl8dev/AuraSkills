package dev.aurelium.auraskills.bukkit.source;

import com.google.common.collect.Sets;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.type.JumpingXpSource;
import dev.aurelium.auraskills.bukkit.AuraSkills;
import dev.aurelium.auraskills.bukkit.util.CompatUtil;
import dev.aurelium.auraskills.common.source.SourceTypes;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class JumpingLeveler extends SourceLeveler {

    private final Set<UUID> prevPlayersOnGround = Sets.newConcurrentHashSet();
    private final Map<UUID, Integer> jumpCounts = new ConcurrentHashMap<>();

    public JumpingLeveler(AuraSkills plugin) {
        super(plugin, SourceTypes.JUMPING);
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onJump(PlayerMoveEvent event) {
        if (disabled()) return;
        Player player = event.getPlayer();

        handleJump(player, event);

        if (player.isOnGround()) {
            prevPlayersOnGround.add(player.getUniqueId());
        } else {
            prevPlayersOnGround.remove(player.getUniqueId());
        }
    }

    @SuppressWarnings("deprecation")
    private void handleJump(Player player, PlayerMoveEvent event) {
        if (player.getVelocity().getY() <= 0) {
            return;
        }

        double jumpVelocity = 0.42F;
        if (CompatUtil.hasEffect(player, Set.of("jump", "jump_boost"))) {
            PotionEffect effect = CompatUtil.getEffect(player, Set.of("jump", "jump_boost"));
            if (effect != null) {
                jumpVelocity += ((float) (effect.getAmplifier() + 1) * 0.1F);
            }
        }
        if (player.getLocation().getBlock().getType() == Material.LADDER || !prevPlayersOnGround.contains(player.getUniqueId())) {
            return;
        }
        if (player.isOnGround() || Double.compare(player.getVelocity().getY(), jumpVelocity) != 0) {
            return;
        }
        var skillSource = plugin.getSkillManager().getSingleSourceOfType(JumpingXpSource.class);
        if (skillSource == null) return;

        JumpingXpSource source = skillSource.source();
        Skill skill = skillSource.skill();

        UUID uuid = player.getUniqueId();
        int jumpCount = jumpCounts.merge(uuid, 1, Integer::sum);
        if (jumpCount >= source.getInterval()) {
            if (failsChecks(event, player, player.getLocation(), skill)) return;

            plugin.getLevelManager().addXp(plugin.getUser(player), skill, source, source.getXp());
            jumpCounts.remove(uuid);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        prevPlayersOnGround.remove(player.getUniqueId());
        jumpCounts.remove(player.getUniqueId());
    }

    public void clearJumpState() {
        prevPlayersOnGround.clear();
        jumpCounts.clear();
    }

}
