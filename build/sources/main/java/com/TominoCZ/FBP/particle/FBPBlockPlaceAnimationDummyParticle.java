package com.TominoCZ.FBP.particle;

import java.beans.IndexedPropertyDescriptor;
import java.io.IOException;

import javax.vecmath.Vector2d;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.model.FBPModelTransformer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.BlockStructure;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateBase;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.statemap.BlockStateMapper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.status.client.CPacketPing;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class FBPBlockPlaceAnimationDummyParticle extends Particle {

	public BlockPos pos;

	Block block;
	IBlockState blockState;

	BlockModelRenderer mr;

	IBakedModel modelPrefab;

	IBakedModel modelForRender;

	Minecraft mc;

	EnumFacing facing;

	Vector3f smoothRot;

	Vector3f prevRot;
	Vector3f rot;

	long textureSeed;

	float startingHeight = 0.1125f;
	float startingAngle = 0.0875f;
	float step = 0.002f;

	float height;
	float prevHeight;

	float smoothHeight;

	boolean lookingUp;

	boolean tickSwitch = true;

	int ticks;

	public FBPBlockPlaceAnimationDummyParticle(World worldIn, double posXIn, double posYIn, double posZIn,
			IBlockState state, EntityPlayer p, long rand) {
		super(worldIn, posXIn, posYIn, posZIn);

		mc = Minecraft.getMinecraft();

		facing = p.getHorizontalFacing();

		lookingUp = Float.valueOf(MathHelper.wrapDegrees(p.rotationPitch)) <= 0;

		height = startingHeight;

		smoothRot = new Vector3f();
		prevRot = new Vector3f();
		rot = new Vector3f();

		pos = new BlockPos(posXIn, posYIn, posZIn);

		worldObj.setBlockState(pos, Blocks.AIR.getDefaultState(), 1);

		textureSeed = rand;

		block = (blockState = state).getBlock();

		mr = mc.getBlockRendererDispatcher().getBlockModelRenderer();

		modelPrefab = mc.getBlockRendererDispatcher().getBlockModelShapes().getModelForState(state);

		modelPrefab = modelForRender = FBPModelTransformer.transform(modelPrefab, blockState, textureSeed,
				new FBPModelTransformer.IVertexTransformer() {

					@SuppressWarnings("incomplete-switch")
					@Override
					public float[] transform(BakedQuad quad, VertexFormatElement element, float... data) {
						if (element.getUsage() == VertexFormatElement.EnumUsage.POSITION) {
							Vector3f vec = new Vector3f(data[0], data[1], data[2]);

							switch (facing) {
							case EAST:
								rot.z = -startingAngle;
								vec.x += 0.010000000149011612D;
								break;
							case NORTH:
								rot.x = -startingAngle;
								vec.z -= 0.010000000149011612D;
								break;
							case SOUTH:
								rot.x = startingAngle;
								vec.z += 0.010000000149011612D;
								break;
							case WEST:
								rot.z = startingAngle;
								vec.x -= 0.010000000149011612D;
								break;
							}

							vec = rotatef(vec, rot.x, rot.y, rot.z);

							return new float[] { vec.x, vec.y + startingHeight, vec.z };
						}

						return data;
					}
				});

		FBP.FBPBlock.copyState(worldObj, pos, blockState);

		prevRot.x = rot.x = 0;
		prevRot.z = rot.z = 0;

		this.canCollide = false;
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void onUpdate() {
		prevHeight = height;

		prevRot.x = rot.x;
		prevRot.y = rot.y;
		prevRot.z = rot.z;

		switch (facing) {
		case EAST:
			rot.z += step;
			break;
		case NORTH:
			rot.x -= step;
			break;
		case SOUTH:
			rot.x += step;
			break;
		case WEST:
			rot.z -= step;
			break;
		}

		height -= step * 4f;

		step *= 1.98982f;
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void renderParticle(VertexBuffer buff, Entity entityIn, float partialTicks, float rotationX, float rotationZ,
			float rotationYZ, float rotationXY, float rotationXZ) {
		if (canCollide) {
			if (particleAge >= 10) {
				this.isExpired = true;
				return;
			}
			else if (particleAge == 0) {
				worldObj.setBlockState(pos, Blocks.AIR.getDefaultState());

				worldObj.sendPacketToServer(
						new CPacketPlayerDigging(Action.ABORT_DESTROY_BLOCK, pos, facing.getOpposite()));

				if ((!(FBP.frozen && !FBP.spawnWhileFrozen)
						&& (FBP.spawnRedstoneBlockParticles || block != Blocks.REDSTONE_BLOCK)) && mc.gameSettings.particleSetting > 2)
					spawnParticles();
			}

			particleAge++;
		} else if (tickSwitch) {
			if (worldObj.getBlockState(pos) != FBP.FBPBlock.getDefaultState()) {
				new Thread() {
					public void run() {
						worldObj.setBlockState(pos, FBP.FBPBlock.getDefaultState());
					}
				}.start();
			}
		}

		tickSwitch = !tickSwitch;

		if (ticks < 3) {
			ticks++;
			return;
		}
		float f = 0, f1 = 0, f2 = 0, f3 = 0;

		float f5 = (float) (prevPosX + (posX - prevPosX) * (double) partialTicks - interpPosX) - 0.5f;
		float f6 = (float) (prevPosY + (posY - prevPosY) * (double) partialTicks - interpPosY) - 0.5f;
		float f7 = (float) (prevPosZ + (posZ - prevPosZ) * (double) partialTicks - interpPosZ) - 0.5f;

		smoothRot.x = prevRot.x + (rot.x - prevRot.x) * partialTicks;
		smoothRot.y = prevRot.y + (rot.y - prevRot.y) * partialTicks;
		smoothRot.z = prevRot.z + (rot.z - prevRot.z) * partialTicks;

		smoothHeight = ((float) (prevHeight + (height - prevHeight) * (double) partialTicks));

		if (smoothHeight <= 0)
			smoothHeight = 0;

		switch (facing) {
		case EAST:
			if (smoothRot.z > startingAngle) {
				this.canCollide = true;
				smoothRot.z = startingAngle;
			}
			break;
		case NORTH:
			if (smoothRot.x < -startingAngle) {
				this.canCollide = true;
				smoothRot.x = -startingAngle;
			}
			break;
		case SOUTH:
			if (smoothRot.x > startingAngle) {
				this.canCollide = true;
				smoothRot.x = startingAngle;
			}
			break;
		case WEST:
			if (smoothRot.z < -startingAngle) {
				this.canCollide = true;
				smoothRot.z = -startingAngle;
			}
			break;
		}

		buff.setTranslation(f5 - pos.getX(), f6 - pos.getY(), f7 - pos.getZ());

		Tessellator.getInstance().draw();
		mc.getRenderManager().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

		buff.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);

		modelForRender = FBPModelTransformer.transform(modelPrefab, blockState, textureSeed,
				new FBPModelTransformer.IVertexTransformer() {
					@Override
					public float[] transform(BakedQuad quad, VertexFormatElement element, float... data) {
						if (element.getUsage() == VertexFormatElement.EnumUsage.POSITION) {
							Vector3f vec = rotatef(new Vector3f(data[0], data[1], data[2]), smoothRot.x, smoothRot.y,
									smoothRot.z);

							return new float[] { vec.x, vec.y - (startingHeight - smoothHeight), vec.z };
						}

						return data;
					}
				});

		GlStateManager.enableCull();
		GlStateManager.enableBlend();
		GlStateManager.enableAlpha();

		mr.renderModelSmooth(worldObj, modelForRender, blockState, pos, buff, false, textureSeed);

		Tessellator.getInstance().draw();
		Minecraft.getMinecraft().getTextureManager()
				.bindTexture(new ResourceLocation("textures/particle/particles.png"));
		buff.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);

		buff.setTranslation(0, 0, 0);
	}

	Vector3f rotatef(Vector3f pos, float AngleX, float AngleY, float AngleZ) {
		float sinAngleX = MathHelper.sin(AngleX);
		float sinAngleY = MathHelper.sin(AngleY);
		float sinAngleZ = MathHelper.sin(AngleZ);

		float cosAngleX = MathHelper.cos(AngleX);
		float cosAngleY = MathHelper.cos(AngleY);
		float cosAngleZ = MathHelper.cos(AngleZ);

		Vector3f pos1 = new Vector3f(pos.x, pos.y, pos.z);
		Vector3f pos2;

		if (lookingUp)
			pos1.y -= 1.0f;

		if (facing == EnumFacing.EAST)
			pos1.x -= 1.0f;
		if (facing == EnumFacing.SOUTH)
			pos1.z -= 1.0f;

		pos2 = new Vector3f(pos1.x, pos1.y * cosAngleX - pos1.z * sinAngleX, pos1.y * sinAngleX + pos1.z * cosAngleX);
		pos2 = new Vector3f(pos2.x * cosAngleY + pos2.z * sinAngleY, pos2.y, pos2.x * sinAngleY - pos2.z * cosAngleY);
		pos2 = new Vector3f(pos2.x * cosAngleZ - pos2.y * sinAngleZ, pos2.x * sinAngleZ + pos2.y * cosAngleZ, pos2.z);

		if (facing == EnumFacing.EAST)
			pos2.x += 1.0f;
		if (facing == EnumFacing.SOUTH)
			pos2.z += 1.0f;

		if (lookingUp)
			pos2.y += 1;
		return pos2;
	}

	@SuppressWarnings("incomplete-switch")
	private void spawnParticles() {
		AxisAlignedBB aabb = block.getSelectedBoundingBox(blockState, worldObj, pos);

		// z- = north
		// x- = west // block pos

		Vector2d[] corners = new Vector2d[] { new Vector2d(aabb.minX, aabb.minZ), new Vector2d(aabb.maxX, aabb.maxZ),

				new Vector2d(aabb.minX, aabb.maxZ), new Vector2d(aabb.maxX, aabb.minZ) };

		Vector2d middle = new Vector2d(pos.getX() + 0.5f, pos.getZ() + 0.5f);

		switch (facing) {
		case EAST:
			corners[1] = null;
			corners[3] = null;
			break;
		case NORTH:
			corners[0] = null;
			corners[3] = null;
			break;
		case SOUTH:
			corners[2] = null;
			corners[1] = null;
			break;
		case WEST:
			corners[0] = null;
			corners[2] = null;
			break;
		}

		for (Vector2d corner : corners) {
			if (corner == null)
				continue;

			double mX = middle.x - corner.x;
			double mZ = middle.y - corner.y;

			mX /= -0.5;
			mZ /= -0.5;
			
			mc.effectRenderer.addEffect(new FBPParticle(worldObj, corner.x, pos.getY() + 0.1f, corner.y, mX, 0, mZ,
					block.getActualState(blockState, worldObj, pos), null, 0.6f).multipleParticleScaleBy(0.5f).multiplyVelocity(0.5f));
		}

		for (Vector2d corner : corners) {
			if (corner == null)
				continue;

			double mX = middle.x - corner.x;
			double mZ = middle.y - corner.y;

			mX /= -0.5;
			mZ /= -0.5;
			
			mc.effectRenderer.addEffect(new FBPParticle(worldObj, corner.x, pos.getY() + 0.1f, corner.y, mX / 3, 0,
					mZ / 3, block.getActualState(blockState, worldObj, pos), null, 0.6f).multipleParticleScaleBy(0.75f).multiplyVelocity(0.75f));
		}
	}

	@Override
	public void setExpired() {
	}
}