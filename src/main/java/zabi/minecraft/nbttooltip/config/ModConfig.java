package zabi.minecraft.nbttooltip.config;

import java.io.File;
import java.util.Arrays;

import net.minecraftforge.common.config.Configuration;

public class ModConfig {
	public final static transient String CATEGORY = "clientside";
	public static transient Configuration config;
	
	private static transient String ticksBeforeScrollCommnet = "How many ticks have to pass before the next line is shown";
	public static int ticksBeforeScroll = 20;
	
	private static transient String maxLinesShownComment = "How many lines are shown at once. Anything greater than this will scroll";
	public static int maxLinesShown = 10;
	
	private static transient String maxCharactorsShownComment = "How many charactors are shown in the same line";
	public static int maxCharactorsShown = 50;
	
	private static transient String fetchTypeComment = "The opening method for the reader window. BOTH means both sides (will open 2 windows when playing SP), CLIENT means client only (will do nothing in servers), SERVER means server only, DISABLED disables the interaction entirely";
	public static EnumFetchType fetchType = EnumFetchType.BOTH;
	
	private static transient String requiresf3Comment = "If set to false it will show the NBT tag regardless of the F3+H status";
	public static boolean requiresf3 = true;
	
	private static transient String showDelimitersComment = "Set this to false to hide the purple delimiters in the item tags";
	public static boolean showDelimiters = true;
	
	private static transient String compressComment = "Set this to true to see the TAG all in the same line";
	public static boolean compress = false;

	private static transient String showSeparatorComment = "Set this to true to add an introductory line in the tooltip";
	public static boolean showSeparator = true;

	public static void init(File file) {
		config = new Configuration(file);
		config.load();
		load();
	}

	public static void load() {
		ticksBeforeScroll = config.get(CATEGORY, "ticksBeforeScroll", 20, ticksBeforeScrollCommnet, 1, 400).getInt();
		maxLinesShown = config.get(CATEGORY, "maxLinesShown", 10, maxLinesShownComment, 1, 100).getInt();
		maxCharactorsShown = config.get(CATEGORY, "maxCharactorsShown", 50, maxCharactorsShownComment, 1, 500).getInt();
        fetchType = EnumFetchType.valueOf(config.get(CATEGORY, "fetchType", EnumFetchType.BOTH.name(), fetchTypeComment, EnumFetchType.stringValues()).getString());
		requiresf3 = config.get(CATEGORY, "requiresf3", true, requiresf3Comment).getBoolean();
		showDelimiters = config.get(CATEGORY, "showDelimiters", true, showDelimitersComment).getBoolean();
		compress = config.get(CATEGORY, "compress", false, compressComment).getBoolean();
		showSeparator = config.get(CATEGORY, "showSeparator", true, showSeparatorComment).getBoolean();
		config.save();
	}
	
	public static enum EnumFetchType {
		DISABLED, SERVER, CLIENT, BOTH;

		public static String[] stringValues() {
			return Arrays.stream(values()).map(v -> v.name()).toArray(String[]::new);
		}
	}
}
