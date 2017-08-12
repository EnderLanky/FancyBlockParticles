package com.TominoCZ.FBP.particle;

import javax.vecmath.Vector2d;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.ModelTransformer;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class BlockPlaceAnimationDummy extends Particle {

	BlockPos pos;

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

	public BlockPlaceAnimationDummy(World worldIn, double posXIn, double posYIn, double posZIn, IBlockState state,
			long rand) {
		super(worldIn, posXIn, posYIn, posZIn);

		smoothRot = new Vector3f();
		prevRot = new Vector3f();
		rot = new Vector3f();

		pos = new BlockPos(posXIn, posYIn, posZIn);

		textureSeed = rand;

		block = (blockState = state).getBlock();

		mc = Minecraft.getMinecraft();

		mr = mc.getBlockRendererDispatcher().getBlockModelRenderer();

		modelPrefab = mc.getBlockRendererDispatcher().getBlockModelShapes().getModelForState(state);

		modelPrefab = modelForRender = ModelTransformer.transform(modelPrefab, blockState, textureSeed,
				new ModelTransformer.IVertexTransformer() {

					@SuppressWarnings("incomplete-switch")
					@Override
					public float[] transform(BakedQuad quad, VertexFormatElement element, float... data) {
						if (element.getUsage() == VertexFormatElement.EnumUsage.POSITION) {
							facing = mc.thePlayer.getHorizontalFacing();

							switch (facing) {
							case EAST:
								rot.z = -0.15f;
								break;
							case NORTH:
								rot.x = -0.15f;
								break;
							case SOUTH:
								rot.x = 0.15f;
								break;
							case WEST:
								rot.z = 0.15f;
								break;
							}

							Vector3f vec = rotatef(new Vector3f(data[0], data[1], data[2]), rot.x, rot.y, rot.z);

							data[0] = 0;
							data[1] = 0;
							data[2] = 0;

							return new float[] { vec.x, vec.y, vec.z };

						}

						return data;
					}
				});

		prevRot.x = rot.x = 0;
		prevRot.z = rot.z = 0;

		this.canCollide = false;
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void onUpdate() {
		prevRot.x = rot.x;
		prevRot.y = rot.y;
		prevRot.z = rot.z;

		switch (facing) {
		case EAST:
			rot.z += 0.07f;
			break;
		case NORTH:
			rot.x -= 0.07f;
			break;
		case SOUTH:
			rot.x += 0.07f;
			break;
		case WEST:
			rot.z -= 0.07f;
			break;
		}
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void renderParticle(VertexBuffer buff, Entity entityIn, float partialTicks, float rotationX, float rotationZ,
			float rotationYZ, float rotationXY, float rotationXZ) {
		if (canCollide) {
			if (particleAge >= 2) {
				this.isExpired = true;
				return;
			}
			if (particleAge == 0) {
				worldObj.setBlockState(pos, block.getActualState(blockState, worldObj, pos));

				if (!(FBP.frozen && !FBP.spawnWhileFrozen)
						&& (FBP.spawnRedstoneBlockParticles || block != Blocks.REDSTONE_BLOCK))
					spawnParticles();
			}
			particleAge++;
		} else
			worldObj.setBlockState(pos, Blocks.BARRIER.getDefaultState());

		float f = 0, f1 = 0, f2 = 0, f3 = 0;

		float f5 = (float) (prevPosX + (posX - prevPosX) * (double) partialTicks - interpPosX) - 0.5f;
		float f6 = (float) (prevPosY + (posY - prevPosY) * (double) partialTicks - interpPosY) - 0.5f;
		float f7 = (float) (prevPosZ + (posZ - prevPosZ) * (double) partialTicks - interpPosZ) - 0.5f;

		smoothRot.x = ((float) (prevRot.x + (rot.x - prevRot.x) * (double) partialTicks));
		smoothRot.y = ((float) (prevRot.y + (rot.y - prevRot.y) * (double) partialTicks));
		smoothRot.z = ((float) (prevRot.z + (rot.z - prevRot.z) * (double) partialTicks));

		switch (facing) {
		case EAST:
			if (smoothRot.z > 0.15f) {
				this.canCollide = true;
				smoothRot.z = 0.15f;
			}
			break;
		case NORTH:
			if (smoothRot.x < -0.15f) {
				this.canCollide = true;
				smoothRot.x = -0.15f;
			}
			break;
		case SOUTH:
			if (smoothRot.x > 0.15f) {
				this.canCollide = true;
				smoothRot.x = 0.15f;
			}
			break;
		case WEST:
			if (smoothRot.z < -0.15f) {
				this.canCollide = true;
				smoothRot.z = -0.15f;
			}
			break;
		}

		buff.setTranslation(f5 - pos.getX(), f6 - pos.getY(), f7 - pos.getZ());

		Tessellator.getInstance().draw();
		mc.getRenderManager().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		buff.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);

		modelForRender = ModelTransformer.transform(modelPrefab, blockState, textureSeed,
				new ModelTransformer.IVertexTransformer() {
					@Override
					public float[] transform(BakedQuad quad, VertexFormatElement element, float... data) {
						if (element.getUsage() == VertexFormatElement.EnumUsage.POSITION) {
							Vector3f vec = rotatef(new Vector3f(data[0], data[1], data[2]), smoothRot.x, smoothRot.y,
									smoothRot.z);

							return new float[] { vec.x, vec.y, vec.z };
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

	Vector3f rotatef(Vector3f pos2, float AngleX, float AngleY, float AngleZ) {
		float sinAngleX = MathHelper.sin(AngleX);
		float sinAngleY = MathHelper.sin(AngleY);
		float sinAngleZ = MathHelper.sin(AngleZ);

		float cosAngleX = MathHelper.cos(AngleX);
		float cosAngleY = MathHelper.cos(AngleY);
		float cosAngleZ = MathHelper.cos(AngleZ);

		Vector3f pos;

		if (facing == EnumFacing.EAST)
			pos2.x -= 1;
		if (facing == EnumFacing.SOUTH)
			pos2.z -= 1;

		pos = new Vector3f(pos2.x, pos2.y * cosAngleX - pos2.z * sinAngleX, pos2.y * sinAngleX + pos2.z * cosAngleX);
		pos = new Vector3f(pos.x * cosAngleY + pos.z * sinAngleY, pos.y, pos.x * sinAngleY - pos.z * cosAngleY);
		pos = new Vector3f(pos.x * cosAngleZ - pos.y * sinAngleZ, pos.x * sinAngleZ + pos.y * cosAngleZ, pos.z);

		if (facing == EnumFacing.EAST)
			pos.x += 1;
		if (facing == EnumFacing.SOUTH)
			pos.z += 1;

		return pos;
	}

	private void spawnParticles() {
		AxisAlignedBB aabb = block.getSelectedBoundingBox(blockState, worldObj, pos);

		Vector2d[] corners = new Vector2d[] { new Vector2d(aabb.minX, aabb.minZ), new Vector2d(aabb.maxX, aabb.maxZ),
				new Vector2d(aabb.minX, aabb.maxZ), new Vector2d(aabb.maxX, aabb.minZ) };

		Vector2d middle = new Vector2d(pos.getX() + 0.5f, pos.getZ() + 0.5f);

		for (Vector2d corner : corners) {
			double mX = middle.x - corner.x;
			double mZ = middle.y - corner.y;

			mX /= -0.5;
			mZ /= -0.5;

			mX -= rand.nextFloat() / 2;
			mZ += rand.nextFloat() / 2;

			mc.effectRenderer.addEffect(new FBPParticle(worldObj, corner.x, pos.getY() + 0.1f, corner.y, mX, 0, mZ,
					block.getActualState(blockState, worldObj, pos), null, 0.6f));
		}
	}

	@Override
	public void setExpired() {
	}
}