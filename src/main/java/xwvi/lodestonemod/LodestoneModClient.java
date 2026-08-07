package xwvi.lodestonemod;

import java.util.List;
import java.util.Optional;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;

public class LodestoneModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ItemTooltipCallback.EVENT.register(LodestoneModClient::appendLodestoneTooltip);
	}

	private static void appendLodestoneTooltip(ItemStack stack, Item.TooltipContext context, TooltipType type, List<Text> lines) {
		if (!stack.isOf(Items.COMPASS)) {
			return;
		}

		LodestoneTrackerComponent tracker = stack.get(DataComponentTypes.LODESTONE_TRACKER);
		if (tracker == null) {
			return;
		}

		Optional<GlobalPos> target = tracker.target();
		if (target.isEmpty()) {
			lines.add(Text.translatable("lodestonemod.tooltip.unbound").formatted(Formatting.GRAY));
			return;
		}

		GlobalPos globalPos = target.get();
		RegistryKey<World> dimension = globalPos.dimension();
		BlockPos pos = globalPos.pos();

		Text dimensionText = Text.literal(formatDimension(dimension)).formatted(Formatting.YELLOW);
		lines.add(Text.translatable("lodestonemod.tooltip.dimension", dimensionText).formatted(Formatting.GRAY));
		lines.add(Text.translatable("lodestonemod.tooltip.coords", pos.getX(), pos.getY(), pos.getZ()).formatted(Formatting.GRAY));

		MinecraftClient client = MinecraftClient.getInstance();
		World currentWorld = client.world;
		if (currentWorld != null && currentWorld.getRegistryKey().equals(dimension)) {
			BlockPos playerPos = client.player != null ? client.player.getBlockPos() : null;
			if (playerPos != null) {
				double dx = playerPos.getX() - pos.getX();
				double dy = playerPos.getY() - pos.getY();
				double dz = playerPos.getZ() - pos.getZ();
				long distance = Math.round(Math.sqrt(dx * dx + dy * dy + dz * dz));
				lines.add(Text.translatable("lodestonemod.tooltip.distance", distance).formatted(Formatting.GRAY));
			}
		} else {
			lines.add(Text.translatable("lodestonemod.tooltip.different_dimension").formatted(Formatting.GRAY));
		}
	}

	private static String formatDimension(RegistryKey<World> dimension) {
		return switch (dimension.getValue().getPath()) {
			case "overworld" -> "Overworld";
			case "the_nether" -> "The Nether";
			case "the_end" -> "The End";
			default -> dimension.getValue().getPath();
		};
	}
}
