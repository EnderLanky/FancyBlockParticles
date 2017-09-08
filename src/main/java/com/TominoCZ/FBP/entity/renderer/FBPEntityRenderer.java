package com.TominoCZ.FBP.entity.renderer;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.particle.FBPParticleRain;
import com.TominoCZ.FBP.particle.FBPParticleSnow;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class FBPEntityRenderer extends EntityRenderer {

	private Minecraft mc;

	MethodHandle getRendererUpdateCount;
	MethodHandle getRainPosX;
	MethodHandle getRainPosY;

	int tick = 0;

	public FBPEntityRenderer(Minecraft mcIn, IResourceManager resourceManagerIn) {
		super(mcIn, resourceManagerIn);
		mc = mcIn;

		MethodHandles.Lookup lookup = MethodHandles.publicLookup();

		try {
			getRendererUpdateCount = lookup.unreflectGetter(
					ReflectionHelper.findField(EntityRenderer.class, "field_78529_t", "rendererUpdateCount"));
			getRainPosX = lookup
					.unreflectGetter(ReflectionHelper.findField(EntityRenderer.class, "field_175076_N", "rainXCoords"));
			getRainPosY = lookup
					.unreflectGetter(ReflectionHelper.findField(EntityRenderer.class, "field_175077_O", "rainYCoords"));
		} catch (Exception e) {

		}
	}

	@Override
	public void updateRenderer() {
		super.updateRenderer();

		if (++tick >= 15) {
			BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
			int r = (mc.gameSettings.renderDistanceChunks + 2) * 9;

			for (int i = 0; i < 64; i++) {
				double angle = mc.theWorld.rand.nextDouble() * Math.PI * 2;
				double radius = MathHelper.sqrt_double(mc.theWorld.rand.nextDouble()) * r;
				double X = mc.thePlayer.posX + radius * Math.cos(angle);
				double Z = mc.thePlayer.posZ + radius * Math.sin(angle);

				blockpos$mutableblockpos.setPos(X, 0, Z);
				Biome biome = mc.theWorld.getBiome(blockpos$mutableblockpos);

				if (biome.getEnableSnow()) {
					mc.effectRenderer.addEffect(new FBPParticleSnow(mc.theWorld, X, mc.thePlayer.posY + 10, Z,
							FBP.random.nextDouble(-0.5, 0.5), 0.5f, FBP.random.nextDouble(-0.5, 0.5),
							Blocks.SNOW.getDefaultState()));
				}
			}

			tick = 0;
		}
	}

	@Override
	protected void renderRainSnow(float partialTicks) {
		net.minecraftforge.client.IRenderHandler renderer = this.mc.theWorld.provider.getWeatherRenderer();
		if (renderer != null) {
			renderer.render(partialTicks, this.mc.theWorld, mc);
			return;
		}

		float f = this.mc.theWorld.getRainStrength(partialTicks);

		if (f > 0.0F) {
			this.enableLightmap();
			Entity entity = this.mc.getRenderViewEntity();
			World world = this.mc.theWorld;
			int i = MathHelper.floor_double(entity.posX);
			int j = MathHelper.floor_double(entity.posY);
			int k = MathHelper.floor_double(entity.posZ);
			Tessellator tessellator = Tessellator.getInstance();
			VertexBuffer vertexbuffer = tessellator.getBuffer();
			GlStateManager.disableCull();
			GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
					GlStateManager.DestFactor.ZERO);
			GlStateManager.alphaFunc(516, 0.1F);
			double d0 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
			double d1 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
			double d2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;
			int l = MathHelper.floor_double(d1);
			int i1 = 5;

			if (this.mc.gameSettings.fancyGraphics) {
				i1 = 10;
			}

			float[] rainXCoords = new float[1024];
			float[] rainYCoords = new float[1024];
			int rendererUpdateCount = 0;

			try {
				rainXCoords = (float[]) getRainPosX.invokeExact((EntityRenderer) this);
				rainYCoords = (float[]) getRainPosY.invokeExact((EntityRenderer) this);
				rendererUpdateCount = (int) getRendererUpdateCount.invokeExact((EntityRenderer) this);
			} catch (Throwable e) {
				e.printStackTrace();
			}

			int j1 = -1;
			float f1 = (float) rendererUpdateCount + partialTicks;

			vertexbuffer.setTranslation(-d0, -d1, -d2);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

			ResourceLocation RAIN_TEXTURES = new ResourceLocation("textures/environment/rain.png");
			ResourceLocation SNOW_TEXTURES = new ResourceLocation("textures/environment/snow.png");

			for (int k1 = k - i1; k1 <= k + i1; ++k1) {
				for (int l1 = i - i1; l1 <= i + i1; ++l1) {
					int i2 = (k1 - k + 16) * 32 + l1 - i + 16;
					double d3 = (double) rainXCoords[i2] * 0.5D;
					double d4 = (double) rainYCoords[i2] * 0.5D;
					blockpos$mutableblockpos.setPos(l1, 0, k1);
					Biome biome = world.getBiome(blockpos$mutableblockpos);

					if (biome.canRain() || biome.getEnableSnow()) {
						int j2 = world.getPrecipitationHeight(blockpos$mutableblockpos).getY();
						int k2 = j - i1;
						int l2 = j + i1;

						if (k2 < j2) {
							k2 = j2;
						}

						if (l2 < j2) {
							l2 = j2;
						}

						int i3 = j2;

						if (j2 < l) {
							i3 = l;
						}

						if (k2 != l2) {
							world.rand.setSeed((long) (l1 * l1 * 3121 + l1 * 45238971 ^ k1 * k1 * 418711 + k1 * 13761));
							blockpos$mutableblockpos.setPos(l1, k2, k1);
							float f2 = biome.getFloatTemperature(blockpos$mutableblockpos);

							if (world.getBiomeProvider().getTemperatureAtHeight(f2, j2) >= 0.15F) {
								if (j1 != 0) {
									if (j1 >= 0) {
										tessellator.draw();
									}

									j1 = 0;
									this.mc.getTextureManager().bindTexture(RAIN_TEXTURES);
									vertexbuffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
								}

								double d5 = -((double) (rendererUpdateCount + l1 * l1 * 3121 + l1 * 45238971
										+ k1 * k1 * 418711 + k1 * 13761 & 31) + (double) partialTicks) / 32.0D
										* (3.0D + world.rand.nextDouble());
								double d6 = (double) ((float) l1 + 0.5F) - entity.posX;
								double d7 = (double) ((float) k1 + 0.5F) - entity.posZ;
								float f3 = MathHelper.sqrt_double(d6 * d6 + d7 * d7) / (float) i1;
								float f4 = ((1.0F - f3 * f3) * 0.5F + 0.5F) * f;
								blockpos$mutableblockpos.setPos(l1, i3, k1);
								int j3 = world.getCombinedLight(blockpos$mutableblockpos, 0);
								int k3 = j3 >> 16 & 65535;
								int l3 = j3 & 65535;

								vertexbuffer.pos((double) l1 - d3 + 0.5D, (double) l2, (double) k1 - d4 + 0.5D)
										.tex(0.0D, (double) k2 * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f4)
										.lightmap(k3, l3).endVertex();
								vertexbuffer.pos((double) l1 + d3 + 0.5D, (double) l2, (double) k1 + d4 + 0.5D)
										.tex(1.0D, (double) k2 * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f4)
										.lightmap(k3, l3).endVertex();
								vertexbuffer.pos((double) l1 + d3 + 0.5D, (double) k2, (double) k1 + d4 + 0.5D)
										.tex(1.0D, (double) l2 * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f4)
										.lightmap(k3, l3).endVertex();
								vertexbuffer.pos((double) l1 - d3 + 0.5D, (double) k2, (double) k1 - d4 + 0.5D)
										.tex(0.0D, (double) l2 * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f4)
										.lightmap(k3, l3).endVertex();
							}
						} else {
							if (j1 != 1) {
								if (j1 >= 0) {
									tessellator.draw();
								}

								j1 = 1;
								this.mc.getTextureManager().bindTexture(SNOW_TEXTURES);
								vertexbuffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
							}

							double d8 = (double) (-((float) (rendererUpdateCount & 511) + partialTicks) / 512.0F);
							double d9 = world.rand.nextDouble()
									+ (double) f1 * 0.01D * (double) ((float) world.rand.nextGaussian());
							double d10 = world.rand.nextDouble()
									+ (double) (f1 * (float) world.rand.nextGaussian()) * 0.001D;
							double d11 = (double) ((float) l1 + 0.5F) - entity.posX;
							double d12 = (double) ((float) k1 + 0.5F) - entity.posZ;
							float f6 = MathHelper.sqrt_double(d11 * d11 + d12 * d12) / (float) i1;
							float f5 = ((1.0F - f6 * f6) * 0.3F + 0.5F) * f;
							blockpos$mutableblockpos.setPos(l1, i3, k1);
							int i4 = (world.getCombinedLight(blockpos$mutableblockpos, 0) * 3 + 15728880) / 4;
							int j4 = i4 >> 16 & 65535;
							int k4 = i4 & 65535;

							if (!FBP.enabled /* && !FBP.fancySnow */) {
								vertexbuffer.pos((double) l1 - d3 + 0.5D, (double) l2, (double) k1 - d4 + 0.5D)
										.tex(0.0D + d9, (double) k2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5)
										.lightmap(j4, k4).endVertex();
								vertexbuffer.pos((double) l1 + d3 + 0.5D, (double) l2, (double) k1 + d4 + 0.5D)
										.tex(1.0D + d9, (double) k2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5)
										.lightmap(j4, k4).endVertex();
								vertexbuffer.pos((double) l1 + d3 + 0.5D, (double) k2, (double) k1 + d4 + 0.5D)
										.tex(1.0D + d9, (double) l2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5)
										.lightmap(j4, k4).endVertex();
								vertexbuffer.pos((double) l1 - d3 + 0.5D, (double) k2, (double) k1 - d4 + 0.5D)
										.tex(0.0D + d9, (double) l2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5)
										.lightmap(j4, k4).endVertex();
							}
						}
					}
				}
			}

			if (j1 >= 0) {
				tessellator.draw();
			}

			vertexbuffer.setTranslation(0.0D, 0.0D, 0.0D);
			GlStateManager.enableCull();
			GlStateManager.disableBlend();
			GlStateManager.alphaFunc(516, 0.1F);
			this.disableLightmap();
		}
	}
}