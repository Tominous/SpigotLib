package gyurix.protocol.wrappers.outpackets;

import gyurix.chat.ChatColor;
import gyurix.chat.ChatTag;
import gyurix.protocol.Reflection;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.scoreboard.team.CollisionRule;
import gyurix.scoreboard.team.NameTagVisibility;
import gyurix.spigotlib.SU;
import gyurix.spigotutils.ServerVersion;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static gyurix.protocol.event.PacketOutType.ScoreboardTeam;

/**
 * Created by GyuriX on 11/15/2016.
 */
public class PacketPlayOutScoreboardTeam extends WrappedPacket {
  private static final Class enumChatFormatCl = Reflection.getNMSClass("EnumChatFormat");
  private static final Field enumChatFormatF = Reflection.getFirstFieldOfType(enumChatFormatCl, char.class);
  private static final Method enumChatFormatM = Reflection.getMethod(enumChatFormatCl, "valueOf", String.class);
  /**
   * 0: create team
   * 1: remove team
   * 2: update team info
   * 3: add players to team
   * 4: remove players from team
   */
  public int action;
  public CollisionRule collisionRule;
  public int color;
  public String displayName = "";
  /**
   * 0x01: Allow friendly fire
   * 0x02: Can see invisible players on same team
   */
  public int friendFlags;
  public String name = "";
  public NameTagVisibility nameTagVisibility;
  public List<String> players;
  public String prefix = "";
  public String suffix = "";

  public PacketPlayOutScoreboardTeam(String name, String displayName,
                                     String prefix, String suffix,
                                     NameTagVisibility nameTagVisibility, CollisionRule collisionRule,
                                     int color, List<String> players,
                                     int action, int friendFlags) {
    this.name = name;
    this.displayName = displayName;
    this.prefix = prefix;
    this.suffix = suffix;
    this.nameTagVisibility = nameTagVisibility;
    this.collisionRule = collisionRule;
    this.color = color;
    this.players = players;
    this.action = action;
    this.friendFlags = friendFlags;
  }

  public PacketPlayOutScoreboardTeam() {

  }

  public PacketPlayOutScoreboardTeam(String name, int action) {
    this.name = name;
    this.action = action;
  }

  public PacketPlayOutScoreboardTeam(String name, int action, ArrayList<String> players) {
    this.name = name;
    this.action = action;
    this.players = players;
  }

  public Object getEnumChatFormat(char c) {
    try {
      return enumChatFormatM.invoke(null, ChatColor.forId(c).name().toUpperCase());
    } catch (Throwable e) {
      SU.error(SU.cs, e, "SpigotLib", "gyurix");
    }
    return null;
  }

  @Override
  public Object getVanillaPacket() {
    if (Reflection.ver.isAbove(ServerVersion.v1_13))
      return ScoreboardTeam.newPacket(name,
              ChatTag.fromColoredText(displayName).toICBC(),
              ChatTag.fromColoredText(prefix).toICBC(),
              ChatTag.fromColoredText(suffix).toICBC(),
              nameTagVisibility == null ? null : nameTagVisibility.name(),
              collisionRule == null ? null : collisionRule.name(),
              getEnumChatFormat((char) color),
              players == null ? new ArrayList<>() : new ArrayList<>(players),
              action,
              friendFlags);
    if (Reflection.ver.isAbove(ServerVersion.v1_9))
      return ScoreboardTeam.newPacket(name, displayName, prefix, suffix, nameTagVisibility == null ? null : nameTagVisibility.name(), collisionRule == null ? null : collisionRule.name(), color, players == null ? new ArrayList<>() : new ArrayList<>(players), action, friendFlags);
    else if (Reflection.ver.isAbove(ServerVersion.v1_8))
      return ScoreboardTeam.newPacket(name, displayName, prefix, suffix, nameTagVisibility == null ? null : nameTagVisibility.name(), color, players == null ? new ArrayList<>() : new ArrayList<>(players), action, friendFlags);
    else
      return ScoreboardTeam.newPacket(name, displayName, prefix, suffix, players == null ? new ArrayList<>() : new ArrayList<>(players), action, friendFlags);
  }

  @Override
  public void loadVanillaPacket(Object packet) {
    Object[] d = ScoreboardTeam.getPacketData(packet);
    name = (String) d[0];
    if (Reflection.ver.isAbove(ServerVersion.v1_13)) {
      displayName = ChatTag.fromICBC(d[1]).toColoredString();
      prefix = ChatTag.fromICBC(d[2]).toColoredString();
      suffix = ChatTag.fromICBC(d[3]).toColoredString();
    } else {
      displayName = (String) d[1];
      prefix = (String) d[2];
      suffix = (String) d[3];
    }
    int from = 3;
    if (Reflection.ver.isAbove(ServerVersion.v1_8)) {
      String name = (String) d[++from];
      nameTagVisibility = name == null ? null : NameTagVisibility.valueOf(name);
      if (Reflection.ver.isAbove(ServerVersion.v1_9)) {
        name = (String) d[++from];
        collisionRule = name == null ? null : CollisionRule.valueOf(name);
      }
      if (Reflection.ver.isAbove(ServerVersion.v1_13))
        try {
          color = (int) (char) enumChatFormatF.get(d[++from]);
        } catch (Throwable e) {
          SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
      else
        color = (int) d[++from];
    }
    players = new ArrayList<>((Collection<String>) d[++from]);
    action = (int) d[++from];
    friendFlags = (int) d[++from];
  }
}
