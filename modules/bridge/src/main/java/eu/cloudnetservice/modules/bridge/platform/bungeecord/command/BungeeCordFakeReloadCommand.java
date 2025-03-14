/*
 * Copyright 2019-2023 CloudNetService team & contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.cloudnetservice.modules.bridge.platform.bungeecord.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public final class BungeeCordFakeReloadCommand extends Command {

  public BungeeCordFakeReloadCommand() {
    super("greload", "bungeecord.command.reload");
  }

  @Override
  public void execute(CommandSender sender, String[] args) {
    sender.sendMessage(TextComponent.fromLegacyText(
      "This is command is disabled as it might remove all servers which were previously registered to the proxy!",
      ChatColor.RED));
  }
}
