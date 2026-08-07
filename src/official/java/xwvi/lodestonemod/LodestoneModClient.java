package xwvi.lodestonemod;

import java.util.List;
import java.util.Optional;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.Level;

public class LodestoneModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ItemTooltipCallback.EVENT.register(LodestoneModClient::appendLodestoneTooltip);
	}

	private static void appendLodestoneTooltip(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, TooltipFlag type, List<Component> lines) {
		if (stack.getItem() != Items.COMPASS) {
			return;
		}

		LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
		if (tracker == null) {
			return;
		}

		Optional<GlobalPos> target = tracker.target();
		if (target.isEmpty()) {
			lines.add(Component.translatable("lodestonemod.tooltip.unbound").withStyle(ChatFormatting.GRAY));
			return;
		}

		GlobalPos globalPos = target.get();
		ResourceKey<Level> dimension = globalPos.dimension();
		BlockPos pos = globalPos.pos();

		Component dimensionText = Component.literal(formatDimension(dimension)).withStyle(ChatFormatting.YELLOW);
		lines.add(Component.translatable("lodestonemod.tooltip.dimension", dimensionText).withStyle(ChatFormatting.GRAY));
		lines.add(Component.translatable("lodestonemod.tooltip.coords", pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.GRAY));

		Minecraft client = Minecraft.getInstance();
		Level currentWorld = client.level;
		if (currentWorld != null && currentWorld.dimension().equals(dimension)) {
			BlockPos playerPos = client.player != null ? client.player.blockPosition() : null;
			if (playerPos != null) {
				double dx = playerPos.getX() - pos.getX();
				double dy = playerPos.getY() - pos.getY();
				double dz = playerPos.getZ() - pos.getZ();
				long distance = Math.round(Math.sqrt(dx * dx + dy * dy + dz * dz));
				lines.add(Component.translatable("lodestonemod.tooltip.distance", distance).withStyle(ChatFormatting.GRAY));
			}
		} else {
			lines.add(Component.translatable("lodestonemod.tooltip.different_dimension").withStyle(ChatFormatting.GRAY));
		}
	}

	private static String formatDimension(ResourceKey<Level> dimension) {
		return switch (dimension.identifier().getPath()) {
			case "overworld" -> "Overworld";
			case "the_nether" -> "The Nether";
			case "the_end" -> "The End";
			default -> dimension.identifier().getPath();
		};
	}
}
