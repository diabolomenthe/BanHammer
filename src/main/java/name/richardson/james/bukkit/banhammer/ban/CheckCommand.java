/*******************************************************************************
 * Copyright (c) 2012 James Richardson.
 * 
 * CheckCommand.java is part of BanHammer.
 * 
 * BanHammer is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * BanHammer is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * BanHammer. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package name.richardson.james.bukkit.banhammer.ban;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import name.richardson.james.bukkit.banhammer.BanHammer;
import name.richardson.james.bukkit.banhammer.BanHandler;
import name.richardson.james.bukkit.banhammer.BanRecord;
import name.richardson.james.bukkit.util.Time;
import name.richardson.james.bukkit.util.command.CommandArgumentException;
import name.richardson.james.bukkit.util.command.CommandPermissionException;
import name.richardson.james.bukkit.util.command.CommandUsageException;
import name.richardson.james.bukkit.util.command.PlayerCommand;

public class CheckCommand extends PlayerCommand {

  public static final String NAME = "check";
  public static final String DESCRIPTION = "Check if a player is banned";
  public static final String PERMISSION_DESCRIPTION = "Allow users to check if a player is banned.";
  public static final String USAGE = "<name>";

  public static final Permission PERMISSION = new Permission("banhammer.check", CheckCommand.PERMISSION_DESCRIPTION, PermissionDefault.OP);

  private final BanHandler handler;

  public CheckCommand(final BanHammer plugin) {
    super(plugin, CheckCommand.NAME, CheckCommand.DESCRIPTION, CheckCommand.USAGE, CheckCommand.PERMISSION_DESCRIPTION, CheckCommand.PERMISSION);
    handler = plugin.getHandler(CheckCommand.class);
  }

  @Override
  public void execute(final CommandSender sender, final Map<String, Object> arguments) throws CommandArgumentException, CommandPermissionException, CommandUsageException {
    final String playerName = (String) arguments.get("playerName");
    final BanRecord ban = handler.getPlayerBan(playerName);

    if (ban != null) {
      if (ban.isActive()) {
        sender.sendMessage(String.format(ChatColor.RED + "%s is banned.", playerName));
        sendBanDetail(sender, ban);
      } else {
        sender.sendMessage(String.format(ChatColor.YELLOW + "%s is not banned.", playerName));
      }
    } else {
      sender.sendMessage(String.format(ChatColor.YELLOW + "%s is not banned.", playerName));
    }

  }

  @Override
  public Map<String, Object> parseArguments(final List<String> arguments) throws CommandArgumentException {
    final Map<String, Object> m = new HashMap<String, Object>();

    try {
      m.put("playerName", arguments.get(0));
    } catch (final IndexOutOfBoundsException e) {
      throw new CommandArgumentException("You must specify a valid player name", "You need to type the whole name.");
    }

    return m;
  }

  protected void sendBanDetail(final CommandSender sender, final BanRecord ban) {
    final Date createdDate = new Date(ban.getCreatedAt());
    final DateFormat dateFormat = new SimpleDateFormat("MMM d");
    final String createdAt = dateFormat.format(createdDate);
    sender.sendMessage(String.format(ChatColor.YELLOW + "Banned by %s on %s", ban.getCreatedBy(), createdAt));
    sender.sendMessage(String.format(ChatColor.YELLOW + "- Reason: %s.", ban.getReason()));
    switch (ban.getType()) {
    case PERMENANT:
      sender.sendMessage(ChatColor.YELLOW + "- Length: Permanent.");
      break;
    case TEMPORARY:
      final Date expiryDate = new Date(ban.getExpiresAt());
      final DateFormat expiryDateFormat = new SimpleDateFormat("MMM d H:mm a ");
      final String expiryDateString = expiryDateFormat.format(expiryDate) + "(" + Calendar.getInstance().getTimeZone().getDisplayName() + ")";
      final Long banTime = ban.getExpiresAt() - ban.getCreatedAt();
      sender.sendMessage(String.format(ChatColor.YELLOW + "- Length: %s", Time.millisToLongDHMS(banTime)));
      sender.sendMessage(String.format(ChatColor.YELLOW + "- Expires on: %s", expiryDateString));
      break;
    }
  }

}
