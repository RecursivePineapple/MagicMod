package magicmod.server.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import magicmod.common.mechanics.AuraBuffer;
import magicmod.common.mechanics.IAuraBuffer;
import magicmod.common.mechanics.aura.AuraFieldRegistry;
import magicmod.common.mechanics.aura.IAuraField;
import magicmod.common.util.MCUtils;

public class AuraFieldCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "aura-fields";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/aura-fields";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (!(sender instanceof EntityPlayer player)) {
            MCUtils.sendErrorMessage(sender, "This command can only be ran by players");
            return;
        }

        for (IAuraField field : AuraFieldRegistry.getFields(player.worldObj)) {
            IAuraBuffer buffer = field.getBuffer((int) player.posX, (int) player.posY, (int) player.posZ, 16d);

            MCUtils.sendInfoMessage(sender, field + ": " + buffer);
        }
    }
}
