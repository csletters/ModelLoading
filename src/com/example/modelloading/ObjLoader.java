package com.example.modelloading;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.util.Log;

public class ObjLoader {

	List<String> lines;
	List<Vertex> vert;
	List<TexCords> tex;
	List<Normal> norm;
	List<Polygon> poly;
	float[] vertices;
	float[] normals;
	float[] uv;
	float[] uvSorted;
	float[] normalsSorted;
	float[] verticesSorted;
	short[] facesVerts;
	short[] facesNormals;
	short[] facesUV ;
    int vertexIndex = 0;
    int normalIndex =0;
    int uvIndex = 0;
	int faceIndex = 0;
	
	public ObjLoader(String filename, Context context) {
		InputStream instream = null;
		lines = new ArrayList<String>();

		try {
			instream = context.getAssets().open(filename);
			DataInputStream in = new DataInputStream(instream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while((strLine = br.readLine()) != null)
			{
				lines.add(strLine);
			}
		} catch (Exception e) {// Catch exception if any
			e.toString();
		}
		
		//parse the file
		vertices = new float[lines.size() *3];
		normals = new float[lines.size() *3];
		uv = new float[lines.size()*2];
        facesVerts = new short[lines.size() * 3];
        facesNormals = new short[lines.size() * 3];
        facesUV = new short[lines.size() * 3];
        
        vert = new ArrayList<Vertex>();
        tex = new ArrayList<TexCords>();
        poly = new ArrayList<Polygon>();
        norm = new ArrayList<Normal>();
        for(int i = 0; i < lines.size(); i++)
        {
        	Vertex tempv = new Vertex();
        	TexCords tempt = new TexCords();
        	Normal tempn = new Normal();
        	
        	Polygon tempp = new Polygon();
        	String line = lines.get(i);
        	
        	if(line.startsWith("v "))
        	{
        		String[] tokens = line.split("[ ]+");
        		tempv.x = Float.parseFloat(tokens[1]);
        		tempv.y = Float.parseFloat(tokens[2]);
        		tempv.z = Float.parseFloat(tokens[3]);
        		vert.add(tempv);

        	}
        	if(line.startsWith("vn "))
        	{
        		String[] tokens = line.split("[ ]+");
        		tempn.x = Float.parseFloat(tokens[1]);
        		tempn.y = Float.parseFloat(tokens[2]);
        		tempn.z = Float.parseFloat(tokens[3]);
                norm.add(tempn);
        	}
            if (line.startsWith("vt ")) {
                String[] tokens = line.split("[ ]+");
                tempt.x = Float.parseFloat(tokens[1]);
                tempt.y = Float.parseFloat(tokens[2]);
                tex.add(tempt);
            }
            
            if (line.startsWith("f ")) {
                String[] tokens = line.split("[ ]+");
                
                String[] parts = tokens[1].split("/");
                tempp.vx = vert.get((short) (Short.parseShort(parts[0])-1)).x;
                tempp.vy = vert.get((short) (Short.parseShort(parts[0])-1)).y;
                tempp.vz = vert.get((short) (Short.parseShort(parts[0])-1)).z;
                
                if (parts.length > 2)
                {
                	tempp.nx = norm.get((short) (Short.parseShort(parts[2])-1)).x;
                	tempp.ny = norm.get((short) (Short.parseShort(parts[2])-1)).y;
                	tempp.nz = norm.get((short) (Short.parseShort(parts[2])-1)).z;
                }
                if (parts.length > 1)
                {
                	tempp.tx = tex.get((short) (Short.parseShort(parts[1])-1)).x;
                	tempp.ty = tex.get((short) (Short.parseShort(parts[1])-1)).y;
                }
                poly.add(tempp);
                faceIndex++;
 
                Polygon tempp2 = new Polygon();
                parts = tokens[2].split("/");
                tempp2.vx = vert.get((short) (Short.parseShort(parts[0])-1)).x;
                tempp2.vy = vert.get((short) (Short.parseShort(parts[0])-1)).y;
                tempp2.vz = vert.get((short) (Short.parseShort(parts[0])-1)).z;
                
                if (parts.length > 2)
                {
                	tempp2.nx = norm.get((short) (Short.parseShort(parts[2])-1)).x;
                	tempp2.ny = norm.get((short) (Short.parseShort(parts[2])-1)).y;
                	tempp2.nz = norm.get((short) (Short.parseShort(parts[2])-1)).z;
                }
                if (parts.length > 1)
                {
                	tempp2.tx = tex.get((short) (Short.parseShort(parts[1])-1)).x;
                	tempp2.ty = tex.get((short) (Short.parseShort(parts[1])-1)).y;
                }
                poly.add(tempp2);
                faceIndex++;

                Polygon tempp3 = new Polygon();
                parts = tokens[3].split("/");
                tempp3.vx = vert.get((short) (Short.parseShort(parts[0])-1)).x;
                tempp3.vy = vert.get((short) (Short.parseShort(parts[0])-1)).y;
                tempp3.vz = vert.get((short) (Short.parseShort(parts[0])-1)).z;
                
                if (parts.length > 2)
                {
                	tempp3.nx = norm.get((short) (Short.parseShort(parts[2])-1)).x;
                	tempp3.ny = norm.get((short) (Short.parseShort(parts[2])-1)).y;
                	tempp3.nz = norm.get((short) (Short.parseShort(parts[2])-1)).z;
                }
                if (parts.length > 1)
                {
                	tempp3.tx = tex.get((short) (Short.parseShort(parts[1])-1)).x;
                	tempp3.ty = tex.get((short) (Short.parseShort(parts[1])-1)).y;
                }
                poly.add(tempp3);
                faceIndex++;
            }

        }
        uvSorted =  new float[faceIndex*2];
        verticesSorted = new float[faceIndex*3];
        normalsSorted = new float[faceIndex*3];
        int index = 0;
        for(int x = 0; x < faceIndex; x++ )
        {
        	verticesSorted[index] = poly.get(x).vx;
        	index++;
        	verticesSorted[index] = poly.get(x).vy;
        	index++;
        	verticesSorted[index] = poly.get(x).vz;
        	index++;
        }
        index = 0;
        for(int x = 0; x < faceIndex; x++ )
        {
        	uvSorted[index] = poly.get(x).tx;
        	index++;
        	uvSorted[index] = poly.get(x).ty;
        	index++;
        }
        index = 0;
        for(int x = 0; x < faceIndex; x++ )
        {
        	//normalize
        	double length = Math.sqrt((poly.get(x).nx*poly.get(x).nx) +(poly.get(x).ny*poly.get(x).ny) +(poly.get(x).nz*poly.get(x).nz));
        	normalsSorted[index] = (float) (poly.get(x).nx/length);
        	index++;
        	normalsSorted[index] = (float) (poly.get(x).ny/length);
        	index++;
        	normalsSorted[index] = (float) (poly.get(x).nz/length);
        	index++;
        }
		
	}
	
	
	
	public float[] getVertices()
	{
		return verticesSorted;
	}
	
	public float[] getNormals()
	{
		return normalsSorted;
	}
	
	public float[] getTexCords()
	{
		return uvSorted;
	}
	
	public short[] getFaceVertices()
	{
		return facesVerts;
	}
	
	public short[] getFaceNormals()
	{
		return facesNormals;
	}
	
	public short[] getFaceTexCords()
	{
		return facesUV;
	}

}

