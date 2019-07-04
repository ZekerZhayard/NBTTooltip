package zabi.minecraft.nbttooltip;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zabi.minecraft.nbttooltip.config.ModConfig;

@Mod(name=NBTTooltip.MOD_NAME, modid=NBTTooltip.MOD_ID, version="0.6", clientSideOnly=true, acceptedMinecraftVersions="[1.8,1.8.9]", guiFactory="zabi.minecraft.nbttooltip.config.ModGuiFactory")
public class NBTTooltip {
	
	public static final String MOD_NAME = "NBT Tooltip";
	public static final String MOD_ID = "nbttooltip";
	
	private static final String FORMAT = EnumChatFormatting.ITALIC.toString()+EnumChatFormatting.DARK_GRAY;
	private static int line_scrolled = 0, time = 0;
	
	@SideOnly(Side.CLIENT)
	@EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		MinecraftForge.EVENT_BUS.register(this);
		ModConfig.init(evt.getSuggestedConfigurationFile());
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onTooltip(ItemTooltipEvent evt) {
		if (!ModConfig.requiresf3 || evt.showAdvancedItemTooltips) {
			NBTTagCompound tag = evt.itemStack.getTagCompound();
			ArrayList<String> ttip = new ArrayList<String>(ModConfig.maxLinesShown);
			if (tag!=null) {
				evt.toolTip.add("");

				if (ModConfig.showDelimiters) {
					ttip.add(EnumChatFormatting.DARK_PURPLE+" - nbt start -");
				}
				if (ModConfig.compress) {
					ttip.add(FORMAT+tag.toString());
				} else {
					unwrapTag(ttip, tag, FORMAT, "", ModConfig.compress?"":"  ");
				}
				if (ModConfig.showDelimiters) {
					ttip.add(EnumChatFormatting.DARK_PURPLE+" - nbt end -");
				}
				ttip = transformTtip(ttip);

				evt.toolTip.addAll(ttip);
			} else {
				evt.toolTip.add(FORMAT+"No NBT tag");
			}
		}
	}

//	@SideOnly(Side.CLIENT)
//	@SubscribeEvent
//	public void onRightClick(RightClickBlock evt) {
//		if (ModConfig.fetchType==EnumFetchType.DISABLED) return;
//		if ((evt.getWorld().isRemote && ModConfig.fetchType != EnumFetchType.SERVER) || (!evt.getWorld().isRemote && ModConfig.fetchType!=EnumFetchType.CLIENT)) {
//			ItemStack stack = evt.getEntityPlayer().getHeldItem(evt.getHand());
//			if (evt.getWorld().getTileEntity(evt.getPos())!=null && stack.getItem()==Items.arrow) {
//				ArrayList<String> tag = new ArrayList<String>();
//				NBTTooltip.unwrapTag(tag, evt.getWorld().getTileEntity(evt.getPos()).writeToNBT(new NBTTagCompound()), "", "", "\t");
//				final StringBuilder sb = new StringBuilder();
//				tag.forEach(s -> {
//					sb.append(s);
//					sb.append('\n');
//				});
//				new InfoWindow(sb.toString(), evt.getWorld().isRemote);
//			}
//		}
//	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent evt) {
		if (evt.phase == Phase.END && !GuiScreen.isShiftKeyDown()) {
			time++;
			if (time>=ModConfig.ticksBeforeScroll/(GuiScreen.isAltKeyDown()?4:1)) {
				time = 0;
				line_scrolled++;
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	private static ArrayList<String> transformTtip(ArrayList<String> ttip) {
		ArrayList<String> newttip = new ArrayList<String>(ModConfig.maxLinesShown);
		if (ModConfig.showSeparator) {
			newttip.add("- NBTTooltip -");
		}
		for (int i = ttip.size() - 1; i >= 0; i--) {
		    String tip = ttip.get(i);
		    if (tip.length() > ModConfig.maxCharactorsShown) {
		        int k = 1;
		        for (int j = ModConfig.maxCharactorsShown; j < tip.length(); j += ModConfig.maxCharactorsShown) {
		            ttip.add(i + k, tip.substring(j - ModConfig.maxCharactorsShown, j));
		            k++;
		        }
		        ttip.remove(i);
		    }
		}
		if (ttip.size()>ModConfig.maxLinesShown) {
			if (ModConfig.maxLinesShown+line_scrolled>ttip.size()) line_scrolled = 0;
			for (int i = 0; i < ModConfig.maxLinesShown; i++) {
				newttip.add(ttip.get(i+line_scrolled));
			}
			return newttip;
		} else {
			line_scrolled = 0;
			newttip.addAll(ttip);
			return newttip;
		}
	}

	@SideOnly(Side.CLIENT)
	static void unwrapTag(List<String> tooltip, NBTBase base, String pad, @Nonnull String tagName, String padIncrement) {
		if (base.getId()==10) {
			NBTTagCompound tag = (NBTTagCompound) base;
			tag.getKeySet().forEach(s -> {
				boolean nested = tag.getTag(s).getId()==10 || tag.getTag(s).getId()==9;
				if (nested) {
					tooltip.add(pad+s+": {");
					unwrapTag(tooltip, tag.getTag(s), pad+padIncrement, s, padIncrement);
					tooltip.add(pad+"}");
				} else {
					addValueToTooltip(tooltip, tag.getTag(s), s, pad);
				}
			});
		} else if (base.getId()==9) {
			NBTTagList tag = (NBTTagList) base;
			int index = 0;
			Iterator<NBTBase> iter = tag.tagList.iterator();
			while (iter.hasNext()) {
				NBTBase nbtnext = iter.next();
				if (nbtnext.getId()==10 || nbtnext.getId()==9) {
					tooltip.add(pad + "["+index+"]: {");
					unwrapTag(tooltip, nbtnext, pad+padIncrement, "", padIncrement);
					tooltip.add(pad+"}");
				} else {
					tooltip.add(pad+"["+index+"] -> "+nbtnext.toString());
				}
				index++;
			}
		} else {
			addValueToTooltip(tooltip, base, tagName, pad);
		}
	}

//	@SideOnly(Side.CLIENT)
//	@SubscribeEvent
//	public void onConfigChanged(ConfigChangedEvent evt) {
//		if (evt.modID.equals(MOD_ID)) {
//			ConfigManager.sync(MOD_ID, Type.INSTANCE);
//		}
//	}
	
	private static void addValueToTooltip(List<String> tooltip, NBTBase nbt, String name, String pad) {
		tooltip.add(pad+name+": "+nbt.toString());
	}

}
