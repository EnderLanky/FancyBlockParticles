package com.TominoCZ.FBP;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.ThreadLocalRandom;

import com.TominoCZ.FBP.handler.FBPConfigHandler;
import com.TominoCZ.FBP.handler.FBPEventHandler;
import com.TominoCZ.FBP.handler.FBPKeyInputHandler;
import com.TominoCZ.FBP.handler.FBPRenderGuiHandler;
import com.TominoCZ.FBP.keys.FBPKeyBindings;
import com.google.common.base.Throwables;

import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

@Mod(clientSideOnly = true, modid = FBP.MODID)
public class FBP {
	@Instance(FBP.MODID)
	public static FBP instance;

	protected final static String MODID = "fbp";

	public static File config;

	public static int minAge, maxAge;

	public static double scaleMult, gravityMult, rotationMult;

	public static boolean enabled = true;
	public static boolean showInMillis = false;
	public static boolean infiniteDuration = false;
	public static boolean randomRotation = true, cartoonMode = false, spawnWhileFrozen = true,
			spawnRedstoneBlockParticles = false, smoothTransitions = true, randomFadingSpeed = true,
			entityCollision = false, bounceOffWalls = true, rollParticles = false, smartBreaking = true, frozen = false;

	public static ThreadLocalRandom random = ThreadLocalRandom.current();

	public static final Vec3d[] CUBE = { new Vec3d(-1, -1, 1), new Vec3d(-1, 1, 1), new Vec3d(1, 1, 1),
			new Vec3d(1, -1, 1), new Vec3d(1, -1, -1), new Vec3d(1, 1, -1), new Vec3d(-1, 1, -1), new Vec3d(-1, -1, -1),
			new Vec3d(-1, -1, -1), new Vec3d(-1, 1, -1), new Vec3d(-1, 1, 1), new Vec3d(-1, -1, 1), new Vec3d(1, -1, 1),
			new Vec3d(1, 1, 1), new Vec3d(1, 1, -1), new Vec3d(1, -1, -1), new Vec3d(1, 1, -1), new Vec3d(1, 1, 1),
			new Vec3d(-1, 1, 1), new Vec3d(-1, 1, -1), new Vec3d(-1, -1, -1), new Vec3d(-1, -1, 1), new Vec3d(1, -1, 1),
			new Vec3d(1, -1, -1) };

	public static MethodHandle setSourcePos;

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		config = new File(evt.getModConfigurationDirectory() + "/FBP/Particle.properties");

		FBPConfigHandler.init();

		//MinecraftForge.EVENT_BUS.register(new FBPRenderGuiHandler());

		FBPKeyBindings.init();

		FMLCommonHandler.instance().bus().register(new FBPKeyInputHandler());
	}

	@EventHandler
	public void init(FMLInitializationEvent evt) {
		MinecraftForge.EVENT_BUS.register(new FBPEventHandler());
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent evt) {
		MinecraftForge.EVENT_BUS.register(new FBPRenderGuiHandler());

		MethodHandles.Lookup lookup = MethodHandles.publicLookup();

		try {
			setSourcePos = lookup
					.unreflectSetter(ReflectionHelper.findField(ParticleDigging.class, "field_181019_az", "sourcePos"));
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	public static boolean isEnabled() {
		boolean result = enabled;

		if (!result)
			frozen = false;

		return result;
	}

	public static boolean isDev() {
		return (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
	}
}