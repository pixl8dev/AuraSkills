package dev.aurelium.auraskills.bukkit.skill;

import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.BlockXpSource;
import dev.aurelium.auraskills.bukkit.AuraSkills;
import dev.aurelium.auraskills.bukkit.skills.foraging.Treecapitator;
import dev.aurelium.auraskills.common.skill.SkillManager;
import dev.aurelium.auraskills.common.source.type.BlockSource;
import dev.aurelium.auraskills.common.util.TestSession;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SkillManagerTest {

    private static AuraSkills plugin;
    private static ServerMock server;

    @BeforeAll
    static void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(AuraSkills.class, TestSession.create());
        server.getScheduler().performOneTick();
    }

    @AfterAll
    static void unload() {
        MockBukkit.unmock();
    }

    @Test
    void testGetSourcesOfType() {
        SkillManager skillManager = plugin.getSkillManager();

        List<SkillSource<BlockXpSource>> blockXpSources = skillManager.getSourcesOfType(BlockXpSource.class);
        assertFalse(blockXpSources.isEmpty());
        List<SkillSource<BlockXpSource>> blockXpSources2 = skillManager.getSourcesOfType(BlockXpSource.class);
        assertFalse(blockXpSources2.isEmpty());
        assertEquals(blockXpSources, blockXpSources2);

        List<SkillSource<BlockSource>> blockSources = skillManager.getSourcesOfType(BlockSource.class);
        assertFalse(blockSources.isEmpty());
        assertEquals(blockSources, blockXpSources);
    }

    @Test
    void testTreecapitatorMatchesAxeWithoutWorldEditWand() {
        PlayerMock player = server.addPlayer();
        TestTreecapitator treecapitator = new TestTreecapitator(plugin);

        assertTrue(treecapitator.matches(Material.DIAMOND_AXE, player));
    }

    @Test
    void testTreecapitatorRejectsConfiguredWorldEditWand() throws ReflectiveOperationException {
        PlayerMock player = server.addPlayer();
        player.addAttachment(plugin, "worldedit.wand", true);

        TestTreecapitator treecapitator = new TestTreecapitator(plugin);
        Field field = Treecapitator.class.getDeclaredField("worldEditWandMaterial");
        field.setAccessible(true);
        field.set(treecapitator, Material.WOODEN_AXE);

        assertFalse(treecapitator.matches(Material.WOODEN_AXE, player));
        assertTrue(treecapitator.matches(Material.DIAMOND_AXE, player));
    }

    private static final class TestTreecapitator extends Treecapitator {

        private TestTreecapitator(AuraSkills plugin) {
            super(plugin);
        }

        private boolean matches(Material material, Player player) {
            return materialMatches(material.toString(), player);
        }
    }
}
