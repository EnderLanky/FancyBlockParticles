package com.TominoCZ.FBP;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.LightUtil;
import scala.actors.threadpool.Arrays;

public class BakedQuadF {

	public BakedQuad actualQuad;
	
	public List<VertexF> vertexes;
	
	public EnumFacing facing;
	
	public VertexFormat format;
	
	public BakedQuadF(BakedQuad quad, VertexFormat format, EnumFacing facing) {
		this.actualQuad = quad;
		this.facing = facing;
		this.format = format;
		
		vertexes = new ArrayList<VertexF>();
	}
	
	public void addVertex(float x, float y, float z, float nX, float nY, float nZ, float U, float V) {
		vertexes.add(new VertexF(x, y, z, nX, nY, nZ, U, V));
	}

	public int[] createVertexData() {
		List<Integer> data = new ArrayList<Integer>(); // 8 each vertex, 4 vertexes => 32 items
		
		for (int i = 0; i < vertexes.size(); i++) {
			int[] tmp = vertexes.get(i).getParsedVertexData(actualQuad, format, i);
			
			for (int j = 0; j < tmp.length; j++)
			{
				data.add(tmp[j]);
			}
		}
			
		int[] result = new int[data.size()];
		
		for (int i  = 0; i < data.size(); i++)
		{
			result[i] = data.get(i);
		}
		
		return result;
	}
}