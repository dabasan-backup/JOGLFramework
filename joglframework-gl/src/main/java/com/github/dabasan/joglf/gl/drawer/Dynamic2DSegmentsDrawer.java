package com.github.dabasan.joglf.gl.drawer;

import java.awt.Point;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daxie.basis.coloru8.ColorU8;
import com.github.dabasan.joglf.gl.shader.ShaderFunctions;
import com.github.dabasan.joglf.gl.shape.Vertex2D;
import com.github.dabasan.joglf.gl.tool.CoordinateFunctions;
import com.github.dabasan.joglf.gl.wrapper.GLWrapper;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;

/**
 * Draws 2D segments.
 * @author Daba
 *
 */
public class Dynamic2DSegmentsDrawer extends Dynamic2DDrawer{
	private Logger logger=LoggerFactory.getLogger(Dynamic2DSegmentsDrawer.class);
	
	private Map<Integer, Vertex2D[]> segments_map;
	
	private IntBuffer pos_vbo;
	private IntBuffer dif_vbo;
	private IntBuffer vao;
	
	public Dynamic2DSegmentsDrawer() {
		segments_map=new TreeMap<>();
		
		pos_vbo=Buffers.newDirectIntBuffer(1);
		dif_vbo=Buffers.newDirectIntBuffer(1);
		vao=Buffers.newDirectIntBuffer(1);
		
		GLWrapper.glGenBuffers(1, pos_vbo);
		GLWrapper.glGenBuffers(1, dif_vbo);
		GLWrapper.glGenVertexArrays(1, vao);
	}
	
	@Override
	public void SetDefaultProgram() {
		this.RemoveAllPrograms();
		this.AddProgram("line_drawer");
	}
	
	@Override
	public void UpdateBuffers() {
		int point_num=segments_map.size()*2;
		
		FloatBuffer pos_buffer=Buffers.newDirectFloatBuffer(point_num*2*2);
		FloatBuffer dif_buffer=Buffers.newDirectFloatBuffer(point_num*4*2);
		
		int window_width=this.GetWindowWidth();
		int window_height=this.GetWindowHeight();
		
		for(Vertex2D[] segment:segments_map.values()) {
			for(int i=0;i<2;i++) {
				Point point=segment[i].GetPoint();
				ColorU8 dif=segment[i].GetColor();
				
				int x=point.x;
				int y=point.y;
				float normalized_x=CoordinateFunctions.NormalizeCoordinate(x, window_width);
				float normalized_y=CoordinateFunctions.NormalizeCoordinate(y, window_height);
				
				pos_buffer.put(normalized_x);
				pos_buffer.put(normalized_y);
				dif_buffer.put(dif.GetR());
				dif_buffer.put(dif.GetG());
				dif_buffer.put(dif.GetB());
				dif_buffer.put(dif.GetA());
			}
		}
		((Buffer)pos_buffer).flip();
		((Buffer)dif_buffer).flip();
		
		GLWrapper.glBindBuffer(GL4.GL_ARRAY_BUFFER, pos_vbo.get(0));
		GLWrapper.glBufferData(GL4.GL_ARRAY_BUFFER, 
				Buffers.SIZEOF_FLOAT*pos_buffer.capacity(), pos_buffer, GL4.GL_DYNAMIC_DRAW);
		GLWrapper.glBindBuffer(GL4.GL_ARRAY_BUFFER, dif_vbo.get(0));
		GLWrapper.glBufferData(GL4.GL_ARRAY_BUFFER, 
				Buffers.SIZEOF_FLOAT*dif_buffer.capacity(), dif_buffer, GL4.GL_DYNAMIC_DRAW);
		
		GLWrapper.glBindBuffer(GL4.GL_ARRAY_BUFFER, 0);
		
		GLWrapper.glBindVertexArray(vao.get(0));
		
		GLWrapper.glBindBuffer(GL4.GL_ARRAY_BUFFER, pos_vbo.get(0));
		GLWrapper.glEnableVertexAttribArray(0);
		GLWrapper.glVertexAttribPointer(0, 2, GL4.GL_FLOAT, false, Buffers.SIZEOF_FLOAT*2, 0);
		
		GLWrapper.glBindBuffer(GL4.GL_ARRAY_BUFFER, dif_vbo.get(0));
		GLWrapper.glEnableVertexAttribArray(1);
		GLWrapper.glVertexAttribPointer(1, 4, GL4.GL_FLOAT, false, Buffers.SIZEOF_FLOAT*4, 0);
		
		GLWrapper.glBindBuffer(GL4.GL_ARRAY_BUFFER, 0);
		GLWrapper.glBindVertexArray(0);
	}
	@Override
	public void DeleteBuffers() {
		GLWrapper.glDeleteBuffers(1, pos_vbo);
		GLWrapper.glDeleteBuffers(1, dif_vbo);
		GLWrapper.glDeleteVertexArrays(1, vao);
	}
	
	public void AddSegment(int segment_id,Vertex2D v1,Vertex2D v2) {
		Vertex2D[] vertices=new Vertex2D[2];
		vertices[0]=v1;
		vertices[1]=v2;
		
		segments_map.put(segment_id, vertices);
	}
	public int DeleteSegment(int segment_id) {
		if(segments_map.containsKey(segment_id)==false) {
			logger.warn("No such segment. segment_id={}",segment_id);
			return -1;
		}
		
		segments_map.remove(segment_id);
		
		return 0;
	}
	public void DeleteAllSegments() {
		segments_map.clear();
	}
	
	public Vertex2D[] GetSegment(int segment_id) {
		return segments_map.get(segment_id);
	}
	
	@Override
	public void Draw() {
		List<String> program_names=this.GetProgramNames();
		
		for(String program_name:program_names) {
			ShaderFunctions.UseProgram(program_name);
			
			GLWrapper.glBindVertexArray(vao.get(0));
			
			int point_num=segments_map.size()*2;
			GLWrapper.glEnable(GL4.GL_BLEND);
			GLWrapper.glDrawArrays(GL4.GL_LINES, 0, point_num);
			GLWrapper.glDisable(GL4.GL_BLEND);
			
			GLWrapper.glBindVertexArray(0);	
		}
	}
}
